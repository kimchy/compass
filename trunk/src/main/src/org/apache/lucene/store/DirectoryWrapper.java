package org.apache.lucene.store;

import java.io.IOException;

/**
 * Allows to get the native directory implementations
 *
 * @author kimchy
 */
public interface DirectoryWrapper {

    Directory getWrappedDirectory();

    void clearWrapper() throws IOException;
}
