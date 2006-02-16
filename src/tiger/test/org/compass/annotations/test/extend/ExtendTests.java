package org.compass.annotations.test.extend;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ExtendTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testCpmAndAnnotations() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        a.setValue2("value2");
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("value", a.getValue());
        assertEquals("value2", a.getValue2());

        CompassHits hits = session.find("value");
        assertEquals(1, hits.length());

        hits = session.find("value2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

}
