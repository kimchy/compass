/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent.atomic;

import java.util.*;

/**
 * An array of object references in which elements may be updated
 * atomically.  See the {@link org.compass.core.util.backport.java.util.concurrent.atomic} package
 * specification for description of the properties of atomic
 * variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicReferenceArray implements java.io.Serializable {
    private static final long serialVersionUID = -6209656149925076980L;

    private final Object[] array;

    /**
     * Creates a new AtomicReferenceArray of given length.
     * @param length the length of the array
     */
    public AtomicReferenceArray(int length) {
        array = new Object[length];
    }

    /**
     * Creates a new AtomicReferenceArray with the same length as, and
     * all elements copied from, the given array.
     *
     * @param array the array to copy elements from
     * @throws NullPointerException if array is null
     */
    public AtomicReferenceArray(Object[] array) {
        if (array == null)
            throw new NullPointerException();
        int length = array.length;
        this.array = new Object[length];
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
    public final synchronized Object get(int i) {
        return array[i];
    }

    /**
     * Sets the element at position <tt>i</tt> to the given value.
     *
     * @param i the index
     * @param newValue the new value
     */
    public final synchronized void set(int i, Object newValue) {
        array[i] = newValue;
    }

    /**
     * Eventually sets the element at position <tt>i</tt> to the given value.
     *
     * @param i the index
     * @param newValue the new value
     * @since 1.6
     */
    public final synchronized void lazySet(int i, Object newValue) {
        array[i] = newValue;
    }


    /**
     * Atomically sets the element at position <tt>i</tt> to the given
     * value and returns the old value.
     *
     * @param i the index
     * @param newValue the new value
     * @return the previous value
     */
    public final synchronized Object getAndSet(int i, Object newValue) {
        Object old = array[i];
        array[i] = newValue;
        return old;
    }

    /**
     * Atomically sets the element at position <tt>i</tt> to the given
     * updated value if the current value <tt>==</tt> the expected value.
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final synchronized boolean compareAndSet(int i, Object expect, Object update) {
        if (array[i] == expect) {
            array[i] = update;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Atomically sets the element at position <tt>i</tt> to the given
     * updated value if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     * @param i the index
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final synchronized boolean weakCompareAndSet(int i, Object expect, Object update) {
        if (array[i] == expect) {
            array[i] = update;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the String representation of the current values of array.
     * @return the String representation of the current values of array.
     */
    public synchronized String toString() {
        if (array.length == 0)
            return "[]";

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < array.length; i++) {
            if (i == 0)
                buf.append('[');
            else
                buf.append(", ");

            buf.append(String.valueOf(array[i]));
        }

        buf.append("]");
        return buf.toString();
    }

}
