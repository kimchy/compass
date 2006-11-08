package org.compass.core.test.managedidconverter;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class SimpleConverter extends AbstractBasicConverter {


    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return "X" + o.toString();
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return str.substring(1, str.length());
    }
}
