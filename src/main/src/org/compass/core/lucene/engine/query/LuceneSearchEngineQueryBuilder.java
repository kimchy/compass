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

import java.io.Reader;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreRangeQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.RangeQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNotQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery;
import org.compass.core.lucene.search.ConstantScorePrefixQuery;

/**
 * @author kimchy
 */
public class LuceneSearchEngineQueryBuilder implements SearchEngineQueryBuilder {

    private LuceneSearchEngineFactory searchEngineFactory;

    public LuceneSearchEngineQueryBuilder(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public SearchEngineBooleanQueryBuilder bool() {
        return bool(false);
    }

    public SearchEngineBooleanQueryBuilder bool(boolean disableCoord) {
        return new LuceneSearchEngineBooleanQueryBuilder(searchEngineFactory, disableCoord);
    }

    public SearchEngineMultiPhraseQueryBuilder multiPhrase(String resourcePropertyName) {
        return new LuceneSearchEngineMultiPhraseQueryBuilder(searchEngineFactory, resourcePropertyName);
    }

    public SearchEngineQuery term(String resourcePropertyName, String value) {
        Query query = new TermQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
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
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
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
        Query query = new ConstantScorePrefixQuery(new Term(resourcePropertyName, prefix));
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
    }

    public SearchEngineQuery wildcard(String resourcePropertyName, String wildcard) {
        Query query = new WildcardQuery(new Term(resourcePropertyName, wildcard));
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
    }

    public SearchEngineQuery matchAll() {
        return new LuceneSearchEngineQuery(searchEngineFactory, new MatchAllDocsQuery());
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value), minimumSimilarity);
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity, int prefixLength) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value), minimumSimilarity, prefixLength);
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
    }

    public SearchEngineQuery fuzzy(String resourcePropertyName, String value) {
        Query query = new FuzzyQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery(searchEngineFactory, query);
    }

    public SearchEngineQueryStringBuilder queryString(String queryString) {
        return new LuceneSearchEngineQueryStringBuilder(searchEngineFactory, queryString);
    }

    public SearchEngineMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString) {
        return new LuceneSearchEngineMultiPropertyQueryStringBuilder(searchEngineFactory, queryString);
    }

    public SearchEngineSpanQuery spanEq(String resourcePropertyName, String value) {
        SpanQuery spanQuery = new SpanTermQuery(new Term(resourcePropertyName, value));
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngineFactory, spanQuery);
    }

    public SearchEngineSpanQuery spanFirst(SearchEngineSpanQuery searchEngineSpanQuery, int end) {
        SpanQuery spanQuery = new SpanFirstQuery(((LuceneSearchEngineSpanQuery) searchEngineSpanQuery).toSpanQuery(), end);
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngineFactory, spanQuery);
    }

    public SearchEngineSpanQuery spanFirst(String resourcePropertyName, String value, int end) {
        SpanQuery spanQuery = new SpanFirstQuery(new SpanTermQuery(new Term(resourcePropertyName, value)), end);
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngineFactory, spanQuery);
    }

    public SearchEngineQuerySpanNearBuilder spanNear(String resourcePropertyName) {
        return new LuceneSearchEngineQuerySpanNearBuilder(searchEngineFactory, resourcePropertyName);
    }

    public SearchEngineSpanQuery spanNot(SearchEngineSpanQuery include, SearchEngineSpanQuery exclude) {
        SpanNotQuery spanNotQuery = new SpanNotQuery(((LuceneSearchEngineSpanQuery) include).toSpanQuery(),
                ((LuceneSearchEngineSpanQuery) exclude).toSpanQuery());
        return new LuceneSearchEngineQuery.LuceneSearchEngineSpanQuery(searchEngineFactory, spanNotQuery);
    }

    public SearchEngineQuerySpanOrBuilder spanOr() {
        return new LuceneSearchEngineQuerySpanOrBuilder(searchEngineFactory);
    }

    public SearchEngineMoreLikeThisQueryBuilder moreLikeThis(SearchEngine searchEngine, Resource idResource) {
        return new LuceneSearchEngineMoreLikeThisQueryBuilder((LuceneSearchEngine) searchEngine, searchEngineFactory, idResource);
    }

    public SearchEngineMoreLikeThisQueryBuilder moreLikeThis(SearchEngine searchEngine, Reader reader) {
        return new LuceneSearchEngineMoreLikeThisQueryBuilder((LuceneSearchEngine) searchEngine, searchEngineFactory, reader);
    }
}
