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

package org.compass.core.engine.subindex;

import org.compass.core.Property;
import org.compass.core.engine.SearchEngineException;

/**
 * Reprensetns a set of sub indexes, and knows how to map a single sub index
 * value from an alias and a set of {@link Property} ids.
 *
 * <p>This allows to have different mappings from {@link org.compass.core.Resource}
 * to a specific sub index.
 *
 * @author kimchy
 */
public interface SubIndexHash {

    /**
     * Returns all the sub indexes that {@link #mapSubIndex(String,org.compass.core.Property[])}
     * might be generating.
     */
    String[] getSubIndexes();

    /**
     * Computes a sub index based on the given alias and ids.
     *
     * @param alias The alias to compute the sub index by (optional)
     * @param ids   The set of ids to compute the sub index by (optional)
     * @return The hashed sub index
     * @throws SearchEngineException
     */
    String mapSubIndex(String alias, Property[] ids) throws SearchEngineException;
}
