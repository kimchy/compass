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

import java.io.IOException;

import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.DirectoryWrapper;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.store.AbstractLuceneSearchEngineStore;
import org.compass.core.mapping.CompassMapping;

/**
 * @author kimchy
 */
public abstract class AbstractCoherenceLuceneSearchEngineStore extends AbstractLuceneSearchEngineStore {

    public static final String BUCKET_SIZE_PROP = "compass.engine.store.coherence.bucketSize";

    private String indexName;

    private String cacheName;

    private NamedCache cache;

    private int bucketSize;

    public AbstractCoherenceLuceneSearchEngineStore(String connection, String subContext) {
        super(connection, subContext);
        int index = connection.indexOf(':');
        this.indexName = connection.substring(0, index) + "X" + subContext;
        this.cacheName = connection.substring(index + 1);
    }

    public void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping) {
        bucketSize = settings.getSettingAsInt(BUCKET_SIZE_PROP, DefaultCoherenceDirectory.DEFAULT_BUCKET_SIZE);
        cache = CacheFactory.getCache(cacheName);
        super.configure(searchEngineFactory, settings, mapping);
    }

    public String getIndexName() {
        return indexName;
    }

    public NamedCache getCache() {
        return cache;
    }

    public int getBucketSize() {
        return bucketSize;
    }

    protected void doDeleteIndex() throws SearchEngineException {
        String[] subIndexes = getSubIndexes();
        for (String subIndex : subIndexes) {
            template.executeForSubIndex(subIndex, false,
                    new LuceneStoreCallback() {
                        public Object doWithStore(Directory dir) throws IOException {
                            if (dir instanceof DirectoryWrapper) {
                                dir = ((DirectoryWrapper) dir).getWrappedDirectory();
                            }
                            ((CoherenceDirectory) dir).deleteContent();
                            return null;
                        }
                    });
        }
    }

    public boolean allowConcurrentCommit() {
        return true;
    }

    protected void doClose() {
        // TODO Do we release here or destroy here?
        cache.release();
    }

    protected CopyFromHolder doBeforeCopyFrom() throws SearchEngineException {
        for (int i = 0; i < getSubIndexes().length; i++) {
            final String subIndex = getSubIndexes()[i];
            template.executeForSubIndex(subIndex, false, new LuceneStoreCallback() {
                public Object doWithStore(Directory dest) {
                    if (dest instanceof DirectoryWrapper) {
                        dest = ((DirectoryWrapper) dest).getWrappedDirectory();
                    }
                    CoherenceDirectory coherenceDir = (CoherenceDirectory) dest;
                    coherenceDir.deleteContent();
                    return null;
                }
            });
        }
        CopyFromHolder holder = new CopyFromHolder();
        holder.createOriginalDirectory = false;
        return holder;
    }

}