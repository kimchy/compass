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

import java.util.concurrent.Callable;

import commonj.work.WorkManager;

/**
 * @author kimchy
 */
public class WorkSubmitterCallable<T> implements Callable<T> {

    private final WorkManager workManager;

    private final Callable<T> callable;

    public WorkSubmitterCallable(WorkManager workManager, Callable<T> callable) {
        this.workManager = workManager;
        this.callable = callable;
    }

    public T call() throws Exception {
        WorkCallableFutureAdapter<T> work = new WorkCallableFutureAdapter<T>(callable);
        workManager.schedule(work);
        return work.get();
    }
}
