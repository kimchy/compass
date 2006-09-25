/*
 * Written by Dawid Kurzyniec, on the basis of public specifications of class
 * java.util.PriorityQueue by Josh Bloch, and released to the public domain,
 * as explained at http://creativecommons.org/licenses/publicdomain
 */

package org.compass.core.util.backport.java.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * An unbounded {@linkplain Queue queue} that supports element retrieval
 * in the order of relative priority. The ordering can be defined via an
 * explicit comparator; otherwise, the natural ordering of elements is used.
 * Element at the head of the queue is always the <em>smallest</em> one
 * according to the given ordering.
 *
 * <p>While this queue is logically
 * unbounded, attempted additions may fail due to resource exhaustion
 * (causing <tt>OutOfMemoryError</tt>). This class does not permit
 * <tt>null</tt> elements.  A priority queue relying on {@linkplain
 * Comparable natural ordering} also does not permit insertion of
 * non-comparable objects (doing so results in
 * <tt>ClassCastException</tt>).
 *
 * <p>This class and its iterator implement all of the
 * <em>optional</em> methods of the {@link Collection} and {@link
 * Iterator} interfaces.  The Iterator provided in method {@link
 * #iterator()} is <em>not</em> guaranteed to traverse the elements of
 * the PriorityQueue in any particular order. If you need
 * ordered traversal, consider using
 * <tt>Arrays.sort(pq.toArray())</tt>.
 *
 * <p>Operations on this class make no guarantees about the ordering
 * of elements with equal priority. If you need to enforce an
 * ordering, you can define custom classes or comparators that use a
 * secondary key to break ties in primary priority values.  See
 * {@link org.compass.core.util.backport.java.util.concurrent.PriorityBlockingQueue}
 * for an example.
 *
 * <p><em>Implementation note:</em> basic mutative methods (insert, offer,
 * remove, poll etc) have complexity O(log(n)). Parameterless inspection methods
 * (peek, element,isEmpty) have complexity O(1). Methods contains(Object) and
 * remove(Object) have complexity O(n).
 *
 * @since 1.5
 * @author Dawid Kurzyniec
 */
public class PriorityQueue extends AbstractQueue implements java.io.Serializable, Queue {

    private final static long serialVersionUID = -7720805057305804111L;

    private final static int DEFAULT_INIT_CAPACITY = 11;

    private transient Object[] buffer;
    private int size;
    private final Comparator comparator;
    private transient int modCount;

    /**
     * Creates a <tt>PriorityQueue</tt> with the default
     * initial capacity (11) that orders its elements according to
     * their {@linkplain Comparable natural ordering}.
     */
    public PriorityQueue() {
        this(DEFAULT_INIT_CAPACITY, null);
    }

    /**
     * Creates a <tt>PriorityQueue</tt> with the specified
     * initial capacity that orders its elements according to their
     * {@linkplain Comparable natural ordering}.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if <tt>initialCapacity</tt> is less
     * than 1
     */
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Creates a <tt>PriorityQueue</tt> with the specified initial
     * capacity that orders its elements according to the specified
     * comparator.
     *
     * @param comparator the comparator used to order this priority queue.
     * If <tt>null</tt> then the order depends on the elements' natural
     * ordering.
     * @throws IllegalArgumentException if <tt>initialCapacity</tt> is less
     * than 1
     */
    public PriorityQueue(Comparator comparator) {
        this(DEFAULT_INIT_CAPACITY, comparator);
    }

    public PriorityQueue(int initialCapacity, Comparator comparator) {
        if (initialCapacity < 1) throw new IllegalArgumentException();
        this.buffer = new Object[initialCapacity];
        this.comparator = comparator;
    }

    /**
     * Creates a <tt>PriorityQueue</tt> containing the elements from
     * the specified priority queue. This priority queue has an initial
     * capacity of 110% of the size of the specified queue, and it is
     * sorted according to the same comparator as the specified queue,
     * or according to the natural ordering of its
     * elements if the specified queue is sorted according to the natural
     * ordering of its elements.
     *
     * @param q the queue whose elements are to be placed
     *        into this priority queue.
     * @throws NullPointerException if the specified queue is null
     */
    public PriorityQueue(PriorityQueue q) {
        this((Collection)q);
    }

    /**
     * Creates a <tt>PriorityQueue</tt> containing the elements
     * from the specified sorted set.  This priority queue has an initial
     * capacity of 110% of the size of the specified set, and it is
     * sorted according to the same comparator as the specified set,
     * or according to the natural ordering of its
     * elements if the specified set is sorted according to the natural
     * ordering of its elements.
     *
     * @param s the set whose elements are to be placed
     *        into this priority queue.
     * @throws NullPointerException if the specified set or any
     *         of its elements are null
     */
    public PriorityQueue(SortedSet s) {
        this((Collection)s);
    }

    /**
     * Creates a <tt>PriorityQueue</tt> containing the elements
     * in the specified collection.  The priority queue has an initial
     * capacity of 110% of the size of the specified collection.  If
     * the specified collection is a {@link java.util.SortedSet} or a {@link
     * PriorityQueue}, this priority queue will be sorted according to
     * the same comparator, or according to the natural ordering of its
     * elements if the collection is sorted according to the natural
     * ordering of its elements.  Otherwise, this priority queue is
     * ordered according to the natural ordering of its elements.
     *
     * @param c the collection whose elements are to be placed
     *        into this priority queue.
     * @throws ClassCastException if elements of the specified collection
     *         cannot be compared to one another according to the priority
     *         queue's ordering.
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    public PriorityQueue(Collection c) {
        int capacity = c.size();
        capacity += size/10;
        if (capacity < 0) capacity = Integer.MAX_VALUE;
        else if (capacity == 0) capacity = 1;
        this.buffer = new Object[capacity];

        if (c instanceof PriorityQueue) {
            PriorityQueue that = (PriorityQueue)c;
            this.comparator = that.comparator;
            this.size = that.size;
            System.arraycopy(that.buffer, 0, this.buffer, 0, this.size);
        }
        else if (c instanceof SortedSet) {
            SortedSet s = (SortedSet)c;
            this.comparator = s.comparator();
            for (Iterator itr = s.iterator(); itr.hasNext();) {
                buffer[size++] = itr.next();
            }
        }
        else {
            this.comparator = null;
            for (Iterator itr = c.iterator(); itr.hasNext();) {
                buffer[size++] = itr.next();
            }
            for (int i=size/2; i>=0; --i) {
                percolateDown(i, buffer[i]);
            }
        }
    }

    /**
     * Returns an iterator over the elements in this queue. The
     * iterator does not return the elements in any particular order.
     * The returned iterator is a thread-safe "fast-fail" iterator
     * that will throw {@link java.util.ConcurrentModificationException} upon
     * detected interference.
     *
     * @return an iterator over the elements in this queue
     */
    public Iterator iterator() {
        return new Itr();
    }

    /**
     * Returns the comparator used to order the elements in this queue,
     * or <tt>null</tt> if this queue uses the {@linkplain Comparable
     * natural ordering} of its elements.
     *
     * @return the comparator used to order the elements in this queue,
     *         or <tt>null</tt> if this queue uses the natural
     *         ordering of its elements.
     */
    public Comparator comparator() {
        return comparator;
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param o the element to add
     * @return <tt>true</tt> (as per the spec for {@link Queue#offer})
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(Object o) {
        if (o == null) throw new NullPointerException();
        if (size == buffer.length) {
            int newlen = buffer.length*2;
            if (newlen < buffer.length) { // overflow
                if (buffer.length == Integer.MAX_VALUE) {
                    throw new OutOfMemoryError();
                }
                newlen = Integer.MAX_VALUE;
            }
            Object[] newbuffer = new Object[newlen];
            System.arraycopy(buffer, 0, newbuffer, 0, size);
            buffer = newbuffer;
        }
        modCount++;
        percolateUp(size++, o);
        return true;
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or returns
     * <tt>null</tt> if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is
     *   empty
     */
    public Object peek() {
        return (size == 0) ? null : buffer[0];
    }

    /**
     * Retrieves and removes the head of this queue, or returns <tt>null</tt>
     * if this queue is empty.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is
     *   empty
     */
    public Object poll() {
        if (size == 0) return null;
        modCount++;
        Object head = buffer[0];
        --size;
        percolateDown(0, buffer[size]);
        buffer[size] = null;
        return head;
    }

    /**
     * Returns the number of elements in this priority queue.
     *
     * @return the number of elements in this priority queue
     */
    public int size() {
        return size;
    }

    /**
     * Assuming that the 'idx' element is to be overwritten, takes an element
     * (usually from the end of the queue) to replace 'idx' and percolates it
     * down the heap.
     */
    private int percolateDown(int idx, Object e) {
        try {
            if (comparator != null) {
                while (true) {
                    int c = (idx<<1)+1;
                    if (c >= size) break;
                    if (c+1 < size) {
                        if (comparator.compare(buffer[c+1], buffer[c]) < 0) c++;
                    }
                    if (comparator.compare(e, buffer[c]) <= 0) break;
                    buffer[idx] = buffer[c];
                    idx = c;
                }
            }
            else {
                Comparable ec = (Comparable)e;
                while (true) {
                    int c = (idx<<1)+1;
                    if (c >= size) break;
                    if (c+1 < size) {
                        if (((Comparable)buffer[c+1]).compareTo(buffer[c]) < 0) c++;
                    }
                    if (ec.compareTo(buffer[c]) <= 0) break;
                    buffer[idx] = buffer[c];
                    idx = c;
                }
            }
            return idx;
        }
        finally {
            buffer[idx] = e;
        }
    }

    /**
     * Takes an element to be inserted into the queue, puts it at 'idx' and
     * percolates it up the heap.
     */
    private int percolateUp(int idx, Object e) {
        try {
            if (comparator != null) {
                while (idx > 0) {
                    int c = (idx-1)>>>1;
                    if (comparator.compare(e, buffer[c]) >= 0) break;
                    buffer[idx] = buffer[c];
                    idx = c;
                }
                return idx;
            }
            else {
                Comparable ce = (Comparable)e;
                while (idx > 0) {
                    int c = (idx-1)>>>1;
                    if (ce.compareTo(buffer[c]) >= 0) break;
                    buffer[idx] = buffer[c];
                    idx = c;
                }
                return idx;
            }
        }
        finally {
            buffer[idx] = e;
        }
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param o the element to add
     * @return <tt>true</tt> (as per the spec for {@link Collection#add})
     * @throws ClassCastException if the specified element cannot be compared
     *         with elements currently in the priority queue according to the
     *         priority queue's ordering
     * @throws NullPointerException if the specified element is null
     */
    public boolean add(Object o) {
        return offer(o);
    }

    /**
     * Retrieves and removes the head of this queue.
     *
     * @return the head of this queue
     */
    public Object remove() {
        if (size == 0) throw new NoSuchElementException();
        Object head = buffer[0];
        modCount++;
        --size;
        percolateDown(0, buffer[size]);
        buffer[size] = null;
        return head;
    }

    /**
     * Retrieves, but does not remove, the head of this queue.
     *
     * @return the head of this queue
     * @throws NoSuchElementException of the queue is empty
     */
    public Object element() {
        if (size == 0) throw new NoSuchElementException();
        return buffer[0];
    }

    /**
     * Returns <tt>true</tt> if this queue contains no elements.
     *
     * @return <tt>true</tt> if this queue contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns <tt>true</tt> if this queue contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this queue contains
     * at least one element <tt>e</tt> such that <tt>o.equals(e)</tt>.
     *
     * @param o object to be checked for containment in this queue
     * @return <tt>true</tt> if this queue contains the specified element
     */
    public boolean contains(Object o) {
        for (int i=0; i<size; i++) {
            if (o.equals(buffer[i])) return true;
        }
        return false;
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The returned array elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    public Object[] toArray() {
        return Arrays.copyOf(buffer, size, Object[].class);
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * <tt>null</tt>.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>Suppose <tt>x</tt> is a queue known to contain only strings.
     * The following code can be used to dump the queue into a newly
     * allocated array of <tt>String</tt>:
     *
     * <pre>
     *     String[] y = x.toArray(new String[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    public Object[] toArray(Object[] a) {
        if (a.length < size) {
            return Arrays.copyOf(buffer, size, a.getClass());
        }
        else {
            System.arraycopy(buffer, 0, a, 0, size);
            if (a.length > size) a[size] = null;
            return a;
        }
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.
     * Returns <tt>true</tt> if this queue contained the specified element
     * (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return <tt>true</tt> if this queue changed as a result of the call
     */
    public boolean remove(Object o) {
        if (o == null) return false;
        if (comparator != null) {
            for (int i = 0; i < size; i++) {
                if (comparator.compare(buffer[i], o) == 0) {
                    removeAt(i);
                    return true;
                }
            }
        }
        else {
            for (int i = 0; i < size; i++) {
                if (((Comparable)buffer[i]).compareTo(o) == 0) {
                    removeAt(i);
                    return true;
                }
            }
        }
        return false;
    }

    private Object removeAt(int i) {
        assert (i < size);
        modCount++;
        --size;
        int newpos;
        Object e = buffer[size];
        buffer[size] = null;
        // first, try percolating down
        newpos = percolateDown(i, e);
        if (newpos != i) return null;
        // not moved; so percolate up
        newpos = percolateUp(i, e);
        return (newpos < i ? e : null);
    }

    /**
     * Removes all of the elements from this queue.
     * The queue will be empty after this call returns.
     */
    public void clear() {
        modCount++;
        Arrays.fill(buffer, 0, size, null);
        size = 0;
    }

    private class Itr implements Iterator {
        int cursor = 0;
        List percolatedElems;
        int cursorPercolated = 0;
        int expectedModCount = modCount;
        int lastRet;
        Object lastRetPercolated;
        Itr() {}

        public boolean hasNext() {
            return cursor < size || percolatedElems != null;
        }

        public Object next() {
            checkForComodification();
            if (cursor < size) {
                lastRet = cursor++;
                return buffer[lastRet];
            }
            else if (percolatedElems != null) {
                lastRet = -1;
                lastRetPercolated = percolatedElems.remove(percolatedElems.size()-1);
                if (percolatedElems.isEmpty()) {
                    percolatedElems = null;
                }
                return lastRetPercolated;
            }
            else {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet >= 0) {
                Object percolatedElem = removeAt(lastRet);
                lastRet = -1;
                if (percolatedElem == null) {
                    cursor--;
                }
                else {
                    if (percolatedElems == null) percolatedElems = new ArrayList();
                    percolatedElems.add(percolatedElem);
                }
            }
            else if (lastRetPercolated != null) {
                PriorityQueue.this.remove(lastRetPercolated);
                lastRetPercolated = null;
            }
            else {
                throw new IllegalStateException();
            }
            expectedModCount = modCount;
        }

        private void checkForComodification() {
            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * @serialData the length of the array (queue capacity) is stored, followed
     * by all of its elements (as Objects)
     */
    private void writeObject(java.io.ObjectOutputStream os) throws java.io.IOException {
        os.defaultWriteObject();
        os.writeInt(buffer.length);
        for (int i=0; i<size; i++) {
            os.writeObject(buffer[i]);
        }
    }

    private void readObject(java.io.ObjectInputStream is)
        throws java.io.IOException, ClassNotFoundException
    {
        is.defaultReadObject();
        this.buffer = new Object[is.readInt()];
        for (int i=0; i<size; i++) {
            buffer[i] = is.readObject();
        }
    }
}
