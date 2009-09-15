package org.compass.needle.coherence;

import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class DataGridCoherenceDirectoryStore extends AbstractCoherenceDirectoryStore {

    public static final String PROTOCOL = "coherence-dg://";

    protected String findConnection(String connection) {
        return connection.substring(PROTOCOL.length());
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        return new DataGridCoherenceDirectory(getCache(), getIndexName() + "/" + subContext + "/" + subIndex, getBucketSize(), getFlushRate());
    }
}
