package org.compass.annotations.test.nounmarshall.component;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.converter.ConversionException;

/**
 * @author kimchy
 */
public class ABTests extends AbstractAnnotationsTestCase {


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testBSingleValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        a.b = b;
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(4, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(2, resource.getProperties("value").length);

        try {
            session.load(A.class, 1);
            fail();
        } catch (ConversionException e) {
            // success
        }

        tr.commit();
        session.close();
    }

    public void testBwoValues() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        B b = new B();
        b.value = "bvalue";
        b.value2 = "bvalue2";
        a.b = b;
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);

        try {
            session.load(A.class, 1);
            fail();
        } catch (ConversionException e) {
            // success
        }

        tr.commit();
        session.close();
    }

    public void testBCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.bs = new ArrayList<B>();
        B b = new B();
        b.value = "bvalue11";
        b.value2 = "bvalue12";
        a.bs.add(b);
        b = new B();
        b.value = "bvalue21";
        b.value = "bvalue22";
        a.bs.add(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(6, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(4, resource.getProperties("value").length);

        tr.commit();
        session.close();
    }

    public void testBCollectionWithNullValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "value";
        a.bs = new ArrayList<B>();
        B b = new B();
        b.value = null;
        b.value2 = "bvalue12";
        a.bs.add(b);
        b = new B();
        b.value = "bvalue21";
        b.value = "bvalue22";
        a.bs.add(b);
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertNotNull(resource);
        assertEquals(5, resource.getProperties().length);
        assertEquals("A", resource.getAlias());
        assertEquals(3, resource.getProperties("value").length);

        tr.commit();
        session.close();
    }
}
