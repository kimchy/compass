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

package org.compass.core.test.array;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ArrayTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "array/Array.cpm.xml" };
    }

    public void testSimpleArray() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        SimpleArray sa = new SimpleArray();
        sa.setId(id);
        sa.setValue("test");
        sa.setStrings(new String[] { "test1", "test2" });
        session.save(sa);

        sa = (SimpleArray) session.load(SimpleArray.class, id);
        assertEquals("test", sa.getValue());
        assertEquals(2, sa.getStrings().length);
        assertEquals("test1", sa.getStrings()[0]);
        assertEquals("test2", sa.getStrings()[1]);

        session.delete(sa);
        sa = (SimpleArray) session.get(SimpleArray.class, id);
        assertNull(sa);

        tr.commit();
    }

    public void testAB() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("test");
        B b1 = new B();
        b1.setValue("test1");
        B b2 = new B();
        b2.setValue("test2");
        a.setArrB(new B[] { b1, b2 });

        session.save(a);

        a = (A) session.load(A.class, id);
        assertEquals("test", a.getValue());
        assertEquals(2, a.getArrB().length);
        assertEquals("test1", a.getArrB()[0].getValue());
        assertEquals("test2", a.getArrB()[1].getValue());

        session.delete(a);

        tr.commit();
    }

    public void testABWithNull() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("test");
        B b1 = new B();
        b1.setValue(null);
        B b2 = new B();
        b2.setValue(null);
        a.setArrB(new B[] { b1, b2 });

        session.save(a);

        a = (A) session.load(A.class, id);
        assertEquals("test", a.getValue());
        assertEquals(2, a.getArrB().length);
        assertNull(a.getArrB()[0]);
        assertNull(a.getArrB()[1]);

        tr.commit();
    }

    public void testABWithPropertyNull() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("test");
        B b1 = new B();
        b1.setValue("test1");
        b1.setValue2(null);
        B b2 = new B();
        b2.setValue(null);
        b2.setValue2("test2");
        a.setArrB(new B[] { b1, b2 });

        session.save(a);

        a = (A) session.load(A.class, id);
        assertEquals("test", a.getValue());
        assertEquals(2, a.getArrB().length);
        assertEquals("test1", a.getArrB()[0].getValue());
        assertNull(a.getArrB()[0].getValue2());
        assertNull(a.getArrB()[1].getValue());
        assertEquals("test2", a.getArrB()[1].getValue2());

        session.delete(a);

        tr.commit();
    }
    
    public void testXY() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long xId = new Long(1);
        Long y1Id = new Long(1);
        Long y2Id = new Long(2);
        X x = new X();
        x.setId(xId);
        x.setValue("xValue");
        Y y1 = new Y();
        y1.setId(y1Id);
        y1.setValue("yValue");
        session.save(y1);
        Y y2 = new Y();
        y2.setId(y2Id);
        y2.setValue("yValue");
        session.save(y2);
        x.setCy(new Y[] { y1, y2 });
        session.save(x);

        x = (X) session.load(X.class, xId);
        assertEquals("xValue", x.getValue());
        assertNotNull(x.getCy());
        assertEquals(2, x.getCy().length);
        Y y = x.getCy()[0];
        assertEquals(1, y.getId().longValue());
        y = x.getCy()[1];
        assertEquals(2, y.getId().longValue());

        tr.commit();
    }

}
