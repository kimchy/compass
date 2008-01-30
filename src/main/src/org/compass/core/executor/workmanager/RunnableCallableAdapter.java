package org.compass.core.executor.workmanager;

import java.util.concurrent.Callable;

/**
 * @author kimchy
 */
public class RunnableCallableAdapter<T> implements Callable {

    private Runnable delegate;

    public RunnableCallableAdapter(Runnable delegate) {
        this.delegate = delegate;
    }

    public T call() throws Exception {
        delegate.run();
        return null;
    }
}
