/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.core.util.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kimchy
 */
public class ConcurrentHashSet<E> implements Set<E> {

    final private ConcurrentHashMap<E, Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<E, Object>();
    }

    /**
     * Returns an iterator over the elements in this set.  The elements
     * are returned in no particular order.
     *
     * @return an Iterator over the elements in this set.
     */
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns the number of elements in this set (its cardinality).
     *
     * @return the number of elements in this set (its cardinality).
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns <tt>true</tt> if this set contains no elements.
     *
     * @return <tt>true</tt> if this set contains no elements.
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested.
     * @return <tt>true</tt> if this set contains the specified element.
     */
    public boolean contains(Object o) {
        if (o == null)
            return false;

        return map.containsKey(o);
    }

    /**
     * Adds the specified element to this set if it is not already
     * present.
     *
     * @param o element to be added to this set.
     * @return <tt>true</tt> if the set did not already contain the specified
     *         element.
     */
    public boolean add(E o) {
        if (o == null)
            return false;

        return map.put(o, PRESENT) == null;
    }

    /**
     * Removes the specified element from this set if it is present.
     *
     * @param o object to be removed from this set, if present.
     * @return <tt>true</tt> if the set contained the specified element.
     */
    public boolean remove(Object o) {
        if (o == null)
            return false;

        return map.remove(o) == PRESENT;
    }

    /**
     * Removes all of the elements from this set.
     */
    public void clear() {
        map.clear();
    }

    /*
      * @see java.util.Set#addAll(java.util.Collection)
      */
    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    /*
      * @see java.util.Set#containsAll(java.util.Collection)
      */
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /*
      * @see java.util.Set#removeAll(java.util.Collection)
      */
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /*
      * @see java.util.Set#retainAll(java.util.Collection)
      */
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /*
      * @see java.util.Set#toArray()
      */
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /*
      * @see java.util.Set#toArray(T[])
      */
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }
}