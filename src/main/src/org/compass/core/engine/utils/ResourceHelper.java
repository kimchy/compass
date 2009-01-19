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

package org.compass.core.engine.utils;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;

/**
 * @author kimchy
 */
public abstract class ResourceHelper {

    private ResourceHelper() {
    }

    public static Property[] toIds(Resource resource, CompassMapping mapping)
            throws SearchEngineException {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(resource.getAlias());
        if (resourceMapping == null) {
            throw new SearchEngineException("Failed to find mappings for alias [" + resource.getAlias() + "]");
        }
        return toIds(resource, resourceMapping);
    }

    /**
     * Same as {@link #toIds(org.compass.core.Resource, org.compass.core.mapping.ResourceMapping, boolean)}
     * with idsMustExist set the <code>true</code>.
     */
    public static Property[] toIds(Resource resource, ResourceMapping resourceMapping)
            throws SearchEngineException {
        return toIds(resource, resourceMapping, true);
    }

    /**
     * Gets the id properties based on the resource mapping from the give resource. If
     * must the flag idsMustExists is set, will throw an exception if id value not found,
     * otherise will return null.
     */
    public static Property[] toIds(Resource resource, ResourceMapping resourceMapping, boolean idsMustExist)
            throws SearchEngineException {
        Mapping[] pMappings = resourceMapping.getResourceIdMappings();
        Property[] ids = new Property[pMappings.length];
        for (int i = 0; i < pMappings.length; i++) {
            ids[i] = resource.getProperty(pMappings[i].getPath().getPath());
            if (ids[i] == null) {
                if (!idsMustExist) {
                    return null;
                }
                throw new SearchEngineException("Id with path [" + pMappings[i].getPath().getPath() + "] for alias ["
                        + resource.getAlias() + "] not found");
            }
            if (!ids[i].isIndexed() || ids[i].isTokenized() || !ids[i].isStored()) {
                throw new SearchEngineException("Id [" + ids[i].getName() + "] for alias [" + resource.getAlias()
                        + "] must be stored and un_tokenized");
            }
        }
        return ids;
    }
}
