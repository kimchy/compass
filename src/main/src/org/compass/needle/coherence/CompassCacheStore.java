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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tangosol.net.cache.CacheStore;
import com.tangosol.util.Base;
import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;

/**
 * A simple Compass implementation of Coherence CacheStore implementing the store and
 * eraze methods (no implementation for load).
 *
 * <p>Note, this implementation relies on the fact that the map key is the id of the
 * map value.
 *
 * @author kimchy
 */
public class CompassCacheStore extends Base implements CacheStore {

    private String entityName;

    private Compass compass;

    private CompassTemplate compassTemplate;

    public CompassCacheStore(String entityName) {
        this.entityName = entityName;
        CompassConfiguration configuration = CompassConfigurationFactory.newConfiguration();
        configuration.configure();
        compass = configuration.buildCompass();
        compassTemplate = new CompassTemplate(compass);
    }

    public CompassCacheStore(String entityName, String resource) {
        this.entityName = entityName;
        CompassConfiguration configuration = CompassConfigurationFactory.newConfiguration();
        configuration.configure(resource);
        compass = configuration.buildCompass();
        compassTemplate = new CompassTemplate(compass);
    }

    public CompassCacheStore(String entityName, File configurationFile) {
        this.entityName = entityName;
        CompassConfiguration configuration = CompassConfigurationFactory.newConfiguration();
        configuration.configure(configurationFile);
        compass = configuration.buildCompass();
        compassTemplate = new CompassTemplate(compass);
    }

    /**
     * Returns <code>null</code>, just implemets store.
     */
    public Object load(Object o) {
        return null;
    }

    /**
     * Returns an empty hash map, just implements store.
     */
    public Map loadAll(Collection collection) {
        return new HashMap();
    }

    public void store(Object key, Object value) {
        compassTemplate.save(entityName, value);
    }

    public void storeAll(final Map entries) {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Iterator it = entries.values().iterator(); it.hasNext();) {
                    session.save(entityName, it.next());
                }
            }
        });
    }

    public void erase(Object key) {
        compassTemplate.delete(entityName, key);
    }

    public void eraseAll(final Collection keys) {
        compassTemplate.execute(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Iterator it = keys.iterator(); it.hasNext();) {
                    session.delete(entityName, it.next());
                }
            }
        });
    }
}
