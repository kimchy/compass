package org.compass.core.lucene.engine.store;

import org.apache.lucene.store.LockFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;

/**
 * A delegate interface allowing to provide a custom lock factory implementation.
 *
 * @author kimchy
 */
public interface LockFactoryProvider {

    LockFactory createLockFactory(String path, String subIndex, CompassSettings settings) throws SearchEngineException;
}
