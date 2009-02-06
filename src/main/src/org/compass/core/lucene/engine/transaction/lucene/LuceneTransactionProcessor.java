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

package org.compass.core.lucene.engine.transaction.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.transaction.support.AbstractConcurrentTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.CommitCallable;
import org.compass.core.lucene.engine.transaction.support.PrepareCommitCallable;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJob;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.context.TransactionalCallable;

/**
 * Lucene based transaction, allows to perfom dirty operations directly over the index
 * using Lucene support for transactions. Reads and search will be performed on the
 * index itself without taking into account any transactional operations.
 *
 * @author kimchy
 */
public class LuceneTransactionProcessor extends AbstractConcurrentTransactionProcessor {

    private static final Log logger = LogFactory.getLog(LuceneTransactionProcessor.class);

    private Map<String, IndexWriter> indexWriterBySubIndex;

    public LuceneTransactionProcessor(LuceneSearchEngine searchEngine) {
        super(logger, searchEngine, false, searchEngine.getSearchEngineFactory().getIndexManager().supportsConcurrentOperations());
        if (isConcurrentOperations()) {
            indexWriterBySubIndex = new ConcurrentHashMap<String, IndexWriter>();
        } else {
            indexWriterBySubIndex = new HashMap<String, IndexWriter>();
        }
    }

    public String getName() {
        return LuceneEnvironment.Transaction.Processor.Lucene.NAME;
    }

    protected String[] getDirtySubIndexes() {
        return indexWriterBySubIndex.keySet().toArray(new String[indexWriterBySubIndex.keySet().size()]);
    }

    @Override
    protected void doRollback() throws SearchEngineException {
        SearchEngineException exception = null;
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            try {
                entry.getValue().rollback();
            } catch (AlreadyClosedException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to abort transaction for sub index [" + entry.getKey() + "] since it is alreayd closed");
                }
            } catch (IOException e) {
                Directory dir = indexManager.getStore().openDirectory(entry.getKey());
                try {
                    if (IndexWriter.isLocked(dir)) {
                        IndexWriter.unlock(dir);
                    }
                } catch (Exception e1) {
                    logger.warn("Failed to check for locks or unlock failed commit for sub index [" + entry.getKey() + "]", e);
                }
                exception = new SearchEngineException("Failed to rollback transaction for sub index [" + entry.getKey() + "]", e);
            } finally {
                indexManager.getIndexWritersManager().trackCloseIndexWriter(entry.getKey(), entry.getValue());
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    @Override
    protected void doPrepare() throws SearchEngineException {
        if (indexWriterBySubIndex.isEmpty()) {
            return;
        }
        if (indexManager.supportsConcurrentCommits()) {
            ArrayList<Callable<Object>> prepareCallables = new ArrayList<Callable<Object>>();
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                prepareCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new PrepareCommitCallable(entry.getKey(), entry.getValue())));
            }
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(prepareCallables, 1);
        } else {
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                try {
                    new PrepareCommitCallable(entry.getKey(), entry.getValue()).call();
                } catch (SearchEngineException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to commit transaction for sub index [" + entry.getKey() + "]", e);
                }
            }
        }
    }

    @Override
    protected void doCommit(boolean onePhase) throws SearchEngineException {
        if (indexWriterBySubIndex.isEmpty()) {
            return;
        }
        // here, we issue doPrepare since if only one of the sub indexes failed with it, then
        // it should fail.
        if (onePhase) {
            try {
                prepare();
            } catch (SearchEngineException e) {
                try {
                    rollback();
                } catch (Exception e1) {
                    logger.trace("Failed to rollback after prepare failure in one phase commit", e);
                }
                throw e;
            }
        }
        if (indexManager.supportsConcurrentCommits()) {
            ArrayList<Callable<Object>> commitCallables = new ArrayList<Callable<Object>>();
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                commitCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new CommitCallable(indexManager, entry.getKey(), entry.getValue(), isInvalidateCacheOnCommit())));
            }
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(commitCallables, 1);
        } else {
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                try {
                    new CommitCallable(indexManager, entry.getKey(), entry.getValue(), isInvalidateCacheOnCommit()).call();
                } catch (SearchEngineException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to commit transaction for sub index [" + entry.getKey() + "]", e);
                }
            }
        }
    }

    @Override
    protected void doProcessJob(TransactionJob job) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(job.getSubIndex());
            job.execute(indexWriter, searchEngineFactory);
        } catch (Exception e) {
            throw new SearchEngineException("Failed to execeute job [" + job + "]", e);
        }
    }

    @Override
    protected LuceneSearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        return performFind(query);
    }

    @Override
    protected LuceneSearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        return performInternalSearch(subIndexes, aliases);
    }

    @Override
    protected Resource[] doGet(ResourceKey resourceKey) throws SearchEngineException {
        return performGet(resourceKey);
    }

    /**
     * Just open an index writer here on the same calling thread so we maintain ordering of operations as well
     * as no need for double check if we created it or not using expensive global locking.
     */
    @Override
    protected void prepareBeforeAsyncDirtyOperation(TransactionJob job) throws SearchEngineException {
        try {
            getOrCreateIndexWriter(job.getSubIndex());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open index writer for sub index [" + job.getSubIndex() + "]", e);
        }
    }

    protected IndexWriter getOrCreateIndexWriter(String subIndex) throws IOException {
        IndexWriter indexWriter = indexWriterBySubIndex.get(subIndex);
        if (indexWriter != null) {
            return indexWriter;
        }
        indexWriter = indexManager.getIndexWritersManager().openIndexWriter(searchEngine.getSettings(), subIndex);
        indexWriterBySubIndex.put(subIndex, indexWriter);
        indexManager.getIndexWritersManager().trackOpenIndexWriter(subIndex, indexWriter);
        return indexWriter;
    }

}
