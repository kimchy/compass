package org.compass.core.test.dynamic.el;

import java.util.Calendar;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class JakartaElDynamicTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"dynamic/el/A.cpm.xml"};
    }

    public void testSimpleExpression() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue("value");
        a.setValue2("value2");
        session.save("a1", a);

        Resource resource = session.loadResource("a1", new Long(1));
        assertEquals("valuevalue2", resource.getValue("test"));

        CompassHits hits = session.find("valuevalue2");
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }

    public void testExpressionWithFormat() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 1);
        a.setDate(cal.getTime());
        session.save("a2", a);

        Resource resource = session.loadResource("a2", new Long(1));
        assertEquals("1977", resource.getValue("test"));

        tr.commit();
        session.close();
    }
}
