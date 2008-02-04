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
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.CompassException;
import org.compass.core.CompassTransaction;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.lucene.util.LuceneUtils;
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

    private boolean isRunning = false;

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
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                searchEngineStore.createIndex();
                return null;
            }
        });
    }

    public void deleteIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting index " + searchEngineStore);
        }
        clearCache();
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                searchEngineStore.deleteIndex();
                return null;
            }
        });
    }

    public boolean verifyIndex() throws SearchEngineException {
        clearCache();
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction(CompassTransaction tr) throws CompassException {
                return searchEngineStore.verifyIndex();
            }
        });
    }

    public boolean indexExists() throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction(CompassTransaction tr) throws CompassException {
                return searchEngineStore.indexExists();
            }
        });
    }

    public void operate(final IndexOperationCallback callback) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                doOperate(callback);
                return null;
            }
        });
    }

    protected void doOperate(final IndexOperationCallback callback) throws SearchEngineException {
        // first aquire write lock for all the sub-indexes
        final String[] subIndexes = searchEngineStore.getSubIndexes();
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
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                doReplaceIndex(indexManager, callback);
                return null;
            }
        });
    }

    protected void doReplaceIndex(final SearchEngineIndexManager indexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
        final LuceneSearchEngineIndexManager luceneIndexManager = (LuceneSearchEngineIndexManager) indexManager;

        doOperate(new IndexOperationCallback() {
            public boolean firstStep() throws SearchEngineException {
                callback.buildIndexIfNeeded();
                return true;
            }

            public void secondStep() throws SearchEngineException {
                if (log.isDebugEnabled()) {
                    log.debug("[Replace Index] Replacing index [" + searchEngineStore + "] with ["
                            + luceneIndexManager.getStore() + "]");
                }
                // copy over the index by renaming the original one, and copy the new one
                searchEngineStore.copyFrom(luceneIndexManager.getStore());
                if (log.isDebugEnabled()) {
                    log.debug("[Replace Index] Index [" + searchEngineStore + "] replaced from ["
                            + luceneIndexManager.getStore() + "]");
                }
            }
        });
    }

    public void notifyAllToClearCache() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Global notification to clear cache");
        }
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
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
            LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
            if (indexHolder != null) {
                IndexReader reader = null;
                try {
                    reader = indexHolder.getIndexReader().reopen();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to refresh sub index cache [" + subIndex + "]");
                }
                if (reader != indexHolder.getIndexReader()) {
                    // if the reader was refreshed, mark the old one to close and replace the holder
                    indexHolder.markForClose();
                    indexHolders.put(subIndex, new LuceneIndexHolder(subIndex, new IndexSearcher(reader)));
                }
            } else {
                // do the same with index holder
                try {
                    indexHolder = new LuceneIndexHolder(subIndex, getDirectory(subIndex));
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to open sub index cache [" + subIndex + "]");
                }
                indexHolders.put(subIndex, indexHolder);
            }
        }
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
                Directory dir = getDirectory(subIndex);
                LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
                if (shouldInvalidateCache(dir, indexHolder)) {
                    refreshCache(subIndex);
                    // get the holder again, since it was refreshed
                    indexHolder = indexHolders.get(subIndex);
                }
                indexHolder.acquire();
                return indexHolder;
            } catch (Exception e) {
                throw new SearchEngineException("Failed to open index searcher for sub-index [" + subIndex + "]", e);
            }
        }
    }

    protected boolean shouldInvalidateCache(Directory dir, LuceneIndexHolder indexHolder) throws IOException, IllegalAccessException {
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
        clearCache();
        searchEngineStore.close();
    }

    public IndexWriter openIndexWriter(String subIndex) throws IOException {
        return openIndexWriter(searchEngineStore.getDirectoryBySubIndex(subIndex, false), false);
    }

    public IndexWriter openIndexWriter(String subIndex, boolean autoCommit) throws IOException {
        return openIndexWriter(searchEngineStore.getDirectoryBySubIndex(subIndex, false), autoCommit, false);
    }

    public IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException {
        return openIndexWriter(dir, true, create);
    }

    public IndexWriter openIndexWriter(Directory dir, boolean autoCommit, boolean create) throws IOException {
        // TODO lucene23 add more specific 2.3 parameters
        IndexWriter indexWriter = new IndexWriter(dir, autoCommit, searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer(),
                create, searchEngineFactory.getIndexDeletionPolicyManager().createIndexDeletionPolicy(dir));
        indexWriter.setMaxMergeDocs(luceneSettings.getMaxMergeDocs());
        indexWriter.setMergeFactor(luceneSettings.getMergeFactor());
        indexWriter.setUseCompoundFile(luceneSettings.isUseCompoundFile());
        indexWriter.setMaxFieldLength(luceneSettings.getMaxFieldLength());
        indexWriter.setMaxBufferedDocs(luceneSettings.getMaxBufferedDocs());
        return indexWriter;
    }

    public void closeIndexWriter(String subIndex, IndexWriter indexWriter, Directory dir) throws SearchEngineException {
        Exception ex = null;
        try {
            closeIndexWriter(indexWriter);
        } catch (Exception e) {
            ex = e;
        }
        try {
            searchEngineStore.closeDirectory(subIndex, dir);
        } catch (Exception e) {
            if (ex == null) {
                ex = e;
            } else {
                log.warn("Caught an exception trying to close the lucene directory "
                        + "with other exception pending, logging and ignoring", e);
            }
        }
        if (ex != null) {
            if (ex instanceof SearchEngineException) {
                throw (SearchEngineException) ex;
            }
            throw new SearchEngineException("Failed while executing a lucene directory based operation", ex);
        }
    }

    protected void closeIndexWriter(IndexWriter indexWriter) throws SearchEngineException {
        try {
            if (indexWriter != null) {
                indexWriter.close();
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to close index reader", e);
        }
    }

    public LuceneSearchEngineStore getStore() {
        return searchEngineStore;
    }

    protected Directory getDirectory(String subIndex) {
        return searchEngineStore.getDirectoryBySubIndex(subIndex, false);
    }

    public void start() {
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
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
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
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
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
            public Boolean doInTransaction(CompassTransaction tr) throws CompassException {
                return searchEngineStore.isLocked();
            }
        });
    }

    public boolean isLocked(final String subIndex) throws SearchEngineException {
        return searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
            public Boolean doInTransaction(CompassTransaction tr) throws CompassException {
                return searchEngineStore.isLocked(subIndex);
            }
        });
    }

    public void releaseLocks() throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                searchEngineStore.releaseLocks();
                return null;
            }
        });
    }

    public void releaseLock(final String subIndex) throws SearchEngineException {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(CompassTransaction tr) throws CompassException {
                searchEngineStore.releaseLock(subIndex);
                return null;
            }
        });
    }

    public void setWaitForCacheInvalidationBeforeSecondStep(long timeToWaitInMillis) {
        this.waitForCacheInvalidationBeforeSecondStep = timeToWaitInMillis;
    }

    public LuceneSettings getSettings() {
        return luceneSettings;
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
