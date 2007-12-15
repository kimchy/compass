package org.compass.core.test.managedid.nostore;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NoStoreManagedIdTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] {"managedid/nostore/mapping.cpm.xml"};
    }

    public void testNoStoreOnProeprtyLevel() {
        verifySimpleNoStoreId("a");
    }

    public void testNoStoreOnClassMappingLevel() {
        verifySimpleNoStoreId("a1");
    }

    public void testAutoWorks() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "value1";
        a.value2 = "value2";

        session.save("a2", a);

        a = (A) session.load("a2", "1");
        assertEquals(1, a.id);
        assertEquals("value1", a.value1);
        assertEquals("value2", a.value2);

        Resource resource = session.loadResource("a2", "1");
        assertNull(resource.getValue("$/a2/value2"));

        tr.commit();
        session.close();
    }

    public void verifySimpleNoStoreId(String alias) {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = "value1";
        a.value2 = "value2";

        session.save(alias, a);

        a = (A) session.load(alias, "1");
        assertEquals(1, a.id);
        assertNull(a.value1);
        assertEquals("value2", a.value2);

        Resource resource = session.loadResource(alias, "1");
        assertNull(resource.getValue("$/" + alias + "/value2"));

        tr.commit();
        session.close();
    }
}
