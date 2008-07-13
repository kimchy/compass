package org.compass.needle.coherence;

import java.io.IOException;

import com.tangosol.net.NamedCache;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.processor.ConditionalRemove;

/**
 * The invocable directory uses Coherence extended support for invocable cache
 * services (not available at all editions).
 *
 * <p>Allow to better implement locking and batch deletions.
 *
 * @author kimchy
 */
public class InvocableCoherenceDirectory extends DataGridCoherenceDirectory {

    public InvocableCoherenceDirectory(String cacheName) {
        super(cacheName);
    }

    public InvocableCoherenceDirectory(String cacheName, String indexName) {
        super(cacheName, indexName);
    }

    public InvocableCoherenceDirectory(String cacheName, String indexName, int bucketSize) {
        super(cacheName, indexName, bucketSize);
    }

    public InvocableCoherenceDirectory(NamedCache cache, String indexName) {
        super(cache, indexName);
    }

    public InvocableCoherenceDirectory(NamedCache cache, String indexName, int bucketSize) {
        super(cache, indexName, bucketSize);
    }

    public InvocableCoherenceDirectory(NamedCache cache, String indexName, int bucketSize, int flushRate) {
        super(cache, indexName, bucketSize, flushRate);
    }

    protected void doInit() {
        setLockFactory(new InvocableCoherenceLockFactory(getCache(), getIndexName()));
    }

    public void deleteFile(String name) throws IOException {
        getCache().invokeAll(new AndFilter(getIndexNameEqualsFilter(), new EqualsFilter(getFileNameKeyExtractor(), name)),
                new ConditionalRemove(AlwaysFilter.INSTANCE, false));
    }

    public void deleteContent() {
        getCache().invokeAll(getIndexNameEqualsFilter(), new ConditionalRemove(AlwaysFilter.INSTANCE, false));
    }
}
