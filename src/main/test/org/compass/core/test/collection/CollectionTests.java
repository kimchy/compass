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

package org.compass.core.test.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CollectionTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"collection/Collection.cpm.xml"};
    }

    public void testSimpleCollectionNull() {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        SimpleTypeCollection o = new SimpleTypeCollection();
        Long id = new Long(1);
        o.setId(id);
        o.setValue("test");
        session.save("simple-type-col", o);

        o = (SimpleTypeCollection) session.load("simple-type-col", id);
        assertEquals("test", o.getValue());
        assertNull(o.getStrings());

        session.delete("simple-type-col", o);
        o = (SimpleTypeCollection) session.get("simple-type-col", id);
        assertNull(o);

        transaction.commit();
    }

    public void testSimpleCollection() {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        SimpleTypeCollection o = new SimpleTypeCollection();
        Long id = new Long(1);
        Collection stringCol = new ArrayList();
        stringCol.add("test1");
        stringCol.add("test2");
        o.setId(id);
        o.setValue("test");
        o.setStrings(stringCol);

        session.save("simple-type-col", o);

        o = (SimpleTypeCollection) session.load("simple-type-col", id);
        assertEquals("test", o.getValue());
        assertNotNull(o.getStrings());
        assertEquals(2, o.getStrings().size());
        ArrayList list = (ArrayList) o.getStrings();
        assertEquals("test1", list.get(0));
        assertEquals("test2", list.get(1));

        Resource r = session.loadResource("simple-type-col", id);
        assertEquals("2", r.getValue("$/simple-type-col/strings/colSize"));

        CompassHits result = session.find("mvalue:test1");
        o = (SimpleTypeCollection) result.data(0);
        assertEquals("test", o.getValue());
        assertNotNull(o.getStrings());
        assertEquals(2, o.getStrings().size());
        list = (ArrayList) o.getStrings();
        assertEquals("test1", list.get(0));
        assertEquals("test2", list.get(1));

        boolean foundStoredClassType = false;
        Resource resource = result.resource(0);
        Property[] properties = resource.getProperties();
        for (int i = 0; i < properties.length; i++) {
            String value = properties[i].getStringValue();
            if (value != null) {
                if (value.indexOf("list") != -1) {
                    foundStoredClassType = true;
                }
            }
        }
        if (!foundStoredClassType) {
            fail("Failed to find stored collection class type");
        }

        session.delete("simple-type-col", o);
        o = (SimpleTypeCollection) session.get("simple-type-col", id);
        assertNull(o);

        transaction.commit();
    }

    public void testNoMetaDataStored() {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        SimpleTypeCollection o = new SimpleTypeCollection();
        Long id = new Long(1);
        Collection stringCol = new ArrayList();
        stringCol.add("test1");
        stringCol.add("test2");
        o.setId(id);
        o.setValue("test");
        o.setStrings(stringCol);

        session.save("no-metadata-stored", o);

        o = (SimpleTypeCollection) session.load("no-metadata-stored", id);
        assertEquals("test", o.getValue());
        assertEquals(0, o.getStrings().size());
        Resource r = session.loadResource("no-metadata-stored", id);
        assertEquals("0", r.getValue("$/no-metadata-stored/strings/colSize"));

        transaction.commit();
        session.close();
    }

    public void testExplicitCollection() {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        ExplicitTypeCollection o = new ExplicitTypeCollection();
        Long id = new Long(1);
        Collection stringCol = new ArrayList();
        stringCol.add("test1");
        stringCol.add("test2");
        o.setId(id);
        o.setValue("test");
        o.setStrings(stringCol);

        session.save(o);

        o = (ExplicitTypeCollection) session.load(ExplicitTypeCollection.class, id);
        assertEquals("test", o.getValue());
        assertNotNull(o.getStrings());
        assertEquals(2, o.getStrings().size());
        ArrayList list = (ArrayList) o.getStrings();
        assertEquals("test1", list.get(0));
        assertEquals("test2", list.get(1));

        CompassHits result = session.find("mvalue:test1");
        o = (ExplicitTypeCollection) result.data(0);
        assertEquals("test", o.getValue());
        assertNotNull(o.getStrings());
        assertEquals(2, o.getStrings().size());
        list = (ArrayList) o.getStrings();
        assertEquals("test1", list.get(0));
        assertEquals("test2", list.get(1));

        Resource resource = result.resource(0);
        Property[] properties = resource.getProperties();
        for (int i = 0; i < properties.length; i++) {
            String value = properties[i].getStringValue();
            if (value != null) {
                if (value.indexOf("ArrayList") != -1) {
                    fail("should not store the collection class");
                }
            }
        }

        session.delete(o);
        o = (ExplicitTypeCollection) session.get(ExplicitTypeCollection.class, id);
        assertNull(o);

        transaction.commit();
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
        Collection bs = new LinkedList();
        bs.add(b1);
        bs.add(b2);
        a.setCb(bs);
        session.save("a", a);

        a = (A) session.load("a", id);
        assertEquals("test", a.getValue());
        List list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("test1", ((B) list.get(0)).getValue());
        assertEquals("test2", ((B) list.get(1)).getValue());

        CompassHits result = session.find("value:test1");
        a = (A) result.data(0);
        assertEquals("test", a.getValue());
        list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals("test1", ((B) list.get(0)).getValue());
        assertEquals("test2", ((B) list.get(1)).getValue());

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
        Collection bs = new LinkedList();
        bs.add(b1);
        bs.add(b2);
        a.setCb(bs);
        session.save("a", a);

        a = (A) session.load("a", id);
        assertEquals("test", a.getValue());
        List list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(0, list.size());

        session.delete("a", a);
        a = (A) session.get("a", id);
        assertNull(a);

        tr.commit();
    }

    public void testABWithBPropertyNull() {
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
        b2.setValue("test2");
        b2.setValue2("value2");
        Collection bs = new LinkedList();
        bs.add(b1);
        bs.add(b2);
        a.setCb(bs);
        session.save("a", a);

        a = (A) session.load("a", id);
        assertEquals("test", a.getValue());
        List list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(2, list.size());

        b1 = (B) list.get(0);
        assertEquals("test1", b1.getValue());
        assertEquals(null, b1.getValue2());

        b2 = (B) list.get(1);
        assertEquals("test2", b2.getValue());
        assertEquals("value2", b2.getValue2());

        session.delete("a", a);
        a = (A) session.get("a", id);
        assertNull(a);

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
        ArrayList list = new ArrayList();
        list.add(y1);
        list.add(y2);
        x.setCy(list);
        session.save(x);

        x = (X) session.load(X.class, xId);
        assertEquals("xValue", x.getValue());
        assertNotNull(x.getCy());
        assertEquals(2, x.getCy().size());
        Y y = (Y) ((ArrayList) x.getCy()).get(0);
        assertEquals(1, y.getId().longValue());
        y = (Y) ((ArrayList) x.getCy()).get(1);
        assertEquals(2, y.getId().longValue());

        tr.commit();
    }

    public void testThreeLevelDeepComponentCollection() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a1 = new A();
        a1.setId(new Long(1));
        a1.setValue("a1");
        a1.setCb(new ArrayList());

        A a21 = new A();
        a21.setValue("a21");
        a21.setCb(new ArrayList());
        a1.getCb().add(a21);

        A a31 = new A();
        a31.setValue("a31");
        a21.getCb().add(a31);

        A a32 = new A();
        a32.setValue("a32");
        a21.getCb().add(a32);

        A a22 = new A();
        a22.setValue("a22");
        a22.setCb(new ArrayList());
        a1.getCb().add(a22);

        A a33 = new A();
        a33.setValue("a33");
        a22.getCb().add(a33);

        A a34 = new A();
        a34.setValue("a34");
        a22.getCb().add(a34);

        A a35 = new A();
        a35.setValue("a35");
        a22.getCb().add(a35);

        session.save("a1", a1);

        a1 = (A) session.load("a1", new Long(1));
        assertEquals("a1", a1.getValue());
        assertEquals(2, a1.getCb().size());
        a21 = (A) ((List) a1.getCb()).get(0);
        assertEquals("a21", a21.getValue());
        a22 = (A) ((List) a1.getCb()).get(1);
        assertEquals("a22", a22.getValue());

        assertEquals(2, a21.getCb().size());
        a31 = (A) ((List) a21.getCb()).get(0);
        assertEquals("a31", a31.getValue());
        a32 = (A) ((List) a21.getCb()).get(1);
        assertEquals("a32", a32.getValue());

        assertEquals(3, a22.getCb().size());
        a33 = (A) ((List) a22.getCb()).get(0);
        assertEquals("a33", a33.getValue());
        a34 = (A) ((List) a22.getCb()).get(1);
        assertEquals("a34", a34.getValue());
        a35 = (A) ((List) a22.getCb()).get(2);
        assertEquals("a35", a35.getValue());

        tr.commit();
        session.close();
    }
}
