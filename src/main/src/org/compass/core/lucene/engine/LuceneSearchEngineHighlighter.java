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

package org.compass.core.lucene.engine;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.TokenSources;
import org.compass.core.CompassHighlighter;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.engine.highlighter.LuceneHighlighterManager;
import org.compass.core.lucene.engine.highlighter.LuceneHighlighterSettings;
import org.compass.core.lucene.engine.highlighter.support.TokenOrderingFilter;

/**
 * @author kimchy
 */
public class LuceneSearchEngineHighlighter implements SearchEngineHighlighter, LuceneDelegatedClose {

    private IndexReader indexReader;

    private boolean closed;

    private Query query;

    private LuceneHighlighterSettings highlighterSettings;

    private LuceneAnalyzerManager analyzerManager;

    private LuceneHighlighterManager highlighterManager;

    private int maxNumFragments = -1;

    private Analyzer analyzer;

    private String separator;

    private int maxBytesToAnalyze = -1;

    private CompassHighlighter.TextTokenizer textTokenizer;

    public LuceneSearchEngineHighlighter(Query query, IndexReader indexReader, LuceneSearchEngine searchEngine) throws SearchEngineException {
        this.indexReader = indexReader;
        this.highlighterManager = searchEngine.getSearchEngineFactory().getHighlighterManager();
        this.highlighterSettings = highlighterManager.getDefaultHighlighterSettings();

        this.analyzerManager = searchEngine.getSearchEngineFactory().getAnalyzerManager();

        if (highlighterSettings.isRewriteQuery()) {
            try {
                this.query = query.rewrite(indexReader);
            } catch (IOException e) {
                throw new SearchEngineException("Failed to rewrite query [" + query + "] for highlighter", e);
            }
        }

        clear();
    }

    public SearchEngineHighlighter clear() {
        analyzer = analyzerManager.getDefaultAnalyzer();
        maxNumFragments = -1;
        separator = null;
        maxBytesToAnalyze = -1;
        return this;
    }

    public SearchEngineHighlighter setMaxNumFragments(int maxNumFragments) throws SearchEngineException {
        this.maxNumFragments = maxNumFragments;
        return this;
    }

    public SearchEngineHighlighter setMaxBytesToAnalyze(int maxBytesToAnalyze) throws SearchEngineException {
        this.maxBytesToAnalyze = maxBytesToAnalyze;
        return this;
    }

    public SearchEngineHighlighter setAnalyzer(String analyzerName) throws SearchEngineException {
        this.analyzer = analyzerManager.getAnalyzerMustExist(analyzerName);
        return this;
    }

    public SearchEngineHighlighter setAnalyzer(Resource resource) throws SearchEngineException {
        this.analyzer = analyzerManager.getAnalyzerByResource(resource);
        return this;
    }

    public SearchEngineHighlighter setHighlighter(String highlighterName) throws SearchEngineException {
        this.highlighterSettings = highlighterManager.getHighlighterSettingsMustExists(highlighterName);
        return this;
    }

    public SearchEngineHighlighter setSeparator(String separator) throws SearchEngineException {
        this.separator = separator;
        return this;
    }

    public SearchEngineHighlighter setTextTokenizer(CompassHighlighter.TextTokenizer textTokenizer)
            throws SearchEngineException {
        this.textTokenizer = textTokenizer;
        return this;
    }

    public String fragment(Resource resource, String propertyName) throws SearchEngineException {
        return fragment(resource, propertyName, getTextFromResource(resource, propertyName));
    }

    public String fragment(Resource resource, String propertyName, String text) throws SearchEngineException {

        Highlighter highlighter = createHighlighter(propertyName);
        TokenStream tokenStream = createTokenStream(resource, propertyName, text);

        try {
            return highlighter.getBestFragment(tokenStream, text);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to highlight fragments for alias [" + resource.getAlias()
                    + "] and property [" + propertyName + "]");
        }
    }

