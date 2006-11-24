package org.compass.core.test.property.nointernalid;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class NoInternalIdTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/nointernalid/mapping.cpm.xml"};
    }

    public void testNoInternalId() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        NoInternalId o = new NoInternalId();
        o.setId(id);
        o.setValue("test");
        session.save(o);

        o = (NoInternalId) session.load(NoInternalId.class, id);
        assertEquals("test", o.getValue());

        session.delete(o);

        tr.commit();
        session.close();
    }
}
