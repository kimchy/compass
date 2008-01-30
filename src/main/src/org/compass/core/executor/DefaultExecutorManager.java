/*
 * Copyright 2004-2006 the original author or authors.
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
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.executor.concurrent.ConcurrentExecutorManager;
import org.compass.core.util.ClassUtils;

/**
 * The "default" implementation of an executor manager. Simply delegates operations
 * to an internal executor manager created based on {@link org.compass.core.config.CompassEnvironment.ExecutorManager}
 * settings.
 *
 * @author kimchy
 */
public class DefaultExecutorManager implements ExecutorManager, CompassConfigurable {

    private ExecutorManager executorManager;

    public void configure(CompassSettings settings) throws CompassException {

        String executorManagerType = settings.getSetting(CompassEnvironment.ExecutorManager.EXECUTOR_MANAGER_TYPE, ConcurrentExecutorManager.class.getName());

        if (executorManagerType.equals(CompassEnvironment.ExecutorManager.Concurrent.NAME)) {
            executorManagerType = ConcurrentExecutorManager.class.getName();
        } else if (executorManagerType.equals(CompassEnvironment.ExecutorManager.Scheduled.NAME)) {
            executorManagerType = ScheduledExecutorService.class.getName();
        } else if (executorManagerType.equals(CompassEnvironment.ExecutorManager.WorkManager.NAME)) {
            executorManagerType = "org.compass.core.executor.workmanager.WorkManagerExecutorManager";
        }

        try {
            executorManager = (ExecutorManager) ClassUtils.forName(executorManagerType, settings.getClassLoader()).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create executor manager [" + executorManagerType + "]", e);
        }

        if (executorManager instanceof CompassConfigurable) {
            ((CompassConfigurable) executorManager).configure(settings);
        }
    }

    public void close() {
        executorManager.close();
    }

    public <T> Future<T> submit(Callable<T> task) {
        return executorManager.submit(task);
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
        return executorManager.invokeAll(tasks);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executorManager.schedule(callable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executorManager.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executorManager.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
