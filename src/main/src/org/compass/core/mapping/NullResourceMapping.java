package org.compass.core.mapping;

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

    public boolean isIncludePropertiesWithNoMappingsInAll() {
        return false;
    }

    public ResourcePropertyMapping[] getResourcePropertyMappings() {
        return new ResourcePropertyMapping[0];
    }

    public ResourcePropertyMapping getResourcePropertyMappingByDotPath(String path) {
        return null;
    }
}
