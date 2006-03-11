/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent.atomic;

/**
 * An object reference that may be updated atomically. See the {@link
 * org.compass.core.util.backport.java.util.concurrent.atomic} package specification for description
 * of the properties of atomic variables.
 * @since 1.5
 * @author Doug Lea
 */
public class AtomicReference  implements java.io.Serializable {
    private static final long serialVersionUID = -1848883965231344442L;

    private volatile Object value;

    /**
     * Creates a new AtomicReference with the given initial value.
     *
     * @param initialValue the initial value
     */
    public AtomicReference(Object initialValue) {
        value = initialValue;
    }

    /**
     * Creates a new AtomicReference with null initial value.
     */
    public AtomicReference() {
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public final Object get() {
        return value;
    }

    /**
     * Sets to the given value.
     *
     * @param newValue the new value
     */
    public final synchronized void set(Object newValue) {
        value = newValue;
    }

    /**
     * Eventually sets to the given value.
     *
     * @param newValue the new value
     * @since 1.6
     */
    public final synchronized void lazySet(Object newValue) {
        value = newValue;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful. False return indicates that
     * the actual value was not equal to the expected value.
     */
    public final synchronized boolean compareAndSet(Object expect, Object update) {
        if (value == expect) {
            value = update;
            return true;
        }
        return false;
    }

    /**
     * Atomically sets the value to the given updated value
     * if the current value <tt>==</tt> the expected value.
     * May fail spuriously.
     * @param expect the expected value
     * @param update the new value
     * @return true if successful.
     */
    public final synchronized boolean weakCompareAndSet(Object expect, Object update) {
        if (value == expect) {
            value = update;
            return true;
        }
        return false;
    }

    /**
     * Atomically sets to the given value and returns the old value.
     *
     * @param newValue the new value
     * @return the previous value
     */
    public final synchronized Object getAndSet(Object newValue) {
        Object old = value;
        value = newValue;
        return old;
    }

    /**
     * Returns the String representation of the current value.
     * @return the String representation of the current value.
     */
    public String toString() {
        return String.valueOf(get());
    }

}
