/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent;

/**
 * A {@link ScheduledFuture} that is {@link Runnable}. Successful
 * execution of the <tt>run</tt> method causes completion of the
 * <tt>Future</tt> and allows access to its results.
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 */
public interface RunnableScheduledFuture extends RunnableFuture, ScheduledFuture {

    /**
     * Returns true if this is a periodic task. A periodic task may
     * re-run according to some schedule. A non-periodic task can be
     * run only once.
     *
     * @return true if this task is periodic
     */
    boolean isPeriodic();
}
