package org.compass.core.converter.dynamic;

import org.compass.core.converter.ConversionException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A pool of {@link ExpressionEvaluator}s where the dynamic expression library
 * does not provide a high performance thread save evaluation.
 *
 * @author kimchy
 */
public class ThreadSafeExpressionEvaluator implements ExpressionEvaluator {

    private final int initialPoolSize;

    private final int maxPoolSize;

    private transient ExpressionEvaluator[] pool;

    private int nextAvailable = 0;

    private final Object mutex = new Object();

    private final ExpressionEvaluatorFactory expressionEvaluatorFactory;

    public ThreadSafeExpressionEvaluator(int initialPoolSize, int maxPoolSize,
                                         ExpressionEvaluatorFactory expressionEvaluatorFactory) {
        this.initialPoolSize = initialPoolSize;
        this.maxPoolSize = maxPoolSize;
        this.expressionEvaluatorFactory = expressionEvaluatorFactory;
    }


    public Object evaluate(Object o, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        ExpressionEvaluator expressionEvaluator = fetchFromPool();
        try {
            return expressionEvaluator.evaluate(o, resourcePropertyMapping);
        } finally {
            putInPool(expressionEvaluator);
        }
    }

    private ExpressionEvaluator fetchFromPool() {
        ExpressionEvaluator result;
        synchronized (mutex) {
            if (pool == null) {
                nextAvailable = -1;
                pool = new ExpressionEvaluator[maxPoolSize];
                for (int i = 0; i < initialPoolSize; i++) {
                    putInPool(expressionEvaluatorFactory.create());
                }
            }
            while (nextAvailable < 0) {
                try {
                    mutex.wait();
                } catch (InterruptedException e) {
                    throw new ConversionException("Interrupted whilst waiting for a free item in the pool", e);
                }
            }
            result = pool[nextAvailable];
            nextAvailable--;
        }
        if (result == null) {
            result = expressionEvaluatorFactory.create();
            putInPool(result);
        }
        return result;
    }

    private void putInPool(ExpressionEvaluator expressionEvaluator) {
        synchronized (mutex) {
            nextAvailable++;
            pool[nextAvailable] = expressionEvaluator;
            mutex.notify();
        }
    }

}
