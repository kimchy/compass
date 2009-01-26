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

package org.compass.core.impl;

import java.io.Reader;
import java.io.Serializable;

import org.compass.core.CompassException;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQuery.CompassSpanQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineBooleanQueryBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineMultiPhraseQueryBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQuerySpanNearBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQuerySpanOrBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQueryStringBuilder;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassQuery;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalCompassSpanQuery;

/**
 * @author kimchy
 */
public class DefaultCompassQueryBuilder implements CompassQueryBuilder {

    public class DefaultCompassBooleanQueryBuilder implements CompassBooleanQueryBuilder {

        private SearchEngineBooleanQueryBuilder queryBuilder;

        public DefaultCompassBooleanQueryBuilder(SearchEngineBooleanQueryBuilder queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public CompassBooleanQueryBuilder addMust(CompassQuery query) {
            queryBuilder.addMust(((DefaultCompassQuery) query).getSearchEngineQuery());
            return this;
        }

        public CompassBooleanQueryBuilder addMustNot(CompassQuery query) {
            queryBuilder.addMustNot(((DefaultCompassQuery) query).getSearchEngineQuery());
            return this;
        }

        public CompassBooleanQueryBuilder addShould(CompassQuery query) {
            queryBuilder.addShould(((DefaultCompassQuery) query).getSearchEngineQuery());
            return this;
        }

        public CompassBooleanQueryBuilder setMinimumNumberShouldMatch(int min) {
            queryBuilder.setMinimumNumberShouldMatch(min);
            return this;
        }

        public CompassQuery toQuery() {
            return buildCompassQuery(queryBuilder.toQuery());
        }

    }

    public class DefaultCompassMultiPhraseQueryBuilder implements CompassMultiPhraseQueryBuilder {

        private SearchEngineMultiPhraseQueryBuilder queryBuilder;

        private ResourcePropertyLookup lookup;

        public DefaultCompassMultiPhraseQueryBuilder(SearchEngineMultiPhraseQueryBuilder queryBuilder, ResourcePropertyLookup lookup) {
            this.queryBuilder = queryBuilder;
            this.lookup = lookup;
        }

        public CompassMultiPhraseQueryBuilder setSlop(int slop) {
            queryBuilder.setSlop(slop);
            return this;
        }

        public CompassMultiPhraseQueryBuilder add(Object value) {
            queryBuilder.add(lookup.getValue(value));
            return this;
        }

        public CompassMultiPhraseQueryBuilder add(Object value, int position) {
            queryBuilder.add(lookup.getValue(value), position);
            return this;
        }

        public CompassMultiPhraseQueryBuilder add(Object[] values) {
            String[] strValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strValues[i] = lookup.getValue(values[i]);
            }
            queryBuilder.add(strValues);
            return this;
        }

        public CompassMultiPhraseQueryBuilder add(Object[] values, int position) {
            String[] strValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strValues[i] = lookup.getValue(values[i]);
            }
            queryBuilder.add(strValues, position);
            return this;
        }

