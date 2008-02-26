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

package org.compass.core.lucene.engine.optimizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * @author kimchy
 */
public abstract class AbstractOptimizer implements LuceneSearchEngineOptimizer {

    protected final Log log = LogFactory.getLog(getClass());

    private LuceneSearchEngineFactory searchEngineFactory;

    private volatile boolean isRunning = false;

    public void start() throws SearchEngineException {
        if (isRunning) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Starting Optimizer");
        }
        doStart();
        isRunning = true;
    }

    protected void doStart() throws SearchEngineException {

    }

    public void stop() throws SearchEngineException {
        if (!isRunning) {
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Stopping Optimizer");
        }
        doStop();
        isRunning = false;
    }

    protected void doStop() throws SearchEngineException {

    }

    public boolean isRunning() {
        return isRunning;
    }

    public void optimize() throws SearchEngineException {
        LuceneSearchEngineIndexManager indexManager = searchEngineFactory.getLuceneIndexManager();
        String[] subIndexes = indexManager.getStore().getSubIndexes();
        for (String subIndex : subIndexes) {
            // here we go indirectly since it might be wrapped in a transaction
            optimize(subIndex);
        }
    }

    public void optimize(final String subIndex) throws SearchEngineException {
        if (!isRunning()) {
            return;
        }

        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction(InternalCompassTransaction tr) throws CompassException {
                doOptimize(subIndex);
                searchEngineFactory.getIndexManager().refreshCache(subIndex);
                return null;
            }
        });
    }

    protected abstract void doOptimize(String subIndex) throws SearchEngineException;

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

}
