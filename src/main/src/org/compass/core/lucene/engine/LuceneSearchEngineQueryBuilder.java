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

package org.compass.core.lucene.engine;

import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery;
import org.compass.core.lucene.util.LuceneQueryParser;

/**
 * 
 * @author kimchy
 * 
 */
public class LuceneSearchEngineQueryBuilder implements SearchEngineQueryBuilder {

    public static class LuceneSearchEngineBooleanQueryBuilder implements SearchEngineBooleanQueryBuilder {

        private LuceneSearchEngine searchEngine;

        private BooleanQuery boolQuery;

        public LuceneSearchEngineBooleanQueryBuilder(LuceneSearchEngine searchEngine, boolean disableCoord) {
            this.searchEngine = searchEngine;
            boolQuery = new BooleanQuery(disableCoord);
        }

        public void addMust(SearchEngineQuery query) {
            boolQuery.add(((LuceneSearchEngineQuery) query).toQuery(), BooleanClause.Occur.MUST);
        }

        public void addMustNot(SearchEngineQuery query) {
            boolQuery.add(((LuceneSearchEngineQuery) query).toQuery(), BooleanClause.Occur.MUST_NOT);
        }

        public void addShould(SearchEngineQuery query) {
            boolQuery.add(((LuceneSearchEngineQuery) query).toQuery(), BooleanClause.Occur.SHOULD);
        }

        public SearchEngineQuery toQuery() {
            return new LuceneSearchEngineQuery(searchEngine, boolQuery);
        }
    }

    public static class LuceneSearchEngineMultiPhraseQueryBuilder implements SearchEngineMultiPhraseQueryBuilder {

        private LuceneSearchEngine searchEngine;

        private String resourceProperty;

        private MultiPhraseQuery multiPhraseQuery;

        public LuceneSearchEngineMultiPhraseQueryBuilder(LuceneSearchEngine searchEngine, String resourceProperty) {
            this.searchEngine = searchEngine;
            this.resourceProperty = resourceProperty;
            this.multiPhraseQuery = new MultiPhraseQuery();
        }

        public SearchEngineMultiPhraseQueryBuilder setSlop(int slop) {
            multiPhraseQuery.setSlop(slop);
            return this;
        }

        public SearchEngineMultiPhraseQueryBuilder add(String value) {
            multiPhraseQuery.add(new Term(resourceProperty, value));
            return this;
        }

        public SearchEngineMultiPhraseQueryBuilder add(String value, int position) {
            multiPhraseQuery.add(new Term[] { new Term(resourceProperty, value) }, position);
            return this;
        }

        public SearchEngineMultiPhraseQueryBuilder add(String[] values) {
            Term[] terms = new Term[values.length];
            for (int i = 0; i < values.length; i++) {
                terms[i] = new Term(resourceProperty, values[i]);
            }
            multiPhraseQuery.add(terms);
            return this;
        }

        public SearchEngineMultiPhraseQueryBuilder add(String[] values, int position) {
            Term[] terms = new Term[values.length];
            for (int i = 0; i < values.length; i++) {
                terms[i] = new Term(resourceProperty, values[i]);
            }
            multiPhraseQuery.add(terms, position);
            return this;
        }

        public SearchEngineQuery toQuery() {
            return new LuceneSearchEngineQuery(searchEngine, multiPhraseQuery);
        }
    }

    public static class LuceneSearchEngineQueryStringBuilder implements SearchEngineQueryStringBuilder {

        private LuceneSearchEngine searchEngine;

        private String analyzer;

        private String defaultSearchProperty;

        private String queryString;

        public LuceneSearchEngineQueryStringBuilder(LuceneSearchEngine searchEngine, String queryString) {
            this.searchEngine = searchEngine;
            this.queryString = queryString;
        }

        public SearchEngineQueryStringBuilder setAnalyzer(String analyzer) {
            this.analyzer = analyzer;
            return this;
        }

        public SearchEngineQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty) {
            this.defaultSearchProperty = defaultSearchProperty;
            return this;
        }

