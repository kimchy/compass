package org.compass.core.converter.basic;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class EnumTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testEnumConverter() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.value1 = AType.TEST1;
        a.value2 = AType.TEST2;
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals(AType.TEST1, a.value1);
        assertEquals(AType.TEST2, a.value2);

        tr.commit();
        session.close();
    }

}
