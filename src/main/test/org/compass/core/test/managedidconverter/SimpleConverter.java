package org.compass.core.test.managedidconverter;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class SimpleConverter extends AbstractBasicConverter<String> {

    @Override
    public String toString(String o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return "X" + o;
    }

    protected String doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        return str.substring(1, str.length());
    }
}
