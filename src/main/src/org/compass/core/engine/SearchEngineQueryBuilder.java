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

package org.compass.core.engine;

import java.io.Reader;

import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;

/**
 * @author kimchy
 */
public interface SearchEngineQueryBuilder {

    public static interface SearchEngineToQuery {

        SearchEngineQuery toQuery();
    }

    public static interface SearchEngineBooleanQueryBuilder extends SearchEngineToQuery {

        SearchEngineBooleanQueryBuilder addMust(SearchEngineQuery query);

        SearchEngineBooleanQueryBuilder addMustNot(SearchEngineQuery query);

        SearchEngineBooleanQueryBuilder addShould(SearchEngineQuery query);

        SearchEngineBooleanQueryBuilder setMinimumNumberShouldMatch(int min);
    }

    public static interface SearchEngineMultiPhraseQueryBuilder extends SearchEngineToQuery {

        SearchEngineMultiPhraseQueryBuilder setSlop(int slop);

        SearchEngineMultiPhraseQueryBuilder add(String value);

        SearchEngineMultiPhraseQueryBuilder add(String value, int position);

        SearchEngineMultiPhraseQueryBuilder add(String[] values);

        SearchEngineMultiPhraseQueryBuilder add(String[] values, int position);
    }

    public static interface SearchEngineQueryStringBuilder extends SearchEngineToQuery {

        SearchEngineQueryStringBuilder setAnalyzer(String analyzer);

        SearchEngineQueryStringBuilder setAnalyzerByAlias(String alias);

        SearchEngineQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty);

        SearchEngineQueryStringBuilder useAndDefaultOperator();

        SearchEngineQueryStringBuilder useOrDefaultOperator();

        SearchEngineQueryStringBuilder forceAnalyzer();

        SearchEngineQueryStringBuilder setQueryParser(String queryParser);

        SearchEngineQueryStringBuilder useSpellCheck();
    }

    public static interface SearchEngineMultiPropertyQueryStringBuilder extends SearchEngineToQuery {

        SearchEngineMultiPropertyQueryStringBuilder setAnalyzer(String analyzer);

        SearchEngineMultiPropertyQueryStringBuilder setAnalyzerByAlias(String alias);

        SearchEngineMultiPropertyQueryStringBuilder add(String resourcePropertyName);

        SearchEngineMultiPropertyQueryStringBuilder useAndDefaultOperator();

        SearchEngineMultiPropertyQueryStringBuilder useOrDefaultOperator();

        SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder forceAnalyzer();

        SearchEngineMultiPropertyQueryStringBuilder setQueryParser(String queryParser);

        SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder useSpellCheck();
    }

    public static interface SearchEngineQuerySpanNearBuilder {

        SearchEngineQuerySpanNearBuilder setSlop(int slop);

        SearchEngineQuerySpanNearBuilder setInOrder(boolean inOrder);

        SearchEngineQuerySpanNearBuilder add(String value);

        SearchEngineQuerySpanNearBuilder add(SearchEngineSpanQuery query);

        SearchEngineSpanQuery toQuery();
    }

    public static interface SearchEngineQuerySpanOrBuilder {

        SearchEngineQuerySpanOrBuilder add(SearchEngineSpanQuery query);

        SearchEngineSpanQuery toQuery();
    }

    public static interface SearchEngineMoreLikeThisQueryBuilder extends SearchEngineToQuery {

        SearchEngineMoreLikeThisQueryBuilder setSubIndexes(String[] subIndexes);

        SearchEngineMoreLikeThisQueryBuilder setAliases(String[] aliases);

        SearchEngineMoreLikeThisQueryBuilder setProperties(String[] properties);

        SearchEngineMoreLikeThisQueryBuilder addProperty(String property);

        SearchEngineMoreLikeThisQueryBuilder setAnalyzer(String analyzer);

        SearchEngineMoreLikeThisQueryBuilder setBoost(boolean boost);

        SearchEngineMoreLikeThisQueryBuilder setMaxNumTokensParsed(int maxNumTokensParsed);

        SearchEngineMoreLikeThisQueryBuilder setMaxQueryTerms(int maxQueryTerms);

        SearchEngineMoreLikeThisQueryBuilder setMaxWordLen(int maxWordLen);

        SearchEngineMoreLikeThisQueryBuilder setMinResourceFreq(int minDocFreq);

        SearchEngineMoreLikeThisQueryBuilder setMinTermFreq(int minTermFreq);

        SearchEngineMoreLikeThisQueryBuilder setMinWordLen(int minWordLen);

        SearchEngineMoreLikeThisQueryBuilder setStopWords(String[] stopWords);
    }

    SearchEngineBooleanQueryBuilder bool();

    SearchEngineBooleanQueryBuilder bool(boolean disableCoord);

    SearchEngineMultiPhraseQueryBuilder multiPhrase(String resourcePropertyName);

    SearchEngineQueryStringBuilder queryString(String queryString);

    SearchEngineMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString);

    SearchEngineQuery wildcard(String resourcePropertyName, String wildcard);

    SearchEngineQuery term(String resourcePropertyName, String value);

    SearchEngineQuery matchAll();

    SearchEngineQuery between(String resourcePropertyName, String low, String high,
                              boolean inclusive, boolean constantScore);

    SearchEngineQuery between(String resourcePropertyName, String low, String high, boolean inclusive);

    SearchEngineQuery lt(String resourcePropertyName, String value);

    SearchEngineQuery le(String resourcePropertyName, String value);

    SearchEngineQuery gt(String resourcePropertyName, String value);

    SearchEngineQuery ge(String resourcePropertyName, String value);

    SearchEngineQuery prefix(String resourcePropertyName, String prefix);

    SearchEngineQuery fuzzy(String resourcePropertyName, String value);

    SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity);

    SearchEngineQuery fuzzy(String resourcePropertyName, String value, float minimumSimilarity, int prefixLength);

    SearchEngineSpanQuery spanEq(String resourcePropertyName, String value);

    SearchEngineSpanQuery spanFirst(SearchEngineSpanQuery searchEngineSpanQuery, int end);

    SearchEngineSpanQuery spanFirst(String resourcePropertyName, String value, int end);

    SearchEngineQuerySpanNearBuilder spanNear(String resourcePropertyName);

    SearchEngineSpanQuery spanNot(SearchEngineSpanQuery include, SearchEngineSpanQuery exclude);

    SearchEngineQuerySpanOrBuilder spanOr();

    SearchEngineMoreLikeThisQueryBuilder moreLikeThis(SearchEngine searchEngine, Resource idResource);

    SearchEngineMoreLikeThisQueryBuilder moreLikeThis(SearchEngine searchEngine, Reader reader);
}
