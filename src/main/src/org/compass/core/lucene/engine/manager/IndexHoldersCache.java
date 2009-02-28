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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * A cache of {@link org.compass.core.lucene.engine.manager.LuceneIndexHolder}. Provides APIs to get an
 * index holder, manage its cache invalidation (either async or sync).
 *
 * <p>NOTE: All operations are not perfomed within a transactional context. The {@link org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager}
 * provides transactionaly context for some of the operations.
 *
 * @author kimchy
 */
public class IndexHoldersCache {

    private static final Log logger = LogFactory.getLog(IndexHoldersCache.class);

    public static final String CLEAR_CACHE_NAME = "clearcache";

    private final LuceneSearchEngineIndexManager indexManager;

    private final Map<String, LuceneIndexHolder> indexHolders = new ConcurrentHashMap<String, LuceneIndexHolder>();

    private volatile ScheduledFuture scheduledRefreshCacheFuture;

    private boolean cacheAsyncInvalidation;

    private long[] lastModifiled;

    private Map<String, IndexHolderCacheLock> subIndexCacheLocks = new HashMap<String, IndexHolderCacheLock>();

    private final ConcurrentMap<String, AtomicInteger> debugOpenHoldersCount;

    private final boolean debug;
    
    public IndexHoldersCache(LuceneSearchEngineIndexManager indexManager) {
        this.indexManager = indexManager;
        for (String subIndex : indexManager.getSubIndexes()) {
            subIndexCacheLocks.put(subIndex, new IndexHolderCacheLock());
        }

        // init debug
        debug = indexManager.getSearchEngineFactory().isDebug();
        if (debug) {
            debugOpenHoldersCount = new ConcurrentHashMap<String, AtomicInteger>();
        } else {
            debugOpenHoldersCount = null;
        }
    }

