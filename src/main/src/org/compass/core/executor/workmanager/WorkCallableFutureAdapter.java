/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.executor.workmanager;

import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.resource.spi.work.Work;

/**
 * @author kimchy
 */
public class WorkCallableFutureAdapter<T> implements Work, ScheduledFuture<T> {

    private Callable<T> callable;

    private ScheduledFuture<T> delegateScheduledFuture;

    private T result;

    private Exception exception;

    private volatile boolean done = false;

    private final Object monitor = new Object();

    public WorkCallableFutureAdapter(Callable<T> callable) {
        this.callable = callable;
    }

    public WorkCallableFutureAdapter(Runnable runnable) {
        this.callable = Executors.callable(runnable, (T) null);
    }

    public void run() {
        try {
            result = callable.call();
        } catch (Exception e) {
            this.exception = e;
        } finally {
            done = true;
            monitor.notifyAll();
        }
    }

    public void release() {
    }

    void setException(Exception e) {
        this.exception = e;
    }

    void setDelegateScheduledFuture(ScheduledFuture<T> delegateScheduledFuture) {
        this.delegateScheduledFuture = delegateScheduledFuture;
    }

    public T get() throws InterruptedException, ExecutionException {
        while (!done) {
            synchronized (monitor) {
                monitor.wait();
            }
        }
        if (exception != null) {
            throw new ExecutionException(exception.getMessage(), exception);
        }
        return result;
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (monitor) {
                monitor.wait(unit.toMillis(timeout));
            }
        }
        if (!done) {
            throw new TimeoutException("Timeout waiting for task to execute for [" + timeout + "]");
        }
        if (exception != null) {
            throw new ExecutionException(exception.getMessage(), exception);
        }
        return result;
    }

    public boolean isDone() {
        return done;
    }

    public long getDelay(TimeUnit unit) {
        if (delegateScheduledFuture != null) {
            return delegateScheduledFuture.getDelay(unit);
        }
        return -1;
    }

    public int compareTo(Delayed o) {
        if (delegateScheduledFuture != null) {
            return delegateScheduledFuture.compareTo(o);
        }
        throw new UnsupportedOperationException();
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return delegateScheduledFuture != null && delegateScheduledFuture.cancel(mayInterruptIfRunning);
    }

    public boolean isCancelled() {
        return delegateScheduledFuture != null && delegateScheduledFuture.isCancelled();
    }
}
