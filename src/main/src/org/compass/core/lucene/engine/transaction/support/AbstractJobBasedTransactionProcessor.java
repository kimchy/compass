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

package org.compass.core.lucene.engine.transaction.support;

import org.apache.commons.logging.Log;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * Base class for jobs based ({@link org.compass.core.lucene.engine.transaction.support.TransactionJobs})
 * transaction processor. Mainly used for simple accumelating of {@link org.compass.core.lucene.engine.transaction.support.TransactionJob}s
 * and then processing them during commit/rollback time.
 *
 * <p>Note, if only search based operations were performed, then prepare/commit/rollback will not be called.
 *
 * @author kimchy
 */
public abstract class AbstractJobBasedTransactionProcessor extends AbstractSearchTransactionProcessor {

    private TransactionJobs jobs;

    public AbstractJobBasedTransactionProcessor(Log logger, LuceneSearchEngine searchEngine) {
        super(logger, searchEngine);
    }

    public void begin() throws SearchEngineException {
        // nothing to do here
    }

    public void prepare() throws SearchEngineException {
        if (jobs == null) {
            return;
        }
        doPrepare(jobs);
    }

    abstract protected void doPrepare(TransactionJobs jobs) throws SearchEngineException;

    public void commit(boolean onePhase) throws SearchEngineException {
        if (jobs == null) {
            return;
        }
        doCommit(onePhase, jobs);
    }

    abstract protected void doCommit(boolean onePhase, TransactionJobs jobs) throws SearchEngineException;

    public void rollback() throws SearchEngineException {
        if (jobs == null) {
            return;
        }
        doRollback(jobs);
    }

    abstract protected void doRollback(TransactionJobs jobs) throws SearchEngineException;

    public void flush() throws SearchEngineException {
    }

    public void create(InternalResource resource) throws SearchEngineException {
        getTransactionJobs().add(new TransactionJob(TransactionJob.Type.CREATE, resource));
    }

    public void update(InternalResource resource) throws SearchEngineException {
        getTransactionJobs().add(new TransactionJob(TransactionJob.Type.UPDATE, resource));
    }

    public void delete(ResourceKey resourceKey) throws SearchEngineException {
        getTransactionJobs().add(new TransactionJob(TransactionJob.Type.DELETE, resourceKey));
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
    
    private TransactionJobs getTransactionJobs() {
        if (jobs == null) {
            jobs = new TransactionJobs();
        }
        return jobs;
    }

}
