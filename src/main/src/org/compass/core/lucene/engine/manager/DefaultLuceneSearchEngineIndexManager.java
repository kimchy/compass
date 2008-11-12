/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.lucene.engine.manager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.merge.policy.MergePolicyFactory;
import org.compass.core.lucene.engine.merge.scheduler.MergeSchedulerFactory;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * @author kimchy
 */
public class DefaultLuceneSearchEngineIndexManager implements LuceneSearchEngineIndexManager {

    private static Log log = LogFactory.getLog(DefaultLuceneSearchEngineIndexManager.class);

    public static final String CLEAR_CACHE_NAME = "clearcache";

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineStore searchEngineStore;

    private LuceneSettings luceneSettings;

    // holds the index cache per sub index
    private Map<String, LuceneIndexHolder> indexHolders = new ConcurrentHashMap<String, LuceneIndexHolder>();

    private Map<String, Object> subIndexLocks = new HashMap<String, Object>();

    private long[] lastModifiled;

    private long waitForCacheInvalidationBeforeSecondStep = 0;

    private boolean cacheAsyncInvalidation;

    private volatile boolean isRunning = false;

    private ScheduledFuture scheduledIndexManagerFuture;

    private ScheduledFuture scheduledRefreshCacheFuture;

    public DefaultLuceneSearchEngineIndexManager(LuceneSearchEngineFactory searchEngineFactory,
                                                 final LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineFactory = searchEngineFactory;
        this.searchEngineStore = searchEngineStore;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (String subIndex : subIndexes) {
            subIndexLocks.put(subIndex, new Object());
        }
    }

