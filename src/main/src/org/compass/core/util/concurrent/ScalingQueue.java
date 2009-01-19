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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A scaling queue that works with a {@link java.util.concurrent.ThreadPoolExecutor}
 * in when offerring which takes the active count and the max threads into account.
 *
 * @author kimchy
 */
public class ScalingQueue<E> extends LinkedBlockingQueue<E> {

    /**
     * The executor this Queue belongs to
     */
    private ThreadPoolExecutor executor;

    /**
     * Creates a <tt>TaskQueue</tt> with a capacity of
     * {@link Integer#MAX_VALUE}.
     */
    public ScalingQueue() {
        super();
    }

    /**
     * Creates a <tt>TaskQueue</tt> with the given (fixed) capacity.
     *
     * @param capacity the capacity of this queue.
     */
    public ScalingQueue(int capacity) {
        super(capacity);
    }

    /**
     * Sets the executor this queue belongs to.
     */
    public void setThreadPoolExecutor(ThreadPoolExecutor executor) {
        this.executor = executor;
    }

    /**
     * Inserts the specified element at the tail of this queue if there is at
     * least one available thread to run the current task. If all pool threads
     * are actively busy, it rejects the offer.
     *
     * @param o the element to add.
     * @return <tt>true</tt> if it was possible to add the element to this
     *         queue, else <tt>false</tt>
     * @see ThreadPoolExecutor#execute(Runnable)
     */
    @Override
    public boolean offer(E o) {
        int allWorkingThreads = executor.getActiveCount() + super.size();
        return allWorkingThreads < executor.getPoolSize() && super.offer(o);
    }
}
