package org.compass.core.converter.dynamic;

import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A general abstraction on top of an expression evaluator, mainly used with
 * {@link ThreadSafeExpressionEvaluator}
 *
 * @author kimchy
 */
public interface ExpressionEvaluator {

    /**
     * Evaluates an expression.
     *
     * @param o                       The data object to be used in the expression
     * @param resourcePropertyMapping The resource mapping
     * @return The evaluated object
     * @throws ConversionException
     */
    Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException;
}
