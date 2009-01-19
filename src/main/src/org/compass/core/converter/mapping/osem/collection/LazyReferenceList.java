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

package org.compass.core.converter.mapping.osem.collection;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.compass.core.spi.InternalCompassSession;

/**
 * A lazy reference list. If no dirty operations are perfomed, will lazily load references from the
 * search engine. Once a dirty operation is perfomed, will {@link #loadFully()}, and then all operations
 * will be perfoemd on the loaded list.
 *
 * @author kimchy
 */
public class LazyReferenceList extends AbstractList implements LazyReferenceCollection {

    private InternalCompassSession session;

    private final ArrayList<LazyReferenceEntry> entries;

    private Object[] objects;

    private ArrayList objectList;

    private boolean fullyLoaded = false;

    public LazyReferenceList(InternalCompassSession session, int size) {
        this.session = session;
        this.entries = new ArrayList<LazyReferenceEntry>(size);
        this.objects = new Object[size];
    }

    public void addLazyEntry(LazyReferenceEntry entry) {
        entries.add(entry);
    }

    public Object get(int index) {
        if (fullyLoaded) {
            return objectList.get(index);
        }
        Object obj = objects[index];
        if (obj == null) {
            obj = session.get(entries.get(index).getAlias(), entries.get(index).getIds());
            objects[index] = obj;
        }
        return obj;
    }

    public int size() {
        if (fullyLoaded) {
            return objectList.size();
        }
        return entries.size();
    }

    public void add(int index, Object element) {
        loadFully();
        objectList.add(index, element);
    }

    public Object remove(int index) {
        loadFully();
        return objectList.remove(index);
    }

    public boolean add(Object o) {
        loadFully();
        return objectList.add(o);
    }

    public Object set(int index, Object element) {
        loadFully();
        return objectList.set(index, element);
    }

    public int indexOf(Object o) {
        if (fullyLoaded) {
            return objectList.indexOf(o);
        }
        return super.indexOf(o);
    }

    public int lastIndexOf(Object o) {
        if (fullyLoaded) {
            return objectList.lastIndexOf(o);
        }
        return super.lastIndexOf(o);
    }

    public void clear() {
        fullyLoaded = true;
        if (objectList != null) {
            objectList.clear();
        } else {
            objectList = new ArrayList();
        }
    }

    public boolean addAll(int index, Collection c) {
        loadFully();
        return objectList.addAll(index, c);
    }

    public boolean contains(Object o) {
        if (fullyLoaded) {
            return objectList.contains(o);
        }
        return super.contains(o);
    }

    public boolean remove(Object o) {
        loadFully();
        return objectList.remove(o);
    }

    public Object[] toArray() {
        if (fullyLoaded) {
            return objectList.toArray();
        }
        return super.toArray();
    }

    public Object[] toArray(Object[] a) {
        if (fullyLoaded) {
            return objectList.toArray(a);
        }
        return super.toArray(a);
    }

    public boolean containsAll(Collection c) {
        if (fullyLoaded) {
            return objectList.containsAll(c);
        }
        return super.containsAll(c);
    }

    public boolean addAll(Collection c) {
        loadFully();
        return objectList.addAll(c);
    }

    public boolean removeAll(Collection c) {
        loadFully();
        return objectList.removeAll(c);
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void loadFully() {
        if (fullyLoaded) {
            return;
        }
        objectList = new ArrayList(size());
        for (Iterator it = iterator(); it.hasNext();) {
            objectList.add(it.next());
        }
        // we don't use objects or entries any more, we can null it.
        objects = null;
        entries.clear();
        fullyLoaded = true;
    }
}
