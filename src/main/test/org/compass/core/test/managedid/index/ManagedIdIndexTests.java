package org.compass.core.test.managedid.index;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ManagedIdIndexTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] { "managedid/index/mapping.cpm.xml" };
    }

    public void testSameMetaDataOnProeprtyDefaultTokenized() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, "test me", "test you");
        session.save("a1", a);

        CompassHits hits = session.find("a1.value1:me");
        assertEquals(1, hits.length());
        hits = session.find("a1.value2:me");
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testSameMetaDataOnProeprtyUntokenized() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, "test me", "test you");
        session.save("a2", a);

        CompassHits hits = session.find("a2.value1:me");
        assertEquals(0, hits.length());
        hits = session.find("a2.value2:me");
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }

    public void testSameMetaDataOnProeprtyDefaultWithSameMetaDataForId() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A(1, "test me", "test you");
        session.save("a3", a);

        CompassHits hits = session.find("a3.value1:me");
        assertEquals(1, hits.length());
        hits = session.find("a3.value2:me");
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }
}
