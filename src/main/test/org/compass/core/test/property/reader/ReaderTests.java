package org.compass.core.test.property.reader;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class ReaderTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/reader/mapping.cpm.xml"};
    }

    public void testReader() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id = new Long(1);
        ReaderType o = new ReaderType();
        o.setId(id);
        session.save(o);

        o = (ReaderType) session.load(ReaderType.class, id);
        assertEquals(id, o.getId());

        tr.commit();
    }
}
