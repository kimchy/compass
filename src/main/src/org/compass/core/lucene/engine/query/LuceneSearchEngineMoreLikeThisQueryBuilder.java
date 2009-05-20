/*
 * Copyright 2004-2009 the original author or authors.
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

package org.compass.core.lucene.engine.query;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.support.ResourceHelper;
import org.compass.core.spi.InternalResource;

/**
 * @author kimchy
 */
public class LuceneSearchEngineMoreLikeThisQueryBuilder implements SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder {

    private LuceneSearchEngine searchEngine;

    private LuceneSearchEngineFactory searchEngineFactory;

    private Reader reader;

    private Resource idResource;

    private LuceneSearchEngineInternalSearch internalSearch;

    private MoreLikeThis moreLikeThis;

    private String[] subIndexes;

    private String[] aliases;

    private boolean propertiesSet;

    public LuceneSearchEngineMoreLikeThisQueryBuilder(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory, Resource idResource) {
        this.idResource = idResource;
        init(searchEngine, searchEngineFactory);
    }

    public LuceneSearchEngineMoreLikeThisQueryBuilder(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory, Reader reader) {
        this.reader = reader;
        init(searchEngine, searchEngineFactory);
    }

    private void init(LuceneSearchEngine searchEngine, LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngine = searchEngine;
        this.searchEngineFactory = searchEngineFactory;
        this.internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        this.moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        this.moreLikeThis.setFieldNames(null);
        this.moreLikeThis.setAnalyzer(searchEngine.getSearchEngineFactory().getAnalyzerManager().getSearchAnalyzer());
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setSubIndexes(String[] subIndexes) {
        this.subIndexes = subIndexes;
        this.internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        MoreLikeThis moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        copy(moreLikeThis);
        this.moreLikeThis = moreLikeThis;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setAliases(String[] aliases) {
        this.aliases = aliases;
        this.internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(subIndexes, aliases);
        MoreLikeThis moreLikeThis = new MoreLikeThis(internalSearch.getReader());
        copy(moreLikeThis);
        this.moreLikeThis = moreLikeThis;
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setProperties(String[] properties) {
        propertiesSet = true;
        moreLikeThis.setFieldNames(properties);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder addProperty(String property) {
        propertiesSet = true;
        if (moreLikeThis.getFieldNames() == null) {
            moreLikeThis.setFieldNames(new String[]{property});
        } else {
            String[] newNames = new String[moreLikeThis.getFieldNames().length + 1];
            System.arraycopy(moreLikeThis.getFieldNames(), 0, newNames, 0, moreLikeThis.getFieldNames().length);
            newNames[newNames.length -1] = property;
            moreLikeThis.setFieldNames(newNames);
        }
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setAnalyzer(String analyzer) {
        moreLikeThis.setAnalyzer(searchEngine.getSearchEngineFactory().getAnalyzerManager().getAnalyzerMustExist(analyzer));
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setBoost(boolean boost) {
        moreLikeThis.setBoost(boost);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxNumTokensParsed(int maxNumTokensParsed) {
        moreLikeThis.setMaxNumTokensParsed(maxNumTokensParsed);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxQueryTerms(int maxQueryTerms) {
        moreLikeThis.setMaxQueryTerms(maxQueryTerms);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMaxWordLen(int maxWordLen) {
        moreLikeThis.setMaxWordLen(maxWordLen);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinResourceFreq(int minDocFreq) {
        moreLikeThis.setMinDocFreq(minDocFreq);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinTermFreq(int minTermFreq) {
        moreLikeThis.setMinTermFreq(minTermFreq);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setMinWordLen(int minWordLen) {
        moreLikeThis.setMinWordLen(minWordLen);
        return this;
    }

    public SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder setStopWords(String[] stopWords) {
        HashSet<String> set = new HashSet<String>();
        set.addAll(Arrays.asList(stopWords));
        moreLikeThis.setStopWords(set);
        return this;
    }

    public SearchEngineQuery toQuery() {
        if (!propertiesSet) {
            moreLikeThis.setFieldNames(new String[]{searchEngine.getSearchEngineFactory().getAllProperty()});
        }
        try {
            Query query;
            if (reader != null) {
                query = moreLikeThis.like(reader);
            } else {
                // we find the doc id based on the actual internal search that was perfomed, since we need the doc id
                // to be sync'ed with the index reader and searcher we use with the MoreLikeThis class
                Query resourceLoadQuery = ResourceHelper.buildResourceLoadQuery(((InternalResource) idResource).getResourceKey());
                final AtomicInteger docId = new AtomicInteger(-1);
                internalSearch.getSearcher().search(resourceLoadQuery, new HitCollector() {
                    @Override
                    public void collect(int doc, float score) {
                        if (score > 0) {
                            docId.set(doc);
                        }
                    }
                });

                BooleanQuery boolQuery = new BooleanQuery();
                boolQuery.add(moreLikeThis.like(docId.intValue()), BooleanClause.Occur.MUST);
                boolQuery.add(resourceLoadQuery, BooleanClause.Occur.MUST_NOT);
                query = boolQuery;
            }
            return new LuceneSearchEngineQuery(searchEngineFactory, query);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to find queries like [" + idResource + "]", e);
        }
    }

    private void copy(MoreLikeThis moreLikeThis) {
        moreLikeThis.setAnalyzer(this.moreLikeThis.getAnalyzer());
        moreLikeThis.setBoost(this.moreLikeThis.isBoost());
        moreLikeThis.setFieldNames(this.moreLikeThis.getFieldNames());
        moreLikeThis.setMaxNumTokensParsed(this.moreLikeThis.getMaxNumTokensParsed());
        moreLikeThis.setMaxQueryTerms(this.moreLikeThis.getMaxQueryTerms());
        moreLikeThis.setMaxWordLen(this.moreLikeThis.getMaxWordLen());
        moreLikeThis.setMinDocFreq(this.moreLikeThis.getMinDocFreq());
        moreLikeThis.setMinTermFreq(this.moreLikeThis.getMinTermFreq());
        moreLikeThis.setMinWordLen(this.moreLikeThis.getMinWordLen());
        moreLikeThis.setStopWords(this.moreLikeThis.getStopWords());
    }
}
