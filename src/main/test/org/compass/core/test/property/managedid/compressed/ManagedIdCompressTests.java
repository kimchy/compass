package org.compass.core.test.property.managedid.compressed;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ManagedIdCompressTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/managedid/compressed/mapping.cpm.xml"};
    }

    public void testNoManagedIdCreatedForCompress() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a  = new A(1, "value");
        session.save("a", a);

        Resource resource = session.loadResource("a", "1");
        assertNull(resource.getProperty("$/a/value"));

        tr.commit();
        session.close();
    }
}
