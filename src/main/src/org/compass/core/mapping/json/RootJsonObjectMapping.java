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

    public Mapping copy() {
        RootJsonObjectMapping copy = new RootJsonObjectMapping();
        super.copy(copy);
        copy.setFullPath(getFullPath());
        copy.setDynamic(isDynamic());
        return copy;
    }

    public AliasMapping shallowCopy() {
        RootJsonObjectMapping copy = new RootJsonObjectMapping();
        super.shallowCopy(copy);
        copy.setFullPath(getFullPath());
        copy.setDynamic(isDynamic());
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
