package org.compass.annotations.test.component.deeplevel2;

import org.compass.annotations.test.AbstractAnnotationsTestCase;
import org.compass.core.CompassHits;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;

/**
 * @author kimchy
 */
public class DeepLevel2Tests extends AbstractAnnotationsTestCase {

    protected void addExtraConf(CompassConfiguration conf) {
        conf.addClass(A.class).addClass(B.class).addClass(C.class);
    }

    public void testProperDotPath() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = 1L;

        B b = new B();
        b.id = 1L;

        C c = new C();
        // TODO if we put the @SearchableId on C.id, then the test will break since we don't marshall C twice.
        c.id = 1L;
        c.property = "cverdi";

        a.c = c;
        a.b = b;
        b.c = c;

        session.save(a);
        session.save(b);

        CompassQueryBuilder queryBuilder = session.queryBuilder();
        CompassHits hitsA = queryBuilder.term("A.b.c.property", "cverdi").hits();
        CompassHits hitsAB = queryBuilder.term("A.c.property", "cverdi").hits();
        CompassHits hitsB = queryBuilder.term("B.c.property", "cverdi").hits();

        assertEquals(1, hitsA.getLength());
        assertEquals(1, hitsAB.getLength());
        assertEquals(1, hitsB.getLength());

        tr.commit();
        session.close();
    }
}