    public String[] fragments(Resource resource, String propertyName) throws SearchEngineException {
        return fragments(resource, propertyName, getTextFromResource(resource, propertyName));
    }

    public String[] fragments(Resource resource, String propertyName, String text) throws SearchEngineException {
        Highlighter highlighter = createHighlighter(propertyName);
        TokenStream tokenStream = createTokenStream(resource, propertyName, text);
        try {
            return highlighter.getBestFragments(tokenStream, text, getMaxNumFragments());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to highlight fragments for alias [" + resource.getAlias()
                    + "] and property [" + propertyName + "]");
        }
    }

    public String fragmentsWithSeparator(Resource resource, String propertyName) throws SearchEngineException {
        return fragmentsWithSeparator(resource, propertyName, getTextFromResource(resource, propertyName));
    }

    public String fragmentsWithSeparator(Resource resource, String propertyName, String text)
            throws SearchEngineException {
        Highlighter highlighter = createHighlighter(propertyName);
        TokenStream tokenStream = createTokenStream(resource, propertyName, text);
        try {
            String actualSeparator = getActualSeparator();
            return highlighter.getBestFragments(tokenStream, text, getMaxNumFragments(), actualSeparator);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to highlight fragments for alias [" + resource.getAlias()
                    + "] and property [" + propertyName + "]");
        }
    }

    public String[] multiValueFragment(Resource resource, String propertyName)
            throws SearchEngineException {
        return multiValueFragment(resource, propertyName, getTextsFromResource(resource, propertyName));
    }

