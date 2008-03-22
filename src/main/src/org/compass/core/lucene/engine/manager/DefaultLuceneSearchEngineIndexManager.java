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

package org.compass.core.lucene.engine.manager;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
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
import org.compass.core.lucene.util.LuceneUtils;
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
    private HashMap<String, LuceneIndexHolder> indexHolders = new HashMap<String, LuceneIndexHolder>();

    private HashMap<String, Object> indexHoldersLocks = new HashMap<String, Object>();

    private long[] lastModifiled;

    private long waitForCacheInvalidationBeforeSecondStep = 0;

    private volatile boolean isRunning = false;

    private ScheduledFuture scheduledFuture;

    public DefaultLuceneSearchEngineIndexManager(LuceneSearchEngineFactory searchEngineFactory,
                                                 final LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineFactory = searchEngineFactory;
        this.searchEngineStore = searchEngineStore;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (String subIndex : subIndexes) {
            indexHoldersLocks.put(subIndex, new Object());
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
                synchronized (indexHoldersLocks.get(subIndex)) {
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
                synchronized (indexHoldersLocks.get(subIndex)) {
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
        synchronized (indexHoldersLocks.get(subIndex)) {
            return indexHolders.containsKey(subIndex);
        }
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
        synchronized (indexHoldersLocks.get(subIndex)) {
            LuceneIndexHolder indexHolder = indexHolders.remove(subIndex);
            if (indexHolder != null) {
                indexHolder.markForClose();
            }
        }
    }

    public void refreshCache() throws SearchEngineException {
        for (String subIndex : searchEngineStore.getSubIndexes()) {
            refreshCache(subIndex);
        }
    }

    public void refreshCache(String subIndex) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            internalRefreshCache(subIndex);
        }
    }

    // should be called within a lock
    private LuceneIndexHolder internalRefreshCache(String subIndex) throws SearchEngineException {
        LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
        if (indexHolder != null) {
            IndexReader reader;
            try {
                reader = indexHolder.getIndexReader().reopen();
            } catch (IOException e) {
                throw new SearchEngineException("Failed to refresh sub index cache [" + subIndex + "]");
            }
            if (reader != indexHolder.getIndexReader()) {
                // if the reader was refreshed, mark the old one to close and replace the holder
                indexHolder.markForClose();
                indexHolder = new LuceneIndexHolder(subIndex, new IndexSearcher(reader));
                indexHolders.put(subIndex, indexHolder);
            }
        } else {
            try {
                indexHolder = new LuceneIndexHolder(subIndex, getDirectory(subIndex));
            } catch (IOException e) {
                throw new SearchEngineException("Failed to open sub index cache [" + subIndex + "]", e);
            }
            indexHolders.put(subIndex, indexHolder);
        }
        return indexHolder;
    }

    public void refreshCache(String subIndex, IndexSearcher indexSearcher) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            LuceneIndexHolder indexHolder = indexHolders.remove(subIndex);
            if (indexHolder != null) {
                indexHolder.markForClose();
            }
            indexHolder = new LuceneIndexHolder(subIndex, indexSearcher);
            indexHolders.put(subIndex, indexHolder);
        }
    }

    public LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            try {
                LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
                if (shouldInvalidateCache(indexHolder)) {
                    indexHolder = internalRefreshCache(subIndex);
                }
                indexHolder.acquire();
                return indexHolder;
            } catch (Exception e) {
                throw new SearchEngineException("Failed to open index searcher for sub-index [" + subIndex + "]", e);
            }
        }
    }

    protected boolean shouldInvalidateCache(LuceneIndexHolder indexHolder) throws IOException, IllegalAccessException {
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
            if (!indexHolder.getIndexReader().isCurrent()) {
                return true;
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
        IndexWriter indexWriter = new IndexWriter(dir, autoCommit, analyzer, create, deletionPolicy);
        indexWriter.setMaxMergeDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_MERGE_DOCS, luceneSettings.getMaxMergeDocs()));
        indexWriter.setMergeFactor(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, luceneSettings.getMergeFactor()));
        indexWriter.setRAMBufferSizeMB(settings.getSettingAsDouble(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, luceneSettings.getRamBufferSize()));
        indexWriter.setMaxBufferedDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, luceneSettings.getMaxBufferedDocs()));
        indexWriter.setMaxBufferedDeleteTerms(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DELETED_TERMS, luceneSettings.getMaxBufferedDeletedTerms()));
        indexWriter.setUseCompoundFile(luceneSettings.isUseCompoundFile());
        indexWriter.setMaxFieldLength(luceneSettings.getMaxFieldLength());
        indexWriter.setTermIndexInterval(luceneSettings.getTermIndexInterval());
        indexWriter.setMergePolicy(MergePolicyFactory.createMergePolicy(settings));
        indexWriter.setMergeScheduler(MergeSchedulerFactory.create(this, settings));
        return indexWriter;
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
        if (luceneSettings.getIndexManagerScheduleInterval() > 0) {
            if (log.isInfoEnabled()) {
                log.info("Starting scheduled index manager with period [" + luceneSettings.getIndexManagerScheduleInterval() + "ms]");
            }
            ScheduledIndexManagerRunnable scheduledIndexManagerRunnable = new ScheduledIndexManagerRunnable(this);
            long period = luceneSettings.getIndexManagerScheduleInterval();
            scheduledFuture = searchEngineFactory.getExecutorManager().scheduleWithFixedDelay(scheduledIndexManagerRunnable, period, period, TimeUnit.MILLISECONDS);

            // set the time to wait for clearing cache to 110% of the schedule time
            setWaitForCacheInvalidationBeforeSecondStep((long) (luceneSettings.getIndexManagerScheduleInterval() * 1.1));
        } else {
            log.info("Not starting scheduled index manager");
            return;
        }

        isRunning = true;
    }

    public void stop() {
        if (!isRunning) {
            return;
        }
        isRunning = false;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            scheduledFuture = null;
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

}
