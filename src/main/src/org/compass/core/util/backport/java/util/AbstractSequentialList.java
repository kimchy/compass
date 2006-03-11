/*
 * Written by Dawid Kurzyniec, based on public domain code written by Doug Lea
 * and publictly available documentation, and released to the public domain, as
 * explained at http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util;
import org.compass.core.util.backport.java.util.concurrent.helpers.Utils;

/**
 * Overrides toArray() and toArray(Object[]) in AbstractCollection to provide
 * implementations valid for concurrent lists.
 *
 * @author Doug Lea
 * @author Dawid Kurzyniec
 */
public abstract class AbstractSequentialList extends java.util.AbstractSequentialList {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractSequentialList() { super(); }

    public Object[] toArray() {
        return Utils.collectionToArray(this);
    }

    public Object[] toArray(Object[] a) {
        return Utils.collectionToArray(this, a);
    }
}
