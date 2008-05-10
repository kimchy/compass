package org.compass.core.test.inheritance.nonrootbaseclass;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;
import org.compass.core.test.inheritance.C;
import org.compass.core.test.inheritance.ExtendsA;

/**
 * @author kimchy
 */
public class NonRootBaseClassTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"inheritance/nonrootbaseclass/mapping.cpm.xml"};
    }

    public void testComponentRefExtendsNonRoot() throws Exception {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        C c = new C();
        c.id = 1;

        Long id = new Long(1);
        ExtendsA extendsA = new ExtendsA();
        extendsA.setId(id);
        extendsA.setValue("value");
        extendsA.setExtendsValue("evalue");
        c.a = extendsA;

        session.save("cExtendsBaseNotRoot", c);
        session.save("extendsBaseNotRoot", extendsA);

        c = (C) session.load("cExtendsBaseNotRoot", new Long(1));
        extendsA = (ExtendsA) c.a;
        assertEquals("value", extendsA.getValue());
        assertEquals("evalue", extendsA.getExtendsValue());

        tr.commit();

        LuceneSubIndexInfo.getIndexInfo("extendsbasenotroot", session);
        // now test that there is no baseNotRoot index
        try {
            LuceneSubIndexInfo indexInfo = LuceneSubIndexInfo.getIndexInfo("basenotroot", session);
            if (indexInfo != null) {
                fail("a subindex should not exists");
            }
        } catch (Exception e) {
            // all is well
        }

        session.close();
    }

}
