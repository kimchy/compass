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

package org.compass.core.lucene.engine;

import java.util.ArrayList;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.spans.SpanQuery;
import org.compass.core.CompassQuery.SortDirection;
import org.compass.core.CompassQuery.SortImplicitType;
import org.compass.core.CompassQuery.SortPropertyType;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;

/**
 * 
 * @author kimchy
 * 
 */
public class LuceneSearchEngineQuery implements SearchEngineQuery {

    public static class LuceneSearchEngineSpanQuery extends LuceneSearchEngineQuery implements SearchEngineSpanQuery {

        private SpanQuery spanQuery;

        public LuceneSearchEngineSpanQuery(LuceneSearchEngine searchEngine, SpanQuery query) {
            super(searchEngine, query);
            this.spanQuery = query;
        }

        public SpanQuery toSpanQuery() {
            return spanQuery;
        }
    }

    private LuceneSearchEngine searchEngine;

    private ArrayList sortFields = new ArrayList();

    private String[] subIndexes;

    private String[] aliases;

    private LuceneSearchEngineQueryFilter filter;

    private Query query;

    public LuceneSearchEngineQuery(LuceneSearchEngine searchEngine, Query query) {
        this.searchEngine = searchEngine;
        this.query = query;
    }

    public SearchEngineQuery addSort(String propertyName) {
        sortFields.add(new SortField(propertyName));
        return this;
    }

    public SearchEngineQuery addSort(String propertyName, SortDirection direction) {
        sortFields.add(new SortField(propertyName, getSortReverse(direction)));
        return this;
    }

    public SearchEngineQuery addSort(String propertyName, SortPropertyType type) {
        sortFields.add(new SortField(propertyName, getSortType(type)));
        return this;
    }

    public SearchEngineQuery addSort(String propertyName, SortPropertyType type, SortDirection direction) {
        sortFields.add(new SortField(propertyName, getSortType(type), getSortReverse(direction)));
        return this;
    }

    public SearchEngineQuery addSort(SortImplicitType implicitType) {
        sortFields.add(new SortField(null, getImplicitSortField(implicitType)));
        return this;
    }

    public SearchEngineQuery addSort(SortImplicitType implicitType, SortDirection direction) {
        sortFields.add(new SortField(null, getImplicitSortField(implicitType), getSortReverse(direction)));
        return this;
    }

    public SearchEngineQuery addSort(SortField sortField) {
        sortFields.add(sortField);
        return this;
    }

    public Sort getSort() {
        if (sortFields.size() == 0) {
            return null;
        }
        SortField[] sortFieldsArr = (SortField[]) sortFields.toArray(new SortField[sortFields.size()]);
        return new Sort(sortFieldsArr);
    }

    private int getImplicitSortField(SortImplicitType implicitType) {
        if (implicitType == SortImplicitType.DOC) {
            return SortField.DOC;
        }
        if (implicitType == SortImplicitType.SCORE) {
            return SortField.SCORE;
        }
        throw new IllegalArgumentException("Faile to create lucene implicit type for [" + implicitType + "]");
    }

    private boolean getSortReverse(SortDirection direction) {
        return direction == SortDirection.REVERSE;
    }

    private int getSortType(SortPropertyType type) {
        if (type == SortPropertyType.AUTO) {
            return SortField.AUTO;
        }
        if (type == SortPropertyType.FLOAT) {
            return SortField.FLOAT;
        }
        if (type == SortPropertyType.INT) {
            return SortField.INT;
        }
        if (type == SortPropertyType.STRING) {
            return SortField.STRING;
        }
        throw new IllegalArgumentException("Faile to create lucene sort property type for [" + type + "]");
    }

    public SearchEngineHits hits() {
        return this.searchEngine.find(this);
    }

    public SearchEngineQuery setBoost(float boost) {
        query.setBoost(boost);
        return this;
    }

    public SearchEngineQuery setSubIndexes(String[] subindexes) {
        this.subIndexes = subindexes;
        return this;
    }

    public String[] getSubIndexes() {
        return this.subIndexes;
    }

    public SearchEngineQuery setAliases(String[] aliases) {
        this.aliases = aliases;
        return this;
    }

    public String[] getAliases() {
        return this.aliases;
    }

    public SearchEngineQuery setFilter(SearchEngineQueryFilter filter) {
        this.filter = (LuceneSearchEngineQueryFilter) filter;
        return this;
    }

    public LuceneSearchEngineQueryFilter getFilter() {
        return this.filter;
    }

    public Query getQuery() {
        return query;
    }

    public String toString() {
        if (query == null) {
            return "<null>";
        }
        return query.toString();
    }
}