        public SearchEngineQuery toQuery() {
            String defaultSearch = defaultSearchProperty;
            if (defaultSearch == null) {
                defaultSearch = searchEngine.getSearchEngineFactory().getLuceneSettings().getDefaultSearchPropery();
            }
            String analyzerName = analyzer;
            Analyzer analyzer = null;
            if (analyzerName == null) {
                analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager().getSearchAnalyzer();
            } else {
                analyzer = searchEngine.getSearchEngineFactory().getAnalyzerManager()
                        .getAnalyzerMustExist(analyzerName);
            }
            Query qQuery = null;
            try {
                qQuery = LuceneQueryParser.parse(queryString, defaultSearch, analyzer);
            } catch (ParseException e) {
                throw new SearchEngineException("Failed to parse query [" + queryString + "].");
            }
            return new LuceneSearchEngineQuery(searchEngine, qQuery);
        }
    }

    public static class LuceneSearchEngineQuerySpanNearBuilder implements SearchEngineQuerySpanNearBuilder {

        private LuceneSearchEngine searchEngine;

        private String resourceProperty;

        private int slop = 0;

        private boolean inOrder = true;

        private ArrayList values = new ArrayList();

        public LuceneSearchEngineQuerySpanNearBuilder(LuceneSearchEngine searchEngine, String resourceProperty) {
            this.searchEngine = searchEngine;
            this.resourceProperty = resourceProperty;
        }

        public SearchEngineQuerySpanNearBuilder setSlop(int slop) {
            this.slop = slop;
            return this;
        }

        public SearchEngineQuerySpanNearBuilder setInOrder(boolean inOrder) {
            this.inOrder = inOrder;
            return this;
        }

        public SearchEngineQuerySpanNearBuilder add(String value) {
            values.add(new SpanTermQuery(new Term(resourceProperty, value)));
            return this;
        }

        public SearchEngineQuerySpanNearBuilder add(SearchEngineSpanQuery query) {
            values.add(((LuceneSearchEngineSpanQuery) query).toQuery());
            return this;
        }

        public SearchEngineSpanQuery toQuery() {
            SpanQuery[] spanQueries = (SpanQuery[]) values.toArray(new SpanQuery[values.size()]);
            SpanNearQuery spanNearQuery = new SpanNearQuery(spanQueries, slop, inOrder);
            return new LuceneSearchEngineSpanQuery(searchEngine, spanNearQuery);
        }

    }
    
    public static class LuceneSearchEngineQuerySpanOrBuilder implements SearchEngineQuerySpanOrBuilder {
        
        private LuceneSearchEngine searchEngine;
        
        private ArrayList queries = new ArrayList();
        
        public LuceneSearchEngineQuerySpanOrBuilder(LuceneSearchEngine searchEngine) {
            this.searchEngine = searchEngine;
        }
        
        public SearchEngineQuerySpanOrBuilder add(SearchEngineSpanQuery query) {
            queries.add(((LuceneSearchEngineSpanQuery) query).toQuery());
            return this;
        }
        
        public SearchEngineSpanQuery toQuery() {
            SpanQuery[] spanQueries = (SpanQuery[]) queries.toArray(new SpanQuery[queries.size()]);
            SpanOrQuery spanOrQuery = new SpanOrQuery(spanQueries);
            return new LuceneSearchEngineSpanQuery(searchEngine, spanOrQuery);
        }
    }

    private LuceneSearchEngine searchEngine;

    public LuceneSearchEngineQueryBuilder(LuceneSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    public SearchEngineBooleanQueryBuilder bool() {
        return bool(false);
    }

    public SearchEngineBooleanQueryBuilder bool(boolean disableCoord) {
        return new LuceneSearchEngineBooleanQueryBuilder(searchEngine, disableCoord);
    }

    public SearchEngineMultiPhraseQueryBuilder multiPhrase(String resourcePropertyName) {
        return new LuceneSearchEngineMultiPhraseQueryBuilder(searchEngine, resourcePropertyName);
    }

    public SearchEngineQuery term(String resourcePropertyName, String value) {
        Query query = new TermQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQuery between(String resourcePropertyName, String low, String high,
                                     boolean inclusive, boolean constantScore) {
        Query query;
        if (constantScore) {
            query = new ConstantScoreRangeQuery(resourcePropertyName, low, high, inclusive, inclusive);
        } else {
            Term lowTerm = null;
            if (low != null) {
                lowTerm = new Term(resourcePropertyName, low);
            }
            Term highTerm = null;
            if (high != null) {
                highTerm = new Term(resourcePropertyName, high);
            }
            query = new RangeQuery(lowTerm, highTerm, inclusive);
        }
        return new LuceneSearchEngineQuery(searchEngine, query);
    }
    
    public SearchEngineQuery between(String resourcePropertyName, String low, String high, boolean inclusive) {
        return between(resourcePropertyName, low, high, inclusive, true);
    }

    public SearchEngineQuery ge(String resourcePropertyName, String value) {
        return between(resourcePropertyName, value, null, true);
    }

    public SearchEngineQuery gt(String resourcePropertyName, String value) {
        return between(resourcePropertyName, value, null, false);
    }

    public SearchEngineQuery le(String resourcePropertyName, String value) {
        return between(resourcePropertyName, null, value, true);
    }

    public SearchEngineQuery lt(String resourcePropertyName, String value) {
        return between(resourcePropertyName, null, value, false);
    }

    public SearchEngineQuery prefix(String resourcePropertyName, String prefix) {
        Query query = new PrefixQuery(new Term(resourcePropertyName, prefix));
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQuery wildcard(String resourcePropertyName, String wildcard) {
        Query query = new WildcardQuery(new Term(resourcePropertyName, wildcard));
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQuery matchAll() {
        return new LuceneSearchEngineQuery(searchEngine, new MatchAllDocsQuery());
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value), minimumSimilarity);
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity, int prefixLength) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value), minimumSimilarity, prefixLength);
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery(searchEngine, query);
    }

    public SearchEngineQueryStringBuilder queryString(String queryString) {
        return new LuceneSearchEngineQueryStringBuilder(searchEngine, queryString);
    }
    
    public SearchEngineSpanQuery spanEq(String resourcePropertyName, String value) {
        SpanQuery spanQuery = new SpanTermQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngine, spanQuery);
    }

    public SearchEngineSpanQuery spanFirst(String resourcePropertyName, String value, int end) {
        SpanQuery spanQuery = new SpanFirstQuery(new SpanTermQuery(new Term(resourcePropertyName, value)), end);
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngine, spanQuery);
    }

    public SearchEngineQuerySpanNearBuilder spanNear(String resourcePropertyName) {
        return new LuceneSearchEngineQuerySpanNearBuilder(searchEngine, resourcePropertyName);
    }

    public SearchEngineSpanQuery spanNot(SearchEngineSpanQuery include, SearchEngineSpanQuery exclude) {
        SpanNotQuery spanNotQuery = new SpanNotQuery(((LuceneSearchEngineSpanQuery) include).toSpanQuery(),
                ((LuceneSearchEngineSpanQuery) exclude).toSpanQuery());
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngine, spanNotQuery);
    }
    
    public SearchEngineQuerySpanOrBuilder spanOr() {
        return new LuceneSearchEngineQuerySpanOrBuilder(searchEngine);
    }
}
