package org.compass.core.mapping.xsem;

import org.compass.core.Property;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceIdMappingProvider;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class XmlIdMapping extends XmlPropertyMapping implements ResourceIdMappingProvider {

    public Mapping[] getIdMappings() {
        return getResourceIdMappings();
    }

    public ResourcePropertyMapping[] getResourceIdMappings() {
        return new ResourcePropertyMapping[]{this};
    }

    public Property.Index getIndex() {
        return Property.Index.UN_TOKENIZED;
    }

    public Property.Store getStore() {
        return Property.Store.YES;
    }

    public boolean isOverrideByName() {
        return true;
    }

    public Mapping copy() {
        XmlIdMapping copy = new XmlIdMapping();
        copy(copy);
        return copy;
    }

}
