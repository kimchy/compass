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

package org.compass.core.test.component.cascade.simple;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * Simple tests for cascading operation between two root objects (A and B)
 * with component mapping.
 *
 * @author kimchy
 */
public class SimpleCascadeTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/cascade/simple/mapping.cpm.xml"};
    }

    public void testCascadeAll() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B(1, "bvalue");
        A a = new A(1, "avalue", b);
        // this should cause cascading for b as well
        session.create("aAll", a);

        a = (A) session.load("aAll", "1");
        session.load("b", "1");

        a.b.value = "bupdated";
        session.save("aAll", a);
        b = (B) session.load("b", "1");
        assertEquals("bupdated", b.value);

        session.delete("aAll", a);
        assertNull(session.get("aAll", "1"));
        assertNull(session.get("b", "1"));

        tr.commit();
        session.close();
    }

    public void testCascadeCreate() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B(1, "bvalue");
        A a = new A(1, "avalue", b);
        // this should cause cascading for b as well
        session.create("aCreate", a);

        a = (A) session.load("aCreate", "1");
        session.load("b", "1");

        a.b.value = "bupdated";
        // this will not cause b to get cascaded
        session.save("aCreate", a);
        b = (B) session.load("b", "1");
        assertEquals("bvalue", b.value);

        session.delete("aCreate", a);
        assertNull(session.get("aAll", "1"));
        assertNotNull(session.get("b", "1"));

        tr.commit();
        session.close();
    }

    public void testCascadeSave() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B(1, "bvalue");
        A a = new A(1, "avalue", b);
        // this should not cause cascading for b as well
        session.create("aSave", a);
        assertNull(session.get("b", "1"));
        session.create("b", b);

        a = (A) session.load("aSave", "1");
        session.load("b", "1");

        a.b.value = "bupdated";
        // this will cause b to get cascaded
        session.save("aSave", a);
        b = (B) session.load("b", "1");
        assertEquals("bupdated", b.value);

        session.delete("aSave", a);
        assertNull(session.get("aAll", "1"));
        assertNotNull(session.get("b", "1"));

        tr.commit();
        session.close();
    }

    public void testCascadeDelete() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B(1, "bvalue");
        A a = new A(1, "avalue", b);
        // this should not cause cascading for b as well
        session.create("aDelete", a);
        assertNull(session.get("b", "1"));
        session.create("b", b);

        a = (A) session.load("aDelete", "1");
        session.load("b", "1");

        a.b.value = "bupdated";
        // this will not cause b to get cascaded
        session.save("aDelete", a);
        b = (B) session.load("b", "1");
        assertEquals("bvalue", b.value);

        session.delete("aDelete", a);
        assertNull(session.get("aAll", "1"));
        assertNull(session.get("b", "1"));

        tr.commit();
        session.close();
    }
}
