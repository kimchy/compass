package org.compass.core.converter.dynamic;

import ognl.Ognl;
import ognl.OgnlContext;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class OgnlDynamicConverter extends AbstractDynamicConverter {

    private Object expression;

    public void setExpression(String expression) throws ConversionException {
        try {
            this.expression = Ognl.parseExpression(expression);
        } catch (Exception e) {
            throw new ConversionException("Failed to compile expression [" + expression + "]", e);
        }
    }

    protected Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        OgnlContext ctx = new OgnlContext();
        ctx.put(DATA_CONTEXT_KEY, o);
        try {
            return Ognl.getValue(expression, ctx, o);
        } catch (Exception e) {
            throw new ConversionException("Failed to evaluate [" + o + "] with expression [" + expression + "]", e);
        }
    }
}
