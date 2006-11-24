package org.compass.core.test.property.multiplemetadata;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MultipleMetaDataTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"property/multiplemetadata/mapping.cpm.xml"};
    }

    public void testMultipleMetaData() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        Long id1 = new Long(1);
        MultipleMetaData o1 = new MultipleMetaData();
        o1.setId(id1);
        o1.setValue1("test1");
        o1.setValue2("test2");
        session.save(o1);
        Long id2 = new Long(2);
        MultipleMetaData o2 = new MultipleMetaData();
        o2.setId(id2);
        o2.setValue1("test1");
        o2.setValue2("testNO");
        session.save(o2);

        CompassHits list = session.find("join2:test1");
        assertEquals(2, list.getLength());

        list = session.find("value21:testNO");
        assertEquals(1, list.getLength());

        tr.commit();
    }
}
