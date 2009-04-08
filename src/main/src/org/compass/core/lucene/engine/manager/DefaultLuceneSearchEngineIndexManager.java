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

package org.compass.core.lucene.engine.manager;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneUtils;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * @author kimchy
 */
public class DefaultLuceneSearchEngineIndexManager implements LuceneSearchEngineIndexManager {

    private static Log log = LogFactory.getLog(DefaultLuceneSearchEngineIndexManager.class);

    private final LuceneSearchEngineFactory searchEngineFactory;

    private final LuceneSearchEngineStore searchEngineStore;

    private final LuceneSettings luceneSettings;

    private final IndexHoldersCache indexHoldersCache;

    private final IndexWritersManager indexWritersManager;

    private long waitForCacheInvalidationBeforeSecondStep = 0;

    private volatile boolean isRunning = false;

    private ScheduledFuture scheduledIndexManagerFuture;

    public DefaultLuceneSearchEngineIndexManager(LuceneSearchEngineFactory searchEngineFactory,
                                                 final LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineFactory = searchEngineFactory;
        this.searchEngineStore = searchEngineStore;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
        this.indexHoldersCache = new IndexHoldersCache(this);
        this.indexWritersManager = new IndexWritersManager(this);
    }

    public void start() {
        if (isRunning) {
            return;
        }
        if (!getExecutorManager().isDisabled()) {
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
                log.info("Scheduled index manager is disabled");
            }
        } else {
            log.info("Scheduled index manager is disabled since executor manager is disabled");
        }

        indexHoldersCache.start();

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
        indexHoldersCache.stop();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void createIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Creating index " + searchEngineStore);
        }
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                clearCache();
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
                indexHoldersCache.doUnderCacheLock(subIndex, new Runnable() {
                    public void run() {
                        clearCache(subIndex);
                        searchEngineStore.cleanIndex(subIndex);
                    }
                });
                return null;
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
        doOperate(callback);
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
            if (log.isDebugEnabled()) {
                log.debug("Trying to obtain write locks");
            }
            final String[] finalSubIndexes = subIndexes;
            searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction() throws CompassException {
                    for (int i = 0; i < finalSubIndexes.length; i++) {
                        Directory dir = getDirectory(finalSubIndexes[i]);
                        writerLocks[i] = dir.makeLock(IndexWriter.WRITE_LOCK_NAME);
                        try {
                            writerLocks[i].obtain(luceneSettings.getTransactionLockTimout());
                        } catch (IOException e) {
                            throw new SearchEngineException("Failed to retrieve transaction locks", e);
                        }
                    }
                    return null;
                }
            });
            if (log.isDebugEnabled()) {
                log.debug("Obtained write locks");
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
            searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction() throws CompassException {
                    LuceneUtils.clearLocks(writerLocks);
                    return null;
                }
            });
        }
    }


    public void replaceIndex(final SearchEngineIndexManager indexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
        doReplaceIndex(indexManager, callback);
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
            searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction() throws CompassException {
                    String[] subIndexes = searchEngineStore.polyCalcSubIndexes(getSubIndexes(), getAliases(), getTypes());
                    for (final String subIndex : subIndexes) {
                        indexHoldersCache.doUnderCacheLock(subIndex, new Runnable() {
                            public void run() {
                                clearCache(subIndex);
                                indexManager.clearCache(subIndex);
                                searchEngineStore.copyFrom(subIndex, indexManager.getStore());
                                refreshCache(subIndex);
                            }
                        });
                    }
                    return null;
                }
            });
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

    public synchronized void close() {
        stop();
        clearCache();
        indexHoldersCache.close();
        indexWritersManager.close();
        searchEngineStore.close();
    }

    public boolean isCached(String subIndex) throws SearchEngineException {
        return indexHoldersCache.isCached(subIndex);
    }

    public boolean isCached() throws SearchEngineException {
        return indexHoldersCache.isCached();
    }

    public void clearCache(String subIndex) throws SearchEngineException {
        indexHoldersCache.clearCache(subIndex);
    }

    public void clearCache() throws SearchEngineException {
        indexHoldersCache.clearCache();
    }

    public void invalidateCache(String subIndex) throws SearchEngineException {
        indexHoldersCache.invalidateCache(subIndex);
    }

    public void invalidateCache() throws SearchEngineException {
        indexHoldersCache.invalidateCache();
    }

    public void refreshCache(final String subIndex) throws SearchEngineException {
        getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                indexHoldersCache.refreshCache(subIndex);
                return null;
            }
        });
    }

    public void refreshCache() throws SearchEngineException {
        getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                indexHoldersCache.refreshCache();
                return null;
            }
        });
    }

    public void notifyAllToClearCache() throws SearchEngineException {
        getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                indexHoldersCache.notifyAllToClearCache();
                return null;
            }
        });
    }

    public void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        indexHoldersCache.checkAndClearIfNotifiedAllToClearCache();
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

    public IndexHoldersCache getIndexHoldersCache() {
        return this.indexHoldersCache;
    }

    public IndexWritersManager getIndexWritersManager() {
        return indexWritersManager;
    }

    public Directory getDirectory(String subIndex) {
        return searchEngineStore.openDirectory(subIndex);
    }

    public void performScheduledTasks() throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                indexHoldersCache.checkAndClearIfNotifiedAllToClearCache();
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
        return !searchEngineFactory.getExecutorManager().isDisabled() && searchEngineStore.supportsConcurrentOperations();
    }

    public boolean supportsConcurrentCommits() {
        return !searchEngineFactory.getExecutorManager().isDisabled() && searchEngineStore.supportsConcurrentCommits();
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

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
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
