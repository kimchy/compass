package org.compass.core.lucene.engine.indexdeletionpolicy;

import org.apache.lucene.store.Directory;

/**
 * {@link org.apache.lucene.index.IndexDeletionPolicy} that implement this interface will be
 * injected with a {@link org.apache.lucene.store.Directory} implementation to be used.
 *
 * <p>The directory will be injected just after construction and before any of
 * {@link org.apache.lucene.index.IndexDeletionPolicy} methods will be called.
 *
 * @author kimchy
 */
public interface DirectoryConfigurable {

    /**
     * {@link org.apache.lucene.index.IndexDeletionPolicy} that implement this interface will be
     * injected with a {@link org.apache.lucene.store.Directory} implementation to be used.
     *
     * <p>The directory will be injected just after construction and before any of
     * {@link org.apache.lucene.index.IndexDeletionPolicy} methods will be called.
     */
    void setDirectory(Directory directory);
}
