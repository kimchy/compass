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

package org.compass.core.lucene.engine.transaction.mt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;

/**
 * The MT (Multi Threaded) transaction processor allows for multi threaded indexing meaning several threads
 * can perfom the indexing process using the same Transaction Processor (Search Engine or Session).
 *
 * <p>Actual operations are delegated to the respective sub index {@link org.apache.lucene.index.IndexWriter}
 * without any buffering or delegation to another thread pool. This makes this transaction processor useful
 * mainly when there are several threas that will index data.
 *
 * @author kimchy
 */
public class MTTransactionProcessorFactory implements TransactionProcessorFactory, SearchEngineFactoryAware {

    private Map<String, Lock> subIndexOpenWritersLocks = new HashMap<String, Lock>();

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        for (String subIndex : searchEngineFactory.getIndexManager().getSubIndexes()) {
            subIndexOpenWritersLocks.put(subIndex, new ReentrantLock());
        }
    }

    /**
     * Creates a new {@link org.compass.core.lucene.engine.transaction.mt.MTTransactionProcessor}.
     */
    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new MTTransactionProcessor(this, searchEngine);
    }

    public void close() {
    }

    /**
     * MT transaction processor is thread safe.
     */
    public boolean isThreadSafe() {
        return true;
    }

    /**
     * Executes the provided task under a lock associtea with the given sub index. Used when trying to open
     * an index writer.
     */
    public <T> T doUnderIndexWriterLock(String subIndex, Callable<T> task) throws Exception {
        Lock lock = subIndexOpenWritersLocks.get(subIndex);
        lock.lock();
        try {
            return task.call();
        } finally {
            lock.unlock();
        }
    }
}