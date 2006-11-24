package org.compass.core.test.property.override;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class OverrideTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/override/mapping.cpm.xml"};
    }

    public void testOverrideIdsAndProperties() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        PropertyOverride po = new PropertyOverride();
        po.setId(id);
        po.setValue("value");

        session.save("po", po);

        Resource r = session.loadResource("po", id);
        assertNull(r.getProperty("wrongId"));
        assertNull(r.getProperty("wrongValue"));
        assertNotNull(r.getProperty("overrideId"));
        assertNotNull(r.getProperty("overrideValue"));

        id = new Long(1);
        po = new PropertyOverride();
        po.setId(id);
        po.setValue("value");

        session.save("po1", po);

        r = session.loadResource("po1", id);
        assertNull(r.getProperty("wrongId"));
        assertNotNull(r.getProperty("wrongValue"));
        assertNotNull(r.getProperty("overrideId"));
        assertNotNull(r.getProperty("overrideValue"));

        tr.commit();
        session.close();
    }
}
