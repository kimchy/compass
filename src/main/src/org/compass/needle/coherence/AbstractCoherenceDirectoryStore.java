/*
 * Copyright 2004-2006 the original author or authors.
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
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.store.AbstractDirectoryStore;
import org.compass.core.lucene.engine.store.CopyFromHolder;

/**
 * @author kimchy
 */
public abstract class AbstractCoherenceDirectoryStore extends AbstractDirectoryStore implements CompassConfigurable {

    public static final String BUCKET_SIZE_PROP = "compass.engine.store.coherence.bucketSize";

    private String indexName;

    private NamedCache cache;

    private int bucketSize;

    public void configure(CompassSettings settings) throws CompassException {
        String connection = findConnection(settings.getSetting(CompassEnvironment.CONNECTION));
        int index = connection.indexOf(':');
        this.indexName = connection.substring(0, index);
        String cacheName = connection.substring(index + 1);

        bucketSize = settings.getSettingAsInt(BUCKET_SIZE_PROP, DataGridCoherenceDirectory.DEFAULT_BUCKET_SIZE);
        cache = CacheFactory.getCache(cacheName);
    }

    protected abstract String findConnection(String connection);

    protected String getIndexName() {
        return indexName;
    }

    protected NamedCache getCache() {
        return cache;
    }

    protected int getBucketSize() {
        return bucketSize;
    }

    public void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        cleanIndex(dir, subContext, subIndex);
    }

    public void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException {
        ((CoherenceDirectory) dir).deleteContent();
    }

    public CopyFromHolder beforeCopyFrom(String subContext, Directory[] dirs) throws SearchEngineException {
        for (Directory dir : dirs) {
            ((CoherenceDirectory) dir).deleteContent();
        }
        return new CopyFromHolder();
    }

    public void close() {
        // TODO Do we release here or destroy here?
        cache.release();
    }
}