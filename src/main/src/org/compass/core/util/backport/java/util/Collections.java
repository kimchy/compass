/*
 * Written by Dawid Kurzyniec, on the basis of public specifications and
 * public domain sources from JSR 166, and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain.
 */

package org.compass.core.util.backport.java.util;

import java.io.Serializable;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.io.*;

/**
 * Augments {@link java.util.Collections} with methods added in Java 5.0
 * and higher. Adds support for dynamically typesafe collection wrappers,
 * and several utility methods.
 *
 * @see java.util.Collections
 */
public class Collections {

    private Collections() {}

    public static void sort(List list) {
        java.util.Collections.sort(list);
    }

    public static void sort(List list, Comparator c) {
        java.util.Collections.sort(list, c);
    }

    public static int binarySearch(List list, Object key) {
        return java.util.Collections.binarySearch(list, key);
    }

    public static int binarySearch(List list, Object key, Comparator c) {
        return java.util.Collections.binarySearch(list, key, c);
    }

    public static void reverse(List list) {
        java.util.Collections.reverse(list);
    }

    public static void shuffle(List list) {
        java.util.Collections.shuffle(list);
    }

    public static void shuffle(List list, Random rnd) {
        java.util.Collections.shuffle(list, rnd);
    }

    public static void swap(List list, int i, int j) {
        java.util.Collections.swap(list, i, i);
    }

    public static void fill(List list, Object obj) {
        java.util.Collections.fill(list, obj);
    }

    public static void copy(List dest, List src) {
        java.util.Collections.copy(dest, src);
    }

    public static Object min(Collection coll) {
        return java.util.Collections.min(coll);
    }

    public static Object min(Collection coll, Comparator comp) {
        return java.util.Collections.min(coll, comp);
    }

    public static Object max(Collection coll) {
        return java.util.Collections.max(coll);
    }

    public static Object max(Collection coll, Comparator comp) {
        return java.util.Collections.max(coll, comp);
    }

    public static void rotate(List list, int distance) {
        java.util.Collections.rotate(list, distance);
    }

    public static boolean replaceAll(List list, Object oldVal, Object newVal) {
        return java.util.Collections.replaceAll(list, oldVal, newVal);
    }

    public static int indexOfSubList(List source, List target) {
        return java.util.Collections.indexOfSubList(source, target);
    }

    public static int lastIndexOfSubList(List source, List target) {
        return java.util.Collections.lastIndexOfSubList(source, target);
    }

    // unmodifiable views

    public static Collection unmodifiableCollection(Collection c) {
        return java.util.Collections.unmodifiableCollection(c);
    }

    public static Set unmodifiableSet(Set s) {
        return java.util.Collections.unmodifiableSet(s);
    }

    public static SortedSet unmodifiableSortedSet(SortedSet s) {
        return java.util.Collections.unmodifiableSortedSet(s);
    }

    public static List unmodifiableList(List l) {
        return java.util.Collections.unmodifiableList(l);
    }

    public static Map unmodifiableMap(Map m) {
        return java.util.Collections.unmodifiableMap(m);
    }

    public static SortedMap unmodifiableSortedMap(SortedMap m) {
        return java.util.Collections.unmodifiableSortedMap(m);
    }

    // synchronized views

    public static Collection synchronizedCollection(Collection c) {
        return java.util.Collections.synchronizedCollection(c);
    }

    public static Set synchronizedSet(Set s) {
        return java.util.Collections.synchronizedSet(s);
    }

    public static SortedSet synchronizedSortedSet(SortedSet s) {
        return java.util.Collections.synchronizedSortedSet(s);
    }

    public static List synchronizedList(List l) {
        return java.util.Collections.synchronizedList(l);
    }

    public static Map synchronizedMap(Map m) {
        return java.util.Collections.synchronizedMap(m);
    }

    public static SortedMap synchronizedSortedMap(SortedMap m) {
        return java.util.Collections.synchronizedSortedMap(m);
    }

