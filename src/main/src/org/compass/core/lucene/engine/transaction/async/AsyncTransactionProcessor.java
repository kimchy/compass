/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.lucene.engine.transaction.async;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.transaction.support.AbstractJobBasedTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.TransactionJobs;
import org.compass.core.spi.ResourceKey;

/**
 * Processes transaction asynchronously. For more information see {@link org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessorFactory}.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessorFactory
 */
public class AsyncTransactionProcessor extends AbstractJobBasedTransactionProcessor {

    private final static Log logger = LogFactory.getLog(AsyncTransactionProcessor.class); 

    private final AsyncTransactionProcessorFactory processorFactory;

    /**
     * Constructs a new processor (for a given transaction) with a back reference to the
     * {@link AsyncTransactionProcessorFactory} in order to add (at commit) the
     * {@link org.compass.core.lucene.engine.transaction.support.TransactionJobs}.
     */
    public AsyncTransactionProcessor(LuceneSearchEngine searchEngine, AsyncTransactionProcessorFactory processorFactory) {
        super(logger, searchEngine);
        this.processorFactory = processorFactory;
    }

    public String getName() {
        return LuceneEnvironment.Transaction.Processor.Async.NAME;
    }

    protected void doPrepare(TransactionJobs jobs) throws SearchEngineException {
        // nothign to do here, we only add on commit
    }

    protected void doCommit(boolean onePhase, TransactionJobs jobs) throws SearchEngineException {
        processorFactory.add(jobs);
    }

    protected void doRollback(TransactionJobs jobs) throws SearchEngineException {
        processorFactory.remove(jobs);
    }

    public LuceneSearchEngineHits find(LuceneSearchEngineQuery query) throws SearchEngineException {
        return performFind(query);
    }

    public LuceneSearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        return performInternalSearch(subIndexes, aliases);
    }

    public Resource[] get(ResourceKey resourceKey) throws SearchEngineException {
        return performGet(resourceKey);
    }
}
