package org.compass.core.test.untokenized;

import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.test.AbstractTestCase;

public class UntokenizedTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"untokenized/A.cpm.xml"};
    }

    public void testUntokenized() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "Client";
        session.save(a);

        a = new A();
        a.id = new Long(2);
        a.value = "Client Type";
        session.save(a);

        // this one will not find anything, since an analyzer is applied to the query string
        // TODO need to find a simple solution for this one
        CompassQuery query = session.queryBuilder().queryString("type:Client").toQuery();
        CompassHits hits = query.hits();
        assertEquals(0, hits.length());

        tr.commit();
        session.close();
    }
}
