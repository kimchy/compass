package org.compass.core.test.genericscol;

import java.util.ArrayList;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class GenericsColTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[] {"genericscol/genericscol.cpm.xml"};
    }

    public void testXmlMappingWithGenericsCollection() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1;
        B b1 = new B();
        b1.value = "value1";
        B b2 = new B();
        b2.value = "value2";
        a.bs = new ArrayList<B>();
        a.bs.add(b1);
        a.bs.add(b2);
        session.save(a);

        tr.commit();
        session.close();
    }

}