    public void start() {
        cacheAsyncInvalidation = indexManager.getSettings().getSettings().getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.CACHE_ASYNC_INVALIDATION, true);
        long cacheInvalidationInterval = indexManager.getSettings().getCacheInvalidationInterval();
        if (cacheInvalidationInterval > 0 && cacheAsyncInvalidation) {
            if (logger.isInfoEnabled()) {
                logger.info("Starting scheduled refresh cache with period [" + cacheInvalidationInterval + "ms]");
            }
            ScheduledRefreshCacheRunnable scheduledRefreshCacheRunnable = new ScheduledRefreshCacheRunnable();
            scheduledRefreshCacheFuture = indexManager.getExecutorManager().scheduleWithFixedDelay(scheduledRefreshCacheRunnable, cacheInvalidationInterval, cacheInvalidationInterval, TimeUnit.MILLISECONDS);
        } else {
            logger.info("Not starting scheduled refresh cache");
        }
    }

    public void stop() {
        if (scheduledRefreshCacheFuture != null) {
            scheduledRefreshCacheFuture.cancel(true);
            scheduledRefreshCacheFuture = null;
        }
    }

    public void close() {
        if (indexManager.getSearchEngineFactory().isDebug()) {
            for (Map.Entry<String, AtomicInteger> entry : debugOpenHoldersCount.entrySet()) {
                if (entry.getValue().get() > 0) {
                    logger.error("[CACHE HOLDER] Sub Index [" + entry.getKey() +  "] has [" + entry.getValue() + "] holder(s) open");
                }
            }
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public ConcurrentMap<String, AtomicInteger> getDebugHoldersCount() {
        return debugOpenHoldersCount;
    }

    public void doUnderCacheLock(String subIndex, Runnable task) {
        synchronized (subIndexCacheLocks.get(subIndex)) {
            task.run();
        }
    }

    public boolean isCached(String subIndex) throws SearchEngineException {
        return indexHolders.containsKey(subIndex);
    }

    public boolean isCached() throws SearchEngineException {
        String[] subIndexes = indexManager.getSubIndexes();
        for (String subIndex : subIndexes) {
            if (isCached(subIndex)) {
                return true;
            }
        }
        return false;
    }

    public void clearCache() throws SearchEngineException {
        for (String subIndex : indexManager.getSubIndexes()) {
            clearCache(subIndex);
        }
    }

    public void clearCache(String subIndex) throws SearchEngineException {
        synchronized (subIndexCacheLocks.get(subIndex)) {
            LuceneIndexHolder indexHolder = indexHolders.remove(subIndex);
            if (indexHolder != null) {
                indexHolder.markForClose();
            }
        }
    }

    public void refreshCache() throws SearchEngineException {
        for (String subIndex : indexManager.getSubIndexes()) {
            refreshCache(subIndex);
        }
    }

    public void refreshCache(final String subIndex) throws SearchEngineException {
        synchronized (subIndexCacheLocks.get(subIndex)) {
            internalRefreshCache(subIndex);
        }
    }

    public void invalidateCache() throws SearchEngineException {
        for (String subIndex : indexManager.getSubIndexes()) {
            invalidateCache(subIndex);
        }
    }

    public void invalidateCache(String subIndex) throws SearchEngineException {
        LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
        if (indexHolder != null) {
            indexHolder.setInvalidated(true);
        }
    }

    public synchronized void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        if (lastModifiled == null) {
            String[] subIndexes = indexManager.getSubIndexes();
            // just update the last modified time, others will see the change and update
            for (String subIndex : subIndexes) {
                Directory dir = indexManager.getDirectory(subIndex);
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
                Directory dir = indexManager.getDirectory(subIndexes[i]);
                try {
                    lastModifiled[i] = dir.fileModified(CLEAR_CACHE_NAME);
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
        String[] subIndexes = indexManager.getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            Directory dir = indexManager.getDirectory(subIndexes[i]);
            long lastMod;
            try {
                lastMod = dir.fileModified(CLEAR_CACHE_NAME);
            } catch (IOException e) {
                throw new SearchEngineException("Failed to check last modified on global index chache on sub index [" + subIndexes[i] + "]", e);
            }
            if (lastModifiled[i] < lastMod) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Global notification to clear cache detected on sub index [" + subIndexes[i] + "]");
                }
                lastModifiled[i] = lastMod;
                clearCache(subIndexes[i]);
            }
        }
    }

    public void notifyAllToClearCache() throws SearchEngineException {
        if (logger.isTraceEnabled()) {
            logger.trace("Global notification to clear cache");
        }
        for (String subIndex : indexManager.getSubIndexes()) {
            Directory dir = indexManager.getDirectory(subIndex);
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

    /**
     * Returns an <b>acquired</b> index holder for the specified sub index. Make sure to call
     * {@link LuceneIndexHolder#release()} once it is no longer needed.
     */
    public LuceneIndexHolder getHolder(String subIndex) throws SearchEngineException {
        try {
            LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
            if (cacheAsyncInvalidation) {
                if (indexHolder == null || indexHolder.isInvalidated()) {
                    synchronized (subIndexCacheLocks.get(subIndex)) {
                        indexHolder = indexHolders.get(subIndex);
                        if (indexHolder == null || indexHolder.isInvalidated()) {
                            indexHolder = internalRefreshCache(subIndex);
                        }
                    }
                }
            } else {
                if (shouldInvalidateCache(indexHolder)) {
                    synchronized (subIndexCacheLocks.get(subIndex)) {
                        indexHolder = internalRefreshCache(subIndex);
                    }
                }
            }
            // we spin here on the aquire until we manage to aquire one
            while (!indexHolder.acquire()) {
                indexHolder = indexHolders.get(subIndex);
                if (indexHolder == null) {
                    synchronized (subIndexCacheLocks.get(subIndex)) {
                        indexHolder = indexHolders.get(subIndex);
                        if (indexHolder == null) {
                            indexHolder = internalRefreshCache(subIndex);
                        }
                    }
                }
            }
            return indexHolder;
        } catch (Exception e) {
            throw new SearchEngineException("Failed to open index searcher for sub-index [" + subIndex + "]", e);
        }
    }

    private LuceneIndexHolder internalRefreshCache(String subIndex) throws SearchEngineException {
        if (logger.isTraceEnabled()) {
            logger.trace("Refreshing cache for sub index [" + subIndex + "]");
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
                LuceneIndexHolder origHolder = indexHolder;
                indexHolder = new LuceneIndexHolder(this, subIndex, indexManager.openIndexSearcher(reader));
                // since not synchronized, we need to mark the one we replaced as closed
                LuceneIndexHolder oldHolder = indexHolders.put(subIndex, indexHolder);
                if (oldHolder != null) {
                    oldHolder.markForClose();
                }
                // mark the original holder as closed, we replaced it
                origHolder.markForClose();
            } else {
                // index did not change, we checked it now, so mark it...
                indexHolder.setInvalidated(false);
                indexHolder.markLastCacheInvalidation();
            }
        } else {
            try {
                IndexReader reader = IndexReader.open(indexManager.getDirectory(subIndex), true);
                indexHolder = new LuceneIndexHolder(this, subIndex, indexManager.openIndexSearcher(reader));
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

    /**
     * Checks if a an index holder should be invalidated.
     */
    protected boolean shouldInvalidateCache(LuceneIndexHolder indexHolder) throws IOException {
        // we have not created an index holder, invalidated by default
        if (indexHolder == null) {
            return true;
        }
        if (indexHolder.isInvalidated()) {
            return true;
        }
        // configured to perform no cache invalidation
        if (indexManager.getSettings().getCacheInvalidationInterval() == -1) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        if ((currentTime - indexHolder.getLastCacheInvalidation()) > indexManager.getSettings().getCacheInvalidationInterval()) {
            indexHolder.markLastCacheInvalidation();
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

    /**
     * A scheduled task that refresh the cache periodically (if needed).
     */
    private class ScheduledRefreshCacheRunnable implements Runnable {

        public void run() {
            indexManager.getTransactionContext().execute(new TransactionContextCallback<Object>() {
                public Object doInTransaction() throws CompassException {
                    for (String subIndex : indexManager.getSubIndexes()) {
                        try {
                            if (indexManager.getStore().indexExists(subIndex)) {
                                LuceneIndexHolder indexHolder = indexHolders.get(subIndex);
                                if (shouldInvalidateCache(indexHolder)) {
                                    synchronized (subIndexCacheLocks.get(subIndex)) {
                                        internalRefreshCache(subIndex);
                                    }
                                }
                            } else {
                                logger.trace("Sub index [" + subIndex + "] does not exists, no refresh perfomed");
                            }
                        } catch (Exception e) {
                            logger.warn("Failed to perform background refresh of cache for for sub-index [" + subIndex + "]", e);
                        }
                    }
                    return null;
                }
            });
        }
    }

    private static class IndexHolderCacheLock {

    }
}
