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

package org.compass.core.mapping.rsem;

import org.compass.core.mapping.*;

import java.util.Iterator;

/**
 * @author kimchy
 */
public class RawResourceMapping extends AbstractResourceMapping implements PostProcessingMapping {

    private ResourcePropertyMapping[] resourcePropertyMappings;

    public Mapping copy() {
        RawResourceMapping copy = new RawResourceMapping();
        copy(copy);
        return copy;
    }

    public AliasMapping shallowCopy() {
        RawResourceMapping copy = new RawResourceMapping();
        shallowCopy(copy);
        return copy;
    }

    public boolean isIncludePropertiesWithNoMappingsInAll() {
        return true;
    }

    public int addMapping(Mapping mapping) {
        // no duplicate mapping names are allowed
        if (mapping instanceof ResourcePropertyMapping) {
            ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
            if (mappingsMap.get(resourcePropertyMapping.getPath()) != null) {
                if (!(resourcePropertyMapping instanceof OverrideByNameMapping) ||
                        !((OverrideByNameMapping) resourcePropertyMapping).isOverrideByName()) {
                    throw new InvalidMappingException("Two resource property mappings are mapped to property path ["
                            + resourcePropertyMapping.getPath() + "], it is not allowed");
                }
            }
        }
        return super.addMapping(mapping);
    }

    protected void doPostProcess() throws MappingException {
        resourcePropertyMappings = new ResourcePropertyMapping[mappingsSize()];
        int i = 0;
        for (Iterator it = mappingsIt(); it.hasNext();) {
            resourcePropertyMappings[i++] = (ResourcePropertyMapping) it.next();
        }
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return (ResourcePropertyMapping) mappingsMap.get(path);
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return resourcePropertyMappings;
    }
}
