package org.compass.core.test.localcache;

import org.compass.core.CompassHits;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class MemoryLocalCacheSimpleTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"localcache/A.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setGroupSettings(LuceneEnvironment.LocalCache.PREFIX, "a",
                new String[]{LuceneEnvironment.LocalCache.CONNECTION}, new String[]{"memory://"});
    }

    public void testSimpleLocalCache() {
        CompassSession session = openSession();
        CompassTransaction tr = session.beginTransaction();

        A a = new A();
        a.id = new Long(1);
        a.value = "test";
        session.save(a);

        tr.commit();
        session.close();

        session = openSession();
        tr = session.beginTransaction();

        CompassHits hits = session.queryBuilder().matchAll().hits();
        assertEquals(1, hits.length());

        tr.commit();
        session.close();
    }
}