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

package org.compass.core.lucene.util;

import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.compass.core.Compass;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilter;
import org.compass.core.CompassSession;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.impl.DefaultCompassHits;
import org.compass.core.impl.DefaultCompassQuery;
import org.compass.core.impl.DefaultCompassQueryFilter;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;

/**
 * Allows to create Compass related objects based on external (internally no supported by Compass)
 * Lucene objects.
 *
 * @author kimchy
 */
public abstract class LuceneHelper {

    /**
     * Creates a new {@link CompassQuery} based on a Lucene {@link Query}.
     * <p/>
     * Allows to create {@link CompassQuery} based on external Lucene {@link Query} that is not supported
     * by one of Compass query builders.
     *
     * @param session Comapss session
     * @param query   The lucene query to wrap
     * @return A compass query wrapping the lucene query
     */
    public static CompassQuery createCompassQuery(CompassSession session, Query query) {
        InternalCompassSession internalCompassSession = (InternalCompassSession) session;
        SearchEngineQuery searchEngineQuery =
                new LuceneSearchEngineQuery((LuceneSearchEngine) internalCompassSession.getSearchEngine(), query);
        return new DefaultCompassQuery(searchEngineQuery, internalCompassSession);
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineQuery} of the given {@link CompassQuery}.
     * <p/>
     * Can be used for example to add custom Sorting using
     * {@link LuceneSearchEngineQuery#addSort(org.apache.lucene.search.SortField)}, or get the actual lucene query
     * using {@link org.compass.core.lucene.engine.LuceneSearchEngineQuery#getQuery()}.
     *
     * @param query The compass query to extract the lucene search engine query from
     * @return The lucene search engine query extracted from the compass query
     */
    public static LuceneSearchEngineQuery getLuceneSearchEngineQuery(CompassQuery query) {
        return (LuceneSearchEngineQuery) ((DefaultCompassQuery) query).getSearchEngineQuery();
    }

    /**
     * Creates a new {@link CompassQueryFilter} based on a Lucene {@link Filter}.
     * <p/>
     * Allows to create {@link CompassQueryFilter} based on external Lucene {@link Filter} that is not supported
     * by one fo Comapss query filter builders.
     *
     * @param session Comapss session
     * @param filter  The lucene filter to wrap
     * @return A compass query filter wrapping lucene query.
     */
    public static CompassQueryFilter createCompassQueryFilter(CompassSession session, Filter filter) {
        SearchEngineQueryFilter searchEngineQueryFilter = new LuceneSearchEngineQueryFilter(filter);
        return new DefaultCompassQueryFilter(searchEngineQueryFilter);
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineQueryFilter} of the given {@link CompassQueryFilter}.
     * <p/>
     * Can be used to get the actual Lucene {@link Filter} using
     * {@link org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter#getFilter()}.
     *
     * @param filter The compass query filter to extract the lucene search engine query filter from
     * @return The lucene search engine query filter extracted from the compass query filter
     */
    public static LuceneSearchEngineQueryFilter getLuceneSearchEngineQueryFilter(CompassQueryFilter filter) {
        return (LuceneSearchEngineQueryFilter) ((DefaultCompassQueryFilter) filter).getFilter();
    }

    /**
     * Returns the underlying {@link LuceneSearchEngineHits} of the given {@link CompassHits}.
     *
     * Used mainly to access the actual Lucene {@link org.apache.lucene.search.Hits}, or get
     * Lucene {@link org.apache.lucene.search.Explanation}.
     */
    public static LuceneSearchEngineHits getLuceneSearchEngineHits(CompassHits hits) {
        return (LuceneSearchEngineHits) ((DefaultCompassHits) hits).getSearchEngineHits();
    }
    
    /**
     * Returns Compass own internal <code>LuceneAnalyzerManager</code>. Can be used
     * to access Lucene {@link org.apache.lucene.analysis.Analyzer} at runtime.
     */
    public static LuceneAnalyzerManager getLuceneAnalyzerManager(Compass compass) {
        return ((LuceneSearchEngineFactory)((InternalCompass) compass).getSearchEngineFactory()).getAnalyzerManager();
    }
}
