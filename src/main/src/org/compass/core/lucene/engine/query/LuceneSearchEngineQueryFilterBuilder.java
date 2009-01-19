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

import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.RangeFilter;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter;

/**
 * @author kimchy
 */
// TODO add makeCacheable to filters. Will have to hook into index change events to invalidate the filter cache.
public class LuceneSearchEngineQueryFilterBuilder implements SearchEngineQueryFilterBuilder {

    public LuceneSearchEngineQueryFilterBuilder() {
    }

    public SearchEngineQueryFilter between(String resourcePropertyName, String low, String high, boolean includeLow, boolean includeHigh) {
        return new LuceneSearchEngineQueryFilter(new RangeFilter(resourcePropertyName, low, high, includeLow, includeHigh));
    }

    public SearchEngineQueryFilter lt(String resourcePropertyName, String value) {
        return between(resourcePropertyName, null, value, false, false);
    }

    public SearchEngineQueryFilter le(String resourcePropertyName, String value) {
        return between(resourcePropertyName, null, value, false, true);
    }

    public SearchEngineQueryFilter gt(String resourcePropertyName, String value) {
        return between(resourcePropertyName, value, null, false, false);
    }

    public SearchEngineQueryFilter ge(String resourcePropertyName, String value) {
        return between(resourcePropertyName, value, null, true, false);
    }

    public SearchEngineQueryFilter query(SearchEngineQuery query) {
        return new LuceneSearchEngineQueryFilter(new QueryWrapperFilter(((LuceneSearchEngineQuery) query).getQuery()));
    }

    public SearchEngineBooleanQueryFilterBuilder bool() {
        return new LuceneSearchEngineBooleanQueryFilterBuilder();
    }
}