    // checked views

    public static Collection checkedCollection(Collection c, Class type) {
        return new CheckedCollection(c, type);
    }

    public static Set checkedSet(Set s, Class type) {
        return new CheckedSet(s, type);
    }

    public static SortedSet checkedSortedSet(SortedSet s, Class type) {
        return new CheckedSortedSet(s, type);
    }

    public static List checkedList(List l, Class type) {
        return new CheckedList(l, type);
    }

    public static Map checkedMap(Map m, Class keyType, Class valueType) {
        return new CheckedMap(m, keyType, valueType);
    }

    public static SortedMap checkedSortedMap(SortedMap m, Class keyType, Class valueType) {
        return new CheckedSortedMap(m, keyType, valueType);
    }

    // empty views

    public static Set emptySet() {
        return java.util.Collections.EMPTY_SET;
    }

    public static List emptyList() {
        return java.util.Collections.EMPTY_LIST;
    }

    public static Map emptyMap() {
        return java.util.Collections.EMPTY_MAP;
    }

    // singleton views

    public static Set singleton(Object o) {
        return java.util.Collections.singleton(o);
    }

    public static List singletonList(Object o) {
        return java.util.Collections.singletonList(o);
    }

    public static Map singletonMap(Object key, Object value) {
        return java.util.Collections.singletonMap(key, value);
    }

    // other utils

    public static List nCopies(int n, Object o) {
        return java.util.Collections.nCopies(n, o);
    }

    public static Comparator reverseOrder() {
        return java.util.Collections.reverseOrder();
    }

    public static Comparator reverseOrder(Comparator cmp) {
        return (cmp instanceof ReverseComparator)
            ? ((ReverseComparator)cmp).cmp
            : new ReverseComparator(cmp);
    }

    public static Enumeration enumeration(Collection c) {
        return java.util.Collections.enumeration(c);
    }

    public static ArrayList list(Enumeration e) {
        return java.util.Collections.list(e);
    }

    public static int frequency(Collection c, Object o) {
        int freq = 0;
        if (o == null) {
            for (Iterator itr = c.iterator(); itr.hasNext();) {
                if (itr.next() == null) freq++;
            }
        }
        else {
            for (Iterator itr = c.iterator(); itr.hasNext();) {
                if (o.equals(itr.next())) freq++;
            }
        }
        return freq;
    }

    public static boolean disjoint(Collection a, Collection b) {
        // set.contains() is usually faster than for other collections
        if (a instanceof Set && (!(b instanceof Set) || a.size() < b.size())) {
            Collection tmp = a;
            a = b;
            b = tmp;
        }
        for (Iterator itr = a.iterator(); itr.hasNext();) {
            if (b.contains(itr.next())) return false;
        }
        return true;
    }

    public static boolean addAll(Collection c, Object[] a) {
        boolean modified = false;
        for (int i=0; i<a.length; i++) {
            modified |= c.add(a[i]);
        }
        return modified;
    }

    public static Set newSetFromMap(Map map) {
        return new SetFromMap(map);
    }

    public static Queue asLifoQueue(Deque deque) {
        return new AsLifoQueue(deque);
    }



    // Checked collections
    private static class CheckedCollection implements Collection, Serializable {

        final Collection coll;
        final Class type;
        transient Object[] emptyArr;
        CheckedCollection(Collection coll, Class type) {
            if (coll == null || type == null) throw new NullPointerException();
            this.coll = coll;
            this.type = type;
        }

        void typeCheck(Object obj) {
            if (!type.isInstance(obj)) {
                throw new ClassCastException(
                    "Attempted to insert an element of type " + obj.getClass().getName() +
                    " to a collection of type " + type.getName());
            }
        }