    public String[] multiValueFragment(Resource resource, String propertyName, String[] texts)
            throws SearchEngineException {
        List fragmentList = new ArrayList();
        Highlighter highlighter = createHighlighter(propertyName);
        for (int i = 0; i < texts.length; i++) {
            String text = texts[i];
            if (text != null && text.length() > 0) {
                //TokenStream tokenStream = createTokenStream(resource, propertyName, text);
                // We have to re-analyze one field value at a time
                TokenStream tokenStream = createTokenStreamFromAnalyzer(propertyName, text);
                try {
                    String fragment = highlighter.getBestFragment(tokenStream, text);
                    if (fragment != null && fragment.length() > 0) {
                        fragmentList.add(fragment);
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to highlight fragments for alias [" + resource.getAlias()
                            + "] and property [" + propertyName + "]");
                }
            }
        }
        return (String[]) fragmentList.toArray(new String[fragmentList.size()]);
    }

    public String multiValueFragmentWithSeparator(Resource resource, String propertyName)
            throws SearchEngineException {
        return multiValueFragmentWithSeparator(resource, propertyName, getTextsFromResource(resource, propertyName));
    }

    public String multiValueFragmentWithSeparator(Resource resource, String propertyName, String[] texts)
            throws SearchEngineException {
        String[] fragments = multiValueFragment(resource, propertyName, texts);
        String actualSeparator = getActualSeparator();
        StringBuffer fragment = new StringBuffer();
        if (fragments.length > 0) {
            for (int i = 0; i < (fragments.length - 1); i++) {
                fragment.append(fragments[i]);
                fragment.append(actualSeparator);
            }
            fragment.append(fragments[fragments.length - 1]);
        }
        return fragment.toString();
    }

    protected TokenStream createTokenStream(Resource resource, String propertyName, String text)
            throws SearchEngineException {
        CompassHighlighter.TextTokenizer actualTextTokenizer = highlighterSettings.getTextTokenizer();
        if (textTokenizer != null) {
            actualTextTokenizer = textTokenizer;
        }
        if (actualTextTokenizer == CompassHighlighter.TextTokenizer.AUTO) {
            TokenStream tokenStream = createTokenStreamFromTermPositions(resource, propertyName);
            if (tokenStream == null) {
                tokenStream = createTokenStreamFromAnalyzer(propertyName, text);
            }
            return tokenStream;
        } else if (actualTextTokenizer == CompassHighlighter.TextTokenizer.ANALYZER) {
            return createTokenStreamFromAnalyzer(propertyName, text);
        } else if (actualTextTokenizer == CompassHighlighter.TextTokenizer.TERM_VECTOR) {
            TokenStream tokenStream = createTokenStreamFromTermPositions(resource, propertyName);
            if (tokenStream == null) {
                throw new SearchEngineException(
                        "Highlighter configured/set to use term vector, but no term vector is available");
            }
            return tokenStream;
        }
        throw new SearchEngineException("No handling for text tokenizer [" + actualTextTokenizer + "]");
    }

    protected TokenStream createTokenStreamFromAnalyzer(String propertyName, String text) {
        TokenStream tokenStream = analyzer.tokenStream(propertyName, new StringReader(text));
        if (tokenStream == null) {
            tokenStream = new TokenOrderingFilter(tokenStream, 10);
        }
        return tokenStream;
    }

    protected TokenStream createTokenStreamFromTermPositions(Resource resource, String propertyName)
            throws SearchEngineException {
        int docId = ((LuceneResource) resource).getDocNum();
        TermFreqVector tfv;
        try {
            tfv = indexReader.getTermFreqVector(docId, propertyName);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read term vector info", e);
        }
        if (tfv != null) {
            if (tfv instanceof TermPositionVector) {
                return TokenSources.getTokenStream((TermPositionVector) tfv);
            }
        }
        return null;
    }

    protected Highlighter createHighlighter(String propertyName) throws SearchEngineException {
        Highlighter highlighter = new Highlighter(highlighterSettings.getFormatter(), highlighterSettings.getEncoder(),
                createScorer(propertyName));
        Fragmenter f = highlighterSettings.getFragmenter();
        highlighter.setTextFragmenter(f);
        if (maxBytesToAnalyze == -1) {
            highlighter.setMaxDocBytesToAnalyze(highlighterSettings.getMaxBytesToAnalyze());
        } else {
            highlighter.setMaxDocBytesToAnalyze(maxBytesToAnalyze);
        }
        return highlighter;
    }

    protected Scorer createScorer(String propertyName) throws SearchEngineException {
        if (highlighterSettings.isComputeIdf()) {
            if (propertyName == null) {
                throw new SearchEngineException("When using a formatter that requires idf or setting the ["
                        + LuceneEnvironment.Highlighter.COMPUTE_IDF
                        + "] setting, a resource property name must be provided");
            }
            return new QueryScorer(query, indexReader, propertyName);
        }
        return new QueryScorer(query);
    }

    private String getTextFromResource(Resource resource, String propertyName) {
        String text = resource.getValue(propertyName);
        if (text == null) {
            throw new SearchEngineException("No text is stored for property [" + propertyName + "] and alias ["
                    + resource.getAlias() + "]");
        }
        return text;
    }

    private String[] getTextsFromResource(Resource resource, String propertyName) {
        String[] texts = resource.getValues(propertyName);
        if (texts == null || texts.length == 0) {
            throw new SearchEngineException("No texts are stored for property [" + propertyName + "] and alias ["
                    + resource.getAlias() + "]");
        }
        return texts;
    }

    private int getMaxNumFragments() {
        if (maxNumFragments == -1) {
            return highlighterSettings.getMaxNumFragments();
        }
        return maxNumFragments;
    }

    private String getActualSeparator() {
        String actualSeparator = separator;
        if (actualSeparator == null) {
            actualSeparator = highlighterSettings.getSeparator();
        }
        return actualSeparator;
    }

    public void closeDelegate() throws SearchEngineException {
        close(true);
    }

    public void close() throws SearchEngineException {
        close(false);
    }

    private void close(boolean removeDelegate) throws SearchEngineException {
        if (closed) {
            return;
        }
        closed = true;
    }

}
