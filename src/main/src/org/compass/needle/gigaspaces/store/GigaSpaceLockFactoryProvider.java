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

package org.compass.needle.gigaspaces.store;

import com.j_spaces.core.IJSpace;
import com.j_spaces.core.client.SpaceFinder;
import org.apache.lucene.store.LockFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.store.LockFactoryProvider;

/**
 * A general lock factory provider based on GigaSpaces. Can be used as a distributed lock manager for
 * other storage systems then GigaSpaces.
 *
 * @author kimchy
 * @see org.compass.needle.gigaspaces.store.GigaSpaceLockFactory
 */
public class GigaSpaceLockFactoryProvider implements LockFactoryProvider {

    public LockFactory createLockFactory(String path, String subContext, String subIndex, CompassSettings settings) throws SearchEngineException {
        String connection = settings.getSetting(CompassEnvironment.CONNECTION);
        IJSpace space;
        try {
            space = (IJSpace) SpaceFinder.find(path, settings.getProperties());
        } catch (Exception e) {
            throw new ConfigurationException("Failed to find Space [" + path + "]", e);
        }

        return new GigaSpaceLockFactory(space, connection + "/" + subContext + "/" + subIndex + "/");
    }
}
