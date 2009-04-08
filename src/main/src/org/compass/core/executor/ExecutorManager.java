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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.compass.core.executor.spi.InternalExecutorManager;

/**
 * An executor manager is an abstraction on top of async and scheduled execution
 * of tasks.
 *
 * @author kimchy
 */
public interface ExecutorManager extends InternalExecutorManager {

    /**
     * Returns <code>true</code> if the executor manager is disabled or not.
     */
    boolean isDisabled();

    /**
     * Similar to {@link #invokeAll(java.util.Collection)}, but only uses it if the number of tasks passes
     * the concurrent limit.
     */
    <T> List<Future<T>> invokeAllWithLimit(Collection<Callable<T>> tasks, int concurrencyThreshold);

    /**
     * Similar to {@link #invokeAllWithLimit(java.util.Collection, int)}, but if one task throws an exception
     * will propogate the exception.
     */
    <T> List<Future<T>> invokeAllWithLimitBailOnException(Collection<Callable<T>> tasks, int concurrencyThreshold);
}
