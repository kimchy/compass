package org.compass.core.mapping.support;

import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A helper resource mapping class that marks the resource mapping as null
 * (i.e. no resource mapping for you!).
 *
 * @author kimchy
 */
public class NullResourceMapping extends AbstractResourceMapping {

    protected void doPostProcess() throws MappingException {
    }

    public Mapping copy() {
        return null;
    }

    public AliasMapping shallowCopy() {
        return null;
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return new ResourcePropertyMapping[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return null;
    }
}
