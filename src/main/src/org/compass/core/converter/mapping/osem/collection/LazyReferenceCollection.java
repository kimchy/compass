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

package org.compass.core.converter.mapping.osem.collection;

import java.util.Collection;

/**
 * A collection that is lazy loaded from the search engine.
 *
 * @author kimchy
 */
public interface LazyReferenceCollection extends Collection {

    /**
     * Loads all the referneced objects into memory from the search engine.
     */
    void loadFully();

    /**
     * Returns <code>true</code> if the collection is fully loaded from the index.
     */
    boolean isFullyLoaded();

    /**
     * Internally used to initialize the lazy collection.
     */
    void addLazyEntry(LazyReferenceEntry entry);
}
