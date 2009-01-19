/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    /**
     * Creates the a lock factory.
     *
     * @param path      The path the lock factory will work with. Does not have to be an actual file system path.
     * @param subContex The sub context the index is created with.
     * @param subIndex  The sub index within the index (under the sub context).
     * @param settings  Additional configuration settings
     * @return The actual lock factory
     * @throws SearchEngineException
     */
    LockFactory createLockFactory(String path, String subContex, String subIndex, CompassSettings settings) throws SearchEngineException;
}
