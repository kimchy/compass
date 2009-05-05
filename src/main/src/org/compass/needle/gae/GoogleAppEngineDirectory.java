/*
 * Copyright 2004-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.compass.needle.gae;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 * If caching of meta data is enabled, we maintain a cache of all the meta data information of files (as Entity).
 * The cache is refreshed (cleared and updated) each time {@link #list()} is called (which is called by Lucene when finding segment files
 * and opening a deleter. Meaning that we update the cache quite frequently). "Locally" each time a file is created
 * or meta data is fetched on cache miss, it is added to the meta data cache.
 *
 * @author kimchy
 */
public class GoogleAppEngineDirectory extends Directory {

    public static final int DEFAULT_BUCKET_SIZE = 20 * 1024;

    public static final int DEFAULT_FLUSH_RATE = 50;

    public static final boolean DEFAULT_CACHE_META_DATA = true;


    static final String META_KEY_KIND = "meta";

    static final String CONTENT_KEY_KIND = "content";

    private final DatastoreService datastoreService;

    private final String indexName;

    private final int bucketSize;

    private final int flushRate;

    private final boolean cacheMetaData;

    private final Key indexKey;

    private final PreparedQuery listQuery;

    private final Map<String, Entity> cachedMetaData = new ConcurrentHashMap<String, Entity>();

    // we store an on going list of created index outputs since Lucene needs them
    // *before* it closes the index output. It calls fileExists in the middle.
    private Map<String, IndexOutput> onGoingIndexOutputs = new ConcurrentHashMap<String, IndexOutput>();

    public GoogleAppEngineDirectory(String indexName, int bucketSize) {
        this(indexName, bucketSize, DEFAULT_FLUSH_RATE);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, int flushRate) {
        this(indexName, bucketSize, flushRate, DEFAULT_CACHE_META_DATA);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, int flushRate, boolean cacheMetaData) {
        this.indexName = indexName;
        this.bucketSize = bucketSize;
        this.flushRate = flushRate;
        this.cacheMetaData = cacheMetaData;
        this.datastoreService = DatastoreServiceFactory.getDatastoreService();

        this.indexKey = KeyFactory.createKey("index", indexName);

        listQuery = datastoreService.prepare(new Query(META_KEY_KIND, indexKey));

        setLockFactory(new GoogleAppEngineLockFactory(this));
    }

    public void deleteContent() throws IOException {
        String[] fileNames = list();
        for (String fileName : fileNames) {
            deleteFile(fileName);
        }
    }

    public String[] list() throws IOException {
        List<Entity> entities = listQuery.asList(FetchOptions.Builder.withChunkSize(Integer.MAX_VALUE));
        String[] result = new String[entities.size()];
        int i = 0;
        for (Entity entity : entities) {
            result[i++] = entity.getKey().getName();
        }
        if (cacheMetaData) {
            cachedMetaData.clear();
            for (Entity entity : entities) {
                cachedMetaData.put(entity.getKey().getName(), entity);
            }
        }
        return result;
    }

    public boolean fileExists(String name) throws IOException {
        if (onGoingIndexOutputs.containsKey(name)) {
            return true;
        }
        try {
            Entity entity = fetchMetaData(name);
            return entity != null;
        } catch (Exception e) {
            return false;
        }
        // TODO why this does not work?
//        return buildMetaDataQuery(name).countEntities() > 0;
    }

    public long fileModified(String name) throws IOException {
        return (Long) fetchMetaData(name).getProperty("modified");
    }

    public void touchFile(String name) throws IOException {
        Entity entity = fetchMetaData(name);
        entity.setProperty("modified", System.currentTimeMillis());
        datastoreService.put(entity);
    }

    public void deleteFile(String name) throws IOException {
        try {
            Entity entity = datastoreService.get(buildMetaDataKey(name));
            if (entity != null) {
                if (cacheMetaData) {
                    cachedMetaData.remove(name);
                }
                List<Key> keysToDelete = new ArrayList<Key>();
                keysToDelete.add(entity.getKey());
                long size = (Long) entity.getProperty("size");
                long count = Math.round((double) size / bucketSize);
                for (int i = 0; i < count; i++) {
                    keysToDelete.add(KeyFactory.createKey(entity.getKey(), CONTENT_KEY_KIND, name + i));
                }
                datastoreService.delete(keysToDelete);
            }
        } catch (EntityNotFoundException e) {
            // its fine, just do nothing
        }
    }

    public void renameFile(String from, String to) throws IOException {
        throw new UnsupportedOperationException("Deprecated");
    }

    public long fileLength(String name) throws IOException {
        return (Long) fetchMetaData(name).getProperty("size");
    }

    public IndexOutput createOutput(String name) throws IOException {
        IndexOutput out;
        if (LuceneFileNames.isSegmentsFile(name)) {
            out = new FlushOnCloseGoogleAppEngineIndexOutput(this, name);
        } else {
            out = new GoogleAppEngineMemIndexOutput(this, name);
        }
        onGoingIndexOutputs.put(name, out);
        return out;
    }

    public IndexInput openInput(String name) throws IOException {
        return new GoogleAppEngineIndexInput(this, fetchMetaData(name));
    }

    public void close() throws IOException {
        // nothing to do here
    }

    private Entity fetchMetaData(String name) throws GoogleAppEngineDirectoryException {
        if (cacheMetaData && !LuceneFileNames.isStaticFile(name)) {
            Entity entity = cachedMetaData.get(name);
            if (entity != null) {
                return entity;
            }
        }
        try {
            Entity entity = datastoreService.get(buildMetaDataKey(name));
            if (entity != null) {
                addMetaData(entity);
            }
            return entity;
        } catch (EntityNotFoundException e) {
            throw new GoogleAppEngineDirectoryException(indexName, name, "Not found");
        }
    }

    private Key buildMetaDataKey(String name) {
        return indexKey.getChild(META_KEY_KIND, name);
    }

    int getBucketSize() {
        return bucketSize;
    }

    DatastoreService getDatastoreService() {
        return datastoreService;
    }

    String getIndexName() {
        return indexName;
    }

    Key getIndexKey() {
        return indexKey;
    }

    int getFlushRate() {
        return flushRate;
    }

    Map<String, IndexOutput> getOnGoingIndexOutputs() {
        return onGoingIndexOutputs;
    }

    void addMetaData(Entity metaDataEntity) {
        if (!cacheMetaData || LuceneFileNames.isStaticFile(metaDataEntity.getKey().getName())) {
            return;
        }
        cachedMetaData.put(metaDataEntity.getKey().getName(), metaDataEntity);
    }
}
