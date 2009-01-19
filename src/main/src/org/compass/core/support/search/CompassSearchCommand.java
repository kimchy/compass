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

package org.compass.core.support.search;

import org.compass.core.CompassQuery;

/**
 * <p>The search command object which holds the query that needs to be
 * executed in the search operation. It might hold the page parameter as well if
 * using the pagination feature.
 *
 * <p>The seach helper can accept either a query string or a {@link org.compass.core.CompassQuery}.
 * If both are set, the CompassQuery will be used.
 *
 * @author kimchy
 * @see org.compass.core.support.search.CompassSearchHelper
 * @see org.compass.core.support.search.CompassSearchResults
 */
public class CompassSearchCommand {

    private String query;

    private CompassQuery compassQuery;

    private Integer page;

    /**
     * Constructs a new search commad. At least the search query must be
     * set using {@link #setQuery(String)}.
     */
    public CompassSearchCommand() {
    }

    /**
     * Constructs a new search command with the give search query.
     *
     * @param query The search query
     */
    public CompassSearchCommand(String query) {
        this(query, null);
    }

    /**
     * Constructs a new search command with the given query and the page number
     * (in case {@link org.compass.core.support.search.CompassSearchHelper} is
     * used with pagination.
     *
     * @param query The search query
     * @param page  The page number
     */
    public CompassSearchCommand(String query, Integer page) {
        this.query = query;
        this.page = page;
    }

    /**
     * Constructs a new search command with the give search query.
     *
     * @param query The search query
     */
    public CompassSearchCommand(CompassQuery query) {
        this(query, null);
    }

    /**
     * Constructs a new search command with the given query and the page number
     * (in case {@link org.compass.core.support.search.CompassSearchHelper} is
     * used with pagination.
     *
     * @param query The search query
     * @param page  The page number
     */
    public CompassSearchCommand(CompassQuery query, Integer page) {
        this.compassQuery = query;
        this.page = page;
    }

    /**
     * Returns the query that will be executed by
     * <code>Compass<code> in the search operation.
     *
     * @return The query for the search operation
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query that will be executed by
     * <code>Compass<code> in the search operation.
     *
     * @param query The search query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Returns a compass query.
     *
     * @return The compass query
     */
    public CompassQuery getCompassQuery() {
        return this.compassQuery;
    }

    /**
     * Returns the page paramter if using the {@link org.compass.core.support.search.CompassSearchHelper}
     * pagination feature.
     *
     * @return The current page number
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Sets the page paramter if using the {@link org.compass.core.support.search.CompassSearchHelper}
     * pagination feature.
     *
     * @param page The page number
     */
    public void setPage(Integer page) {
        this.page = page;
    }
}
