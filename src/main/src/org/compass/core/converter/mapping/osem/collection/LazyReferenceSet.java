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

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.spi.InternalCompassSession;

/**
 * A lazy reference set. If no dirty operations are perfomed, will lazily load references from the
 * search engine. Once a dirty operation is perfomed, will {@link #loadFully()}, and then all operations
 * will be perfoemd on the loaded list.
 *
 * @author kimchy
 */
public class LazyReferenceSet extends AbstractSet implements LazyReferenceCollection {

    private InternalCompassSession session;

    private final ArrayList<LazyReferenceEntry> entries;

    private Object[] objects;

    private Set objectSet;

    private AbstractCollectionMapping.CollectionType collectionType;

    private boolean fullyLoaded = false;

    public LazyReferenceSet(InternalCompassSession session, int size, AbstractCollectionMapping.CollectionType collectionType) {
        this.session = session;
        this.entries = new ArrayList<LazyReferenceEntry>(size);
        this.collectionType = collectionType;
        this.objects = new Object[size];
    }

    public void addLazyEntry(LazyReferenceEntry entry) {
        entries.add(entry);
    }

    public Iterator iterator() {
        if (fullyLoaded) {
            return objectSet.iterator();
        }
        return new InternalIterator();
    }

    public int size() {
        if (fullyLoaded) {
            return objectSet.size();
        }
        return entries.size();
    }

    public boolean add(Object o) {
        loadFully();
        return objectSet.add(o);
    }

    public boolean remove(Object o) {
        loadFully();
        return objectSet.remove(o);
    }

    public boolean removeAll(Collection c) {
        loadFully();
        return objectSet.removeAll(c);
    }

    public boolean contains(Object o) {
        if (fullyLoaded) {
            return objectSet.contains(o);
        }
        return super.contains(o);
    }

    public boolean addAll(Collection c) {
        loadFully();
        return objectSet.addAll(c);
    }

    public void clear() {
        fullyLoaded = true;
        if (objectSet != null) {
            objectSet.clear();
        } else {
            objectSet = createSet();
        }
    }

    public boolean isFullyLoaded() {
        return fullyLoaded;
    }

    public void loadFully() {
        if (fullyLoaded) {
            return;
        }
        objectSet = createSet();
        for (Iterator it = iterator(); it.hasNext();) {
            objectSet.add(it.next());
        }
        // we don't use objects or entries any more, we can null it.
        objects = null;
        entries.clear();
        fullyLoaded = true;
    }

    private Set createSet() {
        if (collectionType == AbstractCollectionMapping.CollectionType.SET) {
            return new HashSet(entries.size());
        } else if (collectionType == AbstractCollectionMapping.CollectionType.SORTED_SET) {
            return new TreeSet();
        } else if (collectionType == AbstractCollectionMapping.CollectionType.LINKED_HASH_SET) {
            return new LinkedHashSet(entries.size());
        } else {
            throw new IllegalStateException("Should not happen, internal compass error");
        }
    }

    private class InternalIterator implements Iterator {

        private int cursor;

        private Iterator dirtyIterator;

        public boolean hasNext() {
            if (dirtyIterator != null) {
                return dirtyIterator.hasNext();
            }
            return cursor < entries.size();
        }

        public Object next() {
            if (dirtyIterator != null) {
                return dirtyIterator.next();
            }
            Object obj = objects[cursor];
            if (obj == null) {
                obj = session.get(entries.get(cursor).getAlias(), entries.get(cursor).getIds());
                objects[cursor] = obj;
            }
            cursor++;
            return obj;
        }

        public void remove() {
            loadFully();
            if (dirtyIterator != null) {
                dirtyIterator.remove();
            }
            dirtyIterator = objectSet.iterator();
            for (int i = 0; i < cursor; i++) {
                dirtyIterator.next();
            }
            dirtyIterator.remove();
        }
    }
}
