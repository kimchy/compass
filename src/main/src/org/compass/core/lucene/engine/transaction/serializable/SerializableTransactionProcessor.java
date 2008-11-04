package org.compass.core.lucene.engine.transaction.serializable;

import java.io.IOException;

import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessor;

/**
 * @author kimchy
 */
public class SerializableTransactionProcessor extends ReadCommittedTransactionProcessor {

    public SerializableTransactionProcessor(LuceneSearchEngine searchEngine) {
        super(searchEngine);
    }

    public void begin() throws SearchEngineException {
        super.begin();
        for (String subIndex : indexManager.getStore().getSubIndexes()) {
            try {
                openIndexWriterIfNeeded(subIndex);
            } catch (IOException e) {
                throw new SearchEngineException("Failed to open index writer for sub index [" + subIndex + "]", e);
            }
        }
    }
}
