/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent.atomic;

/**
 * A <tt>long</tt> array in which elements may be updated atomically.
 * See the {@link org.compass.core.util.backport.java.util.concurrent.atomic} package specification
 * for description of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicLongArray implements java.io.Serializable {
    private static final long serialVersionUID = -2308431214976778248L;

    private final long[] array;

    /**
     * Creates a new AtomicLongArray of given length.
     *
     * @param length the length of the array
     */
    public AtomicLongArray(int length) {
        array = new long[length];
    }

    /**
     * Creates a new AtomicLongArray with the same length as, and
     * all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicLongArray(long[] array) {
        if (array == null)
            throw new NullPointerException();
        int length = array.length;
        this.array = new long[length];
        System.arraycopy(array, 0, this.array, 0, array.length);
    }

    /**
     * Returns the length of the array.
     *
     * @return the length of the array
     */
    public final int length() {
        return array.length;
    }

    /**
     * Gets the current value at position <tt>i</tt>.
     *
     * @param i the index
     * @return the current value
     */
    public final synchronized long get(int i) {
        return array[i];
    }

    /**
     * Sets the element at position <tt>i</tt> to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final synchronized void set(int i, long newValue) {
        array[i] = newValue;
    }

    /**
     * Eventually sets the element at position <tt>i</tt> to the given value.
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final synchronized void lazySet(int i, long newValue) {
        array[i] = newValue;
    }


    /**
     * Atomically sets the element at position <tt>i</tt> to the given value
     * and returns the old value.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final synchronized long getAndSet(int i, long newValue) {
        long old = array[i];
        array[i] = newValue;
        return old;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final synchronized boolean compareAndSet(int i, long expect, long update) {
        if (array[i] == expect) {
            array[i] = update;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     *
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final synchronized boolean weakCompareAndSet(int i, long expect, long update) {
        if (array[i] == expect) {
            array[i] = update;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Atomically increments by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the previous value
     */
    public final synchronized long getAndIncrement(int i) {
        return array[i]++;
    }

    /**
     * Atomically decrements by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the previous value
     */
    public final synchronized long getAndDecrement(int i) {
        return array[i]--;
    }

    /**
     * Atomically adds the given value to the element at index <tt>i</tt>.
     *
     * @param i the index
     * @param delta the value to add
     * @return the previous value
     */
    public final synchronized long getAndAdd(int i, long delta) {
        long old = array[i];
        array[i] += delta;
        return old;
    }


    /**
     * Atomically increments by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the updated value
     */
    public final synchronized long incrementAndGet(int i) {
        return ++array[i];
    }

    /**
     * Atomically decrements by one the element at index <tt>i</tt>.
     *
     * @param i the index
     * @return the updated value
     */
    public final synchronized long decrementAndGet(int i) {
        return --array[i];
    }

    /**
     * Atomically adds the given value to the element at index <tt>i</tt>.
     *
     * @param i the index
     * @param delta the value to add
     * @return the updated value
     */
    public synchronized long addAndGet(int i, long delta) {
        return array[i] += delta;
    }

    /**
     * Returns the String representation of the current values of array.
     * @return the String representation of the current values of array.
     */
    public synchronized String toString() {
        if (array.length == 0)
            return "[]";

        StringBuffer buf = new StringBuffer();
        buf.append('[');
        buf.append(array[0]);

        for (int i = 1; i < array.length; i++) {
            buf.append(", ");
            buf.append(array[i]);
        }

        buf.append("]");
        return buf.toString();
    }

}
