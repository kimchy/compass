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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.util.backport.java.util.concurrent.ScheduledExecutorService;
import org.compass.core.util.backport.java.util.concurrent.Executors;
import org.compass.core.util.backport.java.util.concurrent.TimeUnit;
import org.compass.core.util.concurrent.SingleThreadThreadFactory;

/**
 * @author kimchy
 */
public class ScheduledLuceneSearchEngineIndexManager implements LuceneSearchEngineIndexManager {

    private static Log log = LogFactory.getLog(ScheduledLuceneSearchEngineIndexManager.class);

    private LuceneSearchEngineIndexManager indexManager;

    private ScheduledExecutorService scheduledExecutorService;

    private LuceneSettings settings;

    public ScheduledLuceneSearchEngineIndexManager(LuceneSearchEngineIndexManager indexManager) {
        this.indexManager = indexManager;
        this.settings = indexManager.getSettings();
    }

    public void start() {
        indexManager.start();

        if (log.isInfoEnabled()) {
            log.info("Starting scheduled index manager with period [" + settings.getIndexManagerScheduleInterval()
                    + "ms] daemon [true]");
        }
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new SingleThreadThreadFactory("Compass Scheduled IndexManager", true));
        ScheduledIndexManagerRunnable scheduledIndexManagerRunnable =
                new ScheduledIndexManagerRunnable(indexManager);
        long period = settings.getIndexManagerScheduleInterval();
        scheduledExecutorService.scheduleWithFixedDelay(scheduledIndexManagerRunnable, period, period, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (log.isInfoEnabled()) {
            log.info("Stopping scheduled index manager");
        }
        scheduledExecutorService.shutdown();
        scheduledExecutorService = null;
        indexManager.stop();
    }

    public void close() {
        stop();
        indexManager.close();
    }

    public void createIndex() throws SearchEngineException {
        indexManager.createIndex();
    }

    public boolean verifyIndex() throws SearchEngineException {
        return indexManager.verifyIndex();
    }

    public void deleteIndex() throws SearchEngineException {
        indexManager.deleteIndex();
    }

    public boolean indexExists() throws SearchEngineException {
        return indexManager.indexExists();
    }

    public void operate(IndexOperationCallback callback) throws SearchEngineException {
        stop();
        try {
            indexManager.operate(callback);
        } finally {
            start();
        }
    }

    public void replaceIndex(SearchEngineIndexManager innerIndexManager, ReplaceIndexCallback callback) throws SearchEngineException {
        indexManager.replaceIndex(innerIndexManager, callback);
    }

    public boolean isCached(String subIndex) throws SearchEngineException {
        return indexManager.isCached(subIndex);
    }

    public boolean isCached() throws SearchEngineException {
        return indexManager.isCached();
    }

    public void clearCache(String subIndex) throws SearchEngineException {
        indexManager.clearCache(subIndex);
    }

    public void clearCache() throws SearchEngineException {
        indexManager.clearCache();
    }

    public void notifyAllToClearCache() throws SearchEngineException {
        indexManager.notifyAllToClearCache();
    }

    public void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
        indexManager.checkAndClearIfNotifiedAllToClearCache();
    }

    public boolean isIndexCompound() throws SearchEngineException {
        return indexManager.isIndexCompound();
    }

    public boolean isIndexUnCompound() throws SearchEngineException {
        return indexManager.isIndexUnCompound();
    }

    public void compoundIndex() throws SearchEngineException {
        indexManager.compoundIndex();
    }

    public void unCompoundIndex() throws SearchEngineException {
        indexManager.unCompoundIndex();
    }

    // methods from lucene search engine index manager

    public LuceneSettings getSettings() {
        return indexManager.getSettings();
    }

    public LuceneSearchEngineStore getStore() {
        return indexManager.getStore();
    }

    public IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException {
        return indexManager.openIndexWriter(dir, create);
    }

    public void closeIndexWriter(IndexWriter indexWriter, Directory dir) throws SearchEngineException {
        indexManager.closeIndexWriter(indexWriter, dir);
    }

    public LuceneIndexHolder openIndexHolderByAlias(String alias) throws SearchEngineException {
        return indexManager.openIndexHolderByAlias(alias);
    }

    public LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
        return indexManager.openIndexHolderBySubIndex(subIndex);
    }

    private static class ScheduledIndexManagerRunnable implements Runnable {

        private LuceneSearchEngineIndexManager indexManager;

        public ScheduledIndexManagerRunnable(LuceneSearchEngineIndexManager indexManager) {
            this.indexManager = indexManager;
        }

        public void run() {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Checking for global cache invalidation");
                }
                indexManager.checkAndClearIfNotifiedAllToClearCache();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to check clear cache", e);
                }
            }
            try {
                indexManager.getStore().performScheduledTasks();
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Failed to perform schedule task", e);
                }
            }
        }

    }

}
