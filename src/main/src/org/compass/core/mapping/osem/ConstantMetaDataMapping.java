package org.compass.core.mapping.osem;

import java.util.ArrayList;
import java.util.Iterator;

import org.compass.core.mapping.AbstractResourcePropertyMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.OverrideByNameMapping;

/**
 * 
 * @author kimchy
 * 
 */

public class ConstantMetaDataMapping extends AbstractResourcePropertyMapping implements OverrideByNameMapping {

    private ArrayList metaDataValues = new ArrayList();
    
    private boolean overrideByName;

    public Mapping copy() {
        ConstantMetaDataMapping copy = new ConstantMetaDataMapping();
        super.copy(copy);
        copy.setOverrideByName(isOverrideByName());
        for (Iterator it = metaDataValuesIt(); it.hasNext();) {
            copy.addMetaDataValue((String) it.next());
        }
        return copy;
    }

    public void addMetaDataValue(String value) {
        metaDataValues.add(value);
    }

    public Iterator metaDataValuesIt() {
        return metaDataValues.iterator();
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }
}
