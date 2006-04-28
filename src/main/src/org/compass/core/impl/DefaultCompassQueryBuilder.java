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

package org.compass.core.impl;

import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQuery.CompassSpanQuery;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineBooleanQueryBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineMultiPhraseQueryBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQuerySpanNearBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQuerySpanOrBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineQueryStringBuilder;
import org.compass.core.engine.SearchEngineQueryBuilder.SearchEngineMultiPropertyQueryStringBuilder;
import org.compass.core.mapping.CompassMapping.ResourcePropertyLookup;

/**
 * 
 * @author kimchy
 * 
 */
public class DefaultCompassQueryBuilder implements CompassQueryBuilder {

    public static class DefaultCompassBooleanQueryBuilder implements CompassBooleanQueryBuilder {

        private SearchEngineBooleanQueryBuilder queryBuilder;

        private InternalCompassSession session;

        public DefaultCompassBooleanQueryBuilder(SearchEngineBooleanQueryBuilder queryBuilder,
                InternalCompassSession session) {
            this.queryBuilder = queryBuilder;
            this.session = session;
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

        public CompassQuery toQuery() {
            SearchEngineQuery query = queryBuilder.toQuery();
            return new DefaultCompassQuery(query, session);
        }

    }

    public static class DefaultCompassMultiPhraseQueryBuilder implements CompassMultiPhraseQueryBuilder {

        private SearchEngineMultiPhraseQueryBuilder queryBuilder;

        private InternalCompassSession session;

        private ResourcePropertyLookup lookup;

        public DefaultCompassMultiPhraseQueryBuilder(SearchEngineMultiPhraseQueryBuilder queryBuilder,
                InternalCompassSession session, ResourcePropertyLookup lookup) {
            this.queryBuilder = queryBuilder;
            this.session = session;
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
            SearchEngineQuery query = queryBuilder.toQuery();
            return new DefaultCompassQuery(query, session);
        }
    }

    public static class DefaultCompassQueryStringBuilder implements CompassQueryStringBuilder {

        private SearchEngineQueryStringBuilder queryBuilder;

        private InternalCompassSession session;

        public DefaultCompassQueryStringBuilder(SearchEngineQueryStringBuilder queryBuilder,
                InternalCompassSession session) {
            this.queryBuilder = queryBuilder;
            this.session = session;
        }

        public CompassQueryStringBuilder setAnalyzer(String analyzer) {
            queryBuilder.setAnalyzer(analyzer);
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

        public CompassQuery toQuery() {
            SearchEngineQuery query = queryBuilder.toQuery();
            return new DefaultCompassQuery(query, session);
        }

    }

    public static class DefaultCompassMultiPropertyQueryStringBuilder implements CompassMultiPropertyQueryStringBuilder {

        private SearchEngineMultiPropertyQueryStringBuilder queryBuilder;

        private InternalCompassSession session;

        public DefaultCompassMultiPropertyQueryStringBuilder(SearchEngineMultiPropertyQueryStringBuilder queryBuilder,
                InternalCompassSession session) {
            this.queryBuilder = queryBuilder;
            this.session = session;
        }

        public CompassMultiPropertyQueryStringBuilder setAnalyzer(String analyzer) {
            queryBuilder.setAnalyzer(analyzer);
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder add(String name) {
            queryBuilder.add(session.getMapping().getResourcePropertyLookup(name).getPath());
            return this;
        }

        public CompassMultiPropertyQueryStringBuilder useAndDefaultOperator() {
            queryBuilder.useAndDefaultOperator();
            return this;
        }

        public CompassQuery toQuery() {
            SearchEngineQuery query = queryBuilder.toQuery();
            return new DefaultCompassQuery(query, session);
        }
    }

    public static class DefaultCompassQuerySpanNearBuilder implements CompassQuerySpanNearBuilder {

        private SearchEngineQuerySpanNearBuilder queryBuilder;

        private InternalCompassSession session;

        private ResourcePropertyLookup lookup;

        public DefaultCompassQuerySpanNearBuilder(SearchEngineQuerySpanNearBuilder queryBuilder,
                InternalCompassSession session, ResourcePropertyLookup lookup) {
            this.queryBuilder = queryBuilder;
            this.session = session;
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
            return new DefaultCompassQuery.DefaultCompassSpanQuey(query, session);
        }
    }

    public static class DefaultCompassQuerySpanOrBuilder implements CompassQuerySpanOrBuilder {

        private SearchEngineQuerySpanOrBuilder queryBuilder;

        private InternalCompassSession session;

        public DefaultCompassQuerySpanOrBuilder(SearchEngineQuerySpanOrBuilder queryBuilder,
                InternalCompassSession session) {
            this.queryBuilder = queryBuilder;
            this.session = session;
        }
        
