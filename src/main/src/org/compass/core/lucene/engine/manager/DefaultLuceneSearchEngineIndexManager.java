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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.util.backport.java.util.concurrent.Callable;
import org.compass.core.util.backport.java.util.concurrent.ExecutionException;
import org.compass.core.util.backport.java.util.concurrent.ExecutorService;
import org.compass.core.util.backport.java.util.concurrent.Executors;
import org.compass.core.util.backport.java.util.concurrent.Future;
import org.compass.core.util.concurrent.SingleThreadThreadFactory;

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
    private HashMap indexHolders = new HashMap();

    private HashMap indexHoldersLocks = new HashMap();

    private long[] lastModifiled;

    private long waitForCacheInvalidationBeforeSecondStep = 0;

    private boolean isRunning = false;

    private ExecutorService commitExecutorService;

    private int concurrentCommitThreshold;

    public DefaultLuceneSearchEngineIndexManager(LuceneSearchEngineFactory searchEngineFactory,
                                                 final LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineFactory = searchEngineFactory;
        this.searchEngineStore = searchEngineStore;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            indexHoldersLocks.put(subIndexes[i], new Object());
        }
        if (searchEngineStore.allowConcurrentCommit() &&
                searchEngineFactory.getSettings().getSettingAsBoolean(LuceneEnvironment.Transaction.ENABLE_CONCURRENT_COMMIT, true)) {
            concurrentCommitThreshold = searchEngineFactory.getSettings().getSettingAsInt(LuceneEnvironment.Transaction.CONCURRENT_COMMIT_THRESHOLD, 1);
            int maxThreads = searchEngineFactory.getSettings().getSettingAsInt(LuceneEnvironment.Transaction.MAX_CONCURRENT_COMMIT_THREADS, 10);
            if (searchEngineStore.getSubIndexes().length < maxThreads) {
                maxThreads = searchEngineStore.getSubIndexes().length;
            }
            if (maxThreads> 0) {
                commitExecutorService = Executors.newFixedThreadPool(maxThreads, new SingleThreadThreadFactory("Compass Concurrent Commit", false));
                if (log.isDebugEnabled()) {
                    log.debug("Concurrent commit is enabled with max threads of [" + maxThreads + "]");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Concurrent commit is disabled since maxThreads is set to 0 (might be there are no sub indexes, i.e. no mappings?)");
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Concurrent commit is disabled (either due to setting or store not supporting it)");
            }
        }
    }

    public void createIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Creating index " + searchEngineStore);
        }
        clearCache();
        searchEngineStore.createIndex();
    }

    public void deleteIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Deleting index " + searchEngineStore);
        }
        clearCache();
        searchEngineStore.deleteIndex();
    }

    public boolean verifyIndex() throws SearchEngineException {
        clearCache();
        boolean result = searchEngineStore.verifyIndex();
        if (luceneSettings.isUseCompoundFile() && !isIndexCompound()) {
            log.info("Setting using compound file, but the index is not in compound form, compounding the index...");
            compoundIndex();
        } else if (!luceneSettings.isUseCompoundFile() && !isIndexUnCompound()) {
            log.info("Setting not using compound file, but the index is in compound form, un-compounding the index...");
            unCompoundIndex();
        }
        return result;
    }

    public boolean indexExists() throws SearchEngineException {
        return searchEngineStore.indexExists();
    }

    public void operate(final IndexOperationCallback callback) throws SearchEngineException {
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

    public void replaceIndex(SearchEngineIndexManager indexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
        final LuceneSearchEngineIndexManager luceneIndexManager = (LuceneSearchEngineIndexManager) indexManager;

        operate(new IndexOperationCallback() {
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
        String[] subIndexes = searchEngineStore.getSubIndexes();
        // just update the last modified time, others will see the change and update
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
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
    }

    public boolean isCached(String subIndex) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            return indexHolders.containsKey(subIndex);
        }
    }

    public boolean isCached() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            if (isCached(subIndexes[i])) {
                return true;
            }
        }
        return false;
    }

    public void clearCache() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            clearCache(subIndexes[i]);
        }
    }

    public void clearCache(String subIndex) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            LuceneIndexHolder indexHolder = (LuceneIndexHolder) indexHolders.remove(subIndex);
            if (indexHolder != null) {
                indexHolder.markForClose();
            }
        }
    }

    public LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
        synchronized (indexHoldersLocks.get(subIndex)) {
            try {
                Directory dir = getDirectory(subIndex);
                LuceneIndexHolder indexHolder = (LuceneIndexHolder) indexHolders.get(subIndex);
                if (shouldInvalidateCache(dir, indexHolder)) {
                    clearCache(subIndex);
                    // get a new directory and put it in the cache
                    dir = getDirectory(subIndex);
                    // do the same with index holder
                    indexHolder = new LuceneIndexHolder(subIndex, dir);
                    indexHolders.put(subIndex, indexHolder);
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
        if (commitExecutorService != null) {
            commitExecutorService.shutdown();
        }
        clearCache();
        searchEngineStore.close();
    }

    public IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException {
        IndexWriter indexWriter = new IndexWriter(dir, true, searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer(),
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
        isRunning = true;
    }

    public void stop() {
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public synchronized void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        if (lastModifiled == null) {
            String[] subIndexes = searchEngineStore.getSubIndexes();
            // just update the last modified time, others will see the change and update
            for (int i = 0; i < subIndexes.length; i++) {
                Directory dir = getDirectory(subIndexes[i]);
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
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
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
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
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

    public void compoundIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Compounding index " + searchEngineStore);
        }
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
            try {
                org.apache.lucene.index.LuceneUtils.compoundDirectory(dir, luceneSettings.getTransactionLockTimout());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to compound index", e);
            }
        }
    }

    public void unCompoundIndex() throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("UnCompounding index " + searchEngineStore);
        }
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = getDirectory(subIndexes[i]);
            try {
                org.apache.lucene.index.LuceneUtils.unCompoundDirectory(dir, luceneSettings.getTransactionLockTimout());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to unCompuond index", e);
            }
        }
    }

    public void performScheduledTasks() throws SearchEngineException {
        getStore().performScheduledTasks();
    }

    public void executeCommit(Callable[] commits) throws SearchEngineException {
        if (commits.length == 0) {
            return;
        }
        // if the commit executor is created, we can do concurrent commits
        // if not, serialize it by callling it one after the other
        if (commitExecutorService != null && commits.length > concurrentCommitThreshold) {
            List futures;
            try {
                futures = commitExecutorService.invokeAll(Arrays.asList(commits));
            } catch (InterruptedException e) {
                throw new SearchEngineException("Failed to concurrent commit, interrupted", e);
            }

            for (Iterator it = futures.iterator(); it.hasNext();) {
                Future future = (Future) it.next();
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new SearchEngineException("Failed to concurrent commit, interrupted", e);
                } catch (ExecutionException e) {
                    if (e.getCause() instanceof SearchEngineException) {
                        throw (SearchEngineException) e.getCause();
                    }
                    throw new SearchEngineException("Failed to execute commit", e.getCause());
                }
            }
        } else {
            for (int i = 0; i < commits.length; i++) {
                try {
                    commits[i].call();
                } catch (Exception e) {
                    if (e instanceof SearchEngineException) {
                        throw (SearchEngineException) e;
                    }
                    throw new SearchEngineException("Failed to execute commit", e);
                }
            }
        }
    }

    public void setWaitForCacheInvalidationBeforeSecondStep(long timeToWaitInMillis) {
        this.waitForCacheInvalidationBeforeSecondStep = timeToWaitInMillis;
    }

    public LuceneSettings getSettings() {
        return luceneSettings;
    }
}