    public void createIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Creating index " + searchEngineStore);
        }
        clearCache();
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                searchEngineStore.createIndex();
                return null;
            }
        });
    }

    public void deleteIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting index " + searchEngineStore);
        }
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                clearCache();
                searchEngineStore.deleteIndex();
                return null;
            }
        });
    }

    public void cleanIndex() throws SearchEngineException {
        for (String subIndex : getSubIndexes()) {
            cleanIndex(subIndex);
        }
    }

    public void cleanIndex(final String subIndex) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                synchronized (subIndexLocks.get(subIndex)) {
                    clearCache(subIndex);
                    searchEngineStore.cleanIndex(subIndex);
                    return null;
                }
            }
        });
    }

    public boolean verifyIndex() throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                clearCache();
                return searchEngineStore.verifyIndex();
            }
        });
    }

    public boolean indexExists() throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                return searchEngineStore.indexExists();
            }
        });
    }

    public void operate(final IndexOperationCallback callback) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                doOperate(callback);
                return null;
            }
        });
    }

    protected void doOperate(final IndexOperationCallback callback) throws SearchEngineException {
        // first aquire write lock for all the sub-indexes
        String[] subIndexes = searchEngineStore.getSubIndexes();
        if (callback instanceof IndexOperationPlan) {
            IndexOperationPlan plan = (IndexOperationPlan) callback;
            subIndexes = searchEngineStore.polyCalcSubIndexes(plan.getSubIndexes(), plan.getAliases(), plan.getTypes());
        }
        final Lock[] writerLocks = new Lock[subIndexes.length];

        try {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to obtain write locks");
                }
                for (int i = 0; i < subIndexes.length; i++) {
                    Directory dir = getDirectory(subIndexes[i]);
                    writerLocks[i] = dir.makeLock(IndexWriter.WRITE_LOCK_NAME);
                    writerLocks[i].obtain(luceneSettings.getTransactionLockTimout());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Obtained write locks");
                }
            } catch (Exception e) {
                throw new SearchEngineException("Failed to retrieve dirty transaction locks", e);
            }
            if (log.isDebugEnabled()) {
                log.debug("Calling callback first step");
            }
            // call the first step
            boolean continueToSecondStep = callback.firstStep();
            if (!continueToSecondStep) {
                return;
            }

            // perform the replace operation

            // TODO here we need to make sure that no read operations will happen as well

            // tell eveybody that are using the index, to clear the cache
            clearCache();
            notifyAllToClearCache();

            if (waitForCacheInvalidationBeforeSecondStep != 0 && luceneSettings.isWaitForCacheInvalidationOnIndexOperation()) {
                // now wait for the cache invalidation
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Waiting [" + waitForCacheInvalidationBeforeSecondStep + "ms] for global cache invalidation");
                    }
                    Thread.sleep(waitForCacheInvalidationBeforeSecondStep);
                } catch (InterruptedException e) {
                    log.debug("Interrupted while waiting for cache invalidation", e);
                    throw new SearchEngineException("Interrupted while waiting for cache invalidation", e);
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("Calling callback second step");
            }
            // call the second step
            callback.secondStep();
        } finally {
            LuceneUtils.clearLocks(writerLocks);
        }
    }


    public void replaceIndex(final SearchEngineIndexManager indexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                doReplaceIndex(indexManager, callback);
                return null;
            }
        });
    }

    protected void doReplaceIndex(final SearchEngineIndexManager indexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
        final LuceneSearchEngineIndexManager luceneIndexManager = (LuceneSearchEngineIndexManager) indexManager;
        doOperate(new ReplaceIndexOperationCallback(luceneIndexManager, callback));
    }

    private final class ReplaceIndexOperationCallback implements IndexOperationCallback, IndexOperationPlan {

        private ReplaceIndexCallback callback;

        private LuceneSearchEngineIndexManager indexManager;

        private ReplaceIndexOperationCallback(LuceneSearchEngineIndexManager indexManager, ReplaceIndexCallback callback) {
            this.indexManager = indexManager;
            this.callback = callback;
        }

        public boolean firstStep() throws SearchEngineException {
            callback.buildIndexIfNeeded();
            return true;
        }

        public void secondStep() throws SearchEngineException {
            if (log.isDebugEnabled()) {
                log.debug("[Replace Index] Replacing index [" + searchEngineStore + "] with ["
                        + indexManager.getStore() + "]");
            }
            String[] subIndexes = searchEngineStore.polyCalcSubIndexes(getSubIndexes(), getAliases(), getTypes());
            for (String subIndex : subIndexes) {
                synchronized (subIndexLocks.get(subIndex)) {
                    clearCache(subIndex);
                    indexManager.clearCache(subIndex);
                    searchEngineStore.copyFrom(subIndex, indexManager.getStore());
                    refreshCache(subIndex);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("[Replace Index] Index [" + searchEngineStore + "] replaced from ["
                        + indexManager.getStore() + "]");
            }
        }

        public String[] getSubIndexes() {
            if (callback instanceof IndexOperationPlan) {
                return ((IndexOperationPlan) callback).getSubIndexes();
            }
            return null;
        }

        public String[] getAliases() {
            if (callback instanceof IndexOperationPlan) {
                return ((IndexOperationPlan) callback).getAliases();
            }
            return null;
        }

        public Class[] getTypes() {
            if (callback instanceof IndexOperationPlan) {
                return ((IndexOperationPlan) callback).getTypes();
            }
            return null;
        }
    }

    public void notifyAllToClearCache() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Global notification to clear cache");
        }
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                // just update the last modified time, others will see the change and update
                for (String subIndex : searchEngineStore.getSubIndexes()) {
                    Directory dir = getDirectory(subIndex);
                    try {
                        if (!dir.fileExists(CLEAR_CACHE_NAME)) {
                            dir.createOutput(CLEAR_CACHE_NAME).close();
                        } else {
                            dir.touchFile(CLEAR_CACHE_NAME);
                        }
                    } catch (IOException e) {
                        throw new SearchEngineException("Failed to update/generate global invalidation cahce", e);
                    }
                }
                return null;
            }
        });
    }

    public boolean isCached(String subIndex) throws SearchEngineException {
        return indexHolders.containsKey(subIndex);
    }

    public boolean isCached() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (String subIndex : subIndexes) {
            if (isCached(subIndex)) {
                return true;
            }
        }
        return false;
    }

    public void clearCache() throws SearchEngineException {
        for (String subIndex : searchEngineStore.getSubIndexes()) {
            clearCache(subIndex);
        }
    }

    public void clearCache(String subIndex) throws SearchEngineException {
        LuceneIndexHolder indexHolder = indexHolders.remove(subIndex);
        if (indexHolder != null) {
            indexHolder.markForClose();
        }
    }

    public void refreshCache() throws SearchEngineException {
        for (String subIndex : searchEngineStore.getSubIndexes()) {
            refreshCache(subIndex);
        }
    }

    public void refreshCache(final String subIndex) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                internalRefreshCache(subIndex);
                return null;
            }
        });
    }

    private LuceneIndexHolder internalRefreshCache(String subIndex) throws SearchEngineException {
        if (log.isTraceEnabled()) {
            log.trace("Refreshing cache for sub index [" + subIndex + "]");
        }
        LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
        if (indexHolder != null) {
            IndexReader reader;
            try {
                reader = indexHolder.getIndexReader().reopen();
            } catch (IOException e) {
                throw new SearchEngineException("Failed to refresh sub index cache [" + subIndex + "]", e);
            }
            if (reader != indexHolder.getIndexReader()) {
                // if the reader was refreshed, mark the old one to close and replace the holder
                indexHolder.markForClose();
                indexHolder = new LuceneIndexHolder(subIndex, openIndexSearcher(reader));
                // since not syncronized, we need to mark the one we replaced as closed
                LuceneIndexHolder oldHolder = indexHolders.put(subIndex, indexHolder);
                if (oldHolder != null) {
                    oldHolder.markForClose();
                }
            }
        } else {
            try {
                IndexReader reader = IndexReader.open(getDirectory(subIndex), true);
                indexHolder = new LuceneIndexHolder(subIndex, openIndexSearcher(reader));
            } catch (IOException e) {
                throw new SearchEngineException("Failed to open sub index cache [" + subIndex + "]", e);
            }
            // since not syncronized, we need to mark the one we replaced as closed
            LuceneIndexHolder oldHolder = indexHolders.put(subIndex, indexHolder);
            if (oldHolder != null) {
                oldHolder.markForClose();
            }
        }
        return indexHolder;
    }

    public void refreshCache(String subIndex, IndexSearcher indexSearcher) throws SearchEngineException {
        LuceneIndexHolder indexHolder = new LuceneIndexHolder(subIndex, indexSearcher);
        LuceneIndexHolder oldHolder = indexHolders.put(subIndex, indexHolder);
        if (oldHolder != null) {
            oldHolder.markForClose();
        }
    }

    public LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
        try {
            LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
            if (cacheAsyncInvalidation) {
                if (indexHolder == null) {
                    indexHolder = internalRefreshCache(subIndex);
                }
            } else {
                if (shouldInvalidateCache(indexHolder)) {
                    indexHolder = internalRefreshCache(subIndex);
                }
            }
            // we spin here on the aquire until we manage to aquire one
            while (!indexHolder.acquire()) {
                indexHolder = indexHolders.get(subIndex);
            }
            return indexHolder;
        } catch (Exception e) {
            throw new SearchEngineException("Failed to open index searcher for sub-index [" + subIndex + "]", e);
        }
    }

    /**
     * Checks if a an index holder should be invalidated.
     */
    protected boolean shouldInvalidateCache(LuceneIndexHolder indexHolder) throws IOException {
        long currentTime = System.currentTimeMillis();
        // we have not created an index holder, invalidated by default
        if (indexHolder == null) {
            return true;
        }
        // configured to perform no cache invalidation
        if (luceneSettings.getCacheInvalidationInterval() == -1) {
            return false;
        }
        if ((currentTime - indexHolder.getLastCacheInvalidation()) > luceneSettings.getCacheInvalidationInterval()) {
            indexHolder.setLastCacheInvalidation(currentTime);
            try {
                if (!indexHolder.getIndexReader().isCurrent()) {
                    return true;
                }
            } catch (AlreadyClosedException e) {
                // the directory was closed
                return false;
            } catch (FileNotFoundException e) {
                // no segments file, no index
                return false;
            }
        }
        return false;
    }

    public synchronized void close() {
        stop();
        clearCache();
        searchEngineStore.close();
    }

    public IndexWriter openIndexWriter(CompassSettings settings, String subIndex) throws IOException {
        return openIndexWriter(settings, searchEngineStore.openDirectory(subIndex), false);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, String subIndex, boolean autoCommit) throws IOException {
        return openIndexWriter(settings, searchEngineStore.openDirectory(subIndex), autoCommit, false);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean create) throws IOException {
        return openIndexWriter(settings, dir, true, create);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, true, false, deletionPolicy);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create) throws IOException {
        return openIndexWriter(settings, dir, autoCommit, create, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, autoCommit, create, deletionPolicy, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create, IndexDeletionPolicy deletionPolicy, Analyzer analyzer) throws IOException {
        if (deletionPolicy == null) {
            deletionPolicy = searchEngineFactory.getIndexDeletionPolicyManager().createIndexDeletionPolicy(dir);
        }
        if (analyzer == null) {
            analyzer = searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer();
        }
        IndexWriter indexWriter = new IndexWriter(dir, analyzer, create, deletionPolicy, new IndexWriter.MaxFieldLength(luceneSettings.getMaxFieldLength()));

        indexWriter.setMergePolicy(MergePolicyFactory.createMergePolicy(settings));
        indexWriter.setMergeScheduler(MergeSchedulerFactory.create(this, settings));

        indexWriter.setMaxMergeDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_MERGE_DOCS, luceneSettings.getMaxMergeDocs()));
        indexWriter.setMergeFactor(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, luceneSettings.getMergeFactor()));
        indexWriter.setRAMBufferSizeMB(settings.getSettingAsDouble(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, luceneSettings.getRamBufferSize()));
        indexWriter.setMaxBufferedDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, luceneSettings.getMaxBufferedDocs()));
        indexWriter.setMaxBufferedDeleteTerms(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DELETED_TERMS, luceneSettings.getMaxBufferedDeletedTerms()));
        indexWriter.setUseCompoundFile(searchEngineStore.isUseCompoundFile());
        indexWriter.setMaxFieldLength(luceneSettings.getMaxFieldLength());
        indexWriter.setTermIndexInterval(luceneSettings.getTermIndexInterval());

        indexWriter.setSimilarity(searchEngineFactory.getSimilarityManager().getIndexSimilarity());

        return indexWriter;
    }

    public IndexSearcher openIndexSearcher(IndexReader reader) {
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(searchEngineFactory.getSimilarityManager().getSearchSimilarity());
        return searcher;
    }

    public MultiSearcher openMultiSearcher(Searchable[] searchers) throws IOException {
        MultiSearcher searcher = new MultiSearcher(searchers);
        searcher.setSimilarity(searchEngineFactory.getSimilarityManager().getSearchSimilarity());
        return searcher;
    }

    public LuceneSearchEngineStore getStore() {
        return searchEngineStore;
    }

    protected Directory getDirectory(String subIndex) {
        return searchEngineStore.openDirectory(subIndex);
    }

    public void start() {
        if (isRunning) {
            return;
        }
        long indexManagerScheduleInterval = luceneSettings.getSettings().getSettingAsTimeInMillis(LuceneEnvironment.SearchEngineIndex.INDEX_MANAGER_SCHEDULE_INTERVAL, 60 * 1000);
        if (indexManagerScheduleInterval > 0) {
            if (log.isInfoEnabled()) {
                log.info("Starting scheduled index manager with period [" + indexManagerScheduleInterval + "ms]");
            }
            ScheduledIndexManagerRunnable scheduledIndexManagerRunnable = new ScheduledIndexManagerRunnable(this);
            scheduledIndexManagerFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(scheduledIndexManagerRunnable, indexManagerScheduleInterval, indexManagerScheduleInterval, TimeUnit.MILLISECONDS);

            // set the time to wait for clearing cache to 110% of the schedule time
            setWaitForCacheInvalidationBeforeSecondStep((long) (indexManagerScheduleInterval * 1.1));
        } else {
            log.info("Not starting scheduled index manager");
            return;
        }

        cacheAsyncInvalidation = luceneSettings.getSettings().getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, true);
        long cacheInvalidationInterval = luceneSettings.getCacheInvalidationInterval();
        if (cacheInvalidationInterval > 0 && cacheAsyncInvalidation) {
            if (log.isInfoEnabled()) {
                log.info("Starting scheduled refresh cache with period [" + cacheInvalidationInterval + "ms]");
            }
            ScheduledRefreshCacheRunnable scheduledRefreshCacheRunnable = new ScheduledRefreshCacheRunnable();
            scheduledRefreshCacheFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(scheduledRefreshCacheRunnable, cacheInvalidationInterval, cacheInvalidationInterval, TimeUnit.MILLISECONDS);
        } else {
            log.info("Not starting scheduled refresh cache");
            return;
        }


        isRunning = true;
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        if (scheduledIndexManagerFuture != null) {
            scheduledIndexManagerFuture.cancel(true);
            scheduledIndexManagerFuture = null;
        }
        if (scheduledRefreshCacheFuture != null) {
            scheduledRefreshCacheFuture.cancel(true);
            scheduledRefreshCacheFuture = null;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public synchronized void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                doCheckAndClearIfNotifiedAllToClearCache();
                return null;
            }
        });
    }

    protected void doCheckAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        if (lastModifiled == null) {
            String[] subIndexes = searchEngineStore.getSubIndexes();
            // just update the last modified time, others will see the change and update
            for (String subIndex : subIndexes) {
                Directory dir = getDirectory(subIndex);
                try {
                    if (dir.fileExists(CLEAR_CACHE_NAME)) {
                        continue;
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to check if global clear cache exists", e);
                }
                try {
                    dir.createOutput(CLEAR_CACHE_NAME).close();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to update/generate global invalidation cahce", e);
                }
            }
            lastModifiled = new long[subIndexes.length];
            for (int i = 0; i < subIndexes.length; i++) {
                Directory dir = getDirectory(subIndexes[i]);
                try {
                    lastModifiled[i] = dir.fileModified(CLEAR_CACHE_NAME);
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
            long lastMod;
            try {
                lastMod = dir.fileModified(CLEAR_CACHE_NAME);
            } catch (IOException e) {
                throw new SearchEngineException("Failed to check last modified on global index chache on sub index ["
                        + subIndexes[i] + "]", e);
            }
            if (lastModifiled[i] < lastMod) {
                if (log.isDebugEnabled()) {
                    log.debug("Global notification to clear cache detected on sub index [" + subIndexes[i] + "]");
                }
                lastModifiled[i] = lastMod;
                clearCache(subIndexes[i]);
            }
        }
    }

    public boolean isIndexCompound() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (String subIndexe : subIndexes) {
            Directory dir = getDirectory(subIndexe);
            try {
                if (!org.apache.lucene.index.LuceneUtils.isCompound(dir)) {
                    return false;
                }
            } catch (IOException e) {
                throw new SearchEngineException("Failed to check if index is compound", e);
            }
        }
        return true;
    }

    public boolean isIndexUnCompound() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (String subIndex : subIndexes) {
            Directory dir = getDirectory(subIndex);
            try {
                if (!org.apache.lucene.index.LuceneUtils.isUnCompound(dir)) {
                    return false;
                }
            } catch (IOException e) {
                throw new SearchEngineException("Failed to check if index is unCompound", e);
            }
        }
        return true;
    }

    public void performScheduledTasks() throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                doCheckAndClearIfNotifiedAllToClearCache();
                getStore().performScheduledTasks();
                return null;
            }
        });
    }

    public String[] getSubIndexes() {
        return searchEngineStore.getSubIndexes();
    }

    public boolean subIndexExists(String subIndex) {
        return searchEngineStore.subIndexExists(subIndex);
    }

    public boolean isLocked() throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                return searchEngineStore.isLocked();
            }
        });
    }

    public boolean isLocked(final String subIndex) throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction() throws CompassException {
                return searchEngineStore.isLocked(subIndex);
            }
        });
    }

    public void releaseLocks() throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                searchEngineStore.releaseLocks();
                return null;
            }
        });
    }

    public void releaseLock(final String subIndex) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                searchEngineStore.releaseLock(subIndex);
                return null;
            }
        });
    }

    public String[] calcSubIndexes(String[] subIndexes, String[] aliases, Class[] types) {
        return searchEngineStore.calcSubIndexes(subIndexes, aliases, types);
    }

    public String[] polyCalcSubIndexes(String[] subIndexes, String[] aliases, Class[] types) {
        return searchEngineStore.polyCalcSubIndexes(subIndexes, aliases, types);
    }

    public boolean requiresAsyncTransactionalContext() {
        return searchEngineStore.requiresAsyncTransactionalContext();
    }

    public boolean supportsConcurrentOperations() {
        return searchEngineStore.supportsConcurrentOperations();
    }

    public void setWaitForCacheInvalidationBeforeSecondStep(long timeToWaitInMillis) {
        this.waitForCacheInvalidationBeforeSecondStep = timeToWaitInMillis;
    }

    public LuceneSettings getSettings() {
        return luceneSettings;
    }

    public ExecutorManager getExecutorManager() {
        return searchEngineFactory.getExecutorManager();
    }

    public TransactionContext getTransactionContext() {
        return searchEngineFactory.getTransactionContext();
    }

    private static class ScheduledIndexManagerRunnable implements Runnable {

        private LuceneSearchEngineIndexManager indexManager;

        public ScheduledIndexManagerRunnable(LuceneSearchEngineIndexManager indexManager) {
            this.indexManager = indexManager;
        }

        public void run() {
            try {
                indexManager.performScheduledTasks();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to perform schedule task", e);
                }
            }
        }

    }

    /**
     * A scheduled task that refresh the cache periodically (if needed).
     */
    private class ScheduledRefreshCacheRunnable implements Runnable {

        public void run() {
            searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction() throws CompassException {
                    for (String subIndex : searchEngineStore.getSubIndexes()) {
                        try {
                            if (searchEngineStore.indexExists(subIndex)) {
                                LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
                                if (shouldInvalidateCache(indexHolder)) {
                                    internalRefreshCache(subIndex);
                                }
                            } else {
                                log.trace("Sub index [" + subIndex + "] does not exists, no refresh perfomed");
                            }
                        } catch (Exception e) {
                            log.error("Failed to perform background refresh of cache for for sub-index [" + subIndex + "]", e);
                        }
                    }
                    return null;
                }
            });
        }
    }
}
