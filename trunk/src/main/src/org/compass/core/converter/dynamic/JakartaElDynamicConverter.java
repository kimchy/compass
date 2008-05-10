package org.compass.core.converter.dynamic;

import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.Expression;
import javax.servlet.jsp.el.ExpressionEvaluator;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.commons.el.ExpressionEvaluatorImpl;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class JakartaElDynamicConverter extends AbstractDynamicConverter {

    private ExpressionEvaluator expressionEvaluator;

    private Expression expression;

    public DynamicConverter copy() {
        JakartaElDynamicConverter converter = (JakartaElDynamicConverter) super.copy();
        converter.expressionEvaluator = expressionEvaluator;
        return converter;
    }

    public void setExpression(String expression) throws ConversionException {
        if (expressionEvaluator == null) {
            expressionEvaluator = new ExpressionEvaluatorImpl();
        }
        Class type = String.class;
        if (getType() != null) {
            type = getType();
        }
        try {
            this.expression = expressionEvaluator.parseExpression(expression, type, null);
        } catch (ELException e) {
            throw new ConversionException("Failed to compile expression [" + expression + "]", e);
        }
    }

    protected Object evaluate(final Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        try {
            VariableResolver variableResolver = new VariableResolver() {
                public Object resolveVariable(String variable) throws ELException {
                    return o;
                }
            };
            return expression.evaluate(variableResolver);
        } catch (Exception e) {
            throw new ConversionException("Failed to evaluate [" + o + "] with expression [" + expression + "]", e);
        }
    }
}
