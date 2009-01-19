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

package org.compass.needle.gigaspaces.service;

/**
 * A generics search service that is based on OpenSpaces remoting.
 *
 * @author kimchy
 */
public interface CompassSearchService {

    /**
     * Search and returns <b>all</b> the matching resources for the given query.
     */
    SearchResourceResults searchResource(String query);

    /**
     * Search and returns up to <b>maxResults</b> the matching resources for the given query.
     */
    SearchResourceResults searchResource(String query, int maxResults);

    /**
     * Search and returns up to <b>maxResults</b> the matching resources for the given query. Hits
     * that are higher than the provided <b>fromScore</b> are filtered out. This allows for simpler
     * pagination.
     */
    SearchResourceResults searchResource(String query, int maxResults, float fromScore);

    /**
     * Search and returns <b>all</b> the matching objects for the given query.
     */
    SearchResults search(String query);

    /**
     * Search and returns <b>maxResults</b> the matching objects for the given query.
     */
    SearchResults search(String query, int maxResults);

    /**
     * Search and returns up to <b>maxResults</b> the matching objects for the given query. Hits
     * that are higher than the provided <b>fromScore</b> are filtered out. This allows for simpler
     * pagination.
     */
    SearchResults search(String query, int maxResults, float fromScore);
}
