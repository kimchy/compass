/*
 * Written by Dawid Kurzyniec, on the basis of public specifications and
 * public domain sources from JSR 166 and the Doug Lea's collections package,
 * and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain.
 */

package org.compass.core.util.backport.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;

public class TreeSet extends AbstractSet
                     implements NavigableSet, Cloneable, Serializable {

    private static final long serialVersionUID = -2479143000061671589L;
    private static final Object PRESENT = new Object();

    private transient NavigableMap map;

    public TreeSet() {
        map = new TreeMap();
    }

    public TreeSet(Comparator comparator) {
        map = new TreeMap(comparator);
    }

    public TreeSet(Collection c) {
        map = new TreeMap();
        addAll(c);
    }

    public TreeSet(SortedSet s) {
        map = new TreeMap(s.comparator());
        addAll(s);
    }

    private TreeSet(NavigableMap map) {
        this.map = map;
    }

    public Object lower(Object e) {
        return map.lowerKey(e);
    }

    public Object floor(Object e) {
        return map.floorKey(e);
    }

    public Object ceiling(Object e) {
        return map.ceilingKey(e);
    }

    public Object higher(Object e) {
        return map.higherKey(e);
    }

    public Object pollFirst() {
        Map.Entry e = map.pollFirstEntry();
        return (e != null) ? e.getKey() : null;
    }

    public Object pollLast() {
        Map.Entry e = map.pollLastEntry();
        return (e != null) ? e.getKey() : null;
    }

    public Iterator iterator() {
        return map.keySet().iterator();
    }

    public Iterator descendingIterator() {
        return map.descendingKeySet().iterator();
    }

    public SortedSet subSet(Object fromElement, Object toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    public SortedSet headSet(Object toElement) {
        return headSet(toElement, false);
    }

    public SortedSet tailSet(Object fromElement) {
        return tailSet(fromElement, true);
    }

    public NavigableSet subSet(Object fromElement, boolean fromInclusive,
                               Object toElement,   boolean toInclusive) {
        return new TreeSet(map.subMap(fromElement, fromInclusive,
                                      toElement,   toInclusive));
    }

    public NavigableSet headSet(Object toElement, boolean toInclusive) {
        return new TreeSet(map.headMap(toElement, toInclusive));
    }

    public NavigableSet tailSet(Object fromElement, boolean fromInclusive) {
        return new TreeSet(map.tailMap(fromElement, fromInclusive));
    }

    public NavigableSet descendingSet() {
        return new TreeSet(map.descendingMap());
    }

    public Comparator comparator() {
        return map.comparator();
    }

    public Object first() {
        return map.firstKey();
    }

    public Object last() {
        return map.lastKey();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public Object[] toArray() {
        return map.keySet().toArray();
    }

    public Object[] toArray(Object[] a) {
        return map.keySet().toArray(a);
    }

    public boolean add(Object o) {
        return map.put(o, PRESENT) == null;
    }

    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    public boolean addAll(Collection c) {
        if (map.size() == 0 && c.size() > 0 &&
            c instanceof SortedSet && map instanceof TreeMap &&
            eq(((SortedSet)c).comparator(), this.comparator()))
        {
            ((TreeMap)map).buildFromSorted(new MapIterator(c.iterator()), c.size());
            return true;
        }
        else {
            return super.addAll(c);
        }
    }

    public void clear() {
        map.clear();
    }

    private static class MapIterator implements Iterator {
        final Iterator itr;
        MapIterator(Iterator itr) { this.itr = itr; }
        public boolean hasNext() { return itr.hasNext(); }
        public Object next() {
            return new AbstractMap.SimpleImmutableEntry(itr.next(), PRESENT);
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    public Object clone() {
        TreeSet clone;
        try { clone = (TreeSet)super.clone(); }
        catch (CloneNotSupportedException e) { throw new InternalError(); }
        // deep-copy
        clone.map = new TreeMap(map);
        return clone;
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    static class IOIterator extends TreeMap.IOIterator {
        IOIterator(ObjectInputStream in, int remaining) {
            super(in, remaining);
        }
        public Object next() {
            if (remaining <= 0) throw new NoSuchElementException();
            remaining--;
            try {
                return new AbstractMap.SimpleImmutableEntry(ois.readObject(),
                                                            PRESENT);
            }
            catch (IOException e) { throw new TreeMap.IteratorIOException(e); }
            catch (ClassNotFoundException e) { throw new TreeMap.IteratorNoClassException(e); }
        }
        public void remove() { throw new UnsupportedOperationException(); }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(map.comparator());
        out.writeInt(map.size());
        for (Iterator itr = map.keySet().iterator(); itr.hasNext(); ) {
            out.writeObject(itr.next());
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        Comparator comparator = (Comparator)in.readObject();
        TreeMap map = new TreeMap(comparator);
        int size = in.readInt();
        try {
            map.buildFromSorted(new IOIterator(in, size), size);
            this.map = map;
        }
        catch (TreeMap.IteratorIOException e) {
            throw e.getException();
        }
        catch (TreeMap.IteratorNoClassException e) {
            throw e.getException();
        }
    }
}
