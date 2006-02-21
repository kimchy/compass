package org.compass.annotations.test.inheritance;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class InheritanceTests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class);
        conf.addClass(B.class);
    }

    public void testOverride() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        B b = new B();
        b.id = 1;
        b.value1 = "value1";
        b.value2 = "value2";
        session.save(b);

        b = (B) session.load(B.class, 1);
        assertEquals("value1", b.value1);
        assertEquals("value2", b.value2);

        Resource resource = session.loadResource(B.class, 1);
        assertNull(resource.get("value1"));
        assertNotNull(resource.get("value1e"));
        assertNotNull(resource.get("value2"));
        assertNotNull(resource.get("value2e"));

        tr.commit();
        session.close();
    }

}
