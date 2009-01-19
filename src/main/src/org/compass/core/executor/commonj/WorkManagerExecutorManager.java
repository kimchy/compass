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

package org.compass.core.executor.commonj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import commonj.work.WorkException;
import commonj.work.WorkManager;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.executor.spi.InternalExecutorManager;
import org.compass.core.jndi.NamingHelper;

/**
 * A commonj implemention of {@link org.compass.core.executor.spi.InternalExecutorManager}.
 *
 * <p>submit and invoke operations are executed directly using the work manager with a future
 * adapter.
 *
 * <p>schedule operation uses a {@link java.util.concurrent.ScheduledExecutorService} and submits
 * the work when the command/task is scheduled using adapters as well.
 *
 * @author kimchy
 */
public class WorkManagerExecutorManager implements InternalExecutorManager, CompassConfigurable {

    private WorkManager workManager;

    private ScheduledExecutorService executorService;

    public void configure(CompassSettings settings) throws CompassException {
        String jndiName = settings.getSetting(CompassEnvironment.ExecutorManager.CommonJ.WORK_MANAGER_JNDI_NAME);
        if (jndiName == null) {
            throw new ConfigurationException("When using WorkManager executor manager jndi name must be set using [" + CompassEnvironment.ExecutorManager.WorkManager.JNDI_NAME + "]");
        }
        try {
            this.workManager = (WorkManager) NamingHelper.getInitialContext(settings).lookup(jndiName);
        } catch (Exception e) {
            throw new ConfigurationException("Failed to lookup workmanager under [" + jndiName + "]");
        }
        executorService = Executors.newScheduledThreadPool(1);
    }

    public void submit(Runnable task) {
        submit(Executors.callable(task));
    }

    public <T> Future<T> submit(Callable<T> task) {
        WorkCallableFutureAdapter<T> workCallableAdapter = new WorkCallableFutureAdapter<T>(task);
        try {
            workManager.schedule(workCallableAdapter);
        } catch (WorkException e) {
            workCallableAdapter.setException(e);
        }
        return workCallableAdapter;
    }

    public <T> List<Future<T>> invokeAll(Collection<Callable<T>> tasks) throws InterruptedException {
        ArrayList<Future<T>> futures = new ArrayList<Future<T>>(tasks.size());
        for (Callable<T> task : tasks) {
            futures.add(submit(task));
        }
        return futures;
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        WorkSubmitterCallable<V> workSubmitterCallable = new WorkSubmitterCallable<V>(workManager, callable);
        return executorService.schedule(workSubmitterCallable, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        WorkSubmitterRunnable workSubmitterRunnable = new WorkSubmitterRunnable(workManager, command);
        return executorService.scheduleAtFixedRate(workSubmitterRunnable, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        WorkSubmitterRunnable workSubmitterRunnable = new WorkSubmitterRunnable(workManager, command);
        return executorService.scheduleWithFixedDelay(workSubmitterRunnable, initialDelay, delay, unit);
    }

    public void close() {
        executorService.shutdown();
    }
}
