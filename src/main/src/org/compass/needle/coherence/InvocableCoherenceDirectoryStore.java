package org.compass.needle.coherence;

import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;

/**
 * @author kimchy
 */
public class InvocableCoherenceDirectoryStore extends AbstractCoherenceDirectoryStore {

    public static final String PROTOCOL = "coherence://";

    protected String findConnection(String connection) {
        return connection.substring(PROTOCOL.length());
    }

    public Directory open(String subContext, String subIndex) throws SearchEngineException {
        return new InvocableCoherenceDirectory(getCache(), getIndexName() + "/" + subContext + "/" + subIndex, getBucketSize(), getFlushRate());
    }
}