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

import org.compass.core.CompassException;

/**
 * Cascade mappings responsible for getting objects for cascading
 * operations as well as marking which operations are allowed to
 * be cascaded.
 *
 * @author kimchy
 */
public interface CascadeMapping {

    /**
     * Returns the value that should be cascaded basde on the root object.
     *
     * @param root The root object to extract the cascaded value from
     * @return The cascaded value to cascade
     * @throws CompassException
     */
    Object getCascadeValue(Object root) throws CompassException;

    /**
     * Returns the cascades of the mapping.
     */
    Cascade[] getCascades();

    /**
     * Returns <code>true</code> if cascading should be performed for delete
     * operations.
     */
    boolean shouldCascadeDelete();

    /**
     * Returns <code>true</code> if cascading should be performed for create
     * operations.
     */
    boolean shouldCascadeCreate();

    /**
     * Returns <code>true</code> if cascading should be performed for save
     * operations.
     */
    boolean shouldCascadeSave();

    /**
     * Returns <code>true</code> if cascading should be performed for the
     * cascade parameter.
     */
    boolean shouldCascade(Cascade cascade);
}
