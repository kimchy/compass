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

package org.compass.core.mapping.json;

import org.compass.core.mapping.InvalidMappingException;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.OverrideByNameMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractMultipleMapping;

/**
 * @author kimchy
 */
public class PlainJsonObjectMapping extends AbstractMultipleMapping implements JsonObjectMapping {

    private String fullPath;

    private boolean dynamic;

    private Naming dynamicNaming; // can be null, which means its not set

    public Mapping copy() {
        PlainJsonObjectMapping copy = new PlainJsonObjectMapping();
        super.copy(copy);
        copy.setFullPath(getFullPath());
        copy.setDynamic(isDynamic());
        copy.setDynamicNaming(getDynamicNaming());
        return copy;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Naming getDynamicNaming() {
        return dynamicNaming;
    }

    public void setDynamicNaming(Naming dynamicNaming) {
        this.dynamicNaming = dynamicNaming;
    }

    public int addMapping(Mapping mapping) {
        // no duplicate mapping names are allowed
        if (mapping instanceof ResourcePropertyMapping) {
            ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
            if (mappingsByNameMap.get(resourcePropertyMapping.getName()) != null) {
                if (!(resourcePropertyMapping instanceof OverrideByNameMapping) ||
                        !((OverrideByNameMapping) resourcePropertyMapping).isOverrideByName()) {
                    throw new InvalidMappingException("Two resource property mappings are mapped to property path ["
                            + resourcePropertyMapping.getPath().getPath() + "], it is not allowed");
                }
            }
        }
        return super.addMapping(mapping);
    }
}