        public CompassQuerySpanOrBuilder add(CompassSpanQuery query) {
            queryBuilder.add(((DefaultCompassQuery.DefaultCompassSpanQuey) query).getSearchEngineSpanQuery());
            return this;
        }

        public CompassSpanQuery toQuery() {
            SearchEngineSpanQuery query = queryBuilder.toQuery();
            return new DefaultCompassQuery.DefaultCompassSpanQuey(query, session);
        }
    }

    private SearchEngineQueryBuilder queryBuilder;

    private InternalCompassSession session;

    public DefaultCompassQueryBuilder(SearchEngineQueryBuilder queryBuilder, InternalCompassSession session) {
        this.queryBuilder = queryBuilder;
        this.session = session;
    }

    public CompassBooleanQueryBuilder bool() {
        return new DefaultCompassBooleanQueryBuilder(queryBuilder.bool(), session);
    }

    public CompassBooleanQueryBuilder bool(boolean disableCoord) {
        return new DefaultCompassBooleanQueryBuilder(queryBuilder.bool(disableCoord), session);
    }

    public CompassMultiPhraseQueryBuilder multiPhrase(String name) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        return new DefaultCompassMultiPhraseQueryBuilder(queryBuilder.multiPhrase(lookup.getPath()), session, lookup);
    }

    public CompassQueryStringBuilder queryString(String queryString) {
        return new DefaultCompassQueryStringBuilder(queryBuilder.queryString(queryString), session);
    }

    public CompassMultiPropertyQueryStringBuilder multiPropertyQueryString(String queryString) {
        return new DefaultCompassMultiPropertyQueryStringBuilder(queryBuilder.multiPropertyQueryString(queryString), session);
    }

    public CompassQuery alias(String aliasValue) {
        String aliasProperty = session.getCompass().getSearchEngineFactory().getAliasProperty();
        SearchEngineQuery query = queryBuilder.term(aliasProperty, aliasValue);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery term(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.term(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery matchAll() {
        SearchEngineQuery query = queryBuilder.matchAll();
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery between(String name, Object low, Object high, boolean inclusive, boolean constantScore) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.between(lookup.getPath(), lookup.getValue(low), lookup.getValue(high),
                inclusive, constantScore);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery between(String name, Object low, Object high, boolean inclusive) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.between(lookup.getPath(), lookup.getValue(low), lookup.getValue(high),
                inclusive);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery lt(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.lt(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery le(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.le(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery gt(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.gt(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery ge(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.ge(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery prefix(String name, String prefix) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.prefix(lookup.getPath(), prefix);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery wildcard(String name, String wildcard) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.wildcard(lookup.getPath(), wildcard);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery fuzzy(String name, String value, float minimumSimilarity) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value, minimumSimilarity);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery fuzzy(String name, String value, float minimumSimilarity, int prefixLength) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value, minimumSimilarity, prefixLength);
        return new DefaultCompassQuery(query, session);
    }

    public CompassQuery fuzzy(String name, String value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineQuery query = queryBuilder.fuzzy(lookup.getPath(), value);
        return new DefaultCompassQuery(query, session);
    }

    public CompassSpanQuery spanEq(String name, Object value) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineSpanQuery query = queryBuilder.spanEq(lookup.getPath(), lookup.getValue(value));
        return new DefaultCompassQuery.DefaultCompassSpanQuey(query, session);
    }

    public CompassSpanQuery spanFirst(String name, Object value, int end) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        SearchEngineSpanQuery query = queryBuilder.spanFirst(lookup.getPath(), lookup.getValue(value), end);
        return new DefaultCompassQuery.DefaultCompassSpanQuey(query, session);
    }

    public CompassQuerySpanNearBuilder spanNear(String name) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        return new DefaultCompassQuerySpanNearBuilder(queryBuilder.spanNear(lookup.getPath()), session, lookup);
    }

    public CompassSpanQuery spanNot(CompassSpanQuery include, CompassSpanQuery exclude) {
        SearchEngineSpanQuery query = queryBuilder.spanNot(((DefaultCompassQuery.DefaultCompassSpanQuey) include)
                .getSearchEngineSpanQuery(), ((DefaultCompassQuery.DefaultCompassSpanQuey) exclude)
                .getSearchEngineSpanQuery());
        return new DefaultCompassQuery.DefaultCompassSpanQuey(query, session);
    }
    
    public CompassQuerySpanOrBuilder spanOr() {
        return new DefaultCompassQuerySpanOrBuilder(queryBuilder.spanOr(), session);
    }
}
