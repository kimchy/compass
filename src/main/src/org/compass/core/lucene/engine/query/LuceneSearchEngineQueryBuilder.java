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

package org.compass.core.lucene.engine.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;

/**
 * @author kimchy
 */
public class LuceneSearchEngineQueryBuilder implements SearchEngineQueryBuilder {

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

    public SearchEngineMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString) {
        return new LuceneSearchEngineMultiPropertyQueryStringBuilder(searchEngine, queryString);
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
