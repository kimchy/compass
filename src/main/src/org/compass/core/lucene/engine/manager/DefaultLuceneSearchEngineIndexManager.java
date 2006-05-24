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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.lucene.util.LuceneUtils;

import java.io.IOException;
import java.util.HashMap;

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

    // holds the directories cache per sub index
    private HashMap dirs = new HashMap();

    private long[] lastModifiled;

    private long waitForCacheInvalidationBeforeSecondStep = 0;

    private boolean isRunning = false;

    public DefaultLuceneSearchEngineIndexManager(LuceneSearchEngineFactory searchEngineFactory,
                                                 final LuceneSearchEngineStore searchEngineStore) {
        this.searchEngineFactory = searchEngineFactory;
        this.searchEngineStore = searchEngineStore;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
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
        final Lock[] commitLocks = new Lock[subIndexes.length];

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
                throw new SearchEngineException("Failed to retirieve dirty transaction locks", e);
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

            // first get the commit locks so no read will happen now as well
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to obtain commit/read locks");
                }
                for (int i = 0; i < subIndexes.length; i++) {
                    Directory dir = getDirectory(subIndexes[i]);
                    commitLocks[i] = dir.makeLock(IndexWriter.COMMIT_LOCK_NAME);
                    commitLocks[i].obtain(luceneSettings.getTransactionCommitTimeout());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Obtained commit/read locks");
                }
            } catch (Exception e) {
                throw new SearchEngineException("Failed to retrieve commit transaction locks", e);
            }
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
                log.debug("[Replace Index] Calling callback second step");
            }
            // call the second step
            callback.secondStep();
        } finally {
            LuceneUtils.clearLocks(commitLocks);
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

    public synchronized boolean isCached(String subIndex) throws SearchEngineException {
        return indexHolders.get(subIndex) != null;
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

    public synchronized void clearCache() throws SearchEngineException {
        String[] subIndexes = searchEngineStore.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            clearCache(subIndexes[i]);
        }
    }

    public synchronized void clearCache(String subIndex) throws SearchEngineException {
        LuceneIndexHolder indexHolder = (LuceneIndexHolder) indexHolders.remove(subIndex);
        if (indexHolder != null) {
            indexHolder.markForClose();
        }

        Directory dir = (Directory) dirs.remove(subIndex);
        if (dir != null) {
            try {
                searchEngineStore.closeDirectory(dir);
            } catch (Exception e) {
                log.error("Failed to clear cached index directory for sub-index [" + subIndex + "]", e);
            }
        }
    }

    public synchronized LuceneIndexHolder openIndexHolderByAlias(String alias) throws SearchEngineException {
        String subIndex = searchEngineStore.getSubIndexForAlias(alias);
        return openIndexHolderBySubIndex(subIndex);
    }

    public synchronized LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
        try {
            Directory dir = getDirectory(subIndex);
            LuceneIndexHolder indexHolder = (LuceneIndexHolder) indexHolders.get(subIndex);
            if (shouldInvalidateCache(dir, indexHolder)) {
                clearCache(subIndex);
                // get a new directory and put it in the cache
                dir = getDirectory(subIndex);
                // do the same with index holder
                indexHolder = new LuceneIndexHolder(new IndexSearcher(dir));
                indexHolders.put(subIndex, indexHolder);
            }
            indexHolder.acquire();
            return indexHolder;
        } catch (Exception e) {
            throw new SearchEngineException("Failed to open index searcher for sub-index [" + subIndex + "]", e);
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

    public IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException {
        IndexWriter indexWriter = new IndexWriter(dir, searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer(),
                create);
        indexWriter.setMaxMergeDocs(luceneSettings.getMaxMergeDocs());
        indexWriter.setMergeFactor(luceneSettings.getMergeFactor());
        indexWriter.setUseCompoundFile(luceneSettings.isUseCompoundFile());
        indexWriter.setMaxFieldLength(luceneSettings.getMaxFieldLength());
        indexWriter.setMaxBufferedDocs(luceneSettings.getMaxBufferedDocs());
        return indexWriter;
    }

    public void closeIndexWriter(IndexWriter indexWriter, Directory dir) throws SearchEngineException {
        Exception ex = null;
        try {
            closeIndexWriter(indexWriter);
        } catch (Exception e) {
            ex = e;
        }
        try {
            searchEngineStore.closeDirectory(dir);
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

    public Directory getDirectory(String subIndex) {
        Directory dir = (Directory) dirs.get(subIndex);
        if (dir == null) {
            dir = getStore().getDirectoryBySubIndex(subIndex, false);
            dirs.put(subIndex, dir);
        }
        return dir;
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
                if (!org.apache.lucene.index.LuceneUtils.isCompound(dir, luceneSettings.getTransactionCommitTimeout()))
                {
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
                if (!org.apache.lucene.index.LuceneUtils.isUnCompound(dir, luceneSettings.getTransactionCommitTimeout()))
                {
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
                org.apache.lucene.index.LuceneUtils.compoundDirectory(dir,
                        luceneSettings.getTransactionLockTimout(), luceneSettings.getTransactionCommitTimeout());
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
                org.apache.lucene.index.LuceneUtils.unCompoundDirectory(dir,
                        luceneSettings.getTransactionLockTimout(), luceneSettings.getTransactionCommitTimeout());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to unCompuond index", e);
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
