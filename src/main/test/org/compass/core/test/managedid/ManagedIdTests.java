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

package org.compass.core.test.managedid;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ManagedIdTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "managedid/ManagedId.cpm.xml" };
    }

    public void testAutoDifferentMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue1("value1");
        a.setValue2("value2");

        session.save(a);

        Resource r = session.getResource(A.class, id);
        String val = r.getValue("$/a/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/a/value1");
        assertNull(val);
        val = r.getValue("$/a/value2");
        assertNull(val);

        tr.commit();
    }

    public void testAutoSameMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        B b = new B();
        b.setId(id);
        b.setValue1("value1");
        b.setValue2("value2");

        session.save(b);

        Resource r = session.getResource(B.class, id);
        String val = r.getValue("$/b/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/b/value1");
        assertNotNull(val);
        assertEquals("value1", val);
        val = r.getValue("$/b/value2");
        assertNotNull(val);
        assertEquals("value2", val);

        tr.commit();
    }

    public void testAutoMultipleMetaDataWithDifferent() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        C c = new C();
        c.setId(id);
        c.setValue1("value1");
        c.setValue2("value2");

        session.save(c);

        Resource r = session.getResource(C.class, id);
        String val = r.getValue("$/c/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/c/value1");
        assertNull(val);
        val = r.getValue("$/c/value2");
        assertNull(val);

        tr.commit();
    }

    public void testAutoConstnat() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        Constant c = new Constant();
        c.setId(id);
        c.setValue1("value1");
        c.setValue2("value2");

        session.save(c);

        Resource r = session.getResource(Constant.class, id);
        String val = r.getValue("$/constant/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/constant/value1");
        assertNotNull(val);
        val = r.getValue("$/constant/value2");
        assertNull(val);

        tr.commit();
    }

    public void testAutoWithComponents() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        ChildComponent c = new ChildComponent();
        c.setValue1("value1");
        c.setValue2("value2");

        Long id = new Long(1);
        ParentComponent parentComponent = new ParentComponent();

        parentComponent.setId(id);
        parentComponent.setValue1("value1");
        parentComponent.setValue2("value2");
        parentComponent.setChildComponent(c);

        session.save(parentComponent);

        Resource r = session.getResource(ParentComponent.class, id);
        String val = r.getValue("$/parentComp/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/parentComp/value1");
        assertNotNull(val);
        val = r.getValue("$/parentComp/value2");
        assertNotNull(val);
        val = r.getValue("$/parentComp/childComponent/value1");
        assertNotNull(val);
        val = r.getValue("$/parentComp/childComponent/value2");
        assertNotNull(val);

        tr.commit();
    }

    public void testAutoWitRreferences() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ChildReference cr = new ChildReference();
        cr.setId(id);
        cr.setValue1("value1");
        cr.setValue2("value2");

        session.save(cr);

        ParentReference pr = new ParentReference();
        pr.setId(id);
        pr.setValue1("value1");
        pr.setValue2("value2");
        pr.setChildReference(cr);

        session.save(pr);

        Resource r = session.getResource(ParentReference.class, id);
        String val = r.getValue("$/parentRef/id");
        assertNotNull(val);
        assertEquals("1", val);
        val = r.getValue("$/parentRef/value1");
        assertNull(val);
        val = r.getValue("$/parentRef/value2");
        assertNull(val);
        val = r.getValue("$/parentRef/childReference/id");
        assertNotNull(val);
        assertEquals("1", val);

        tr.commit();
    }

    public void testNoManagedId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        D d = new D();
        d.setId(id);
        d.setValue("value1");

        session.save(d);

        d = (D) session.load(D.class, new Long(1));
        assertNotNull(d);
        assertEquals(new Long(1), d.getId());
        assertNull(d.getValue());

        tr.commit();
    }
}
