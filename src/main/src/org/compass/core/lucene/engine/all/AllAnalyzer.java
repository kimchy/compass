/*
 * Copyright 2004-2006 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.lucene.engine.all;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.compass.core.Property;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalResource;

/**
 * The All Analyzer is a specific analyzer that is used to wrap the analyzer passed when adding
 * a document. It will gather all the tokens that the actual analyzer generates for fields are
 * aree included in All and allow to get them using {@link #createAllTokenStream()} (which will
 * be used to create thea all field with).
 *
 * <p>Un tokenized fields (which will not go through the analysis process) are identied when this
 * analyzed is constructed and are added to the all field if they are supposed to be included.
 * There are two options with the untokenized fields, either add them as is (un tokenized), or
 * analyze them just for the all properties.
 *
 * @author kimchy
 */
public class AllAnalyzer extends Analyzer {

    private Analyzer analyzer;

    private ResourceMapping resourceMapping;

    private LuceneSearchEngine searchEngine;

    private ArrayList tokens = new ArrayList();

    private AllTokenStreamCollector allTokenStreamCollector = new AllTokenStreamCollector();

    public AllAnalyzer(Analyzer analyzer, InternalResource resource, LuceneSearchEngine searchEngine) {
        this.analyzer = analyzer;
        this.resourceMapping = resource.resourceKey().getResourceMapping();
        this.searchEngine = searchEngine;

        if (resourceMapping.isAllSupported()) {
            if (!resourceMapping.isExcludeAliasFromAll()) {
                // add the alias to all prpoerty (lowecased, so finding it will be simple)
                tokens.add(new Token(resource.getAlias().toLowerCase(), 0, resource.getAlias().length()));
                // add the extended property
                Property[] properties = resource.getProperties(searchEngine.getSearchEngineFactory().getExtendedAliasProperty());
                if (properties != null) {
                    for (int i = 0; i < properties.length; i++) {
                        tokens.add(new Token(properties[i].getStringValue().toLowerCase(), 0, properties[i].getStringValue().length()));
                    }
                }
            }

            // go over all the un tokenized properties and add them as tokens (if required)
            // they are added since they will never get analyzed thus tokenStream will never
            // be called on them
            Property[] properties = resource.getProperties();
            for (int i = 0; i < properties.length; i++) {
                LuceneProperty property = (LuceneProperty) properties[i];
                ResourcePropertyMapping resourcePropertyMapping = property.getPropertyMapping();
                // if not found within the property, try and get it based on the name from the resource mapping
                if (resourcePropertyMapping == null) {
                    resourcePropertyMapping = resourceMapping.getResourcePropertyMapping(property.getName());
                }
                if (resourcePropertyMapping != null && resourcePropertyMapping.getIndex() == Property.Index.UN_TOKENIZED
                        && !resourcePropertyMapping.isInternal() && !(resourcePropertyMapping.getExcludeFromAll() == ResourcePropertyMapping.ExcludeFromAllType.YES)) {
                    String value = property.getStringValue();
                    if (value != null) {
                        // if NO exclude from all, just add it
                        // if NO_ANALYZED, will analyze it as well
                        if (resourcePropertyMapping.getExcludeFromAll() == ResourcePropertyMapping.ExcludeFromAllType.NO) {
                            tokens.add(new Token(value, 0, value.length()));
                        } else
                        if (resourcePropertyMapping.getExcludeFromAll() == ResourcePropertyMapping.ExcludeFromAllType.NO_ANALYZED) {
                            Analyzer propAnalyzer;
                            if (resourcePropertyMapping.getAnalyzer() != null) {
                                propAnalyzer = searchEngine.getSearchEngineFactory()
                                        .getAnalyzerManager().getAnalyzerMustExist(resourcePropertyMapping.getAnalyzer());
                            } else {
                                propAnalyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerByResource(resource);
                            }
                            TokenStream ts = propAnalyzer.tokenStream(property.getName(), new StringReader(value));
                            try {
                                Token token = ts.next();
                                while (token != null) {
                                    tokens.add(token);
                                    token = ts.next();
                                }
                            } catch (IOException e) {
                                throw new SearchEngineException("Failed to analyzer " + property, e);
                            }
                        }
                    }
                }
            }
        }
    }

    public TokenStream tokenStream(String fieldName, Reader reader) {
        TokenStream retVal = analyzer.tokenStream(fieldName, reader);
        if (!resourceMapping.isAllSupported()) {
            return retVal;
        }
        ResourcePropertyMapping resourcePropertyMapping = resourceMapping.getResourcePropertyMapping(fieldName);
        if (resourcePropertyMapping == null) {
            if (!searchEngine.getSearchEngineFactory().getPropertyNamingStrategy().isInternal(fieldName)) {
                if (resourceMapping.isIncludePropertiesWithNoMappingsInAll()) {
                    allTokenStreamCollector.setTokenStream(retVal);
                    retVal = allTokenStreamCollector;
                }
            }
        } else if (!(resourcePropertyMapping.getExcludeFromAll() == ResourcePropertyMapping.ExcludeFromAllType.YES)
                && !resourcePropertyMapping.isInternal()) {
            allTokenStreamCollector.setTokenStream(retVal);
            retVal = allTokenStreamCollector;
        }
        return retVal;
    }

    public int getPositionIncrementGap(String fieldName) {
        return analyzer.getPositionIncrementGap(fieldName);
    }

    public TokenStream createAllTokenStream() {
        return new AllTokenStream();
    }

    private class AllTokenStream extends TokenStream {

        private Iterator tokenIt;

        private int offset = 0;

        private AllTokenStream() {
        }

        public Token next() throws IOException {
            if (tokenIt == null) {
                tokenIt = tokens.iterator();
            }
            if (tokenIt.hasNext()) {
                Token token = (Token) tokenIt.next();
                // TODO fix offset when upgrading to newer lucene version as it has setters for it (also uses char[])
                int delta = token.endOffset() - token.startOffset();
                Token retVal = new Token(token.termText(), offset, offset + delta, token.type());
                retVal.setPositionIncrement(token.getPositionIncrement());
                offset += delta;
                return retVal;
            }

            return null;
        }
    }

    private class AllTokenStreamCollector extends TokenStream {

        private TokenStream tokenStream;

        public void setTokenStream(TokenStream tokenStream) {
            this.tokenStream = tokenStream;
        }

        public Token next() throws IOException {
            Token token = tokenStream.next();
            if (token != null) {
                tokens.add(token);
            }
            return token;
        }

        public void reset() throws IOException {
            tokenStream.reset();
        }

        public void close() throws IOException {
            tokens.clear();
            tokenStream.close();
        }
    }
}
