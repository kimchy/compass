package org.compass.core.util.backport.java.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.AbstractSequentialList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LinkedList extends AbstractSequentialList
    implements List, Deque, Cloneable, Serializable
{
    private static final long serialVersionUID = 876323262645176354L;

    private transient int size = 0;
    private transient int modCount;

    // bi-directional cyclic list; head contains a sentinel entry
    private transient Entry head;

    private static class Entry {
        Entry prev;
        Entry next;
        Object val;
        Entry(Object val) {
            this.val = val;
        }
    }

    public LinkedList() {
        Entry sentinel = new Entry(null);
        sentinel.next = sentinel.prev = sentinel;
        head = sentinel;
    }

    public LinkedList(Collection c) {
        this();
        addAll(c);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean contains(Object o) {
        return findFirst(o) != null;
    }

    private Entry getAt(int idx) {
        Entry e;
        int size = this.size;
        if (idx < 0 || idx >= size) {
            throw new ArrayIndexOutOfBoundsException("Index: " + idx +
                                                     "; Size: " + size);
        }
        if (idx < (size >> 1)) {
            for (e = head.next; idx>0; idx--) e = e.next;
            return e;
        }
        else {
            idx = size-idx-1;
            for (e = head.prev; idx>0; idx--) e = e.prev;
            return e;
        }
    }

    private Entry findFirst(Object o) {
        if (o == null) {
            for (Entry e = head.next; e != head; e = e.next) {
                if (e.val == null) return e;
            }
        }
        else {
            for (Entry e = head.next; e != head; e = e.next) {
                if (o.equals(e.val)) return e;
            }
        }
        return null;
    }

    private Entry findLast(Object o) {
        if (o == null) {
            for (Entry e = head.prev; e != head; e = e.prev) {
                if (e.val == null) return e;
            }
        }
        else {
            for (Entry e = head.prev; e != head; e = e.prev) {
                if (o.equals(e.val)) return e;
            }
        }
        return null;
    }

    public int indexOf(Object o) {
        int idx=0;
        if (o == null) {
            for (Entry e = head.next; e != head; e = e.next, idx++) {
                if (e.val == null) return idx;
            }
        }
        else {
            for (Entry e = head.next; e != head; e = e.next, idx++) {
                if (o.equals(e.val)) return idx;
            }
        }
        return -1;
    }

    public int lastIndexOf(Object o) {
        int idx=size-1;
        if (o == null) {
            for (Entry e = head.prev; e != head; e = e.prev, idx--) {
                if (e.val == null) return idx;
            }
        }
        else {
            for (Entry e = head.prev; e != head; e = e.prev, idx--) {
                if (o.equals(e.val)) return idx;
            }
        }
        return -1;
    }

    public Object[] toArray() {
        Object[] a = new Object[size];
        int i=0;
        for (Entry e = head.next; e != head; e = e.next) a[i++] = e.val;
        return a;
    }

    public Object[] toArray(Object[] a) {
        int size = this.size;
        if (a.length < size) {
            a = (Object[])Array.newInstance(a.getClass().getComponentType(), size);
        }
        int i=0;
        for (Entry e = head.next; e != head; e = e.next) a[i++] = e.val;
        if (i < a.length) a[i++] = null;
        return a;
    }

    public boolean add(Object o) {
        insertBefore(head, o);
        return true;
    }

    private void insertAfter(Entry e, Object val) {
        modCount++;
        Entry succ = e.next;
        Entry newe = new Entry(val);
        newe.prev = e;
        newe.next = succ;
        e.next = newe;
        succ.prev = newe;
        size++;
    }

    private void insertBefore(Entry e, Object val) {
        modCount++;
        Entry pred = e.prev;
        Entry newe = new Entry(val);
        newe.prev = pred;
        newe.next = e;
        pred.next = newe;
        e.prev = newe;
        size++;
    }

    private Object remove(Entry e) {
        if (e == head) throw new NoSuchElementException();
        modCount++;
        Entry succ = e.next;
        Entry pred = e.prev;
        pred.next = succ;
        succ.prev = pred;
        size--;
        return e.val;
    }

    public boolean remove(Object o) {
        Entry e = findFirst(o);
        if (e == null) return false;
        remove(e);
        return true;
    }

    public boolean addAll(Collection c) {
        return insertAllBefore(head, c);
    }

    public boolean addAll(int index, Collection c) {
        return insertAllBefore((index == size) ? head : getAt(index), c);
    }

    private boolean insertAllBefore(Entry succ, Collection c) {
        Iterator itr = c.iterator();
        if (!itr.hasNext()) return false;
        modCount++;
        Entry first = new Entry(itr.next());
        Entry prev = first;
        Entry curr = first;
        int added = 1;
        while (itr.hasNext()) {
            curr = new Entry(itr.next());
            prev.next = curr;
            curr.prev = prev;
            prev = curr;
            added++;
        }

        Entry pred = succ.prev;
        first.prev = pred;
        curr.next = succ;
        pred.next = first;
        succ.prev = curr;
        size += added;

        return true;
    }

    public void clear() {
        modCount++;
        head.next = head.prev = head;
        size = 0;
    }

    public Object get(int index) {
        return getAt(index).val;
    }

    public Object set(int index, Object element) {
        Entry e = getAt(index);
        Object old = e.val;
        e.val = element;
        return old;
    }

    public void add(int index, Object element) {
        if (index == size) insertBefore(head, element);
        else insertBefore(index == size ? head : getAt(index), element);
    }

    public Object remove(int index) {
        return remove(getAt(index));
    }

    public ListIterator listIterator() {
        return new Itr();
    }

    public ListIterator listIterator(int index) {
        return new Itr(index == size ? head : getAt(index), index);
    }

    public void addFirst(Object e) {
        insertAfter(head, e);
    }

    public void addLast(Object e) {
        insertBefore(head, e);
    }

    public boolean offerFirst(Object e) {
        insertAfter(head, e);
        return true;
    }

    public boolean offerLast(Object e) {
        insertBefore(head, e);
        return true;
    }

    public Object removeFirst() {
        return remove(head.next);
    }

    public Object removeLast() {
        return remove(head.prev);
    }

    public Object pollFirst() {
        return (size == 0) ? null : remove(head.next);
    }

    public Object pollLast() {
        return (size == 0) ? null : remove(head.prev);
    }

    public Object getFirst() {
        if (size == 0) throw new NoSuchElementException();
        else return head.next.val;
    }

    public Object getLast() {
        if (size == 0) throw new NoSuchElementException();
        else return head.prev.val;
    }

    public Object peekFirst() {
        return (size == 0) ? null : head.next.val;
    }

    public Object peekLast() {
        return (size == 0) ? null : head.prev.val;
    }

    public boolean removeFirstOccurrence(Object o) {
        Entry e = findFirst(o);
        if (e == null) return false;
        remove(e);
        return true;
    }

    public boolean removeLastOccurrence(Object o) {
        Entry e = findLast(o);
        if (e == null) return false;
        remove(e);
        return true;
    }

    public boolean offer(Object e) {
        return add(e);
    }

    public Object remove() {
        return removeFirst();
    }

    public Object poll() {
        return pollFirst();
    }

    public Object element() {
        return getFirst();
    }

    public Object peek() {
        return peekFirst();
    }

    public void push(Object e) {
        addFirst(e);
    }

    public Object pop() {
        return removeFirst();
    }

    public Iterator descendingIterator() {
        return new DescItr();
    }

    private class Itr implements ListIterator {
        int expectedModCount;
        int idx;
        Entry cursor;
        Entry lastRet;
        Itr(Entry cursor, int idx) {
            this.cursor = cursor;
            this.idx = idx;
            this.expectedModCount = modCount;
        }

        Itr() {
            this(head.next, 0);
        }

        public boolean hasNext() {
            return cursor != head;
        }

        public int nextIndex() {
            return idx;
        }

        public boolean hasPrevious() {
            return cursor.prev != head;
        }

        public int previousIndex() {
            return idx-1;
        }

        public Object next() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (cursor == head) throw new NoSuchElementException();
            lastRet = cursor;
            cursor = cursor.next;
            idx++;
            return lastRet.val;
        }

        public Object previous() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (cursor.prev == head) throw new NoSuchElementException();
            lastRet = cursor = cursor.prev;
            idx--;
            return lastRet.val;
        }

        public void add(Object val) {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            insertBefore(cursor, val);
            lastRet = null;
            idx++;
            expectedModCount++;
        }

        public void set(Object newVal) {
            if (lastRet == null) throw new IllegalStateException();
            lastRet.val = newVal;
        }

        public void remove() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (lastRet == null) throw new IllegalStateException();
            if (lastRet.next == cursor) idx--; else cursor = lastRet.next;
            LinkedList.this.remove(lastRet);
            lastRet = null;
            expectedModCount++;
        }
    }

    private class DescItr implements ListIterator {
        int expectedModCount;
        int idx;
        Entry cursor;
        Entry lastRet;
        DescItr(Entry cursor, int idx) {
            this.cursor = cursor;
            this.idx = idx;
            this.expectedModCount = modCount;
        }

        DescItr() {
            this(head.prev, 0);
        }

        public boolean hasNext() {
            return cursor != head;
        }

        public int nextIndex() {
            return idx;
        }

        public boolean hasPrevious() {
            return cursor.next != head;
        }

        public int previousIndex() {
            return idx-1;
        }

        public Object next() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (cursor == head) throw new NoSuchElementException();
            lastRet = cursor;
            cursor = cursor.prev;
            idx++;
            return lastRet.val;
        }

        public Object previous() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (cursor.next == head) throw new NoSuchElementException();
            lastRet = cursor = cursor.next;
            idx--;
            return lastRet;
        }

        public void add(Object val) {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            insertAfter(cursor, val);
            lastRet = null;
            idx++;
            expectedModCount++;
        }

        public void set(Object newVal) {
            if (lastRet == null) throw new IllegalStateException();
            lastRet.val = newVal;
        }

        public void remove() {
            if (expectedModCount != modCount) throw new ConcurrentModificationException();
            if (lastRet == null) throw new IllegalStateException();
            if (lastRet.next == cursor) idx--; else cursor = lastRet.next;
            LinkedList.this.remove(lastRet);
            lastRet = null;
            expectedModCount++;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(size);
        for (Entry e = head.next; e != head; e = e.next) {
            out.writeObject(e.val);
        }
    }

    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        int size = in.readInt();
        Entry head = new Entry(null);
        head.next = head.prev = head;
        for (int i=0; i < size; i++) {
            insertBefore(head, in.readObject());
        }
        this.size = size;
        this.head = head;
    }

    public Object clone() {
        LinkedList clone = null;
        try { clone = (LinkedList) super.clone(); }
        catch (CloneNotSupportedException e) { throw new InternalError(); }
        Entry head = new Entry(null);
        head.next = head.prev = head;
        clone.head = head;
        clone.addAll(this);
        return clone;
    }
}
