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

package org.compass.core.test.component.deephierarchy;

import java.util.ArrayList;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class DeepHierarchyTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/deephierarchy/mapping.cpm.xml"};
    }

    public void testBLevelNulls() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        B b1 = new B();
        b1.id = 1;
        a.bs = new ArrayList();
        a.bs.add(null);
        a.bs.add(b1);

        session.save("a", a);

        // compass will not store nulls within collections
        a = (A) session.load("a", new Integer(1));
        assertEquals(1, a.bs.size());

        tr.commit();
        session.close();
    }

    public void testCLevelNulls1() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        B b1 = new B();
        b1.id = 1;
        B b2 = new B();
        b2.id = 2;
        a.bs = new ArrayList();
        a.bs.add(b1);
        a.bs.add(b2);

        C c1 = new C();
        c1.id = 1;
        C c2 = new C();
        c2.id = 2;
        b2.cs = new ArrayList();
        b2.cs.add(c1);
        b2.cs.add(c2);

        session.save("a", a);

        a = (A) session.load("a", new Integer(1));
        assertEquals(2, a.bs.size());
        b1 = (B) a.bs.get(0);
        assertNull(b1.cs);
        b2 = (B) a.bs.get(1);
        assertNotNull(b2);
        assertEquals(2, b2.cs.size());
        assertEquals(1, ((C) b2.cs.get(0)).id);
        assertEquals(2, ((C) b2.cs.get(1)).id);

        tr.commit();
        session.close();
    }

    public void testCLevelNullsInterlieved() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        B b1 = new B();
        b1.id = 1;
        B b2 = new B();
        b2.id = 2;
        a.bs = new ArrayList();
        a.bs.add(b1);
        a.bs.add(b2);

        C c1 = new C();
        c1.id = 1;
        b1.cs = new ArrayList();
        b1.cs.add(null);
        b1.cs.add(c1);


        C c2 = new C();
        c2.id = 2;
        b2.cs = new ArrayList();
        b2.cs.add(null);
        b2.cs.add(c1);
        b2.cs.add(c2);

        session.save("a", a);

        a = (A) session.load("a", new Integer(1));
        assertEquals(2, a.bs.size());
        b1 = (B) a.bs.get(0);
        assertNotNull(b1.cs);
        assertEquals(1, b1.cs.size());
        assertEquals(1, ((C) b1.cs.get(0)).id);
        b2 = (B) a.bs.get(1);
        assertNotNull(b2);
        assertEquals(2, b2.cs.size());
        assertEquals(1, ((C) b2.cs.get(0)).id);
        assertEquals(2, ((C) b2.cs.get(1)).id);

        tr.commit();
        session.close();
    }
}
