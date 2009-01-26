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

package org.compass.core.lucene.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.compass.core.CompassQuery.SortDirection;
import org.compass.core.CompassQuery.SortImplicitType;
import org.compass.core.CompassQuery.SortPropertyType;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryFilter;
import org.compass.core.lucene.engine.queryparser.QueryHolder;
import org.compass.core.lucene.search.CountHitCollector;

/**
 * @author kimchy
 */
public class LuceneSearchEngineQuery implements SearchEngineQuery, Cloneable {

    public static class LuceneSearchEngineSpanQuery extends LuceneSearchEngineQuery implements SearchEngineSpanQuery {

        private SpanQuery spanQuery;

        public LuceneSearchEngineSpanQuery(LuceneSearchEngineFactory searchEngineFactory, SpanQuery query) {
            super(searchEngineFactory, query);
            this.spanQuery = query;
        }

        public SpanQuery toSpanQuery() {
            return spanQuery;
        }
    }

    private final LuceneSearchEngineFactory searchEngineFactory;

    private ArrayList<SortField> sortFields = new ArrayList<SortField>();

    private String[] subIndexes;

    private String[] aliases;

    private LuceneSearchEngineQueryFilter filter;

    private Query origQuery;

    private Query query;

    private String defaultSearchProperty;

    private boolean rewrite;

    private boolean suggested;

    public LuceneSearchEngineQuery(LuceneSearchEngineFactory searchEngineFactory, Query query) {
        this(searchEngineFactory, new QueryHolder(query));
    }

    public LuceneSearchEngineQuery(LuceneSearchEngineFactory searchEngineFactory, QueryHolder query) {
        this(searchEngineFactory, query, searchEngineFactory.getLuceneSettings().getDefaultSearchPropery());
    }

    public LuceneSearchEngineQuery(LuceneSearchEngineFactory searchEngineFactory, QueryHolder query, String defualtSearchProperty) {
        this.searchEngineFactory = searchEngineFactory;
        this.query = query.getQuery();
        this.origQuery = query.getQuery();
        this.suggested = query.isSuggested();
        this.defaultSearchProperty = defualtSearchProperty;
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

    public SearchEngineQuery addSort(String propertyName, Locale locale, SortDirection direction) {
        sortFields.add(new SortField(propertyName, locale, getSortReverse(direction)));
        return this;
    }

    public SearchEngineQuery addSort(String propertyName, Locale locale) {
        sortFields.add(new SortField(propertyName, locale));
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
        SortField[] sortFieldsArr = sortFields.toArray(new SortField[sortFields.size()]);
        return new Sort(sortFieldsArr);
    }

    private int getImplicitSortField(SortImplicitType implicitType) {
        switch (implicitType) {
            case DOC:
                return SortField.DOC;
            case SCORE:
                return SortField.SCORE;
            default:
                throw new IllegalArgumentException("Faile to create lucene implicit type for [" + implicitType + "]");
        }
    }

    private boolean getSortReverse(SortDirection direction) {
        return direction == SortDirection.REVERSE;
    }

    private int getSortType(SortPropertyType type) {
        switch (type) {
            case AUTO:
                return SortField.AUTO;
            case BYTE:
                return SortField.BYTE;
            case DOUBLE:
                return SortField.DOUBLE;
            case FLOAT:
                return SortField.FLOAT;
            case INT:
                return SortField.INT;
            case LONG:
                return SortField.LONG;
            case STRING:
                return SortField.STRING;
            default:
                throw new IllegalArgumentException("Failed to convert type [" + type + "]");
        }
    }

    public SearchEngineHits hits(SearchEngine searchEngine) {
        return ((LuceneSearchEngine) searchEngine).find(this);
    }

    public long count(SearchEngine searchEngine) {
        return count(searchEngine, 0.0f);
    }

    public long count(SearchEngine searchEngine, float minimumScore) {
        LuceneSearchEngineInternalSearch internalSearch = (LuceneSearchEngineInternalSearch) searchEngine.internalSearch(getSubIndexes(), getAliases());
        CountHitCollector countHitCollector = new CountHitCollector(minimumScore);
        try {
            if (internalSearch.getSearcher() == null) {
                // no index, return 0
                return 0;
            }
            internalSearch.getSearcher().search(getQuery(), getLuceneFilter(), countHitCollector);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to count query [" + query + "]", e);
        }
        return countHitCollector.getTotalHits();
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
        if (aliases == null) {
            query = origQuery;
            return this;
        }

        String aliasProperty = searchEngineFactory.getLuceneSettings().getAliasProperty();
        BooleanQuery boolQuery2 = new BooleanQuery();
        for (String alias : aliases) {
            boolQuery2.add(new TermQuery(new Term(aliasProperty, alias)), BooleanClause.Occur.SHOULD);
        }

        BooleanQuery boolQuery = new BooleanQuery();
        boolQuery.add(origQuery, BooleanClause.Occur.MUST);
        boolQuery.add(boolQuery2, BooleanClause.Occur.MUST);
        this.query = boolQuery;

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

    public Filter getLuceneFilter() {
        if (filter == null) {
            return null;
        }
        return filter.getFilter();
    }

    public SearchEngineQuery rewrite() {
        this.rewrite = true;
        return this;
    }

    public boolean isRewrite() {
        return this.rewrite;
    }

    public boolean isSuggested() {
        return this.suggested;
    }

    public Query getOriginalQuery() {
        return this.origQuery;
    }

    public Query getQuery() {
        return this.query;
    }

    public String toString() {
        if (query == null) {
            return "<null>";
        }
        // remove the "zzz-all:" prefix
        return query.toString().replace(defaultSearchProperty + ":", "");
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    // breaks encapsulation, but we need it

    public void setQuery(Query query) {
        this.query = query;
        this.origQuery = query;
    }

    public void setSuggested(boolean suggested) {
        this.suggested = suggested;
    }
}
