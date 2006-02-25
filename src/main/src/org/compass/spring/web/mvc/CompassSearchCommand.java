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

package org.compass.spring.web.mvc;

/**
 * The Spring's MVC command object which holds the query that needs to be
 * executed in the search operation. It might hold the page parameter as well if
 * using the pagination feature. The command is used by the
 * {@link org.compass.spring.web.mvc.CompassSearchController}.
 * 
 * @author kimchy
 */
public class CompassSearchCommand {

    private String query;

    private Integer page;

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
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Returns the page paramter if using the {@link CompassSearchController}
     * pagination feature.
     * 
     * @return The current page number
     */
    public Integer getPage() {
        return page;
    }

    /**
     * Sets the page paramter if using the {@link CompassSearchController}
     * pagination feature.
     * 
     * @param page
     */
    public void setPage(Integer page) {
        this.page = page;
    }
}