        public int size()                        { return coll.size(); }
        public void clear()                      { coll.clear(); }
        public boolean isEmpty()                 { return coll.isEmpty(); }
        public Object[] toArray()                { return coll.toArray(); }
        public Object[] toArray(Object[] a)      { return coll.toArray(a); }
        public boolean contains(Object o)        { return coll.contains(o); }
        public boolean remove(Object o)          { return coll.remove(o); }
        public boolean containsAll(Collection c) { return coll.containsAll(c); }
        public boolean removeAll(Collection c)   { return coll.removeAll(c); }
        public boolean retainAll(Collection c)   { return coll.retainAll(c); }
        public String toString()                 { return coll.toString(); }

        public boolean add(Object o) {
            typeCheck(o);
            return coll.add(o);
        }

        public boolean addAll(Collection c) {
            Object[] checked;
            try {
                checked = c.toArray(getEmptyArr());
            }
            catch (ArrayStoreException e) {
                throw new ClassCastException(
                    "Attempted to insert an element of invalid type " +
                    " to a collection of type " + type.getName());
            }
            return coll.addAll(Arrays.asList(checked));
        }

        public Iterator iterator() {
            return new Itr(coll.iterator());
        }

        protected Object[] getEmptyArr() {
            if (emptyArr == null) emptyArr = (Object[])Array.newInstance(type, 0);
            return emptyArr;
        }

        class Itr implements Iterator {
            final Iterator itr;
            Itr(Iterator itr)            { this.itr = itr; }
            public boolean hasNext()     { return itr.hasNext(); }
            public Object next()         { return itr.next(); }
            public void remove()         { itr.remove(); }
        }
    }

    private static class CheckedList extends CheckedCollection
        implements List, Serializable
    {
        final List list;
        CheckedList(List list, Class type) {
            super(list, type);
            this.list = list;
        }
        public Object get(int index)     { return list.get(index); }
        public Object remove(int index)  { return list.remove(index); }
        public int indexOf(Object o)     { return list.indexOf(o); }
        public int lastIndexOf(Object o) { return list.lastIndexOf(o); }

        public int hashCode()            { return list.hashCode(); }
        public boolean equals(Object o)  { return list.equals(o); }

        public Object set(int index, Object element) {
            typeCheck(element);
            return list.set(index, element);
        }

        public void add(int index, Object element) {
            typeCheck(element);
            list.add(index, element);
        }

        public boolean addAll(int index, Collection c) {
            Object[] checked;
            try {
                checked = c.toArray(getEmptyArr());
            }
            catch (ArrayStoreException e) {
                throw new ClassCastException(
                    "Attempted to insert an element of invalid type " +
                    " to a list of type " + type.getName());
            }

            return list.addAll(index, Arrays.asList(checked));
        }

        public List subList(int fromIndex, int toIndex) {
            return new CheckedList(list.subList(fromIndex, toIndex), type);
        }

        public ListIterator listIterator() {
            return new ListItr(list.listIterator());
        }

        public ListIterator listIterator(int index) {
            return new ListItr(list.listIterator(index));
        }

        private class ListItr implements ListIterator {
            final ListIterator itr;
            ListItr(ListIterator itr)    { this.itr = itr; }
            public boolean hasNext()     { return itr.hasNext(); }
            public boolean hasPrevious() { return itr.hasPrevious(); }
            public int nextIndex()       { return itr.nextIndex(); }
            public int previousIndex()   { return itr.previousIndex(); }
            public Object next()         { return itr.next(); }
            public Object previous()     { return itr.previous(); }
            public void remove()         { itr.remove(); }

            public void set(Object element) {
                typeCheck(element);
                itr.set(element);
            }

            public void add(Object element) {
                typeCheck(element);
                itr.add(element);
            }
        }
    }

    private static class CheckedSet extends CheckedCollection
        implements Set, Serializable
    {
        CheckedSet(Set set, Class type) {
            super(set, type);
        }

        public int hashCode()            { return coll.hashCode(); }
        public boolean equals(Object o)  { return coll.equals(o); }
    }

