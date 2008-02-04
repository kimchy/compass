package org.compass.core.test.engine.lucene;

import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransaction;

/**
 * @author kimchy
 */
public class FSFsTransLuceneReadCommittedTransactionEngineTests extends AbstractReadCommittedTransactionTests {

    protected CompassSettings buildCompassSettings() {
        CompassSettings settings = super.buildCompassSettings();
        settings.setSetting(CompassEnvironment.CONNECTION, "target/test-index");
        settings.setSetting(CompassEnvironment.Transaction.ISOLATION_CLASS, ReadCommittedTransaction.class.getName());
        // TODO lucene23 translog
//        settings.setSetting(LuceneEnvironment.Transaction.TransLog.TYPE, FSTransLog.class.getName());
        return settings;
    }

    public void testSettings() {
        assertEquals(ReadCommittedTransaction.class.getName(), getSettings().getSetting(
                CompassEnvironment.Transaction.ISOLATION_CLASS));
    }

}
