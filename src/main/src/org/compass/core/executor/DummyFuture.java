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

package org.compass.core.executor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A dummy future representing a result that was already processed (with a
 * result or an exception).
 *
 * @author kimchy
 */
public class DummyFuture<T> implements Future<T> {

    private Exception e;

    private T result;

    public DummyFuture(Exception e) {
        this.e = e;
    }

    public DummyFuture(T result) {
        this.result = result;
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    public T get() throws InterruptedException, ExecutionException {
        if (e != null) {
            throw new ExecutionException(e);
        }
        return result;
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return get();
    }

    public boolean isCancelled() {
        return false;
    }

    public boolean isDone() {
        return true;
    }
}
