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

package org.compass.annotations;

/**
 * Controls if the {@link org.compass.annotations.Searchable} class should filter duplciates. Duplciates
 * are objects that have already been marshalled during the marshalling process of a single root object
 * (and its object graph). Filtering them out means reducing the size of the index (content, of course,
 * is still searchable), though object graph queries and possible "boost" information by having it several
 * times is lost.
 *
 * <p>By default, controlled by global setting {@link org.compass.core.config.CompassEnvironment.Osem#FILTER_DUPLICATES}
 * which defaults to <code>false</code>.
 *
 * @author kimchy
 */
public enum FilterDuplicates {
    /**
     * Defaults to Compass global osem setting {@link org.compass.core.config.CompassEnvironment.Osem#FILTER_DUPLICATES}.
     */
    NA,
    /**
     * The searchable class will filter duplciates. Will override
     * anything set in Compass global osem setting filterDuplicates.
     */
    TRUE,
    /**
     * The searchable class will not filter duplicates. Will override
     * anything set in Compass global osem setting filterDuplicates.
     */
    FALSE
}