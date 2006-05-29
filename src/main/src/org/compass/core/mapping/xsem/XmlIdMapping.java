package org.compass.core.mapping.xsem;

import org.compass.core.Property;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.ResourceIdMappingProvider;

/**
 * @author kimchy
 */
public class XmlIdMapping extends XmlPropertyMapping implements ResourceIdMappingProvider {

    public ResourcePropertyMapping[] getIdMappings() {
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
