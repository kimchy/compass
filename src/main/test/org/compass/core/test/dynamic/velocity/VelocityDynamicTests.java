package org.compass.core.test.dynamic.velocity;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class VelocityDynamicTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"dynamic/velocity/A.cpm.xml"};
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
        assertEquals("value value2", resource.getValue("test"));

        tr.commit();
        session.close();
    }
}
