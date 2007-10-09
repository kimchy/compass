package org.compass.core.test.managedidconverter;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class SimpleConverter extends AbstractBasicConverter {

    protected String doToString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return "X" + o.toString();
    }

    protected Object doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        return str.substring(1, str.length());
    }
}
