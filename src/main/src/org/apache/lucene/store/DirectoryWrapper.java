package org.apache.lucene.store;

/**
 * Allows to get the native directory implementations
 *
 * @author kimchy
 */
public interface DirectoryWrapper {

    Directory getWrappedDirectory();
}
