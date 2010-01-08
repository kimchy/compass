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
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;

/**
 * <p>
 * If caching of meta data is enabled, we maintain a cache of all the meta data
 * information of files (as Entity). The cache is refreshed (cleared and
 * updated) each time {@link #list()} is called (which is called by Lucene when
 * finding segment files and opening a deleter. Meaning that we update the cache
 * quite frequently). "Locally" each time a file is created or meta data is
 * fetched on cache miss, it is added to the meta data cache.
 *
 * <p>
 * Allows to provide a set of regex patterns that will be used to match on
 * (Lucene) file names to choose if certain file names will also be cached in
 * memcache as well.
 *
 * @author kimchy
 */
public class GoogleAppEngineDirectory extends Directory {

    public static final int DEFAULT_BUCKET_SIZE = 20 * 1024;

    public static final int DEFAULT_FLUSH_RATE = 50;

    public static final int DEFAULT_TRANSACTION_RETRY_COUNT = 3;

    public static final boolean DEFAULT_CACHE_META_DATA = true;

    static final String META_KEY_KIND = "meta";

    static final String CONTENT_KEY_KIND = "content";

    private final DatastoreService datastoreService;

    private final MemcacheService memcacheService;

    private final String indexName;

    private final int bucketSize;

    private final int flushRate;

    private final boolean cacheMetaData;

    private final Key indexKey;

    private final PreparedQuery listQuery;

    private final Map<String, Entity> cachedMetaData = new ConcurrentHashMap<String, Entity>();

    private final Pattern[] memcacheRegexPatterns;

    private final int transactionRetryCount;

    // we store an on going list of created index outputs since Lucene needs
    // them
    // *before* it closes the index output. It calls fileExists in the middle.
    private Map<String, IndexOutput> onGoingIndexOutputs = new ConcurrentHashMap<String, IndexOutput>();

    public GoogleAppEngineDirectory(String indexName) {
        this(indexName, DEFAULT_BUCKET_SIZE);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize) {
        this(indexName, bucketSize, DEFAULT_FLUSH_RATE, new String[0]);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, String[] memcacheRegexPatterns) {
        this(indexName, bucketSize, DEFAULT_FLUSH_RATE, memcacheRegexPatterns);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, int flushRate, String[] memcacheRegexPatterns) {
        this(indexName, bucketSize, flushRate, DEFAULT_CACHE_META_DATA, memcacheRegexPatterns);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, int flushRate, boolean cacheMetaData,
                                    String[] memcacheRegexPatterns) {
        this(indexName, bucketSize, flushRate, cacheMetaData, DEFAULT_TRANSACTION_RETRY_COUNT, memcacheRegexPatterns);
    }

    public GoogleAppEngineDirectory(String indexName, int bucketSize, int flushRate, boolean cacheMetaData,
                                    int transactionRetryCount, String[] memcacheRegexPatterns) {
        this.indexName = indexName;
        this.bucketSize = bucketSize;
        this.flushRate = flushRate;
        this.cacheMetaData = cacheMetaData;
        this.datastoreService = DatastoreServiceFactory.getDatastoreService();
        this.memcacheService = MemcacheServiceFactory.getMemcacheService();
        this.transactionRetryCount = transactionRetryCount;

        memcacheService.setNamespace("index/" + indexName);

        this.indexKey = KeyFactory.createKey("index", indexName);

        listQuery = datastoreService.prepare(new Query(META_KEY_KIND, indexKey));

        if (memcacheRegexPatterns == null) {
            memcacheRegexPatterns = new String[0];
        }

        this.memcacheRegexPatterns = new Pattern[memcacheRegexPatterns.length];

        for (int i = 0; i < memcacheRegexPatterns.length; i++) {
            this.memcacheRegexPatterns[i] = Pattern.compile(memcacheRegexPatterns[i]);
        }

        setLockFactory(new GoogleAppEngineLockFactory(this));
    }

    public void deleteContent() throws IOException {
        String[] fileNames = list();
        for (String fileName : fileNames) {
            deleteFile(fileName);
        }
    }

    public String[] list() throws IOException {

        return doInTransaction(new Callable<String[]>() {

            @Override
            public String[] call() throws Exception {
                return doList(listQuery);
            }

            @Override
            public String[] call(Transaction transaction) {
                Query query = new Query(META_KEY_KIND, indexKey);
                PreparedQuery listQuery = datastoreService.prepare(transaction, query);
                return doList(listQuery);
            }

            private String[] doList(PreparedQuery listQuery) {
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
        });

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
        // return buildMetaDataQuery(name).countEntities() > 0;
    }

    public long fileModified(String name) throws IOException {
        return (Long) fetchMetaData(name).getProperty("modified");
    }

    public void touchFile(String name) throws IOException {
        Entity entity = fetchMetaData(name);
        entity.setProperty("modified", System.currentTimeMillis());
        datastoreService.put(entity);
    }

    public void deleteFile(final String name) throws IOException {

        doInTransaction(new Callable<Void>() {

            @Override
            public Void call(Transaction transaction) throws Exception {

                Entity entity;

                try {
                    entity = datastoreService.get(buildMetaDataKey(name));
                } catch (EntityNotFoundException ex) {
                    // Nothing to delete, we're okay.
                    return null;
                }

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

                    datastoreService.delete(transaction, keysToDelete);

                }

                return null;
            }
        });

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
        boolean useMemcache = false;
        for (Pattern pattern : memcacheRegexPatterns) {
            if (pattern.matcher(name).matches()) {
                useMemcache = true;
                break;
            }
        }
        return new GoogleAppEngineIndexInput(this, fetchMetaData(name), useMemcache);
    }

