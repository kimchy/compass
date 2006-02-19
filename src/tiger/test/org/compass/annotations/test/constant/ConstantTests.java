package org.compass.annotations.test.constant;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class ConstantTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;

        session.save(a);

        a = (A) session.load(A.class, 1);

        CompassHits hits = session.find("val11");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        hits = session.find("const2:val21");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        tr.commit();
        session.close();
    }
}
