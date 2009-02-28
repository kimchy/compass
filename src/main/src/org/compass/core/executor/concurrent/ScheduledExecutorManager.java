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

package org.compass.core.executor.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.executor.spi.InternalExecutorManager;
import org.compass.core.util.concurrent.NamedThreadFactory;

/**
 * An executor manager based on {@link java.util.concurrent.ScheduledExecutorService}.
 *
 * <p>Reads the {@link org.compass.core.config.CompassEnvironment.ExecutorManager.Scheduled#CORE_POOL_SIZE} in order
 * to configure the inital thread count and uses the {@link ScheduledExecutorService} for all operations.
 *
 * @author kimchy
 */
public class ScheduledExecutorManager implements InternalExecutorManager, CompassConfigurable {

    private ScheduledExecutorService executorService;

    public void configure(CompassSettings settings) throws CompassException {
        int corePoolSize = settings.getSettingAsInt(CompassEnvironment.ExecutorManager.Scheduled.CORE_POOL_SIZE, 10);
        executorService = Executors.newScheduledThreadPool(corePoolSize, new NamedThreadFactory("Compass Executor Thread", true));
    }

    public void submit(Runnable task) {
        executorService.submit(task);
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executorService.schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public void close() {
        executorService.shutdown();
    }
}