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

import java.util.ArrayList;

import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.lucene.util.ChainedFilter;
import org.compass.core.lucene.engine.LuceneSearchEngineQueryFilter;
import org.apache.lucene.search.Filter;

/**
 * @author kimchy
 */
public class LuceneSearchEngineBooleanQueryFilterBuilder implements SearchEngineQueryFilterBuilder.SearchEngineBooleanQueryFilterBuilder {

    private ArrayList types = new ArrayList();

    private ArrayList filters = new ArrayList();

    public LuceneSearchEngineBooleanQueryFilterBuilder() {

    }

    public void and(SearchEngineQueryFilter filter) {
        types.add(ChainedFilter.ChainedFilterType.AND);
        filters.add(((LuceneSearchEngineQueryFilter) filter).getFilter());
    }

    public void or(SearchEngineQueryFilter filter) {
        types.add(ChainedFilter.ChainedFilterType.OR);
        filters.add(((LuceneSearchEngineQueryFilter) filter).getFilter());
    }

    public void andNot(SearchEngineQueryFilter filter) {
        types.add(ChainedFilter.ChainedFilterType.ANDNOT);
        filters.add(((LuceneSearchEngineQueryFilter) filter).getFilter());
    }

    public void xor(SearchEngineQueryFilter filter) {
        types.add(ChainedFilter.ChainedFilterType.XOR);
        filters.add(((LuceneSearchEngineQueryFilter) filter).getFilter());
    }

    public SearchEngineQueryFilter toFilter() {
        if (filters.size() == 0) {
            throw new IllegalArgumentException("Must add at least one filter");
        }
        Filter[] filtersArr = (Filter[]) filters.toArray(new Filter[filters.size()]);
        ChainedFilter.ChainedFilterType[] typesArr = (ChainedFilter.ChainedFilterType[]) types.toArray(new ChainedFilter.ChainedFilterType[types.size()]);
        return new LuceneSearchEngineQueryFilter(new ChainedFilter(filtersArr, typesArr));
    }
}
