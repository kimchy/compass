package org.compass.needle.coherence;

import com.tangosol.net.NamedCache;
import org.apache.lucene.store.Directory;

/**
 * An extension on top of Lucene directory implemented by different Coherence
 * based directories.
 *
 * @author kimchy
 */
public abstract class CoherenceDirectory extends Directory {

    abstract public void deleteContent();

    abstract public NamedCache getCache();

    abstract public String getIndexName();

    abstract public int getBucketSize();

    abstract public int getFlushRate();
}
