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

package org.compass.core.test.poly;

import java.util.ArrayList;
import java.util.List;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class PolyTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "poly/Poly.cpm.xml" };
    }

    public void testPoly() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        PolyImpl1 impl1 = new PolyImpl1();
        impl1.setId(new Long(1));
        impl1.setValue("test1");
        session.save("poly", impl1);

        PolyImpl2 impl2 = new PolyImpl2();
        impl2.setId(new Long(2));
        impl2.setValue("test2");
        session.save("poly", impl2);

        impl1 = (PolyImpl1) session.load("poly", new Long(1));
        assertEquals("test1", impl1.getValue());

        impl2 = (PolyImpl2) session.load("poly", new Long(2));
        assertEquals("test2", impl2.getValue());

        tr.commit();
    }

    public void testComponent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Component comp = new Component();
        comp.setId(new Long(1));

        PolyImpl1 impl1 = new PolyImpl1();
        impl1.setId(new Long(1));
        impl1.setValue("test1");
        comp.setPi1(impl1);

        PolyImpl2 impl2 = new PolyImpl2();
        impl2.setId(new Long(2));
        impl2.setValue("test2");
        comp.setPi2(impl2);

        session.save(comp);

        comp = (Component) session.load(Component.class, new Long(1));
        assertEquals(1, comp.getId().longValue());
        assertNotNull(comp.getPi1());
        assertEquals(PolyImpl1.class.getName(), comp.getPi1().getClass().getName());
        assertEquals("test1", comp.getPi1().getValue());
        assertNotNull(comp.getPi2());
        assertEquals(PolyImpl2.class.getName(), comp.getPi2().getClass().getName());
        assertEquals("test2", comp.getPi2().getValue());

        tr.commit();
    }

    public void testCol() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Col col = new Col();
        col.setId(new Long(1));
        List list = new ArrayList();

        PolyImpl1 impl1 = new PolyImpl1();
        impl1.setId(new Long(1));
        impl1.setValue("test1");
        list.add(impl1);

        PolyImpl2 impl2 = new PolyImpl2();
        impl2.setId(new Long(2));
        impl2.setValue("test2");
        list.add(impl2);

        col.setList(list);
        session.save(col);

        col = (Col) session.load(Col.class, new Long(1));
        assertEquals(1, col.getId().longValue());
        assertNotNull(col.getList());
        assertEquals(2, col.getList().size());
        assertEquals(PolyImpl1.class.getName(), col.getList().get(0).getClass().getName());
        assertEquals("test1", ((PolyImpl1) col.getList().get(0)).getValue());
        assertEquals(PolyImpl2.class.getName(), col.getList().get(1).getClass().getName());
        assertEquals("test2", ((PolyImpl2) col.getList().get(1)).getValue());

        tr.commit();
    }

    public void testArr() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Arr arr = new Arr();
        arr.setId(id);

        PolyImpl1 impl1 = new PolyImpl1();
        impl1.setId(new Long(1));
        impl1.setValue("test1");

        PolyImpl2 impl2 = new PolyImpl2();
        impl2.setId(new Long(2));
        impl2.setValue("test2");

        arr.setPi(new PolyInterface[] { impl1, impl2 });
        session.save(arr);

        arr = (Arr) session.load(Arr.class, id);
        assertEquals(1, arr.getId().longValue());
        assertEquals(2, arr.getPi().length);
        assertEquals(PolyImpl1.class.getName(), arr.getPi()[0].getClass().getName());
        assertEquals("test1", arr.getPi()[0].getValue());
        assertEquals(PolyImpl2.class.getName(), arr.getPi()[1].getClass().getName());
        assertEquals("test2", arr.getPi()[1].getValue());

        tr.commit();
    }

    public void testPolyClass() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        PolyImpl1 impl1 = new PolyImpl1();
        impl1.setId(new Long(1));
        impl1.setValue("test1");
        session.save("poly2", impl1);

        PolyImpl2 impl2 = new PolyImpl2();
        impl2.setId(new Long(2));
        impl2.setValue("test2");
        session.save("poly2", impl2);

        impl1 = (PolyImpl1) session.load("poly2", new Long(1));
        assertEquals("test1", impl1.getValue());

        // this will fail on a class cast if we did not have the poly-class
        impl1 = (PolyImpl1) session.load("poly2", new Long(2));
        assertEquals("test2", impl1.getValue());

        tr.commit();
    }
}
