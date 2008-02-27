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

import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilter;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class DefaultCompassQuery implements CompassQuery, Cloneable {

    public static class DefaultCompassSpanQuey extends DefaultCompassQuery implements CompassSpanQuery {

        private SearchEngineSpanQuery spanQuery;

        public DefaultCompassSpanQuey(SearchEngineSpanQuery searchEngineQuery, InternalCompassSession session) {
            super(searchEngineQuery, session);
            this.spanQuery = searchEngineQuery;
        }

        public SearchEngineSpanQuery getSearchEngineSpanQuery() {
            return spanQuery;
        }
    }

    private SearchEngineQuery searchEngineQuery;

    private InternalCompassSession session;

    private CompassQueryFilter filter;

    public DefaultCompassQuery(SearchEngineQuery searchEngineQuery, InternalCompassSession session) {
        this.searchEngineQuery = searchEngineQuery;
        this.session = session;
    }

    public CompassQuery setBoost(float boost) {
        searchEngineQuery.setBoost(boost);
        return this;
    }

    public CompassQuery addSort(String name) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath());
        return this;
    }

    public CompassQuery addSort(String name, SortDirection direction) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath(), direction);
        return this;
    }

    public CompassQuery addSort(String name, SortPropertyType type) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath(), type);
        return this;
    }

    public CompassQuery addSort(String name, SortPropertyType type, SortDirection direction) {
        ResourcePropertyLookup lookup = session.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath(), type, direction);
        return this;
    }

    public CompassQuery addSort(SortImplicitType implicitType) {
        searchEngineQuery.addSort(implicitType);
        return this;
    }

    public CompassQuery addSort(SortImplicitType implicitType, SortDirection direction) {
        searchEngineQuery.addSort(implicitType, direction);
        return this;
    }

    public CompassQuery setSubIndexes(String[] subIndexes) {
        searchEngineQuery.setSubIndexes(subIndexes);
        return this;
    }

    public CompassQuery setAliases(String[] aliases) {
        searchEngineQuery.setAliases(aliases);
        return this;
    }

    public CompassQuery setTypes(Class[] types) {
        if (types == null) {
            setAliases(null);
            return this;
        }
        String[] aliases = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ResourceMapping resourceMapping = session.getMapping().getRootMappingByClass(types[i]);
            aliases[i] = resourceMapping.getAlias();
        }
        setAliases(aliases);
        return this;
    }

    public CompassQuery setFilter(CompassQueryFilter filter) {
        this.filter = filter;
        searchEngineQuery.setFilter(((DefaultCompassQueryFilter) filter).getFilter());
        return this;
    }

    public CompassQueryFilter getFilter() {
        return this.filter;
    }

    public CompassQuery rewrite() {
        searchEngineQuery.rewrite();
        return this;
    }

    public boolean isSuggested() {
        return searchEngineQuery.isSuggested();
    }

    public CompassHits hits() throws CompassException {
        SearchEngineHits searchEngineHits = searchEngineQuery.hits();
        return new DefaultCompassHits(searchEngineHits, session);
    }

    public SearchEngineQuery getSearchEngineQuery() {
        return searchEngineQuery;
    }

    public String toString() {
        return searchEngineQuery.toString();
    }

    public Object clone() throws CloneNotSupportedException {
        DefaultCompassQuery clone = (DefaultCompassQuery) super.clone();
        clone.searchEngineQuery = (SearchEngineQuery) searchEngineQuery.clone();
        return clone;
    }
}
