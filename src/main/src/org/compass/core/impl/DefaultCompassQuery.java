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

import java.util.Locale;

import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilter;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQuery.SearchEngineSpanQuery;
import org.compass.core.events.FilterOperation;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyLookup;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassQuery;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalCompassSpanQuery;

/**
 * @author kimchy
 */
public class DefaultCompassQuery implements InternalCompassQuery, Cloneable {

    public static class DefaultCompassSpanQuey extends DefaultCompassQuery implements InternalCompassSpanQuery {

        private SearchEngineSpanQuery spanQuery;

        public DefaultCompassSpanQuey(SearchEngineSpanQuery searchEngineQuery, InternalCompass compass) {
            super(searchEngineQuery, compass);
            this.spanQuery = searchEngineQuery;
        }

        public SearchEngineSpanQuery getSearchEngineSpanQuery() {
            return spanQuery;
        }
    }

    private static final ThreadLocal<InternalCompassSession> attachedSession = new ThreadLocal<InternalCompassSession>();

    private SearchEngineQuery searchEngineQuery;

    private InternalCompass compass;

    private CompassQueryFilter filter;

    public DefaultCompassQuery(SearchEngineQuery searchEngineQuery, InternalCompass compass) {
        this.searchEngineQuery = searchEngineQuery;
        this.compass = compass;
    }

    public CompassQuery attach(CompassSession session) {
        InternalCompassSession compassSession = (InternalCompassSession) session;
        attachedSession.set(compassSession);
        compassSession.addDelegateClose(this);
        return this;
    }

    public CompassQuery attach(CompassSearchSession session) {
        attachedSession.set((InternalCompassSession) session);
        return this;
    }

    public void detach() {
        attachedSession.remove();
    }

    public void close() {
        detach();
    }

    public CompassQuery setBoost(float boost) {
        searchEngineQuery.setBoost(boost);
        return this;
    }

    public CompassQuery addSort(String name) {
        ResourcePropertyLookup lookup = compass.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath());
        return this;
    }

    public CompassQuery addSort(String name, SortDirection direction) {
        ResourcePropertyLookup lookup = compass.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath(), direction);
        return this;
    }

    public CompassQuery addSort(String name, SortPropertyType type) {
        ResourcePropertyLookup lookup = compass.getMapping().getResourcePropertyLookup(name);
        searchEngineQuery.addSort(lookup.getPath(), type);
        return this;
    }

    public CompassQuery addSort(String name, SortPropertyType type, SortDirection direction) {
        ResourcePropertyLookup lookup = compass.getMapping().getResourcePropertyLookup(name);
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

    public CompassQuery addSort(String propertyName, Locale locale, SortDirection direction) {
        searchEngineQuery.addSort(propertyName, locale, direction);
        return this;
    }

    public CompassQuery addSort(String propertyName, Locale locale) {
        searchEngineQuery.addSort(propertyName, locale);
        return this;
    }

    public CompassQuery setSubIndexes(String... subIndexes) {
        searchEngineQuery.setSubIndexes(subIndexes);
        return this;
    }

    public CompassQuery setAliases(String... aliases) {
        searchEngineQuery.setAliases(aliases);
        return this;
    }

    public CompassQuery setTypes(Class... types) {
        if (types == null) {
            searchEngineQuery.setAliases(null);
            return this;
        }
        String[] aliases = new String[types.length];
        for (int i = 0; i < types.length; i++) {
            ResourceMapping resourceMapping = compass.getMapping().getRootMappingByClass(types[i]);
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

    public CompassQuery getSuggestedQuery() {
        if (compass.getSpellCheckManager() == null) {
            return this;
        }
        return compass.getSpellCheckManager().suggest(this);
    }

    public boolean isSuggested() {
        return searchEngineQuery.isSuggested();
    }

    public long count() {
        return searchEngineQuery.count(session().getSearchEngine());
    }

    public long count(float minimumScore) {
        return searchEngineQuery.count(session().getSearchEngine(), minimumScore);
    }

    public CompassHits hits() throws CompassException {
        InternalCompassSession session = session();
        SearchEngineHits searchEngineHits = searchEngineQuery.hits(session.getSearchEngine());
        return new DefaultCompassHits(searchEngineHits, session, this);
    }

    public void delete() throws CompassException {
        InternalCompassSession session = session();
        session.getFirstLevelCache().evictAll();
        if (session.getCompass().getEventManager().onPreDelete(this) == FilterOperation.YES) {
            return;
        }
        session.getSearchEngine().delete(searchEngineQuery);
        session.getCompass().getEventManager().onPostDelete(this);
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

    public InternalCompassSession getSession() {
        return session();
    }

    private InternalCompassSession session() throws CompassException {
        InternalCompassSession session = attachedSession.get();
        if (session == null) {
            // if we don't find a session, try and find if there is a session bounded to a transaction
            // if there is, use it
            session = (InternalCompassSession) compass.getTransactionFactory().getTransactionBoundSession();
            if (session == null) {
                session = (InternalCompassSession) compass.getLocalTransactionFactory().getTransactionBoundSession();
            }
            if (session != null) {
                attach(session);
            }
        }
        if (session == null) {
            throw new CompassException("Trying to execute a query without an attached session, have you called attach on the query?");
        }
        session.startTransactionIfNeeded();
        return session;
    }
}
