package org.compass.core.mapping.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.InvalidMappingException;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.OverrideByNameMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourceMapping;

/**
 * @author kimchy
 */
//TODO support recursive mappings using object (or array)
//TODO support appending names based on deep level names
public class JsonRootObjectMapping extends AbstractResourceMapping {

    private ResourcePropertyMapping[] resourcePropertyMappings;

    private Map<String, ResourcePropertyMapping> resourcePropertyMappingsByPath = new HashMap<String, ResourcePropertyMapping>();

    private JsonContentMapping contentMapping;

    public Mapping copy() {
        JsonRootObjectMapping copy = new JsonRootObjectMapping();
        super.copy(copy);
        return copy;
    }

    public AliasMapping shallowCopy() {
        JsonRootObjectMapping copy = new JsonRootObjectMapping();
        super.shallowCopy(copy);
        return copy;
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
        if (mapping instanceof JsonContentMapping) {
            contentMapping = (JsonContentMapping) mapping;
        }
        return super.addMapping(mapping);
    }

    protected void doPostProcess() throws MappingException {
        ArrayList<ResourcePropertyMapping> list = new ArrayList<ResourcePropertyMapping>();
        for (Iterator<Mapping> it = mappingsIt(); it.hasNext();) {
            Mapping m = it.next();
            // TODO recurse over to JsonObjectMapping and JsonArrayMapping
            if (m instanceof ResourcePropertyMapping) {
                list.add((ResourcePropertyMapping) m);
                resourcePropertyMappingsByPath.put(m.getPath().getPath(), (ResourcePropertyMapping) m);
            }
        }
        resourcePropertyMappings = list.toArray(new ResourcePropertyMapping[list.size()]);
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
}
