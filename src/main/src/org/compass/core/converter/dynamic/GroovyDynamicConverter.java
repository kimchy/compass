package org.compass.core.converter.dynamic;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * @author kimchy
 */
public class GroovyDynamicConverter extends AbstractDynamicConverter {

    private String expression;

    private ThreadSafeExpressionEvaluator expressionEvaluator;

    public void setExpression(final String expression) throws ConversionException {
        this.expression = expression;
        this.expressionEvaluator = new ThreadSafeExpressionEvaluator(10, 20,
                new ExpressionEvaluatorFactory() {
                    public ExpressionEvaluator create() throws ConversionException {
                        return new GroovyExpressionEvaluator();
                    }
                });
    }


    protected Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return expressionEvaluator.evaluate(o, resourcePropertyMapping);
    }

    public class GroovyExpressionEvaluator implements ExpressionEvaluator {

        private Script script;

        public GroovyExpressionEvaluator() {
            this.script = new GroovyShell().parse(expression);
        }

        public Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
            Binding binding = new Binding();
            binding.setVariable(DATA_CONTEXT_KEY, o);
            // thread safe so we are ok
            script.setBinding(binding);
            try {
                return script.run();
            } catch (Exception e) {
                throw new ConversionException("Failed to evaluate [" + o + "] with expression [" + expression + "]", e);
            }
        }
    }
}