    private static class CheckedSortedSet extends CheckedSet
        implements SortedSet, Serializable
    {
        final SortedSet set;
        CheckedSortedSet(SortedSet set, Class type) {
            super(set, type);
            this.set = set;
        }
        public Object first()          { return set.first(); }
        public Object last()           { return set.last(); }
        public Comparator comparator() { return set.comparator(); }

        public SortedSet headSet(Object toElement) {
            return new CheckedSortedSet(set.headSet(toElement), type);
        }

        public SortedSet tailSet(Object fromElement) {
            return new CheckedSortedSet(set.tailSet(fromElement), type);
        }

        public SortedSet subSet(Object fromElement, Object toElement) {
            return new CheckedSortedSet(set.subSet(fromElement, toElement), type);
        }
    }

//    public static NavigableSet checkedNavigableSet(NavigableSet set, Class type) {
//        return new CheckedNavigableSet(set, type);
//    }
//
//    private static class CheckedNavigableSet extends CheckedSortedSet
//        implements NavigableSet, Serializable
//    {
//        final NavigableSet set;
//        CheckedNavigableSet(NavigableSet set, Class type) {
//            super(set, type);
//            this.set = set;
//        }
//        public Object lower(Object e)   { return set.lower(e); }
//        public Object floor(Object e)   { return set.floor(e); }
//        public Object ceiling(Object e) { return set.ceiling(e); }
//        public Object higher(Object e)  { return set.higher(e); }
//        public Object pollFirst()       { return set.pollFirst(); }
//        public Object pollLast()        { return set.pollLast(); }
//
//        public Iterator descendingIterator() {
//            return new Itr(set.descendingIterator());
//        }
//
//        public NavigableSet navigableSubSet(Object fromElement,
//                                            Object toElement) {
//            return new CheckedNavigableSet(
//                set.navigableSubSet(fromElement, toElement), type);
//        }
//
//        public NavigableSet navigableHeadSet(Object toElement) {
//            return new CheckedNavigableSet(set.navigableHeadSet(toElement), type);
//        }
//
//        public NavigableSet navigableTailSet(Object fromElement) {
//            return new CheckedNavigableSet(set.navigableTailSet(fromElement), type);
//        }
//    }

    private static class CheckedMap implements Map {
        final Map map;
        final Class keyType, valueType;
        transient Set entrySet;

        CheckedMap(Map map, Class keyType, Class valueType) {
            if (map == null || keyType == null || valueType == null) {
                throw new NullPointerException();
            }
            this.map = map;
            this.keyType = keyType;
            this.valueType = valueType;
        }

        private void typeCheckKey(Object key) {
            if (keyType.isInstance(key)) {
                throw new ClassCastException(
                    "Attempted to use a key of type " + key.getClass().getName() +
                    " with a map with keys of type " + keyType.getName());
            }
        }

        private void typeCheckValue(Object value) {
            if (valueType.isInstance(value)) {
                throw new ClassCastException(
                    "Attempted to use a value of type " + value.getClass().getName() +
                    " with a map with values of type " + valueType.getName());
            }
        }

        public int hashCode()                  { return map.hashCode(); }
        public boolean equals(Object o)        { return map.equals(o); }

        public int size()                      { return map.size(); }
        public void clear()                    { map.clear(); }
        public boolean isEmpty()               { return map.isEmpty(); }
        public boolean containsKey(Object key) { return map.containsKey(key); }
        public boolean containsValue(Object value)
                                               { return map.containsValue(value); }

        // key and value sets do not support additions
        public Collection values()             { return map.values(); }
        public Set keySet()                    { return map.keySet(); }

        private transient Object[] emptyKeyArray;
        private transient Object[] emptyValueArray;

