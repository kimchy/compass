/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util;
import java.util.*;

/**
 * A {@link SortedMap} extended with navigation methods returning the
 * closest matches for given search targets. Methods
 * <tt>lowerEntry</tt>, <tt>floorEntry</tt>, <tt>ceilingEntry</tt>,
 * and <tt>higherEntry</tt> return <tt>Map.Entry</tt> objects
 * associated with keys respectively less than, less than or equal,
 * greater than or equal, and greater than a given key, returning
 * <tt>null</tt> if there is no such key.  Similarly, methods
 * <tt>lowerKey</tt>, <tt>floorKey</tt>, <tt>ceilingKey</tt>, and
 * <tt>higherKey</tt> return only the associated keys. All of these
 * methods are designed for locating, not traversing entries.
 *
 * <p>A <tt>NavigableMap</tt> may be viewed and traversed in either
 * ascending or descending key order.  The <tt>Map</tt> methods
 * <tt>keySet</tt> and <tt>entrySet</tt> return ascending views, and
 * the additional methods <tt>descendingKeySet</tt> and
 * <tt>descendingEntrySet</tt> return descending views. The
 * performance of ascending traversals is likely to be faster than
 * descending traversals.  Notice that it is possible to perform
 * subrange traversals in either direction using <tt>navigableSubMap</tt>.
 * Methods <tt>navigableSubMap</tt>, <tt>navigableHeadMap</tt>, and
 * <tt>navigableTailMap</tt> differ from the similarly named
 * <tt>SortedMap</tt> methods only in their declared return types.
 * Submaps of any <tt>NavigableMap<tt> must implement the
 * <tt>NavigableMap</tt> interface.
 *
 * <p>This interface additionally defines methods <tt>firstEntry</tt>,
 * <tt>pollFirstEntry</tt>, <tt>lastEntry</tt>, and
 * <tt>pollLastEntry</tt> that return and/or remove the least and
 * greatest mappings, if any exist, else returning <tt>null</tt>.
 *
 * <p> Implementations of entry-returning methods are expected to
 * return <tt>Map.Entry</tt> pairs representing snapshots of mappings
 * at the time they were produced, and thus generally do <em>not</em>
 * support the optional <tt>Entry.setValue</tt> method. Note however
 * that it is possible to change mappings in the associated map using
 * method <tt>put</tt>.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @since 1.6
 */
public interface NavigableMap extends SortedMap {
    /**
     * Returns a key-value mapping associated with the greatest key
     * strictly less than the given key, or <tt>null</tt> if there is
     * no such key.
     *
     * @param key the key
     * @return an entry with the greatest key less than <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Map.Entry lowerEntry(Object key);

    /**
     * Returns the greatest key strictly less than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key
     * @return the greatest key less than <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Object lowerKey(Object key);

    /**
     * Returns a key-value mapping associated with the greatest key
     * less than or equal to the given key, or <tt>null</tt> if there
     * is no such key.
     *
     * @param key the key
     * @return an entry with the greatest key less than or equal to
     *         <tt>key</tt>, or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Map.Entry floorEntry(Object key);

    /**
     * Returns the greatest key less than or equal to the given key,
     * or <tt>null</tt> if there is no such key.
     *
     * @param key the key
     * @return the greatest key less than or equal to <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Object floorKey(Object key);

    /**
     * Returns a key-value mapping associated with the least key
     * greater than or equal to the given key, or <tt>null</tt> if
     * there is no such key.
     *
     * @param key the key
     * @return an entry with the least key greater than or equal to
     *         <tt>key</tt>, or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Map.Entry ceilingEntry(Object key);

    /**
     * Returns the least key greater than or equal to the given key,
     * or <tt>null</tt> if there is no such key.
     *
     * @param key the key
     * @return the least key greater than or equal to <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Object ceilingKey(Object key);

    /**
     * Returns a key-value mapping associated with the least key
     * strictly greater than the given key, or <tt>null</tt> if there
     * is no such key.
     *
     * @param key the key
     * @return an entry with the least key greater than <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Map.Entry higherEntry(Object key);

    /**
     * Returns the least key strictly greater than the given key, or
     * <tt>null</tt> if there is no such key.
     *
     * @param key the key
     * @return the least key greater than <tt>key</tt>,
     *         or <tt>null</tt> if there is no such key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map does not permit null keys
     */
    Object higherKey(Object key);

    /**
     * Returns a key-value mapping associated with the least
     * key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return an entry with the least key,
     *         or <tt>null</tt> if this map is empty
     */
    Map.Entry firstEntry();

