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

package org.compass.core.mapping;

import java.util.Iterator;

/**
 * A mapping type that can hold interlly multiple mappings (associated with a name).
 *
 * @author kimchy
 */
public interface MultipleMapping extends Mapping {

    /**
     * Returns the mapping associated with the given name. Don't be confused with
     * {@link #getName()}, since the mappings might be saved not under the mapping
     * name (it might be under the mapping path). It really depends on the actual
     * implementation.
     *
     * @param name The name tha mapping was registered under when it was added.
     * @return The mapping found, or <code>null</code> if nothing was found.
     */
    Mapping getMapping(String name);

    /**
     * Returns the mapping at the given index.
     */
    Mapping getMapping(int index);

    /**
     * Returns an iterator over the stored mappings.
     */
    Iterator<Mapping> mappingsIt();
}
