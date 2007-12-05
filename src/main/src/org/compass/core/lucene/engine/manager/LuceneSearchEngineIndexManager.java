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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.util.backport.java.util.concurrent.Callable;

/**
 * Specialized Lucene index manager extension.
 *
 * @author kimchy
 */
public interface LuceneSearchEngineIndexManager extends SearchEngineIndexManager {

    public static class LuceneIndexHolder {

        private long lastCacheInvalidation = System.currentTimeMillis();

        private IndexSearcher indexSearcher;

        private int count = 0;

        private boolean markForClose = false;

        private Directory dir;

        private String subIndex;

        public LuceneIndexHolder(String subIndex, Directory dir) throws IOException {
            this.dir = dir;
            this.indexSearcher = new IndexSearcher(dir);
            this.subIndex = subIndex;
        }

        public LuceneIndexHolder(String subIndex, IndexSearcher indexSearcher, Directory dir) {
            this.subIndex = subIndex;
            this.indexSearcher = indexSearcher;
            this.dir = dir;
        }

        public IndexSearcher getIndexSearcher() {
            return indexSearcher;
        }

        public IndexReader getIndexReader() {
            return indexSearcher.getIndexReader();
        }

        public Directory getDirectory() {
            return this.dir;
        }

        public String getSubIndex() {
            return this.subIndex;
        }

        public synchronized void acquire() {
            count++;
        }

        public synchronized void release() {
            count--;
            checkIfCanClose();
        }

        public synchronized void markForClose() {
            markForClose = true;
            checkIfCanClose();
        }

        private void checkIfCanClose() {
            if (markForClose && count <= 0) {
                try {
                    indexSearcher.close();
                } catch (Exception e) {
                    // do nothing
                }
            }
        }

        public long getLastCacheInvalidation() {
            return lastCacheInvalidation;
        }

        public void setLastCacheInvalidation(long lastCacheInvalidation) {
            this.lastCacheInvalidation = lastCacheInvalidation;
        }
    }

    LuceneSettings getSettings();

    LuceneSearchEngineStore getStore();

    IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException;

    void closeIndexWriter(String subIndex, IndexWriter indexWriter, Directory dir) throws SearchEngineException;

    LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException;

    void refreshCache(String subIndex, IndexSearcher indexSearcher) throws SearchEngineException;

    /**
     * Since there might be several instances of Compass running against the same index, they
     * need to be globally notified to invalidate the cache after the commit lock has been
     * obtained for the second step on the {@link #operate(org.compass.core.engine.SearchEngineIndexManager.IndexOperationCallback)}
     * or {@link #replaceIndex(org.compass.core.engine.SearchEngineIndexManager, org.compass.core.engine.SearchEngineIndexManager.ReplaceIndexCallback)}.
     * <p/>
     * If directly set to 0, will not wait.
     * <p/>
     * This one will default to the {@link ScheduledLuceneSearchEngineIndexManager} interval.
     *
     * @param timeToWaitInMillis
     */
    void setWaitForCacheInvalidationBeforeSecondStep(long timeToWaitInMillis);

    /**
     * Returns <code>true</code> if the index is in a compound form. Will return <code>true</code>
     * if the index is empty or it does not exists.
     *
     * @throws SearchEngineException
     */
    boolean isIndexCompound() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the index is in a none compound form. Will return <code>true</code>
     * if the index is empty or it does not exists.
     *
     * @throws SearchEngineException
     */
    boolean isIndexUnCompound() throws SearchEngineException;

    /**
     * Compounds the index.
     *
     * @throws SearchEngineException
     */
    void compoundIndex() throws SearchEngineException;

    /**
     * Uncompounds the index.
     *
     * @throws SearchEngineException
     */
    void unCompoundIndex() throws SearchEngineException;

    void executeCommit(Callable[] commits) throws SearchEngineException;
}
