package org.compass.annotations.test.classconverter.simple;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class SimpleClassConverterTests extends AbstractAnnotationsTestCase {

    @Override
    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testSimpleClassConverter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.zipcode = new ZipCode("test");
        a.zipcode2 = new ZipCode("best");
        session.save(a);

        assertEquals(0, session.find("test").length());
        assertEquals(1, session.find("zipcode:test").length());
        assertEquals(1, session.find("best").length());
        assertEquals(1, session.find("zipcode2:best").length());

        tr.commit();
        session.close();
    }
}
