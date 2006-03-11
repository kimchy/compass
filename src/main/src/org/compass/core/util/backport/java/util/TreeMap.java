/*
 * Written by Dawid Kurzyniec, on the basis of public specifications and
 * public domain sources from JSR 166 and the Doug Lea's collections package,
 * and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain.
 */

package org.compass.core.util.backport.java.util;

import java.util.Comparator;
import java.util.Map;
import java.util.AbstractSet;
import java.util.SortedSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import java.util.SortedMap;
import java.util.NoSuchElementException;
import java.util.ConcurrentModificationException;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Sorted map implementation based on a red-black tree and implementing
 * all the methods from the NavigableMap interface.
 *
 * @author Dawid Kurzyniec
 */
public class TreeMap extends AbstractMap
                     implements NavigableMap, Serializable {

    private static final long serialVersionUID = 919286545866124006L;

    private final Comparator comparator;

    private transient Entry root;

    private transient int size = 0;
    private transient int modCount = 0;

    private transient Set entrySet;
//    private transient Set valueSet;
//    private transient Set keySet;
    private transient Set descendingEntrySet;
    private transient Set descendingKeySet;

    public TreeMap() {
        this.comparator = null;
    }

    public TreeMap(Comparator comparator) {
        this.comparator = comparator;
    }

    public TreeMap(SortedMap map) {
        this.comparator = map.comparator();
        this.buildFromSorted(map.entrySet().iterator(), map.size());
    }

    public TreeMap(Map map) {
        this.comparator = null;
        putAll(map);
    }

    public int size() { return size; }

    public void clear() {
        root = null;
        size = 0;
        modCount++;
    }

    public Object clone() {
        TreeMap clone;
        try { clone = (TreeMap)super.clone(); }
        catch (CloneNotSupportedException e) { throw new InternalError(); }
        clone.root = null;
        clone.size = 0;
        clone.modCount = 0;
        if (!isEmpty()) {
            clone.buildFromSorted(this.entrySet().iterator(), this.size);
        }
        return clone;
    }

    public Object put(Object key, Object value) {
        if (root == null) {
            root = new Entry(key, value);
            size++;
            modCount++;
            return null;
        }
        else {
            Entry t = root;
            for (;;) {
                int diff = compare(key, t.getKey(), comparator);
                if (diff == 0) return t.setValue(value);
                else if (diff <= 0) {
                    if (t.left != null) t = t.left;
                    else {
                        size++;
                        modCount++;
                        Entry e = new Entry(key, value);
                        e.parent = t;
                        t.left = e;
                        fixAfterInsertion(e);
                        return null;
                    }
                }
                else {
                    if (t.right != null) t = t.right;
                    else {
                        size++;
                        modCount++;
                        Entry e = new Entry(key, value);
                        e.parent = t;
                        t.right = e;
                        fixAfterInsertion(e);
                        return null;
                    }
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public Object get(Object key) {
        Entry entry = getEntry(key);
        return (entry == null) ? null : entry.getValue();
    }

    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    public Set entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }
        return entrySet;
    }

    public static class Entry
        implements Map.Entry, Cloneable, java.io.Serializable {

        private static final boolean RED = false;
        private static final boolean BLACK = true;

        private Object key;
        private Object element;

        /**
         * The node color (RED, BLACK)
         */
        private boolean color;

        /**
         * Pointer to left child
         */
        private Entry left;

        /**
         * Pointer to right child
         */
        private Entry right;

        /**
         * Pointer to parent (null if root)
         */
        private Entry parent;

        /**
         * Make a new node with given element, null links, and BLACK color.
         * Normally only called to establish a new root.
         */
        public Entry(Object key, Object element) {
            this.key = key;
            this.element = element;
            this.color = BLACK;
        }

        /**
         * Return a new Entry with same element and color as self,
         * but with null links. (Since it is never OK to have
         * multiple identical links in a RB tree.)
         */
        protected Object clone() throws CloneNotSupportedException {
            Entry t = new Entry(key, element);
            t.color = color;
            return t;
        }

        public final Object getKey() {
            return key;
        }

        /**
         * return the element value
         */
        public final Object getValue() {
            return element;
        }

        /**
         * set the element value
         */
        public final Object setValue(Object v) {
            Object old = element;
            element = v;
            return old;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry)o;
            return eq(key, e.getKey()) && eq(element, e.getValue());
        }

        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^
                   (element == null ? 0 : element.hashCode());
        }

        public String toString() {
            return key + "=" + element;
        }
    }

    /**
     * Return the inorder successor, or null if no such
     */
    private static Entry successor(Entry e) {
        if (e.right != null) {
            for (e = e.right; e.left != null; e = e.left) {}
            return e;
        } else {
            Entry p = e.parent;
            while (p != null && e == p.right) {
                e = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Return the inorder predecessor, or null if no such
     */
    private static Entry predecessor(Entry e) {
        if (e.left != null) {
            for (e = e.left; e.right != null; e = e.right) {}
            return e;
        }
        else {
            Entry p = e.parent;
            while (p != null && e == p.left) {
                e = p;
                p = p.parent;
            }
            return p;
        }
    }

    private Entry getEntry(Object key) {
        Entry t = root;
        if (comparator != null) {
            for (;;) {
                if (t == null) return null;
                int diff = comparator.compare(key, t.key);
                if (diff == 0) return t;
                t = (diff < 0) ? t.left : t.right;
            }
        }
        else {
            Comparable c = (Comparable)key;
            for (;;) {
                if (t == null) return null;
                int diff = c.compareTo(t.key);
                if (diff == 0) return t;
                t = (diff < 0) ? t.left : t.right;
            }
        }
    }

    private Entry getHigherEntry(Object key) {
        Entry t = root;
        if (t == null) return null;
        for (;;) {
            int diff = compare(key, t.key, comparator);
            if (diff < 0) {
                if (t.left != null) t = t.left; else return t;
            }
            else {
                if (t.right != null) {
                    t = t.right;
                }
                else {
                    Entry parent = t.parent;
                    while (parent != null && t == parent.right) {
                        t = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
        }
    }

    private Entry getFirstEntry() {
        Entry e = root;
        if (e == null) return null;
        while (e.left != null) e = e.left;
        return e;
    }

    private Entry getLastEntry() {
        Entry e = root;
        if (e == null) return null;
        while (e.right != null) e = e.right;
        return e;
    }

    private Entry getCeilingEntry(Object key) {
        Entry e = root;
        if (e == null) return null;
        for (;;) {
            int diff = compare(key, e.key, comparator);
            if (diff < 0) {
                if (e.left != null) e = e.left; else return e;
            }
            else if (diff > 0) {
                if (e.right != null) {
                    e = e.right;
                }
                else {
                    Entry p = e.parent;
                    while (p != null && e == p.right) {
                        e = p;
                        p = p.parent;
                    }
                    return p;
                }
            }
            else return e;
        }
    }

    private Entry getLowerEntry(Object key) {
        Entry e = root;
        if (e == null) return null;
        for (;;) {
            int diff = compare(key, e.key, comparator);
            if (diff > 0) {
                if (e.right != null) e = e.right; else return e;
            }
            else {
                if (e.left != null) {
                    e = e.left;
                }
                else {
                    Entry p = e.parent;
                    while (p != null && e == p.left) {
                        e = p;
                        p = p.parent;
                    }
                    return p;
                }
            }
        }
    }

    private Entry getFloorEntry(Object key) {
        Entry e = root;
        if (e == null) return null;
        for (;;) {
            int diff = compare(key, e.key, comparator);
            if (diff > 0) {
                if (e.right != null) e = e.right; else return e;
            }
            else if (diff < 0) {
                if (e.left != null) {
                    e = e.left;
                }
                else {
                    Entry p = e.parent;
                    while (p != null && e == p.left) {
                        e = p;
                        p = p.parent;
                    }
                    return p;
                }
            }
            else return e;
        }
    }

    void buildFromSorted(Iterator itr, int size) {
        modCount++;
        this.size = size;
        // nodes at the bottom (unbalanced) level must be red
        int bottom = 0;
        for (int ssize = 1; ssize-1 < size; ssize <<= 1) bottom++;
        this.root = createFromSorted(itr, size, 0, bottom);
    }

    private static Entry createFromSorted(Iterator itr, int size,
                                          int level, int bottom) {
        level++;
        if (size == 0) return null;
        int leftSize = (size-1) >> 1;
        int rightSize = size-1-leftSize;
        Entry left = createFromSorted(itr, leftSize, level, bottom);
        Map.Entry orig = (Map.Entry)itr.next();
        Entry right = createFromSorted(itr, rightSize, level, bottom);
        Entry e = new Entry(orig.getKey(), orig.getValue());
        if (left != null) {
            e.left = left;
            left.parent = e;
        }
        if (right != null) {
            e.right = right;
            right.parent = e;
        }
        if (level == bottom) e.color = Entry.RED;
        return e;
    }

    /**
     * Delete the current node, and then rebalance the tree it is in
     * @param root the root of the current tree
     * @return the new root of the current tree. (Rebalancing
     * can change the root!)
     */
    private void delete(Entry e) {

        // handle case where we are only node
        if (e.left == null && e.right == null && e.parent == null) {
            root = null;
            size = 0;
            modCount++;
            return;
        }
        // if strictly internal, swap places with a successor
        if (e.left != null && e.right != null) {
            Entry s = successor(e);
            e.key = s.key;
            e.element = s.element;
            e = s;
        }

        // Start fixup at replacement node (normally a child).
        // But if no children, fake it by using self

        if (e.left == null && e.right == null) {

            if (e.color == Entry.BLACK)
                fixAfterDeletion(e);

            // Unlink  (Couldn't before since fixAfterDeletion needs parent ptr)

            if (e.parent != null) {
                if (e == e.parent.left)
                    e.parent.left = null;
                else if (e == e.parent.right)
                    e.parent.right = null;
                e.parent = null;
            }

        }
        else {
            Entry replacement = e.left;
            if (replacement == null)
                replacement = e.right;

            // link replacement to parent
            replacement.parent = e.parent;

            if (e.parent == null)
                root = replacement;
            else if (e == e.parent.left)
                e.parent.left = replacement;
            else
                e.parent.right = replacement;

            e.left = null;
            e.right = null;
            e.parent = null;

            // fix replacement
            if (e.color == Entry.BLACK)
                fixAfterDeletion(replacement);

        }

        size--;
        modCount++;
    }

    /**
     * Return color of node p, or BLACK if p is null
     * (In the CLR version, they use
     * a special dummy `nil' node for such purposes, but that doesn't
     * work well here, since it could lead to creating one such special
     * node per real node.)
     *
     */
    static boolean colorOf(Entry p) {
        return (p == null) ? Entry.BLACK : p.color;
    }

    /**
     * return parent of node p, or null if p is null
     */
    static Entry parentOf(Entry p) {
        return (p == null) ? null : p.parent;
    }

    /**
     * Set the color of node p, or do nothing if p is null
     */
    private static void setColor(Entry p, boolean c) {
        if (p != null) p.color = c;
    }

    /**
     * return left child of node p, or null if p is null
     */
    private static Entry leftOf(Entry p) {
        return (p == null) ? null : p.left;
    }

    /**
     * return right child of node p, or null if p is null
     */
    private static Entry rightOf(Entry p) {
        return (p == null) ? null : p.right;
    }

    /** From CLR */
    private final void rotateLeft(Entry e) {
        Entry r = e.right;
        e.right = r.left;
        if (r.left != null)
            r.left.parent = e;
        r.parent = e.parent;
        if (e.parent == null) root = r;
        else if (e.parent.left == e)
            e.parent.left = r;
        else
            e.parent.right = r;
        r.left = e;
        e.parent = r;
    }

    /** From CLR */
    private final void rotateRight(Entry e) {
        Entry l = e.left;
        e.left = l.right;
        if (l.right != null)
            l.right.parent = e;
        l.parent = e.parent;
        if (e.parent == null) root = l;
        else if (e.parent.right == e)
            e.parent.right = l;
        else
            e.parent.left = l;
        l.right = e;
        e.parent = l;
    }

    /** From CLR */
    private final void fixAfterInsertion(Entry e) {
        e.color = Entry.RED;
        Entry x = e;

        while (x != null && x != root && x.parent.color == Entry.RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == Entry.RED) {
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(y, Entry.BLACK);
                    setColor(parentOf(parentOf(x)), Entry.RED);
                    x = parentOf(parentOf(x));
                }
                else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(parentOf(parentOf(x)), Entry.RED);
                    if (parentOf(parentOf(x)) != null)
                        rotateRight(parentOf(parentOf(x)));
                }
            }
            else {
                Entry y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == Entry.RED) {
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(y, Entry.BLACK);
                    setColor(parentOf(parentOf(x)), Entry.RED);
                    x = parentOf(parentOf(x));
                }
                else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(parentOf(parentOf(x)), Entry.RED);
                    if (parentOf(parentOf(x)) != null)
                        rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = Entry.BLACK;
    }

    /** From CLR */
    private final Entry fixAfterDeletion(Entry e) {
        Entry x = e;
        while (x != root && colorOf(x) == Entry.BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry sib = rightOf(parentOf(x));
                if (colorOf(sib) == Entry.RED) {
                    setColor(sib, Entry.BLACK);
                    setColor(parentOf(x), Entry.RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }
                if (colorOf(leftOf(sib)) == Entry.BLACK &&
                    colorOf(rightOf(sib)) == Entry.BLACK) {
                    setColor(sib, Entry.RED);
                    x = parentOf(x);
                }
                else {
                    if (colorOf(rightOf(sib)) == Entry.BLACK) {
                        setColor(leftOf(sib), Entry.BLACK);
                        setColor(sib, Entry.RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(rightOf(sib), Entry.BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            }
            else {
                Entry sib = leftOf(parentOf(x));
                if (colorOf(sib) == Entry.RED) {
                    setColor(sib, Entry.BLACK);
                    setColor(parentOf(x), Entry.RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }
                if (colorOf(rightOf(sib)) == Entry.BLACK &&
                    colorOf(leftOf(sib)) == Entry.BLACK) {
                    setColor(sib, Entry.RED);
                    x = parentOf(x);
                }
                else {
                    if (colorOf(leftOf(sib)) == Entry.BLACK) {
                        setColor(rightOf(sib), Entry.BLACK);
                        setColor(sib, Entry.RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), Entry.BLACK);
                    setColor(leftOf(sib), Entry.BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }
        setColor(x, Entry.BLACK);
        return root;
    }

    private class BaseEntryIterator {
        Entry cursor;
        Entry lastRet;
        int expectedModCount;
        BaseEntryIterator(Entry cursor) {
            this.cursor = cursor;
            this.expectedModCount = modCount;
        }
        public boolean hasNext() {
            return (cursor != null);
        }
        Entry nextEntry() {
            Entry curr = cursor;
            if (curr == null) throw new NoSuchElementException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            cursor = successor(curr);
            lastRet = curr;
            return curr;
        }
        public void remove() {
            if (lastRet == null) throw new IllegalStateException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            // if removal strictly internal, it swaps places with a successor
            if (lastRet.left != null && lastRet.right != null) cursor = lastRet;
            delete(lastRet);
            lastRet = null;
            expectedModCount++;
        }
    }

    class EntryIterator extends BaseEntryIterator implements Iterator {
        EntryIterator(Entry cursor) { super(cursor); }
        public Object next() { return nextEntry(); }
    }

    class KeyIterator extends BaseEntryIterator implements Iterator {
        KeyIterator(Entry cursor) { super(cursor); }
        public Object next() { return nextEntry().key; }
    }

    class ValueIterator extends BaseEntryIterator implements Iterator {
        ValueIterator(Entry cursor) { super(cursor); }
        public Object next() { return nextEntry().element; }
    }

    private class BaseDescendingEntryIterator {
        Entry cursor;
        Entry lastRet;
        int expectedModCount;
        BaseDescendingEntryIterator(Entry cursor) {
            this.cursor = cursor;
            this.expectedModCount = modCount;
        }
        public boolean hasNext() {
            return (cursor != null);
        }
        Entry prevEntry() {
            Entry curr = cursor;
            if (curr == null) throw new NoSuchElementException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            cursor = predecessor(curr);
            lastRet = curr;
            return curr;
        }
        public void remove() {
            if (lastRet == null) throw new IllegalStateException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            // if removal strictly internal, it swaps places with a successor
            if (lastRet.left != null && lastRet.right != null) cursor = lastRet;
            delete(lastRet);
            lastRet = null;
            expectedModCount++;
        }
    }

    class DescendingEntryIterator extends BaseDescendingEntryIterator implements Iterator {
        DescendingEntryIterator(Entry cursor) { super(cursor); }
        public Object next() { return prevEntry(); }
    }

    class DescendingKeyIterator extends BaseDescendingEntryIterator implements Iterator {
        DescendingKeyIterator(Entry cursor) { super(cursor); }
        public Object next() { return prevEntry().key; }
    }

    class DescendingValueIterator extends BaseDescendingEntryIterator implements Iterator {
        DescendingValueIterator(Entry cursor) { super(cursor); }
        public Object next() { return prevEntry().element; }
    }

    private Entry getMatchingEntry(Object o) {
        if (!(o instanceof Map.Entry)) return null;
        Map.Entry e = (Map.Entry)o;
        Entry found = TreeMap.this.getEntry(e.getKey());
        return (found != null && eq(found.getValue(), e.getValue())) ? found : null;
    }

    class EntrySet extends AbstractSet {
        public int size() { return TreeMap.this.size(); }
        public boolean isEmpty() { return TreeMap.this.isEmpty(); }
        public void clear() { TreeMap.this.clear(); }

        public Iterator iterator() {
            return new EntryIterator(getFirstEntry());
        }

        public boolean contains(Object o) {
            return getMatchingEntry(o) != null;
        }

        public boolean remove(Object o) {
            Entry e = getMatchingEntry(o);
            if (e == null) return false;
            delete(e);
            return true;
        }
    }

    class DescendingEntrySet extends EntrySet {
        public Iterator iterator() {
            return new DescendingEntryIterator(getLastEntry());
        }
    }

    class ValueSet extends AbstractSet {
        public int size() { return TreeMap.this.size(); }
        public boolean isEmpty() { return TreeMap.this.isEmpty(); }
        public void clear() { TreeMap.this.clear(); }

        public boolean contains(Object o) {
            for (Entry e = getFirstEntry(); e != null; e = successor(e)) {
                if (eq(o, e.element)) return true;
            }
            return false;
        }

        public Iterator iterator() {
            return new ValueIterator(getFirstEntry());
        }

        public boolean remove(Object o) {
            for (Entry e = getFirstEntry(); e != null; e = successor(e)) {
                if (eq(o, e.element)) {
                    delete(e);
                    return true;
                }
            }
            return false;
        }
    }

    class KeySet extends AbstractSet {
        public int size() { return TreeMap.this.size(); }
        public boolean isEmpty() { return TreeMap.this.isEmpty(); }
        public void clear() { TreeMap.this.clear(); }

        public boolean contains(Object o) {
            return getEntry(o) != null;
        }

        public Iterator iterator() {
            return new KeyIterator(getFirstEntry());
        }

        public boolean remove(Object o) {
            Entry found = getEntry(o);
            if (found == null) return false;
            delete(found);
            return true;
        }
    }

    class DescendingKeySet extends KeySet {
        public Iterator iterator() {
            return new DescendingKeyIterator(getLastEntry());
        }
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    private static int compare(Object o1, Object o2, Comparator cmp) {
        return (cmp == null)
            ? ((Comparable)o1).compareTo(o2)
            : cmp.compare(o1, o2);
    }

    /**
     * @since 1.6
     */
    public Map.Entry lowerEntry(Object key) {
        Map.Entry e = getLowerEntry(key);
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Object lowerKey(Object key) {
        Map.Entry e = getLowerEntry(key);
        return (e == null) ? null : e.getKey();
    }

    /**
     * @since 1.6
     */
    public Map.Entry floorEntry(Object key) {
        Entry e = getFloorEntry(key);
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Object floorKey(Object key) {
        Entry e = getFloorEntry(key);
        return (e == null) ? null : e.key;
    }

    /**
     * @since 1.6
     */
    public Map.Entry ceilingEntry(Object key) {
        Entry e = getCeilingEntry(key);
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Object ceilingKey(Object key) {
        Entry e = getCeilingEntry(key);
        return (e == null) ? null : e.key;
    }

    /**
     * @since 1.6
     */
    public Map.Entry higherEntry(Object key) {
        Entry e = getHigherEntry(key);
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Object higherKey(Object key) {
        Entry e = getHigherEntry(key);
        return (e == null) ? null : e.key;
    }

    /**
     * @since 1.6
     */
    public Map.Entry firstEntry() {
        Entry e = getFirstEntry();
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Map.Entry lastEntry() {
        Entry e = getLastEntry();
        return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
    }

    /**
     * @since 1.6
     */
    public Map.Entry pollFirstEntry() {
        Entry e = getFirstEntry();
        if (e == null) return null;
        Map.Entry res = new AbstractMap.SimpleImmutableEntry(e);
        delete(e);
        return res;
    }

    /**
     * @since 1.6
     */
    public Map.Entry pollLastEntry() {
        Entry e = getLastEntry();
        if (e == null) return null;
        Map.Entry res = new AbstractMap.SimpleImmutableEntry(e);
        delete(e);
        return res;
    }

    public Set descendingKeySet() {
        if (descendingKeySet == null) {
            descendingKeySet = new DescendingKeySet();
        }
        return descendingKeySet;
    }

    public Set descendingEntrySet() {
        if (descendingEntrySet == null) {
            descendingEntrySet = new DescendingEntrySet();
        }
        return descendingEntrySet;
    }

    public NavigableMap navigableSubMap(Object fromKey, Object toKey) {
        return new SubMap(fromKey, toKey);
    }

    public NavigableMap navigableHeadMap(Object toKey) {
        return new SubMap(null, toKey);
    }

    public NavigableMap navigableTailMap(Object fromKey) {
        return new SubMap(fromKey, null);
    }

    public Comparator comparator() {
        return comparator;
    }

    public SortedMap subMap(Object fromKey, Object toKey) {
        return navigableSubMap(fromKey, toKey);
    }

    public SortedMap headMap(Object toKey) {
        return navigableHeadMap(toKey);
    }

    public SortedMap tailMap(Object fromKey) {
        return navigableTailMap(fromKey);
    }

    public Object firstKey() {
        Entry e = getFirstEntry();
        return (e == null) ? null : e.key;
    }

    public Object lastKey() {
        Entry e = getLastEntry();
        return (e == null) ? null : e.key;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean containsValue(Object value) {
        if (root == null) return false;
        return (value == null) ? containsNull(root) : containsValue(root, value);
    }

    private static boolean containsNull(Entry e) {
        if (e.element == null) return true;
        if (e.left != null && containsNull(e.left)) return true;
        if (e.right != null && containsNull(e.right)) return true;
        return false;
    }

    private static boolean containsValue(Entry e, Object val) {
        if (val.equals(e.element)) return true;
        if (e.left != null && containsValue(e.left, val)) return true;
        if (e.right != null && containsValue(e.right, val)) return true;
        return false;
    }

    public Object remove(Object key) {
        Entry e = getEntry(key);
        if (e == null) return null;
        Object old = e.getValue();
        delete(e);
        return old;
    }

    public void putAll(Map map) {
        if (map instanceof SortedMap) {
            SortedMap smap = (SortedMap)map;
            if (eq(this.comparator, smap.comparator())) {
                this.buildFromSorted(smap.entrySet().iterator(), map.size());
                return;
            }
        }
        // not a sorted map, or comparator mismatch
        super.putAll(map);
    }

//    public Set keySet() {
//        if (keySet == null) {
//            keySet = new KeySet();
//        }
//        return keySet;
//    }
//
//    public Collection values() {
//        if (valueSet == null) {
//            valueSet = new ValueSet();
//        }
//        return valueSet;
//    }
//
    private class SubMap extends AbstractMap implements NavigableMap, Serializable {

        private static final long serialVersionUID = -6520786458950516097L;

        final Object fromKey, toKey;
        transient int cachedSize = -1, cacheVersion;
        transient Set entrySet;
        transient Set descendingEntrySet;
        transient Set descendingKeySet;

        SubMap(Object fromKey, Object toKey) {
            if (fromKey != null && toKey != null && compare(fromKey, toKey, comparator) > 0) {
                throw new IllegalArgumentException("fromKey > toKey");
            }
            this.fromKey = fromKey;
            this.toKey = toKey;
        }

        private boolean tooHigh(Object key) {
            return (toKey != null && compare(key, toKey, comparator) >= 0);
        }

        private boolean tooLow(Object key) {
            return (fromKey != null && compare(key, fromKey, comparator) < 0);
        }

        private TreeMap.Entry lower(Object key) {
            TreeMap.Entry e = tooHigh(key) ? getLowerEntry(toKey) : getLowerEntry(key);
            return (e == null || tooLow(e.key)) ? null : e;
        }

        public Map.Entry lowerEntry(Object key) {
            TreeMap.Entry e = lower(key);
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object lowerKey(Object key) {
            TreeMap.Entry e = lower(key);
            return (e == null) ? null : e.key;
        }

        private TreeMap.Entry floor(Object key) {
            TreeMap.Entry e = tooHigh(key) ? getLowerEntry(toKey) : getFloorEntry(key);
            return (e == null || tooLow(e.key)) ? null : e;
        }

        public Map.Entry floorEntry(Object key) {
            TreeMap.Entry e = floor(key);
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object floorKey(Object key) {
            TreeMap.Entry e = floor(key);
            return (e == null) ? null : e.key;
        }

        private TreeMap.Entry ceiling(Object key) {
            TreeMap.Entry e = tooLow(key) ? getCeilingEntry(fromKey) : getCeilingEntry(key);
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        public Map.Entry ceilingEntry(Object key) {
            TreeMap.Entry e = ceiling(key);
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object ceilingKey(Object key) {
            TreeMap.Entry e = ceiling(key);
            return (e == null) ? null : e.key;
        }

        private TreeMap.Entry higher(Object key) {
            TreeMap.Entry e = tooLow(key) ? getCeilingEntry(fromKey) : getHigherEntry(key);
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        public Map.Entry higherEntry(Object key) {
            TreeMap.Entry e = higher(key);
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object higherKey(Object key) {
            TreeMap.Entry e = higher(key);
            return (e == null) ? null : e.key;
        }

        private TreeMap.Entry first() {
            TreeMap.Entry e = (fromKey != null) ? getCeilingEntry(fromKey) : getFirstEntry();
            return (e == null || tooHigh(e.key)) ? null : e;
        }

        public Map.Entry firstEntry() {
            TreeMap.Entry e = first();
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object firstKey() {
            TreeMap.Entry e = first();
            return (e == null) ? null : e.key;
        }

        private TreeMap.Entry last() {
            TreeMap.Entry e = (toKey != null) ? getLowerEntry(toKey) : getLastEntry();
            return (e == null || tooLow(e.key)) ? null : e;
        }

        public Map.Entry lastEntry() {
            TreeMap.Entry e = last();
            return (e == null) ? null : new AbstractMap.SimpleImmutableEntry(e);
        }

        public Object lastKey() {
            TreeMap.Entry e = last();
            return (e == null) ? null : e.key;
        }

        public Map.Entry pollFirstEntry() {
            TreeMap.Entry e = first();
            if (e == null) return null;
            Map.Entry result = new SimpleImmutableEntry(e);
            delete(e);
            return result;
        }

        public java.util.Map.Entry pollLastEntry() {
            TreeMap.Entry e = last();
            if (e == null) return null;
            Map.Entry result = new SimpleImmutableEntry(e);
            delete(e);
            return result;
        }

        public Set descendingKeySet() {
            if (descendingKeySet == null) descendingKeySet = new SubDescendingKeySet();
            return descendingKeySet;
        }

        public Set descendingEntrySet() {
            if (descendingEntrySet == null) descendingEntrySet = new SubDescendingEntrySet();
            return descendingEntrySet;
        }

        public NavigableMap navigableSubMap(Object fromKey, Object toKey) {
            if (this.fromKey != null && compare(fromKey, this.fromKey, comparator) < 0) {
                fromKey = this.fromKey;
            }
            if (this.toKey != null && compare(toKey, this.toKey, comparator) > 0) {
                toKey = this.toKey;
            }
            return new SubMap(fromKey, toKey);
        }

        public NavigableMap navigableHeadMap(Object toKey) {
            if (this.toKey != null && compare(toKey, this.toKey, comparator) > 0) {
                toKey = this.toKey;
            }
            return new SubMap(this.fromKey, toKey);
        }

        public NavigableMap navigableTailMap(Object fromKey) {
            if (this.fromKey != null && compare(fromKey, this.fromKey, comparator) < 0) {
                fromKey = this.fromKey;
            }
            return new SubMap(fromKey, this.toKey);
        }

        public Comparator comparator() {
            return comparator;
        }

        public SortedMap subMap(Object fromKey, Object toKey) {
            return navigableSubMap(fromKey, toKey);
        }

        public SortedMap headMap(Object toKey) {
            return navigableHeadMap(toKey);
        }

        public SortedMap tailMap(Object fromKey) {
            return navigableTailMap(fromKey);
        }

        public int size() {
            if (cachedSize < 0 || cacheVersion != modCount) {
                cachedSize = recalculateSize();
                cacheVersion = modCount;
            }
            return cachedSize;
        }

        private int recalculateSize() {
            Object terminalKey;
            if (toKey != null) {
                TreeMap.Entry terminator = getFloorEntry(toKey);
                if (terminator == null) return 0;
                terminalKey = terminator.key;
            }
            else {
                terminalKey = null;
            }

            int size = 0;
            for (TreeMap.Entry e = first(); e != null && e.key != terminalKey;
                 e = successor(e)) {
                size++;
            }
            return size;
        }

        public boolean isEmpty() {
            return first() == null;
        }

        public boolean containsKey(Object key) {
            return (!tooLow(key) && !tooHigh(key) && TreeMap.this.containsKey(key));
        }

        public Object get(Object key) {
            if (tooLow(key) || tooHigh(key)) return null;
            else return TreeMap.this.get(key);
        }

        public Object put(Object key, Object value) {
            if (tooLow(key) || tooHigh(key))
                throw new IllegalArgumentException("Key out of range");
            return TreeMap.this.put(key, value);
        }

        public Object remove(Object key) {
            if (tooLow(key) || tooHigh(key)) return null;
            return TreeMap.this.remove(key);
        }

        public Set entrySet() {
            if (entrySet == null) entrySet = new SubEntrySet();
            return entrySet;
        }

        private TreeMap.Entry getMatchingSubEntry(Object o) {
            if (!(o instanceof Map.Entry)) return null;
            Map.Entry e = (Map.Entry)o;
            Object key = e.getKey();
            if (tooLow(key) || tooHigh(key)) return null;
            TreeMap.Entry found = getEntry(key);
            return (found != null && eq(found.getValue(), e.getValue())) ? found : null;
        }

        class SubEntrySet extends AbstractSet {
            public int size() { return SubMap.this.size(); }
            public boolean isEmpty() { return SubMap.this.isEmpty(); }

            public Iterator iterator() {
                TreeMap.Entry terminator = (toKey != null) ? getFloorEntry(toKey) : null;
                return new SubEntryIterator(first(), terminator);
            }

            public boolean contains(Object o) {
                return getMatchingSubEntry(o) != null;
            }

            public boolean remove(Object o) {
                TreeMap.Entry e = getMatchingSubEntry(o);
                if (e == null) return false;
                delete(e);
                return true;
            }
        }

        class SubDescendingEntrySet extends SubEntrySet {
            public Iterator iterator() {
                TreeMap.Entry terminator = (fromKey != null) ? getLowerEntry(fromKey) : null;
                return new SubDescendingEntryIterator(last(), terminator);
            }
        }

        class SubDescendingKeySet extends AbstractSet {
            public int size() { return SubMap.this.size(); }
            public boolean isEmpty() { return SubMap.this.isEmpty(); }

            public Iterator iterator() {
                TreeMap.Entry terminator = (fromKey != null) ? getLowerEntry(fromKey) : null;
                final Iterator itr = new SubDescendingEntryIterator(last(), terminator);
                return new Iterator() {
                    public boolean hasNext() { return itr.hasNext(); }
                    public Object next() { return ((Map.Entry)itr.next()).getKey(); }
                    public void remove() { itr.remove(); }
                };
            }

            public boolean contains(Object o) {
                return SubMap.this.containsKey(o);
            }

            public boolean remove(Object o) {
                return SubMap.this.remove(o) != null;
            }
        }
    }

    class SubEntryIterator extends BaseEntryIterator implements Iterator {
        final Object terminalKey;
        SubEntryIterator(TreeMap.Entry cursor, TreeMap.Entry terminator) {
            super(cursor);
            this.terminalKey = terminator == null ? null : terminator.key;
        }
        public boolean hasNext() {
            return cursor != null && cursor.key != terminalKey;
        }
        public Object next() {
            Entry curr = cursor;
            if (curr == null || cursor.key == terminalKey)
                throw new NoSuchElementException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            cursor = successor(curr);
            lastRet = curr;
            return curr;
        }
    }

    class SubDescendingEntryIterator extends BaseEntryIterator implements Iterator {
        final Object terminalKey;
        SubDescendingEntryIterator(TreeMap.Entry cursor, TreeMap.Entry terminator) {
            super(cursor);
            this.terminalKey = terminator == null ? null : terminator.key;
        }
        public boolean hasNext() {
            return cursor != null && cursor.key != terminalKey;
        }
        public Object next() {
            Entry curr = cursor;
            if (curr == null || cursor.key == terminalKey)
                throw new NoSuchElementException();
            if (expectedModCount != modCount)
                throw new ConcurrentModificationException();
            cursor = predecessor(curr);
            lastRet = curr;
            return curr;
        }
    }

    // serialization

    static class IteratorIOException extends RuntimeException {
        IteratorIOException(java.io.IOException e) {
            super(e);
        }
        java.io.IOException getException() {
            return (java.io.IOException)getCause();
        }
    }

    static class IteratorNoClassException extends RuntimeException {
        IteratorNoClassException(ClassNotFoundException e) {
            super(e);
        }
        ClassNotFoundException getException() {
            return (ClassNotFoundException)getCause();
        }
    }

    static class IOIterator implements Iterator {
        final java.io.ObjectInputStream ois;
        int remaining;
        IOIterator(java.io.ObjectInputStream ois, int remaining) {
            this.ois = ois;
            this.remaining = remaining;
        }
        public boolean hasNext() {
            return remaining > 0;
        }
        public Object next() {
            if (remaining <= 0) throw new NoSuchElementException();
            remaining--;
            try {
                return new AbstractMap.SimpleImmutableEntry(ois.readObject(),
                                                            ois.readObject());
            }
            catch (java.io.IOException e) { throw new IteratorIOException(e); }
            catch (ClassNotFoundException e) { throw new IteratorNoClassException(e); }
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size);
        for (Entry e = getFirstEntry(); e != null; e = successor(e)) {
            out.writeObject(e.key);
            out.writeObject(e.element);
        }
    }

    private void readObject(ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        int size = in.readInt();
        try {
            buildFromSorted(new IOIterator(in, size), size);
        }
        catch (IteratorIOException e) {
            throw e.getException();
        }
        catch (IteratorNoClassException e) {
            throw e.getException();
        }
    }
}
