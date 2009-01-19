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

package org.compass.core.executor.spi;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An executor manager is an abstraction on top of async and scheduled execution
 * of tasks.
 *
 * @author kimchy
 */
public interface InternalExecutorManager {

    /**
     * @see java.util.concurrent.ExecutorService#submit(Runnable)
     */
    void submit(Runnable task);

    /**
     * @see java.util.concurrent.ExecutorService#submit(java.util.concurrent.Callable)
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * @see java.util.concurrent.ExecutorService#invokeAll(java.util.Collection)
     */
    <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException;

    /**
     * @see java.util.concurrent.ScheduledExecutorService#schedule(java.util.concurrent.Callable, long, java.util.concurrent.TimeUnit)
     */
    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * @see java.util.concurrent.ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, java.util.concurrent.TimeUnit)
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay,  long period, TimeUnit unit);

    /**
     * @see java.util.concurrent.ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, java.util.concurrent.TimeUnit)
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay,  long delay, TimeUnit unit);

    /**
     * Shuts down this executor manager.
     */
    void close();
}