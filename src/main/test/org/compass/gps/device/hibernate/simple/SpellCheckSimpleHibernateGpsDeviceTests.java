package org.compass.gps.device.hibernate.simple;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * A test to verify that spell check works with Gps (Hibernate chosen here)
 *
 * @author kimchy
 */
public class SpellCheckSimpleHibernateGpsDeviceTests extends ScrollableSimpleHibernateGpsDeviceTests {

    protected void setUpCoreCompass(CompassConfiguration conf) {
        super.setUpCoreCompass(conf);
        conf.setSetting(LuceneEnvironment.SpellCheck.ENABLE, "true");
    }

    public void testSimpleSpellCheck() {
        compassGps.index();

        CompassSession sess = compass.openSession();
        CompassTransaction tr = sess.beginTransaction();

        CompassHits hits = sess.queryBuilder().queryString("valu").toQuery().hits();
        assertEquals(0, hits.length());
        assertFalse(hits.getQuery().isSuggested());
        assertTrue(hits.getSuggestedQuery().isSuggested());
        assertEquals("value", hits.getSuggestedQuery().toString());

        tr.commit();
        sess.close();
    }
}
