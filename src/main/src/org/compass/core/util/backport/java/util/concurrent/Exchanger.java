/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent;
import org.compass.core.util.backport.java.util.concurrent.*; // for javadoc (till 6280605 is fixed)
import org.compass.core.util.backport.java.util.concurrent.locks.*;
import org.compass.core.util.backport.java.util.concurrent.helpers.*;

/**
 * A synchronization point at which threads can pair and swap elements
 * within pairs.  Each thread presents some object on entry to the
 * {@link #exchange exchange} method, matches with a partner thread,
 * and receives its partner's object on return.
 *
 * <p><b>Sample Usage:</b>
 * Here are the highlights of a class that uses an {@code Exchanger}
 * to swap buffers between threads so that the thread filling the
 * buffer gets a freshly emptied one when it needs it, handing off the
 * filled one to the thread emptying the buffer.
 * <pre>{@code
 * class FillAndEmpty {
 *   Exchanger<DataBuffer> exchanger = new Exchanger<DataBuffer>();
 *   DataBuffer initialEmptyBuffer = ... a made-up type
 *   DataBuffer initialFullBuffer = ...
 *
 *   class FillingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialEmptyBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           addToBuffer(currentBuffer);
 *           if (currentBuffer.isFull())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ... }
 *     }
 *   }
 *
 *   class EmptyingLoop implements Runnable {
 *     public void run() {
 *       DataBuffer currentBuffer = initialFullBuffer;
 *       try {
 *         while (currentBuffer != null) {
 *           takeFromBuffer(currentBuffer);
 *           if (currentBuffer.isEmpty())
 *             currentBuffer = exchanger.exchange(currentBuffer);
 *         }
 *       } catch (InterruptedException ex) { ... handle ...}
 *     }
 *   }
 *
 *   void start() {
 *     new Thread(new FillingLoop()).start();
 *     new Thread(new EmptyingLoop()).start();
 *   }
 * }
 * }</pre>
 *
 * <p>Memory consistency effects: For each pair of threads that
 * successfully exchange objects via an {@code Exchanger}, actions
 * prior to the {@code exchange()} in each thread
 * <a href="package-summary.html#MemoryVisibility"><i>happen-before</i></a>
 * those subsequent to a return from the corresponding {@code exchange()}
 * in the other thread.
 *
 * @since 1.5
 * @author Doug Lea and Bill Scherer and Michael Scott
 */
public class Exchanger {
    private final Object lock = new Object();

    /** Holder for the item being exchanged */
    private Object item;

    /**
     * Arrival count transitions from 0 to 1 to 2 then back to 0
     * during an exchange.
     */
    private int arrivalCount;

    /**
     * Main exchange function, handling the different policy variants.
     */
    private Object doExchange(Object x, boolean timed, long nanos) throws InterruptedException, TimeoutException {
        synchronized (lock) {
            Object other;
            long deadline = timed ? Utils.nanoTime() + nanos : 0;
            // If arrival count already at two, we must wait for
            // a previous pair to finish and reset the count;
            while (arrivalCount == 2) {
                if (!timed)
                    lock.wait();
                else if (nanos > 0) {
                    TimeUnit.NANOSECONDS.timedWait(lock, nanos);
                    nanos = deadline - Utils.nanoTime();
                }
                else
                    throw new TimeoutException();
            }

            int count = ++arrivalCount;

            // If item is already waiting, replace it and signal other thread
            if (count == 2) {
                other = item;
                item = x;
                lock.notifyAll();
                return other;
            }

            // Otherwise, set item and wait for another thread to
            // replace it and signal us.

            item = x;
            InterruptedException interrupted = null;
            try {
                while (arrivalCount != 2) {
                    if (!timed)
                        lock.wait();
                    else if (nanos > 0) {
                        TimeUnit.NANOSECONDS.timedWait(lock, nanos);
                        nanos = deadline - Utils.nanoTime();
                    }
                    else
                        break; // timed out
                }
            } catch (InterruptedException ie) {
                interrupted = ie;
            }

            // Get and reset item and count after the wait.
            // (We need to do this even if wait was aborted.)
            other = item;
            item = null;
            count = arrivalCount;
            arrivalCount = 0;
            lock.notifyAll();

            // If the other thread replaced item, then we must
            // continue even if cancelled.
            if (count == 2) {
                if (interrupted != null)
                    Thread.currentThread().interrupt();
                return other;
            }

            // If no one is waiting for us, we can back out
            if (interrupted != null)
                throw interrupted;
            else  // must be timeout
                throw new TimeoutException();
        }
    }

    /**
     * Creates a new Exchanger.
     **/
    public Exchanger() {
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * it is {@link Thread#interrupt interrupted}),
     * and then transfers the given object to it, receiving its object
     * in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread. The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of two things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@link Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @param x the object to exchange
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     */
    public Object exchange(Object x) throws InterruptedException {
        try {
            return doExchange(x, false, 0);
        } catch (TimeoutException cannotHappen) {
            throw new Error(cannotHappen);
        }
    }

    /**
     * Waits for another thread to arrive at this exchange point (unless
     * the current thread is {@link Thread#interrupt interrupted} or
     * the specified waiting time elapses), and then transfers the given
     * object to it, receiving its object in return.
     *
     * <p>If another thread is already waiting at the exchange point then
     * it is resumed for thread scheduling purposes and receives the object
     * passed in by the current thread. The current thread returns immediately,
     * receiving the object passed to the exchange by that other thread.
     *
     * <p>If no other thread is already waiting at the exchange then the
     * current thread is disabled for thread scheduling purposes and lies
     * dormant until one of three things happens:
     * <ul>
     * <li>Some other thread enters the exchange; or
     * <li>Some other thread {@link Thread#interrupt interrupts} the current
     * thread; or
     * <li>The specified waiting time elapses.
     * </ul>
     * <p>If the current thread:
     * <ul>
     * <li>has its interrupted status set on entry to this method; or
     * <li>is {@link Thread#interrupt interrupted} while waiting
     * for the exchange,
     * </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * <p>If the specified waiting time elapses then {@link TimeoutException}
     * is thrown.
     * If the time is
     * less than or equal to zero, the method will not wait at all.
     *
     * @param x the object to exchange
     * @param timeout the maximum time to wait
     * @param unit the time unit of the <tt>timeout</tt> argument
     * @return the object provided by the other thread
     * @throws InterruptedException if the current thread was
     *         interrupted while waiting
     * @throws TimeoutException if the specified waiting time elapses
     *         before another thread enters the exchange
     */
    public Object exchange(Object x, long timeout, TimeUnit unit)
        throws InterruptedException, TimeoutException {
        return doExchange(x, true, unit.toNanos(timeout));
    }
}