        public void putAll(Map m) {
            // for compatibility with 5.0, all-or-nothing semantics
            if (emptyKeyArray == null) {
                emptyKeyArray = (Object[])Array.newInstance(keyType, 0);
            }
            if (emptyValueArray == null) {
                emptyValueArray = (Object[])Array.newInstance(valueType, 0);
            }

            Object[] keys, values;

            try {
                keys = m.keySet().toArray(emptyKeyArray);
            }
            catch (ArrayStoreException e) {
                throw new ClassCastException(
                    "Attempted to use an invalid key type " +
                    " with a map with keys of type " + keyType.getName());
            }
            try {
                values = m.keySet().toArray(emptyKeyArray);
            }
            catch (ArrayStoreException e) {
                throw new ClassCastException(
                    "Attempted to use an invalid value type " +
                    " with a map with values of type " + valueType.getName());
            }
            if (keys.length != values.length) {
                throw new ConcurrentModificationException();
            }
            for (int i=0; i<keys.length; i++) {
                map.put(keys[i], values[i]);
            }
        }

        public Set entrySet() {
            if (entrySet == null) entrySet = new EntrySetView(map.entrySet());
            return entrySet;
        }

        public Object get(Object key)          { return map.get(key); }
        public Object remove(Object key)       { return map.remove(key); }

        public Object put(Object key, Object value) {
            typeCheckKey(key);
            typeCheckValue(value);
            return map.put(key, value);
        }

