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
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryFilter;
import org.compass.core.CompassSession;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.impl.DefaultCompassQuery;
import org.compass.core.impl.DefaultCompassQueryFilter;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter;

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
}
