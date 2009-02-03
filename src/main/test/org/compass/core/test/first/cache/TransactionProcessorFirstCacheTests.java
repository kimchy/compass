package org.compass.core.test.first.cache;

import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.cache.first.NullFirstLevelCache;
import org.compass.core.cache.first.PlainFirstLevelCache;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.test.AbstractTestCase;

/**
 * @author kimchy
 */
public class TransactionProcessorFirstCacheTests extends AbstractTestCase {

    protected String[] getMappings() {
        return new String[]{"first/cache/first-cache.cpm.xml"};
    }

    protected void addSettings(CompassSettings settings) {
        settings.setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
    }

    public void testNoFirstLevelCache() {
        CompassSession session = openSession();
        assertTrue( ((InternalCompassSession) session).getFirstLevelCache() instanceof NullFirstLevelCache);
        session.close();
    }

    public void testNoFirstLevelCacheWithTransactionBegin() {
        CompassSession session = openSession();
        session.getSettings().setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        CompassTransaction tr = session.beginTransaction();
        assertTrue( ((InternalCompassSession) session).getFirstLevelCache() instanceof NullFirstLevelCache);
        tr.commit();
        session.close();
    }

    // TODO Enable Test
    public void XtestDefaultFirstLevelCacheWithTransactionBegin() {
        CompassSession session = openSession();
        session.getSettings().setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.ReadCommitted.NAME);
        CompassTransaction tr = session.beginTransaction();
        assertTrue( ((InternalCompassSession) session).getFirstLevelCache() instanceof PlainFirstLevelCache);
        tr.commit();
        session.close();
    }
}