    /**
     * Returns a key-value mapping associated with the greatest
     * key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return an entry with the greatest key,
     *         or <tt>null</tt> if this map is empty
     */
    Map.Entry lastEntry();

    /**
     * Removes and returns a key-value mapping associated with
     * the least key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return the removed first entry of this map,
     *         or <tt>null</tt> if this map is empty
     */
    Map.Entry pollFirstEntry();

    /**
     * Removes and returns a key-value mapping associated with
     * the greatest key in this map, or <tt>null</tt> if the map is empty.
     *
     * @return the removed last entry of this map,
     *         or <tt>null</tt> if this map is empty
     */
    Map.Entry pollLastEntry();

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set's iterator returns the keys in descending order.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map, sorted in
     *         descending order
     */
    Set descendingKeySet();

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set's iterator returns the entries in descending key order.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map,
     *         sorted in descending key order
     */
    Set descendingEntrySet();

    /**
     * Returns a view of the portion of this map whose keys range from
     * <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive.  (If
     * <tt>fromKey</tt> and <tt>toKey</tt> are equal, the returned map
     * is empty.)  The returned map is backed by this map, so changes
     * in the returned map are reflected in this map, and vice-versa.
     * The returned map supports all optional map operations that this
     * map supports.
     *
     * <p>The returned map will throw an <tt>IllegalArgumentException</tt>
     * on an attempt to insert a key outside its range.
     *
     * @param fromKey low endpoint (inclusive) of the keys in the returned map
     * @param toKey high endpoint (exclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys range from
     *         <tt>fromKey</tt>, inclusive, to <tt>toKey</tt>, exclusive
     * @throws ClassCastException if <tt>fromKey</tt> and <tt>toKey</tt>
     *         cannot be compared to one another using this map's comparator
     *         (or, if the map has no comparator, using natural ordering).
     *         Implementations may, but are not required to, throw this
     *         exception if <tt>fromKey</tt> or <tt>toKey</tt>
     *         cannot be compared to keys currently in the map.
     * @throws NullPointerException if <tt>fromKey</tt> or <tt>toKey</tt>
     *         is null and this map does not permit null keys
     * @throws IllegalArgumentException if <tt>fromKey</tt> is greater than
     *         <tt>toKey</tt>; or if this map itself has a restricted
     *         range, and <tt>fromKey</tt> or <tt>toKey</tt> lies
     *         outside the bounds of the range
     */
    NavigableMap navigableSubMap(Object fromKey, Object toKey);

    /**
     * Returns a view of the portion of this map whose keys are
     * strictly less than <tt>toKey</tt>.  The returned map is backed
     * by this map, so changes in the returned map are reflected in
     * this map, and vice-versa.  The returned map supports all
     * optional map operations that this map supports.
     *
     * <p>The returned map will throw an <tt>IllegalArgumentException</tt>
     * on an attempt to insert a key outside its range.
     *
     * @param toKey high endpoint (exclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys are strictly
     *         less than <tt>toKey</tt>
     * @throws ClassCastException if <tt>toKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>toKey</tt> does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if <tt>toKey</tt> cannot be compared to keys
     *         currently in the map.
     * @throws NullPointerException if <tt>toKey</tt> is null
     *         and this map does not permit null keys
     * @throws IllegalArgumentException if this map itself has a
     *         restricted range, and <tt>toKey</tt> lies outside the
     *         bounds of the range
     */
    NavigableMap navigableHeadMap(Object toKey);

    /**
     * Returns a view of the portion of this map whose keys are
     * greater than or equal to <tt>fromKey</tt>.  The returned map is
     * backed by this map, so changes in the returned map are
     * reflected in this map, and vice-versa.  The returned map
     * supports all optional map operations that this map supports.
     *
     * <p>The returned map will throw an <tt>IllegalArgumentException</tt>
     * on an attempt to insert a key outside its range.
     *
     * @param fromKey low endpoint (inclusive) of the keys in the returned map
     * @return a view of the portion of this map whose keys are greater
     *         than or equal to <tt>fromKey</tt>
     * @throws ClassCastException if <tt>fromKey</tt> is not compatible
     *         with this map's comparator (or, if the map has no comparator,
     *         if <tt>fromKey</tt> does not implement {@link Comparable}).
     *         Implementations may, but are not required to, throw this
     *         exception if <tt>fromKey</tt> cannot be compared to keys
     *         currently in the map.
     * @throws NullPointerException if <tt>fromKey</tt> is null
     *         and this map does not permit null keys
     * @throws IllegalArgumentException if this map itself has a
     *         restricted range, and <tt>fromKey</tt> lies outside the
     *         bounds of the range
     */
    NavigableMap navigableTailMap(Object fromKey);
}
