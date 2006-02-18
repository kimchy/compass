package org.compass.core.test.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.compass.core.*;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class CollectionTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "collection/Collection.cpm.xml" };
    }
    
    public void testSimpleCollectionNull() {
        CompassSession session = openSession();
        CompassTransaction transaction = session.beginTransaction();

        SimpleTypeCollection o = new SimpleTypeCollection();
        Long id = new Long(1);
        o.setId(id);
        o.setValue("test");
        session.save(o);

        o = (SimpleTypeCollection) session.load(SimpleTypeCollection.class, id);
        assertEquals("test", o.getValue());
        assertNull(o.getStrings());

        session.delete(o);
        o = (SimpleTypeCollection) session.get(SimpleTypeCollection.class, id);
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

        session.save(o);

        o = (SimpleTypeCollection) session.load(SimpleTypeCollection.class, id);
        assertEquals("test", o.getValue());
        assertNotNull(o.getStrings());
        assertEquals(2, o.getStrings().size());
        ArrayList list = (ArrayList) o.getStrings();
        assertEquals("test1", list.get(0));
        assertEquals("test2", list.get(1));

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

        session.delete(o);
        o = (SimpleTypeCollection) session.get(SimpleTypeCollection.class, id);
        assertNull(o);

        transaction.commit();
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
        session.save(a);
        
        a = (A) session.load(A.class, id);
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
        session.save(a);
        
        a = (A) session.load(A.class, id);
        assertEquals("test", a.getValue());
        List list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(0, list.size());

        session.delete(a);
        a = (A) session.get(A.class, id);
        assertNull(a);

        tr.commit();
    }

    public void testABWithSingleNull() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        A a = new A();
        a.setId(id);
        a.setValue("test");
        B b2 = new B();
        b2.setValue("test");
        Collection bs = new LinkedList();
        bs.add(null);
        bs.add(b2);
        a.setCb(bs);
        session.save(a);
        
        a = (A) session.load(A.class, id);
        assertEquals("test", a.getValue());
        List list = (List) a.getCb();
        assertNotNull(list);
        assertEquals(1, list.size());
        b2 = (B) list.get(0);
        assertEquals("test", b2.getValue());

        session.delete(a);
        a = (A) session.get(A.class, id);
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
        session.save(a);
        
        a = (A) session.load(A.class, id);
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
        
        session.delete(a);
        a = (A) session.get(A.class, id);
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
}
