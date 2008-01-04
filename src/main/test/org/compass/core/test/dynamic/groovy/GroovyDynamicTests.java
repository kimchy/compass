package org.compass.core.test.dynamic.groovy;

import java.util.Calendar;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class GroovyDynamicTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"dynamic/groovy/A.cpm.xml"};
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

    public void testExpressionWithNullValue() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setDate(null);
        session.save("a2", a);

        Resource resource = session.loadResource("a2", new Long(1));
        assertEquals("moo", resource.getValue("test"));

        tr.commit();
        session.close();
    }

    public void testTwoCompassInstancesBuild() throws Exception {
        Compass compass2 = buildCompass();


        CompassSession session = compass2.openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.setId(new Long(1));
        a.setValue("value");
        a.setValue2("value2");
        session.save("a1", a);

        Resource resource = session.loadResource("a1", new Long(1));
        Property[] properties = resource.getProperties("test");
        assertEquals(1, properties.length);

        tr.commit();
        session.close();

        compass2.getSearchEngineIndexManager().clearCache();
        compass2.close();
    }
}
