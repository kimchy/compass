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

package org.compass.core.test.reference.collection.lazy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceCollection;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class LazyReferenceCollectionTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"reference/collection/lazy/mapping.cpm.xml"};
    }

    public void testSimpleLazyCollection() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        initData(session);

        A a = session.load(A.class, 1);
        assertTrue(a.bList instanceof LazyReferenceCollection);
        assertEquals(2, a.bList.size());
        assertFalse(((LazyReferenceCollection) a.bList).isFullyLoaded());
        assertTrue(a.bSet instanceof LazyReferenceCollection);
        assertEquals(2, a.bSet.size());
        assertFalse(((LazyReferenceCollection) a.bSet).isFullyLoaded());

        tr.commit();
        session.close();
    }

    public void testLazyCollectionIteartor() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        initData(session);

        A a = session.load(A.class, 1);
        Iterator<B> it = a.bList.iterator();
        assertEquals(1, it.next().id);
        assertEquals(2, it.next().id);

        tr.commit();
        session.close();
    }

    public void testSimpleLazyCollectionWithModifications() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        initData(session);

        A a = session.load(A.class, 1);
        assertFalse(((LazyReferenceCollection) a.bList).isFullyLoaded());
        B b3 = new B();
        b3.id = 3;
        b3.value = "b3";
        a.bList.add(b3);
        assertTrue(((LazyReferenceCollection) a.bList).isFullyLoaded());
        session.save(a);

        a = session.load(A.class, 1);
        assertEquals(3, a.bList.size());
        assertFalse(((LazyReferenceCollection) a.bList).isFullyLoaded());

        tr.commit();
        session.close();
    }

    private void initData(CompassSession session) {
        B b1 = new B();
        b1.id = 1;
        b1.value = "b1";

        B b2 = new B();
        b2.id = 2;
        b2.value = "b2";

        A a = new A();
        a.id = 1;
        a.bList = new ArrayList<B>();
        a.bList.add(b1);
        a.bList.add(b2);

        a.bSet = new HashSet<B>();
        a.bSet.add(b1);
        a.bSet.add(b2);

        session.save(a);
    }
}
