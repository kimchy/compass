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

package org.compass.core.util.concurrent;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A handler for rejected tasks that inserts the specified element into this
 * queue, waiting if necessary up to the specified wait time for space to become
 * available.
 *
 * @author kimchy
 */
public class TimedBlockingPolicy implements RejectedExecutionHandler {
    
    private final long waitTime;

    /**
     * @param waitTime wait time in milliseconds for space to become available.
     */
    public TimedBlockingPolicy(long waitTime) {
        this.waitTime = waitTime;
    }

    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            boolean successful = executor.getQueue().offer(r, waitTime, TimeUnit.MILLISECONDS);
            if (!successful)
                throw new RejectedExecutionException("Rejected execution after waiting "
                        + waitTime + " ms for task [" + r.getClass() + "] to be executed.");
        } catch (InterruptedException e) {
            throw new RejectedExecutionException(e);
        }
    }
}
