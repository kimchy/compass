/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent;
import org.compass.core.util.backport.java.util.*;
import java.util.Comparator;
import java.util.Collection;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Map;

/**
 * A scalable concurrent {@link NavigableSet} implementation based on
 * a {@link ConcurrentSkipListMap}.  The elements of the set are kept
 * sorted according to their {@linkplain Comparable natural ordering},
 * or by a {@link Comparator} provided at set creation time, depending
 * on which constructor is used.
 *
 * <p>This implementation provides expected average <i>log(n)</i> time
 * cost for the <tt>contains</tt>, <tt>add</tt>, and <tt>remove</tt>
 * operations and their variants.  Insertion, removal, and access
 * operations safely execute concurrently by multiple threads.
 * Iterators are <i>weakly consistent</i>, returning elements
 * reflecting the state of the set at some point at or since the
 * creation of the iterator.  They do <em>not</em> throw {@link
 * ConcurrentModificationException}, and may proceed concurrently with
 * other operations.  Ascending ordered views and their iterators are
 * faster than descending ones.
 *
 * <p>Beware that, unlike in most collections, the <tt>size</tt>
 * method is <em>not</em> a constant-time operation. Because of the
 * asynchronous nature of these sets, determining the current number
 * of elements requires a traversal of the elements. Additionally, the
 * bulk operations <tt>addAll</tt>, <tt>removeAll</tt>,
 * <tt>retainAll</tt>, and <tt>containsAll</tt> are <em>not</em>
 * guaranteed to be performed atomically. For example, an iterator
 * operating concurrently with an <tt>addAll</tt> operation might view
 * only some of the added elements.
 *
 * <p>This class and its iterators implement all of the
 * <em>optional</em> methods of the {@link Set} and {@link Iterator}
 * interfaces. Like most other concurrent collection implementations,
 * this class does not permit the use of <tt>null</tt> elements,
 * because <tt>null</tt> arguments and return values cannot be reliably
 * distinguished from the absence of elements.
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @since 1.6
 */
