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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.executor.spi.InternalExecutorManager;
import org.compass.core.util.concurrent.NamedThreadFactory;
import org.compass.core.util.concurrent.ScalingExecutros;

/**
 * An executor manager based on {@link java.util.concurrent.ThreadPoolExecutor} for tasks and a slim
 * {@link ScheduledExecutorService} for scheduled tasks.
 *
 * <p>Uses the {@link org.compass.core.config.CompassEnvironment.ExecutorManager} settings to confiure the
 * thread pool.
 *
 * @author kimchy
 */
public class ConcurrentExecutorManager implements InternalExecutorManager, CompassConfigurable {

    private static final Log log = LogFactory.getLog(ConcurrentExecutorManager.class);

    private ExecutorService executorService;

    private ScheduledExecutorService scheduledExecutorService;

    public void configure(CompassSettings settings) throws CompassException {
        int corePoolSize = settings.getSettingAsInt(CompassEnvironment.ExecutorManager.Concurrent.CORE_POOL_SIZE, 10);
        int maximumPoolSize = settings.getSettingAsInt(CompassEnvironment.ExecutorManager.Concurrent.MAXIMUM_POOL_SIZE, 50);
        long keepAliveTime = settings.getSettingAsTimeInMillis(CompassEnvironment.ExecutorManager.Concurrent.KEEP_ALIVE_TIME, 60000);

        executorService = ScalingExecutros.newScalingThreadPool(corePoolSize, maximumPoolSize, keepAliveTime, new NamedThreadFactory("Compass Executor Thread", true));

        if (log.isDebugEnabled()) {
            log.debug("Using concurrent executor manager with core size [" + corePoolSize + "], max size [" + maximumPoolSize + "], and keep alive time [" + keepAliveTime + "ms]");
        }

        int scheduledCorePoolSize = settings.getSettingAsInt(CompassEnvironment.ExecutorManager.Concurrent.SCHEDULED_CORE_POOL_SIZE, 1);
        scheduledExecutorService = Executors.newScheduledThreadPool(scheduledCorePoolSize,
                new NamedThreadFactory("Compass Scheduled Executor Thread", true));

        if (log.isDebugEnabled()) {
            log.debug("Using concurrent executor manager scheduler with size [" + scheduledCorePoolSize + "]");
        }
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
        return scheduledExecutorService.schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public void close() {
        executorService.shutdown();
        scheduledExecutorService.shutdown();
    }
}