        public CompassQuery toQuery() {
            return buildCompassQuery(queryBuilder.toQuery());
        }
    }

    public class DefaultCompassQueryStringBuilder implements CompassQueryStringBuilder {

        private SearchEngineQueryStringBuilder queryBuilder;

        public DefaultCompassQueryStringBuilder(SearchEngineQueryStringBuilder queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public CompassQueryStringBuilder setAnalyzer(String analyzer) throws CompassException {
            queryBuilder.setAnalyzer(analyzer);
            return this;
        }

        public CompassQueryStringBuilder setAnalyzerByAlias(String alias) throws CompassException {
            queryBuilder.setAnalyzerByAlias(alias);
            return this;
        }

        public CompassQueryStringBuilder setQueryParser(String queryParser) throws CompassException {
            queryBuilder.setQueryParser(queryParser);
            return this;
        }

        public CompassQueryStringBuilder useSpellCheck() throws CompassException {
            queryBuilder.useSpellCheck();
            return this;
        }

        public CompassQueryStringBuilder setDefaultSearchProperty(String defaultSearchProperty) {
            queryBuilder.setDefaultSearchProperty(defaultSearchProperty);
            return this;
        }

        public CompassQueryStringBuilder useAndDefaultOperator() {
            queryBuilder.useAndDefaultOperator();
            return this;
        }

        public CompassQueryStringBuilder useOrDefaultOperator() {
            queryBuilder.useOrDefaultOperator();
            return this;
        }

        public CompassQueryStringBuilder forceAnalyzer() {
            queryBuilder.forceAnalyzer();
            return this;
        }

        public CompassQuery toQuery() {
            return buildCompassQuery(queryBuilder.toQuery());
        }

    }

    public class DefaultCompassMultiPropertyQueryStringBuilder implements CompassMultiPropertyQueryStringBuilder {

        private SearchEngineMultiPropertyQueryStringBuilder queryBuilder;

        public DefaultCompassMultiPropertyQueryStringBuilder(SearchEngineMultiPropertyQueryStringBuilder queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public CompassMultiPropertyQueryStringBuilder setAnalyzer(String analyzer) throws CompassException {
            queryBuilder.setAnalyzer(analyzer);
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder setAnalyzerByAlias(String alias) throws CompassException {
            queryBuilder.setAnalyzerByAlias(alias);
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder setQueryParser(String queryParser) throws CompassException {
            queryBuilder.setQueryParser(queryParser);
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder useSpellCheck() {
            queryBuilder.useSpellCheck();
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder add(String name) {
            queryBuilder.add(compass.getMapping().getResourcePropertyLookup(name).getPath());
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder useAndDefaultOperator() {
            queryBuilder.useAndDefaultOperator();
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder useOrDefaultOperator() {
            queryBuilder.useOrDefaultOperator();
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder forceAnalyzer() {
            queryBuilder.forceAnalyzer();
            return this;
        }

        public CompassQuery toQuery() {
            return buildCompassQuery(queryBuilder.toQuery());
        }
    }

    public class DefaultCompassQuerySpanNearBuilder implements CompassQuerySpanNearBuilder {

        private SearchEngineQuerySpanNearBuilder queryBuilder;

        private ResourcePropertyLookup lookup;

        public DefaultCompassQuerySpanNearBuilder(SearchEngineQuerySpanNearBuilder queryBuilder, ResourcePropertyLookup lookup) {
            this.queryBuilder = queryBuilder;
            this.lookup = lookup;
        }

        public CompassQuerySpanNearBuilder setSlop(int slop) {
            queryBuilder.setSlop(slop);
            return this;
        }

        public CompassQuerySpanNearBuilder setInOrder(boolean inOrder) {
            queryBuilder.setInOrder(inOrder);
            return this;
        }

        public CompassQuerySpanNearBuilder add(Object value) {
            queryBuilder.add(lookup.getValue(value));
            return this;
        }

        public CompassQuerySpanNearBuilder add(CompassSpanQuery query) {
            queryBuilder.add(((DefaultCompassQuery.DefaultCompassSpanQuey) query).getSearchEngineSpanQuery());
            return this;
        }

        public CompassSpanQuery toQuery() {
            SearchEngineSpanQuery query = queryBuilder.toQuery();
            return buildCompassQuery(query);
        }
    }

    public class DefaultCompassQuerySpanOrBuilder implements CompassQuerySpanOrBuilder {

        private SearchEngineQuerySpanOrBuilder queryBuilder;

        public DefaultCompassQuerySpanOrBuilder(SearchEngineQuerySpanOrBuilder queryBuilder) {
            this.queryBuilder = queryBuilder;
        }

        public CompassQuerySpanOrBuilder add(CompassSpanQuery query) {
            queryBuilder.add(((DefaultCompassQuery.DefaultCompassSpanQuey) query).getSearchEngineSpanQuery());
            return this;
        }

        public CompassSpanQuery toQuery() {
            SearchEngineSpanQuery query = queryBuilder.toQuery();
            return buildCompassQuery(query);
        }
    }

    public class DefaultCompassMoreLikeThisQuery implements CompassMoreLikeThisQuery {

        private SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder queryBuilder;

        private InternalCompassSession session;

        public DefaultCompassMoreLikeThisQuery(SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder queryBuilder, InternalCompassSession session) {
            this.queryBuilder = queryBuilder;
            this.session = session;
        }

        public CompassMoreLikeThisQuery setSubIndexes(String[] subIndexes) {
            queryBuilder.setSubIndexes(subIndexes);
            return this;
        }

        public CompassMoreLikeThisQuery setAliases(String[] aliases) {
            queryBuilder.setAliases(aliases);
            return this;
        }

        public CompassMoreLikeThisQuery setProperties(String[] properties) {
            if (properties == null) {
                queryBuilder.setProperties(properties);
            } else {
                String[] updatedProperties = new String[properties.length];
                for (int i = 0; i < properties.length; i++) {
                    ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(properties[i]);
                    updatedProperties[i] = lookup.getPath();
                }
                queryBuilder.setProperties(updatedProperties);
            }
            return this;
        }

        public CompassMoreLikeThisQuery addProperty(String property) {
            ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(property);
            queryBuilder.addProperty(lookup.getPath());
            return this;
        }

        public CompassMoreLikeThisQuery setAnalyzer(String analyzer) {
            queryBuilder.setAnalyzer(analyzer);
            return this;
        }

        public CompassMoreLikeThisQuery setBoost(boolean boost) {
            queryBuilder.setBoost(boost);
            return this;
        }

        public CompassMoreLikeThisQuery setMaxNumTokensParsed(int maxNumTokensParsed) {
            queryBuilder.setMaxNumTokensParsed(maxNumTokensParsed);
            return this;
        }

        public CompassMoreLikeThisQuery setMaxQueryTerms(int maxQueryTerms) {
            queryBuilder.setMaxQueryTerms(maxQueryTerms);
            return this;
        }

        public CompassMoreLikeThisQuery setMaxWordLen(int maxWordLen) {
            queryBuilder.setMaxWordLen(maxWordLen);
            return this;
        }

        public CompassMoreLikeThisQuery setMinWordLen(int minWordLen) {
            queryBuilder.setMinWordLen(minWordLen);
            return this;
        }

        public CompassMoreLikeThisQuery setMinResourceFreq(int minDocFreq) {
            queryBuilder.setMinResourceFreq(minDocFreq);
            return this;
        }

        public CompassMoreLikeThisQuery setMinTermFreq(int minTermFreq) {
            queryBuilder.setMinTermFreq(minTermFreq);
            return this;
        }

        public CompassMoreLikeThisQuery setStopWords(String[] stopWords) {
            queryBuilder.setStopWords(stopWords);
            return this;
        }

        public CompassQuery toQuery() {
            return buildCompassQuery(queryBuilder.toQuery());
        }
    }

    private final SearchEngineQueryBuilder queryBuilder;

    private final InternalCompass compass;

    private final InternalCompassSession session;

    private boolean convertOnlyWithDotPath = false;

    private boolean addAliasQueryIfNeeded = true;

    public DefaultCompassQueryBuilder(SearchEngineQueryBuilder queryBuilder, InternalCompass compass) {
        this(queryBuilder, compass, null);
    }

    public DefaultCompassQueryBuilder(SearchEngineQueryBuilder queryBuilder, InternalCompass compass, InternalCompassSession session) {
        this.queryBuilder = queryBuilder;
        this.compass = compass;
        this.session = session;
    }

    public CompassQueryBuilder convertOnlyWithDotPath(boolean convertOnlyWithDotPath) {
        this.convertOnlyWithDotPath = convertOnlyWithDotPath;
        return this;
    }

    public CompassQueryBuilder addAliasQueryIfNeeded(boolean addAliasQueryIfNeeded) {
        this.addAliasQueryIfNeeded = addAliasQueryIfNeeded;
        return this;
    }

    public CompassBooleanQueryBuilder bool() {
        return new DefaultCompassBooleanQueryBuilder(queryBuilder.bool());
    }

    public CompassBooleanQueryBuilder bool(boolean disableCoord) {
        return new DefaultCompassBooleanQueryBuilder(queryBuilder.bool(disableCoord));
    }

    public CompassMultiPhraseQueryBuilder multiPhrase(String name) {
        ResourcePropertyLookup lookup = getLookup(name);
        return new DefaultCompassMultiPhraseQueryBuilder(queryBuilder.multiPhrase(lookup.getPath()), lookup);
    }

    public CompassQueryStringBuilder queryString(String queryString) {
        return new DefaultCompassQueryStringBuilder(queryBuilder.queryString(queryString));
    }

    public CompassMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString) {
        return new DefaultCompassMultiPropertyQueryStringBuilder(queryBuilder.multiPropertyQueryString(queryString));
    }

    public CompassQuery alias(String aliasValue) {
        if (!compass.getMapping().hasRootMappingByAlias(aliasValue)) {
            throw new CompassException("Alias [" + aliasValue + "] not found in Compass mappings definitions");
        }
        String aliasProperty = compass.getSearchEngineFactory().getAliasProperty();
        SearchEngineQuery query = queryBuilder.term(aliasProperty, aliasValue);
        return buildCompassQuery(query);
    }

    public CompassQuery polyAlias(String aliasValue) {
        return bool().addShould(term(compass.getSearchEngineFactory().getAliasProperty(), aliasValue))
                .addShould(term(compass.getSearchEngineFactory().getExtendedAliasProperty(), aliasValue))
                .setMinimumNumberShouldMatch(1)
                .toQuery();
    }

    public CompassQuery term(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.term(lookup.getPath(), lookup.getValue(value));
        InternalCompassQuery compassQuery = new DefaultCompassQuery(wrapWithAliasQueryIfNeeded(lookup, query), compass);
        attachIfPossible(compassQuery);
        return compassQuery;
    }

    public CompassQuery matchAll() {
        SearchEngineQuery query = queryBuilder.matchAll();
        return buildCompassQuery(query);
    }

    public CompassQuery between(String name, Object low, Object high, boolean inclusive, boolean constantScore) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.between(lookup.getPath(), lookup.getValue(low), lookup.getValue(high),
                inclusive, constantScore);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery between(String name, Object low, Object high, boolean inclusive) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.between(lookup.getPath(), lookup.getValue(low), lookup.getValue(high),
                inclusive);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery lt(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.lt(lookup.getPath(), lookup.getValue(value));
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery le(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.le(lookup.getPath(), lookup.getValue(value));
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery gt(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.gt(lookup.getPath(), lookup.getValue(value));
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery ge(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.ge(lookup.getPath(), lookup.getValue(value));
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery prefix(String name, String prefix) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.prefix(lookup.getPath(), prefix);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery wildcard(String name, String wildcard) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.wildcard(lookup.getPath(), wildcard);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery fuzzy(String name, String value, float minimumSimilarity) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value, minimumSimilarity);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery fuzzy(String name, String value, float minimumSimilarity, int prefixLength) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value, minimumSimilarity, prefixLength);
        return buildCompassQuery(query, lookup);
    }

    public CompassQuery fuzzy(String name, String value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value);
        return buildCompassQuery(query, lookup);
    }

    public CompassSpanQuery spanEq(String name, Object value) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineSpanQuery query = queryBuilder.spanEq(lookup.getPath(), lookup.getValue(value));
        return buildCompassQuery(query);
    }

    public CompassSpanQuery spanFirst(String name, Object value, int end) {
        ResourcePropertyLookup lookup = getLookup(name);
        SearchEngineSpanQuery query = queryBuilder.spanFirst(lookup.getPath(), lookup.getValue(value), end);
        return buildCompassQuery(query);
    }

    public CompassSpanQuery spanFirst(CompassSpanQuery spanQuery, int end) {
        SearchEngineSpanQuery query = queryBuilder.spanFirst(((DefaultCompassQuery.DefaultCompassSpanQuey) spanQuery).getSearchEngineSpanQuery(), end);
        return buildCompassQuery(query);
    }

    public CompassQuerySpanNearBuilder spanNear(String name) {
        ResourcePropertyLookup lookup = getLookup(name);
        return new DefaultCompassQuerySpanNearBuilder(queryBuilder.spanNear(lookup.getPath()), lookup);
    }

    public CompassSpanQuery spanNot(CompassSpanQuery include, CompassSpanQuery exclude) {
        SearchEngineSpanQuery query = queryBuilder.spanNot(((DefaultCompassQuery.DefaultCompassSpanQuey) include)
                .getSearchEngineSpanQuery(), ((DefaultCompassQuery.DefaultCompassSpanQuey) exclude)
                .getSearchEngineSpanQuery());
        return buildCompassQuery(query);
    }

    public CompassQuerySpanOrBuilder spanOr() {
        return new DefaultCompassQuerySpanOrBuilder(queryBuilder.spanOr());
    }

    public CompassMoreLikeThisQuery moreLikeThis(String alias, Serializable id) {
        if (session == null) {
            throw new CompassException("moreLikeThis query can only be used when constructed using a CompassSession");
        }
        Resource idResource = session.getMarshallingStrategy().marshallIds(alias, id);
        SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder mltQueryBuilder = queryBuilder.moreLikeThis(session.getSearchEngine(), idResource);
        return new DefaultCompassMoreLikeThisQuery(mltQueryBuilder, session);
    }

    public CompassMoreLikeThisQuery moreLikeThis(Reader reader) {
        if (session == null) {
            throw new CompassException("moreLikeThis query can only be used when constructed using a CompassSession");
        }
        SearchEngineQueryBuilder.SearchEngineMoreLikeThisQueryBuilder mltQueryBuilder = queryBuilder.moreLikeThis(session.getSearchEngine(), reader);
        return new DefaultCompassMoreLikeThisQuery(mltQueryBuilder, session);
    }

    private InternalCompassSpanQuery buildCompassQuery(SearchEngineSpanQuery query) {
        InternalCompassSpanQuery compassSpanQuery = new DefaultCompassQuery.DefaultCompassSpanQuey(query, compass);
        attachIfPossible(compassSpanQuery);
        return compassSpanQuery;
    }

    private InternalCompassQuery buildCompassQuery(SearchEngineQuery query, ResourcePropertyLookup lookup) {
        return buildCompassQuery(wrapWithAliasQueryIfNeeded(lookup, query));
    }


    private InternalCompassQuery buildCompassQuery(SearchEngineQuery query) {
        InternalCompassQuery compassQuery = new DefaultCompassQuery(query, compass);
        attachIfPossible(compassQuery);
        return compassQuery;
    }

    private void attachIfPossible(InternalCompassQuery query) {
        if (session != null) {
            query.attach(session);
            session.addDelegateClose(query);
        }
    }

    private SearchEngineQuery wrapWithAliasQueryIfNeeded(ResourcePropertyLookup lookup, SearchEngineQuery query) {
        if (!addAliasQueryIfNeeded) {
            return query;
        }
        if (lookup == null) {
            return query;
        }
        String alias = lookup.getDotPathAlias();
        if (alias == null) {
            return query;
        }
        return queryBuilder.bool()
                .addMust(query)
                .addMust(queryBuilder.bool()
                        .addShould(queryBuilder.term(compass.getSearchEngineFactory().getAliasProperty(), alias))
                        .addShould(queryBuilder.term(compass.getSearchEngineFactory().getExtendedAliasProperty(), alias))
                        .setMinimumNumberShouldMatch(1)
                        .toQuery())
                .toQuery();
    }

    private ResourcePropertyLookup getLookup(String name) {
        ResourcePropertyLookup lookup = compass.getMapping().getResourcePropertyLookup(name);
        lookup.setConvertOnlyWithDotPath(convertOnlyWithDotPath);
        return lookup;
    }
}
