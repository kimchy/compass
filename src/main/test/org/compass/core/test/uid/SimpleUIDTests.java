package org.compass.core.test.uid;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.spi.InternalResource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class SimpleUIDTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"uid/mapping.cpm.xml"};
    }

    public void testSingleId() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id1 = 1;
        a.id2 = 2;
        session.save("a1", a);

        Resource resource = session.loadResource("a1", 1);
        assertEquals("a1#1#", resource.getUID());
        // also check that the actual id is stored in the resource
        assertEquals("a1#1#", resource.getValue( ((InternalResource) resource).getResourceKey().getResourceMapping().getUIDPath() ));

        tr.commit();
        session.close();
    }

    public void testMultilpeId() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id1 = 1;
        a.id2 = 2;
        session.save("a2", a);

        Resource resource = session.loadResource("a2", 1, 2);
        assertEquals("a2#1#2#", resource.getUID());
        // also check that the actual id is stored in the resource
        assertEquals("a2#1#2#", resource.getValue( ((InternalResource) resource).getResourceKey().getResourceMapping().getUIDPath() ));

        tr.commit();
        session.close();
    }
}
