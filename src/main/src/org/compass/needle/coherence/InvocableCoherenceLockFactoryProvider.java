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

package org.compass.needle.coherence;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.apache.lucene.store.LockFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.store.LockFactoryProvider;

/**
 * A general lock factory provider based on Cohernce. Can be used as a distributed lock manager for
 * other storage systems then Cohernece.
 *
 * @author kimchy
 * @see org.compass.needle.coherence.InvocableCoherenceLockFactory
 */
public class InvocableCoherenceLockFactoryProvider implements LockFactoryProvider {

    public LockFactory createLockFactory(String path, String subContex, String subIndex, CompassSettings settings) throws SearchEngineException {
        String connection = settings.getSetting(CompassEnvironment.CONNECTION);
        NamedCache cache = CacheFactory.getCache(path);
        return new InvocableCoherenceLockFactory(cache, connection + "/" + subContex + "/" + subIndex + "/");
    }
}