        private class EntrySetView extends AbstractSet implements Set {
            final Set entrySet;
            EntrySetView(Set entrySet)        { this.entrySet = entrySet; }
            public int size()                 { return entrySet.size(); }
            public boolean isEmpty()          { return entrySet.isEmpty(); }
            public boolean remove(Object o)   { return entrySet.remove(o); }
            public void clear()               { entrySet.clear(); }

            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry)) return false;
                return entrySet.contains(new EntryView((Map.Entry)o));
            }

            public Iterator iterator() {
                final Iterator itr = entrySet.iterator();
                return new Iterator() {
                    public boolean hasNext() { return itr.hasNext(); }
                    public Object next()     { return new EntryView((Map.Entry)itr.next()); }
                    public void remove()     { itr.remove(); }
                };
            }

            public Object[] toArray() {
                Object[] a = entrySet.toArray();
                if (a.getClass().getComponentType().isAssignableFrom(EntryView.class)) {
                    for (int i=0; i<a.length; i++) {
                        a[i] = new EntryView( (Entry) a[i]);
                    }
                    return a;
                }
                else {
                    Object[] newa = new Object[a.length];
                    for (int i=0; i<a.length; i++) {
                        newa[i] = new EntryView( (Entry) a[i]);
                    }
                    return newa;
                }
            }

             public Object[] toArray(Object[] a) {
                 Object[] base;
                 if (a.length == 0) {
                     base = a;
                 }
                 else {
                     base = (Object[])(Array.newInstance(a.getClass().getComponentType(), a.length));
                 }
                 base = entrySet.toArray(base);
                 // if the returned array is type-incompatible with EntryView,
                 // tough - we can't tolerate this anyway
                 for (int i=0; i<base.length; i++) {
                     base[i] = new EntryView((Map.Entry)base[i]);
                 }
                 if (base.length > a.length) {
                     a = base;
                 }
                 else {
                     // need to copy back to a
                     System.arraycopy(base, 0, a, 0, base.length);
                     if (base.length < a.length) a[base.length] = null;
                 }
                 return a;
            }
        }

        private class EntryView implements Map.Entry, Serializable {
            final Map.Entry entry;
            EntryView(Map.Entry entry) {
                this.entry = entry;
            }
            public Object getKey()          { return entry.getKey(); }
            public Object getValue()        { return entry.getValue(); }
            public int hashCode()           { return entry.hashCode(); }
            public boolean equals(Object o) {
                if (o == this) return true;
                if (!(o instanceof Map.Entry)) return false;
                Map.Entry e = (Map.Entry)o;
                return eq(getKey(), e.getKey()) && eq(getValue(), e.getValue());
            }

            public Object setValue(Object val) {
                typeCheckValue(val);
                return entry.setValue(val);
            }
        }
    }

    private static boolean eq(Object o1, Object o2) {
        return (o1 == null) ? o2 == null : o1.equals(o2);
    }

    private static class CheckedSortedMap extends CheckedMap
                                          implements SortedMap, Serializable {
        final SortedMap map;
        CheckedSortedMap(SortedMap map, Class keyType, Class valueType) {
            super(map, keyType, valueType);
            this.map = map;
        }
        public Comparator comparator()  { return map.comparator(); }
        public Object firstKey()        { return map.firstKey(); }
        public Object lastKey()         { return map.lastKey(); }

        public SortedMap subMap(Object fromKey, Object toKey) {
            return new CheckedSortedMap(map.subMap(fromKey, toKey), keyType, valueType);
        }

        public SortedMap headMap(Object toKey) {
            return new CheckedSortedMap(map.headMap(toKey), keyType, valueType);
        }

        public SortedMap tailMap(Object fromKey) {
            return new CheckedSortedMap(map.tailMap(fromKey), keyType, valueType);
        }
    }

    private static class SetFromMap extends AbstractSet implements Serializable {

        private final static Object PRESENT = Boolean.TRUE;

        final Map map;
        transient Set keySet;

        SetFromMap(Map map) {
            this.map = map;
            this.keySet = map.keySet();
        }

        public int hashCode()               { return keySet.hashCode(); }
        public int size()                   { return map.size(); }
        public void clear()                 { map.clear(); }
        public boolean isEmpty()            { return map.isEmpty(); }
        public boolean add(Object o)        { return map.put(o, PRESENT) == null; }
        public boolean contains(Object o)   { return map.containsKey(o); }
        public boolean equals(Object o)     { return keySet.equals(o); }
        public boolean remove(Object o)     { return map.remove(o) == PRESENT; }

        public boolean removeAll(Collection c) { return keySet.removeAll(c); }
        public boolean retainAll(Collection c) { return keySet.retainAll(c); }
        public Iterator iterator()             { return keySet.iterator(); }
        public Object[] toArray()              { return keySet.toArray(); }
        public Object[] toArray(Object[] a)    { return keySet.toArray(a); }

        public boolean addAll(Collection c) {
            boolean modified = false;
            for (Iterator it = c.iterator(); it.hasNext();) {
                modified |= (map.put(it.next(), PRESENT) == null);
            }
            return modified;
        }

        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            keySet = map.keySet();
        }
    }

    private static class AsLifoQueue extends AbstractQueue
        implements Queue, Serializable
    {
        final Deque deque;
        AsLifoQueue(Deque deque)            { this.deque = deque; }
        public boolean add(Object e)        { return deque.offerFirst(e); }
        public boolean offer(Object e)      { return deque.offerFirst(e); }
        public Object remove()              { return deque.removeFirst(); }
        public Object poll()                { return deque.pollFirst(); }
        public Object element()             { return deque.getFirst(); }
        public Object peek()                { return deque.peekFirst(); }
        public int size()                   { return deque.size(); }
        public void clear()                 { deque.clear(); }
        public boolean isEmpty()            { return deque.isEmpty(); }
        public Object[] toArray()           { return deque.toArray(); }
        public Object[] toArray(Object[] a) { return deque.toArray(a); }
        public boolean contains(Object o)   { return deque.contains(o); }
        public boolean remove(Object o)     { return deque.remove(o); }
        public Iterator iterator()          { return deque.iterator(); }
    }

    private static class ReverseComparator implements Comparator, Serializable {
        final Comparator cmp;
        ReverseComparator(Comparator cmp) {
            this.cmp = cmp;
        }
        public int compare(Object o1, Object o2) {
            return cmp.compare(o2, o1);
        }
    }
}
