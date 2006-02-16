package org.compass.annotations.test.metadata;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class MetaDataTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testMultipleMetaDatas() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals("value", a.getValue());

        CompassHits hits = session.find("value:value");
        assertEquals(0, hits.length());
        hits = session.find("value1:value");
        assertEquals(1, hits.length());
        hits = session.find("value2:value");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

}
