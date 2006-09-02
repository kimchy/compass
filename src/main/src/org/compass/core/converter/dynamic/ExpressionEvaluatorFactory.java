package org.compass.core.converter.dynamic;

import org.compass.core.converter.ConversionException;

/**
 * A factory for {@link ExpressionEvaluator}.
 *
 * @author kimchy
 */
public interface ExpressionEvaluatorFactory {

    /**
     * Creates a new {@link ExpressionEvaluator}.
     *
     * @return A new expression evaluator
     * @throws ConversionException
     */
    ExpressionEvaluator create() throws ConversionException;
}
