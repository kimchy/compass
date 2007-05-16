package org.compass.core.test.reference.withprop;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReferenceWithPropTests extends AbstractTestCase {

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX, "b",
                new String[]{CompassEnvironment.Converter.TYPE},
                new String[]{BConverter.class.getName()});
    }

    protected String[] getMappings() {
        return new String[]{"reference/withprop/mapping.cpm.xml"};
    }

    public void testReferenceWithProperty() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.b = new B();
        a.b.id = new Long(1);
        a.b.value = "test";

        session.save(a);
        session.save(a.b);

        a = (A) session.load(A.class, "1");
        assertEquals("test", a.b.value);
        B b = (B) session.load(B.class, "1");
        assertEquals("test", b.value);

        // A test that shows we don't support property and reference on the same mapping
        CompassHits hits = session.find("test");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}
