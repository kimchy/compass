package org.compass.core.test.reference.withprop;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class BConverter extends AbstractBasicConverter {

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return ((B) o).value;
    }

    public Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        B b = new B();
        b.value = "1";
        return b;
    }
}
