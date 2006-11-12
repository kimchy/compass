package org.compass.core.test.component.nullvalue;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NullValueTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"component/nullvalue/mapping.cpm.xml"};
    }

    public void testB1WithC1AndC2AsNulls() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, "avalue");
        a.b1 = new B("b1value");
        a.b2 = new B("b2value");
        a.b2.c1 = new C("b2c1value");
        a.b2.c2 = new C("b2c2value");

        session.save("a", a);

        a = (A) session.load("a", new Integer(1));
        assertEquals(a.b1.value, "b1value");
        assertNull(a.b1.c1);
        assertNull(a.b1.c2);
        assertEquals(a.b2.value, "b2value");
        assertEquals(a.b2.c1.value, "b2c1value");
        assertEquals(a.b2.c2.value, "b2c2value");

        tr.commit();
        session.close();
    }
}
