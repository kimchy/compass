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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.transaction.context.TransactionContext;

/**
 * Specialized Lucene index manager extension.
 *
 * @author kimchy
 */
public interface LuceneSearchEngineIndexManager extends SearchEngineIndexManager {

    LuceneSettings getSettings();

    LuceneSearchEngineStore getStore();

    IndexHoldersCache getIndexHoldersCache();

    IndexWritersManager getIndexWritersManager();

    IndexSearcher openIndexSearcher(IndexReader reader);

    MultiSearcher openMultiSearcher(Searchable[] searchers) throws IOException;

    Directory getDirectory(String subIndex) throws SearchEngineException;

    /**
     * Since there might be several instances of Compass running against the same index, they
     * need to be globally notified to invalidate the cache after the commit lock has been
     * obtained for the second step on the {@link #operate(org.compass.core.engine.SearchEngineIndexManager.IndexOperationCallback)}
     * or {@link #replaceIndex(org.compass.core.engine.SearchEngineIndexManager, org.compass.core.engine.SearchEngineIndexManager.ReplaceIndexCallback)}.
     *
     * <p>If directly set to 0, will not wait.
     *
     * @param timeToWaitInMillis
     */
    void setWaitForCacheInvalidationBeforeSecondStep(long timeToWaitInMillis);

    /**
     * Returns an executor manager that can be used to execute / submit tasks.
     */
    ExecutorManager getExecutorManager();

    TransactionContext getTransactionContext();

    LuceneSearchEngineFactory getSearchEngineFactory();
}