public class ConcurrentSkipListSet
    extends AbstractSet
    implements NavigableSet, Cloneable, java.io.Serializable {

    private static final long serialVersionUID = -2479143111061671589L;

    /**
     * The underlying map. Uses Boolean.TRUE as value for each
     * element.  Note that this class relies on default serialization,
     * which is a little wasteful in saving all of the useless value
     * fields of the underlying map, but enables this field to be
     * declared final, which is necessary for thread safety.
     */
    private final ConcurrentSkipListMap m;

    /**
     * Constructs a new, empty set that orders its elements according to
     * their {@linkplain Comparable natural ordering}.
     */
    public ConcurrentSkipListSet() {
        m = new ConcurrentSkipListMap();
    }

    /**
     * Constructs a new, empty set that orders its elements according to
     * the specified comparator.
     *
     * @param comparator the comparator that will be used to order this set.
     *        If <tt>null</tt>, the {@linkplain Comparable natural
     *        ordering} of the elements will be used.
     */
    public ConcurrentSkipListSet(Comparator comparator) {
        m = new ConcurrentSkipListMap(comparator);
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection, that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param c The elements that will comprise the new set
     * @throws ClassCastException if the elements in <tt>c</tt> are
     *         not {@link Comparable}, or are not mutually comparable
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public ConcurrentSkipListSet(Collection c) {
        m = new ConcurrentSkipListMap();
        addAll(c);
    }

    /**
     * Constructs a new set containing the same elements and using the
     * same ordering as the specified sorted set.
     *
     * @param s sorted set whose elements will comprise the new set
     * @throws NullPointerException if the specified sorted set or any
     *         of its elements are null
     */
    public ConcurrentSkipListSet(SortedSet s) {
        m = new ConcurrentSkipListMap(s.comparator());
        addAll(s);
    }

    /**
     * Returns a shallow copy of this <tt>ConcurrentSkipListSet</tt>
     * instance. (The elements themselves are not cloned.)
     *
     * @return a shallow copy of this set
     */
    public Object clone() {
        ConcurrentSkipListSet clone = null;
        try {
            clone = (ConcurrentSkipListSet) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }

        clone.m.initialize();
        clone.addAll(this);
        return clone;
    }

    /* ---------------- Set operations -------------- */

    /**
     * Returns the number of elements in this set.  If this set
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, it
     * returns <tt>Integer.MAX_VALUE</tt>.
     *
     * <p>Beware that, unlike in most collections, this method is
     * <em>NOT</em> a constant-time operation. Because of the
     * asynchronous nature of these sets, determining the current
     * number of elements requires traversing them all to count them.
     * Additionally, it is possible for the size to change during
     * execution of this method, in which case the returned result
     * will be inaccurate. Thus, this method is typically not very
     * useful in concurrent applications.
     *
     * @return the number of elements in this set
     */
    public int size() {
        return m.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     * @return <tt>true</tt> if this set contains no elements
     */
    public boolean isEmpty() {
        return m.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this set
     * contains an element <tt>e</tt> such that <tt>o.equals(e)</tt>.
     *
     * @param o object to be checked for containment in this set
     * @return <tt>true</tt> if this set contains the specified element
     * @throws ClassCastException if the specified element cannot be
     *         compared with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean contains(Object o) {
        return m.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * More formally, adds the specified element <tt>e</tt> to this set if
     * the set contains no element <tt>e2</tt> such that <tt>e.equals(e2)</tt>.
     * If this set already contains the element, the call leaves the set
     * unchanged and returns <tt>false</tt>.
     *
     * @param e element to be added to this set
     * @return <tt>true</tt> if this set did not already contain the
     *         specified element
     * @throws ClassCastException if <tt>e</tt> cannot be compared
     *         with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(Object e) {
        return m.putIfAbsent(e, Boolean.TRUE) == null;
    }

    /**
     * Removes the specified element from this set if it is present.
     * More formally, removes an element <tt>e</tt> such that
     * <tt>o.equals(e)</tt>, if this set contains such an element.
     * Returns <tt>true</tt> if this set contained the element (or
     * equivalently, if this set changed as a result of the call).
     * (This set will not contain the element once the call returns.)
     *
     * @param o object to be removed from this set, if present
     * @return <tt>true</tt> if this set contained the specified element
     * @throws ClassCastException if <tt>o</tt> cannot be compared
     *         with the elements currently in this set
     * @throws NullPointerException if the specified element is null
     */
    public boolean remove(Object o) {
        return m.removep(o);
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        m.clear();
    }

    /**
     * Returns an iterator over the elements in this set in ascending order.
     *
     * @return an iterator over the elements in this set in ascending order
     */
    public Iterator iterator() {
        return m.keyIterator();
    }

    /**
     * Returns an iterator over the elements in this set in descending order.
     *
     * @return an iterator over the elements in this set in descending order
     */
    public Iterator descendingIterator() {
        return m.descendingKeyIterator();
    }

    /* ---------------- AbstractSet Overrides -------------- */

    /**
     * Compares the specified object with this set for equality.  Returns
     * <tt>true</tt> if the specified object is also a set, the two sets
     * have the same size, and every member of the specified set is
     * contained in this set (or equivalently, every member of this set is
     * contained in the specified set).  This definition ensures that the
     * equals method works properly across different implementations of the
     * set interface.
     *
     * @param o the object to be compared for equality with this set
     * @return <tt>true</tt> if the specified object is equal to this set
     */
    public boolean equals(Object o) {
        // Override AbstractSet version to avoid calling size()
        if (o == this)
            return true;
        if (!(o instanceof Set))
            return false;
        Collection c = (Collection) o;
        try {
            return containsAll(c) && c.containsAll(this);
        } catch (ClassCastException unused)   {
            return false;
        } catch (NullPointerException unused) {
            return false;
        }
    }

    /**
     * Removes from this set all of its elements that are contained in
     * the specified collection.  If the specified collection is also
     * a set, this operation effectively modifies this set so that its
     * value is the <i>asymmetric set difference</i> of the two sets.
     *
     * @param  c collection containing elements to be removed from this set
     * @return <tt>true</tt> if this set changed as a result of the call
     * @throws ClassCastException if the types of one or more elements in this
     *         set are incompatible with the specified collection
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public boolean removeAll(Collection c) {
        // Override AbstractSet version to avoid unnecessary call to size()
        boolean modified = false;
        for (Iterator i = c.iterator(); i.hasNext(); )
            if (remove(i.next()))
                modified = true;
        return modified;
    }

    /* ---------------- Relational operations -------------- */

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     */
    public Object lower(Object e) {
        return m.lowerKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     */
    public Object floor(Object e) {
        return m.floorKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     */
    public Object ceiling(Object e) {
        return m.ceilingKey(e);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified element is null
     */
    public Object higher(Object e) {
        return m.higherKey(e);
    }

    public Object pollFirst() {
        return m.pollFirstKey();
    }

    public Object pollLast() {
        return m.pollLastKey();
    }


    /* ---------------- SortedSet operations -------------- */


    public Comparator comparator() {
        return m.comparator();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public Object first() {
        return m.firstKey();
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public Object last() {
        return m.lastKey();
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if <tt>fromElement</tt> or
     *         <tt>toElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet navigableSubSet(Object fromElement, Object toElement) {
        return new ConcurrentSkipListSubSet(m, fromElement, toElement);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if <tt>toElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet navigableHeadSet(Object toElement) {
        return new ConcurrentSkipListSubSet(m, null, toElement);
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if <tt>fromElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public NavigableSet navigableTailSet(Object fromElement) {
        return new ConcurrentSkipListSubSet(m, fromElement, null);
    }

    /**
     * Equivalent to {@link #navigableSubSet} but with a return type
     * conforming to the <tt>SortedSet</tt> interface.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if <tt>fromElement</tt> or
     *         <tt>toElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet subSet(Object fromElement, Object toElement) {
        return navigableSubSet(fromElement, toElement);
    }

    /**
     * Equivalent to {@link #navigableHeadSet} but with a return type
     * conforming to the <tt>SortedSet</tt> interface.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if <tt>toElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet headSet(Object toElement) {
        return navigableHeadSet(toElement);
    }


    /**
     * Equivalent to {@link #navigableTailSet} but with a return type
     * conforming to the <tt>SortedSet</tt> interface.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if <tt>fromElement</tt> is null
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedSet tailSet(Object fromElement) {
        return navigableTailSet(fromElement);
    }

    /**
     * Subsets returned by {@link ConcurrentSkipListSet} subset operations
     * represent a subrange of elements of their underlying
     * sets. Instances of this class support all methods of their
     * underlying sets, differing in that elements outside their range are
     * ignored, and attempts to add elements outside their ranges result
     * in {@link IllegalArgumentException}.  Instances of this class are
     * constructed only using the <tt>subSet</tt>, <tt>headSet</tt>, and
     * <tt>tailSet</tt> methods of their underlying sets.
     */
    static class ConcurrentSkipListSubSet
        extends AbstractSet
        implements NavigableSet, java.io.Serializable {

        private static final long serialVersionUID = -7647078645896651609L;

        /** The underlying submap  */
        private final ConcurrentSkipListMap.ConcurrentSkipListSubMap s;

        /**
         * Creates a new submap.
         * @param fromElement inclusive least value, or <tt>null</tt> if from start
         * @param toElement exclusive upper bound or <tt>null</tt> if to end
         * @throws IllegalArgumentException if fromElement and toElement
         * nonnull and fromElement greater than toElement
         */
        ConcurrentSkipListSubSet(ConcurrentSkipListMap map,
                                 Object fromElement, Object toElement) {
            s = new ConcurrentSkipListMap.ConcurrentSkipListSubMap
                (map, fromElement, toElement);
        }

        // subsubset construction

        public NavigableSet navigableSubSet(Object fromElement, Object toElement) {
            if (!s.inOpenRange(fromElement) || !s.inOpenRange(toElement))
                throw new IllegalArgumentException("element out of range");
            return new ConcurrentSkipListSubSet(s.getMap(),
                                                   fromElement, toElement);
        }

        public NavigableSet navigableHeadSet(Object toElement) {
            Object least = s.getLeast();
            if (!s.inOpenRange(toElement))
                throw new IllegalArgumentException("element out of range");
            return new ConcurrentSkipListSubSet(s.getMap(),
                                                   least, toElement);
        }

        public NavigableSet navigableTailSet(Object fromElement) {
            Object fence = s.getFence();
            if (!s.inOpenRange(fromElement))
                throw new IllegalArgumentException("element out of range");
            return new ConcurrentSkipListSubSet(s.getMap(),
                                                   fromElement, fence);
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
            return navigableSubSet(fromElement, toElement);
        }

        public SortedSet headSet(Object toElement) {
            return navigableHeadSet(toElement);
        }

        public SortedSet tailSet(Object fromElement) {
            return navigableTailSet(fromElement);
        }

        // relays to submap methods

        public int size()                 { return s.size(); }
        public boolean isEmpty()          { return s.isEmpty(); }
        public boolean contains(Object o) { return s.containsKey(o); }
        public void clear()               { s.clear(); }
        public Object first()                  { return s.firstKey(); }
        public Object last()                   { return s.lastKey(); }
        public Object ceiling(Object e)             { return s.ceilingKey(e); }
        public Object lower(Object e)               { return s.lowerKey(e); }
        public Object floor(Object e)               { return s.floorKey(e); }
        public Object higher(Object e)              { return s.higherKey(e); }
        public boolean remove(Object o) { return s.remove(o)==Boolean.TRUE; }
        public boolean add(Object e)       { return s.put(e, Boolean.TRUE)==null; }
        public Comparator comparator() { return s.comparator(); }
        public Iterator iterator()     { return s.keySet().iterator(); }
        public Iterator descendingIterator() {
            return s.descendingKeySet().iterator();
        }
        public Object pollFirst() {
            Map.Entry e = s.pollFirstEntry();
            return (e == null)? null : e.getKey();
        }
        public Object pollLast() {
            Map.Entry e = s.pollLastEntry();
            return (e == null)? null : e.getKey();
        }

    }
}
