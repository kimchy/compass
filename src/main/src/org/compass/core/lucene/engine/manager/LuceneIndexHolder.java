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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

/**
 * @author kimchy
 */
public class LuceneIndexHolder {

    private volatile long lastCacheInvalidation = System.currentTimeMillis();

    private IndexSearcher indexSearcher;

    private IndexReader indexReader;

    private int count = 0;

    private boolean markForClose = false;

    private String subIndex;

    public LuceneIndexHolder(String subIndex, Directory dir) throws IOException {
        this.indexReader = IndexReader.open(dir, true);
        this.indexSearcher = new IndexSearcher(indexReader);
        this.subIndex = subIndex;
    }

    public LuceneIndexHolder(String subIndex, IndexSearcher indexSearcher) {
        this.subIndex = subIndex;
        this.indexSearcher = indexSearcher;
        this.indexReader = indexSearcher.getIndexReader();
    }

    public void refresh(IndexReader indexReader) {
        try {
            indexSearcher.close();
        } catch (IOException e) {
            // do nothing
        }
        this.indexReader = indexReader;
        indexSearcher = new IndexSearcher(indexReader);
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }

    public IndexReader getIndexReader() {
        return indexReader;
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
            try {
                indexReader.close();
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
