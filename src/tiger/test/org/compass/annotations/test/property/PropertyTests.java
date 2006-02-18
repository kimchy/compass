package org.compass.annotations.test.property;

import java.util.ArrayList;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class PropertyTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testCollectionWithGenericsParameter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        ArrayList<String> values = new ArrayList<String>();
        values.add("test1");
        values.add("test2");
        a.values = values;

        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals(values, a.values);

        CompassHits hits = session.find("test1");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);
        hits = session.find("test2");
        assertEquals(1, hits.length());
        a = (A) hits.data(0);
        assertEquals(1, a.id);

        tr.commit();
        session.close();
    }

}
