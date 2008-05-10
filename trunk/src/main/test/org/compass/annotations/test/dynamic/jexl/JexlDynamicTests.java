package org.compass.annotations.test.dynamic.jexl;

import java.util.Calendar;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class JexlDynamicTests extends AbstractAnnotationsTestCase {


    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
    }

    public void testSimpleExpression() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(1);
        a.setValue("value");
        a.setValue2("value2");
        Calendar cal = Calendar.getInstance();
        cal.set(1977, 4, 1);
        a.setDate(cal.getTime());
        session.save(a);

        Resource resource = session.loadResource(A.class, 1);
        assertEquals("valuevalue2", resource.getValue("test"));
        assertEquals("value", resource.getValue("test2"));
        assertEquals("1977", resource.getValue("date"));

        tr.commit();
        session.close();
    }
}
