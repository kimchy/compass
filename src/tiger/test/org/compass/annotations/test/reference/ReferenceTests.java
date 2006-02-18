package org.compass.annotations.test.reference;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ReferenceTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class);
    }

    public void testSimpleReference() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value = "avalue";

        B b = new B();
        b.id = 1;
        b.value = "bvalue";
        a.b = b;

        B b1 = new B();
        b1.id = 2;
        b1.value = "bvalue1";

        B b2 = new B();
        b2.id = 3;
        b2.value = "bvalue2";
        ArrayList<B> bValues = new ArrayList<B>();
        bValues.add(b1);
        bValues.add(b2);
        a.bValues = bValues;

        session.save(b);
        session.save(b1);
        session.save(b2);
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("avalue", a.value);
        assertEquals("bvalue", a.b.value);
        assertEquals("bvalue1", a.bValues.get(0).value);
        assertEquals("bvalue2", a.bValues.get(1).value);
        b = (B) session.load(B.class, 1);
        assertEquals("bvalue", b.value);

        CompassHits hits = session.find("bvalue");
        assertEquals(1, hits.length());
        b = (B) hits.data(0);
        assertEquals("bvalue", b.value);

        hits = session.find("avalue");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals("avalue", a.value);
        assertEquals("bvalue", a.b.value);

        tr.commit();
        session.close();
    }

}