    public void close() throws IOException {
        // nothing to do here
    }

    private Entity fetchMetaData(final String name) throws GoogleAppEngineDirectoryException {

        if (cacheMetaData && !LuceneFileNames.isStaticFile(name)) {
            Entity entity = cachedMetaData.get(name);
            if (entity != null) {
                return entity;
            }
        }

        return doInTransaction(new Callable<Entity>() {

            @Override
            public Entity call(Transaction transaction) throws GoogleAppEngineDirectoryException {
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

        });

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

    MemcacheService getMemcacheService() {
        return this.memcacheService;
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

    int getTransactionRetryCount() {
        return transactionRetryCount;
    }

    /**
     * Runs the given Callable<T> object in its own transaction. It attempts to
     * retry the transaction as many times as dictated by the configuration.
     *
     * @param <T> the return type
     * @param c   the callable
     * @return
     * @throws GoogleAppEngineDirectoryException
     *
     */
    <T> T doInTransaction(Callable<T> c) throws GoogleAppEngineDirectoryException {
        return doInTransaction(transactionRetryCount, false, c);
    }

    /**
     * Runs the given Callable<T> object in its own transaction if there is
     * currently a transaction running, as not to interfere with the currently
     * running transaction. In which case it will invoke
     * Callable.call(Transaction).
     *
     * If there is no running transaction, it will invoke Callable.call()
     * without the use of a transaction.
     *
     * The rationale behind this:
     *
     * A transaction can take place only within the same entity group on GAE.
     * The index itself is not part of the object being indexed, and if an
     * object is being changed within the context of an existing transaction the
     * indexing process will interfere with the process of updating the data
     * itself. However, a separate transaction can be used to write to the index
     * so long as the last "current" transaction is put back in place when the
     * writing to the index has finished. So if there is a running transaction,
     * the following executes within its own transaction and is committed. That
     * replaces the transaction state to what it was before this call was made.
     *
     * The Google App Engine documentation also recommends re-trying the
     * transaction several times before giving up so this code also will attempt
     * to redo the transaction as many times as it makes sense.
     *
     * For more information see: <h href="http://code.google.com/appengine/docs/java/datastore/transactions.html#What_Can_Be_Done_In_a_Transaction">Google App Engine: What can be done in a transaction?</a>
     *
     * @param <T>      the return type
     * @param attempts the number of times to retry the transaction
     * @param force    set to true if you want to ignore the current state of the
     *                 transaction and run the given code in a transaction
     *                 regardless.
     * @param c        the callable to run
     * @return the object returned by the callable
     * @throws GoogleAppEngineDirectoryException
     *          if there was a problem with the transaction
     */
    <T> T doInTransaction(final int attempts, boolean force, Callable<T> c) throws GoogleAppEngineDirectoryException {
        int remaining;

        try {
            ConcurrentModificationException cme = null;

            // Checks if there is an active transaction
            Transaction trans = datastoreService.getCurrentTransaction(null);

            // If there is no currently running transaction that the indexing
            // will interfere with, so we just let the call proceed without a
            // transaction.
            if (!force && trans == null) {
                return c.call();
            }

            for (remaining = attempts; remaining > 0; --remaining) {
                T r;
                trans = datastoreService.beginTransaction();

                try {

                    r = c.call(trans);

                    trans.commit();

                    return r;
                } catch (ConcurrentModificationException ex) {
                    // Continues and tries to re-do the transaction.
                    cme = ex;
                    continue;
                } finally {
                    // If it gets to this point and the transaction object is
                    // still active we assume it failed for some reason so we
                    // roll it back.
                    if (trans.isActive()) {
                        trans.rollback();
                    }
                }
            }

            // We tried as many times as we could, so we give up.
            throw new GoogleAppEngineAttemptsExpiredException("Datastore too busy to complete transaction.", cme);

        } catch (ConcurrentModificationException ex) {
            //Depending on the preferences of the DatastoreService, it may 
            //run simple calls inside of a transaction.
            throw new GoogleAppEngineDirectoryException("Transaction failed.", ex);
        } catch (GoogleAppEngineDirectoryException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (EntityNotFoundException ex) {
            String name = ex.getKey().getName();
            String kind = ex.getKey().getKind();
            throw new GoogleAppEngineDirectoryException("Could not find entity [" + name + "] of kind [" + kind + "]", ex);
        } catch (Exception ex) {
            throw new GoogleAppEngineDirectoryException("Transaction failed.", ex);
        }
    }

    /**
     * Callback class for transactions. Subclasses must override at least one of
     * these methods or it will recurse to infinity.
     *
     * @author patricktwohig
     * @param <T>
     */
    static abstract class Callable<T> implements java.util.concurrent.Callable<T> {

        /**
         * Called when there is not transaction active. If code needs to be
         * different than that called from within a transaction this method will
         * be called.
         *
         * By default, this method calls <code>this.call(null)</code>
         */
        public T call() throws Exception {
            return call(null);
        }

        /**
         * Called if there is a transaction active, or called with null if there
         * is not.
         *
         * By default, this method calls <code>this.call()</code>
         *
         * @param transaction
         * @return
         * @throws Exception
         */
        public T call(Transaction transaction) throws Exception {
            return call();
        }

    }

    Entity getEntity(final Key key) throws GoogleAppEngineDirectoryException {
        return doInTransaction(new Callable<Entity>() {

            @Override
            public Entity call(Transaction transaction) throws Exception {
                return datastoreService.get(transaction, key);
            }

        });

    }

}
