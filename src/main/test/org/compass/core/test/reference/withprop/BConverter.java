package org.compass.core.test.reference.withprop;

import org.compass.core.converter.ConversionException;
import org.compass.core.converter.basic.AbstractBasicConverter;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class BConverter extends AbstractBasicConverter<B> {

    protected String doToString(B o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return o.value;
    }

    protected B doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        B b = new B();
        b.value = "1";
        return b;
    }
}
