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

import commonj.work.WorkException;
import commonj.work.WorkManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author kimchy
 */
public class WorkSubmitterRunnable implements Runnable {

    private static final Log logger = LogFactory.getLog(WorkSubmitterRunnable.class);

    private final WorkManager workManager;

    private final Runnable runnable;

    public WorkSubmitterRunnable(WorkManager workManager, Runnable runnable) {
        this.workManager = workManager;
        this.runnable = runnable;
    }

    public void run() {
        WorkCallableFutureAdapter work = new WorkCallableFutureAdapter(runnable);
        try {
            workManager.schedule(work);
        } catch (WorkException e) {
            logger.warn("Failed to schedule work [" + runnable + "] on work manager", e);
        }
        try {
            work.get();
        } catch (Exception e) {
            logger.warn("Failed to wait for work [" + runnable + "] on work manager", e);
        }
    }
}