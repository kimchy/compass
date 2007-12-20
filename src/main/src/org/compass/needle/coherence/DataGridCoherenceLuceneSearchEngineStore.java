package org.compass.needle.coherence;

import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class DataGridCoherenceLuceneSearchEngineStore extends AbstractCoherenceLuceneSearchEngineStore {

    public DataGridCoherenceLuceneSearchEngineStore(String connection, String subContext) {
        super(connection, subContext);
    }

    protected Directory doOpenDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException {
        return new DataGridCoherenceDirectory(getCache(), getIndexName() + "X" + subIndex, getBucketSize());
    }
}
