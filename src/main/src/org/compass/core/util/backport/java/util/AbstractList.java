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
public abstract class AbstractList extends java.util.AbstractList {

    /**
     * Sole constructor. (For invocation by subclass constructors, typically
     * implicit.)
     */
    protected AbstractList() { super(); }

    public Object[] toArray() {
        return Utils.collectionToArray(this);
    }

    public Object[] toArray(Object[] a) {
        return Utils.collectionToArray(this, a);
    }
}
