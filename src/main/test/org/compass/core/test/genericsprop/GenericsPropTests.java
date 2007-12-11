package org.compass.core.test.genericsprop;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class GenericsPropTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] {"genericsprop/genericsprop.cpm.xml"};
    }

    public void testXmlMappingWithGenericsCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        a.values.add("value1");
        a.values.add("value2");
        session.save(a);

        a = (A) session.load(A.class, 1);
        assertEquals(2, a.values.size());
        assertEquals("value1", a.values.get(0));
        assertEquals("value2", a.values.get(1));

        tr.commit();
        session.close();
    }

}
