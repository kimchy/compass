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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

/**
 * @author kimchy
 */
public class LuceneIndexHolder {

    private final IndexHoldersCache indexHoldersCache;

    private final String subIndex;

    private final IndexSearcher indexSearcher;

    private final IndexReader indexReader;

    private volatile long lastCacheInvalidation = System.currentTimeMillis();

    private volatile boolean invalidated;

    private int count = 0;

    private boolean markForClose = false;

    private boolean closed;

    public LuceneIndexHolder(IndexHoldersCache indexHoldersCache, String subIndex, IndexSearcher indexSearcher) {
        this.indexHoldersCache = indexHoldersCache;
        this.subIndex = subIndex;
        this.indexSearcher = indexSearcher;
        this.indexReader = indexSearcher.getIndexReader();
        if (indexHoldersCache.isDebug()) {
            AtomicInteger count = indexHoldersCache.getDebugHoldersCount().get(subIndex);
            if (count == null) {
                AtomicInteger newCount = new AtomicInteger();
                count = indexHoldersCache.getDebugHoldersCount().putIfAbsent(subIndex, newCount);
                if (count == null) {
                    count = newCount;
                }
            }
            count.incrementAndGet();
        }
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

    public synchronized boolean acquire() {
        if (markForClose) {
            return false;
        }
        count++;
        return true;
    }

    public synchronized void release() {
        count--;
        checkIfCanClose();
    }

    public synchronized void markForClose() {
        markForClose = true;
        checkIfCanClose();
    }

    public boolean isInvalidated() {
        return invalidated;
    }

    public void setInvalidated(boolean invalidated) {
        this.invalidated = invalidated;
    }

    private void checkIfCanClose() {
        if (markForClose && count <= 0 && !closed) {
            if (indexHoldersCache.isDebug()) {
                indexHoldersCache.getDebugHoldersCount().get(subIndex).decrementAndGet();
            }
            closed = true;
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

    public void markLastCacheInvalidation() {
        this.lastCacheInvalidation = System.currentTimeMillis();
    }
}
