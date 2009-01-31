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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;

/**
 * @author kimchy
 */
public class RootJsonObjectMapping extends AbstractResourceMapping implements JsonObjectMapping {

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private Map<String, ResourcePropertyMapping> resourcePropertyMappingsByPath = new HashMap<String, ResourcePropertyMapping>();

    private JsonContentMapping contentMapping;

    private String fullPath;

    private boolean dynamic = false;

    private Naming dynamicNaming; // can be null, which means its not set

    public Mapping copy() {
        RootJsonObjectMapping copy = new RootJsonObjectMapping();
        super.copy(copy);
        copy.setFullPath(getFullPath());
        copy.setDynamic(isDynamic());
        copy.setDynamicNaming(getDynamicNaming());
        return copy;
    }

    public AliasMapping shallowCopy() {
        RootJsonObjectMapping copy = new RootJsonObjectMapping();
        super.shallowCopy(copy);
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
//        if (mapping instanceof ResourcePropertyMapping) {
//            ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
//            if (mappingsByNameMap.get(resourcePropertyMapping.getName()) != null) {
//                if (!(resourcePropertyMapping instanceof OverrideByNameMapping) ||
//                        !((OverrideByNameMapping) resourcePropertyMapping).isOverrideByName()) {
//                    throw new InvalidMappingException("Two resource property mappings are mapped to property path ["
//                            + resourcePropertyMapping.getPath().getPath() + "], it is not allowed");
//                }
//            }
//        }
        if (mapping instanceof JsonContentMapping) {
            contentMapping = (JsonContentMapping) mapping;
        }
        return super.addMapping(mapping);
    }

    protected void doPostProcess() throws MappingException {
        ResourcePropertyMappingGatherer resourcePropertyMappingGatherer = new ResourcePropertyMappingGatherer();
        JsonMappingIterator.iterateMappings(resourcePropertyMappingGatherer, this, true);
        resourcePropertyMappings = resourcePropertyMappingGatherer.getResourcePropertyMappings();
        for (ResourcePropertyMapping m : resourcePropertyMappings) {
            if (m.isInternal()) {
                continue;
            }
            if (m instanceof JsonMapping) {
                JsonMapping jsonMapping = (JsonMapping) m;
                resourcePropertyMappingsByPath.put(jsonMapping.getFullPath(), m);
            }
        }
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return resourcePropertyMappings;
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return resourcePropertyMappingsByPath.get(path);
    }

    public JsonContentMapping getContentMapping() {
        return this.contentMapping;
    }

    private class ResourcePropertyMappingGatherer implements JsonMappingIterator.JsonMappingCallback {

        private ArrayList<ResourcePropertyMapping> resourcePropertyMappings = new ArrayList<ResourcePropertyMapping>();

        public ResourcePropertyMapping[] getResourcePropertyMappings() {
            return resourcePropertyMappings.toArray(new ResourcePropertyMapping[resourcePropertyMappings.size()]);
        }

        public void onJsonRootObject(RootJsonObjectMapping jsonObjectMapping) {
        }

        public void onJsonObject(PlainJsonObjectMapping jsonObjectMapping) {
        }

        public void onJsonContent(JsonContentMapping jsonContentMapping) {
        }

        public void onJsonProperty(JsonPropertyMapping jsonPropertyMapping) {
            resourcePropertyMappings.add(jsonPropertyMapping);
        }

        public void onJsonArray(JsonArrayMapping jsonArrayMapping) {
        }

        public boolean onBeginMultipleMapping(JsonMapping mapping) {
            return true;
        }

        public void onEndMultipleMapping(JsonMapping mapping) {
        }
    }
}
