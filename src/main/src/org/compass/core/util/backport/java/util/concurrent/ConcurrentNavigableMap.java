/*
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util.concurrent;
import org.compass.core.util.backport.java.util.*;
import java.util.SortedMap;

/**
 * A {@link ConcurrentMap} supporting {@link NavigableMap} operations,
 * and recursively so for its navigable sub-maps.
 *
 * <p>This interface is a member of the
 * <a href="{@docRoot}/../guide/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @author Doug Lea
 * @since 1.6
 */
public interface ConcurrentNavigableMap
    extends ConcurrentMap, NavigableMap
{
    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    NavigableMap navigableSubMap(Object fromKey, Object toKey);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    NavigableMap navigableHeadMap(Object toKey);

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    NavigableMap navigableTailMap(Object fromKey);

    /**
     * Equivalent to {@link #navigableSubMap}.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap subMap(Object fromKey, Object toKey);

    /**
     * Equivalent to {@link #navigableHeadMap}.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap headMap(Object toKey);

    /**
     * Equivalent to {@link #navigableTailMap}.
     *
     * <p>{@inheritDoc}
     *
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException     {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    SortedMap tailMap(Object fromKey);
}
