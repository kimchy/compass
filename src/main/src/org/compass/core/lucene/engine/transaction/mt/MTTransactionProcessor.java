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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.compass.core.lucene.engine.transaction.support.AbstractSearchTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.CommitCallable;
import org.compass.core.lucene.engine.transaction.support.PrepareCommitCallable;
import org.compass.core.lucene.engine.transaction.support.WriterHelper;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.context.TransactionalCallable;

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
public class MTTransactionProcessor extends AbstractSearchTransactionProcessor {

    private static final Log logger = LogFactory.getLog(MTTransactionProcessor.class);

    private final MTTransactionProcessorFactory transactionProcessorFactory;

    private final Map<String, IndexWriter> indexWriterBySubIndex;

    public MTTransactionProcessor(MTTransactionProcessorFactory transactionProcessorFactory, LuceneSearchEngine searchEngine) {
        super(logger, searchEngine);
        this.transactionProcessorFactory = transactionProcessorFactory;
        indexWriterBySubIndex = new ConcurrentHashMap<String, IndexWriter>();
    }

    public String getName() {
        return LuceneEnvironment.Transaction.Processor.MT.NAME;
    }

    public void begin() throws SearchEngineException {
        // nothing to do here
    }

    public synchronized void prepare() throws SearchEngineException {
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

    public synchronized void commit(boolean onePhase) throws SearchEngineException {
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

    public synchronized void rollback() throws SearchEngineException {
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
        indexWriterBySubIndex.clear();
        if (exception != null) {
            throw exception;
        }
    }

    public LuceneSearchEngineHits find(LuceneSearchEngineQuery query) throws SearchEngineException {
        return performFind(query);
    }

    public Resource[] get(ResourceKey resourceKey) throws SearchEngineException {
        return performGet(resourceKey);
    }

    public LuceneSearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        return performInternalSearch(subIndexes, aliases);
    }

    public void flush() throws SearchEngineException {
        // nothing to do here
    }

    public void flushCommit(String... aliases) throws SearchEngineException {
        // intersect
        Set<String> calcSubIndexes = new HashSet<String>();
        if (aliases == null || aliases.length == 0) {
            calcSubIndexes.addAll(indexWriterBySubIndex.keySet());
        } else {
            Set<String> dirtySubIndxes = indexWriterBySubIndex.keySet();
            Set<String> requiredSubIndexes = new HashSet<String>(Arrays.asList(indexManager.polyCalcSubIndexes(null, aliases, null)));
            for (String subIndex : indexManager.getSubIndexes()) {
                if (dirtySubIndxes.contains(subIndex) && requiredSubIndexes.contains(subIndex)) {
                    calcSubIndexes.add(subIndex);
                }
            }
        }
        for (String subIndex : calcSubIndexes) {
            try {
                indexWriterBySubIndex.get(subIndex).commit();
            } catch (IOException e) {
                throw new SearchEngineException("Failed to flush commit sub index [" + subIndex + "]", e);
            }
        }
    }

    public void create(InternalResource resource) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resource.getSubIndex());
            WriterHelper.processCreate(indexWriter, resource);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource [" + resource + "] on sub index [" + resource.getSubIndex() + "]", e);
        }
    }

    public void update(InternalResource resource) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resource.getSubIndex());
            WriterHelper.processUpdate(indexWriter, resource);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to update resource [" + resource + "] on sub index [" + resource.getSubIndex() + "]", e);
        }
    }

    public void delete(ResourceKey resourceKey) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resourceKey.getSubIndex());
            WriterHelper.processDelete(indexWriter, resourceKey);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete resource [" + resourceKey + "] on sub index [" + resourceKey.getSubIndex() + "]", e);
        }
    }

    public void delete(LuceneSearchEngineQuery query) throws SearchEngineException {
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(query.getSubIndexes(), query.getAliases());
            for (String subIndex : calcSubIndexes) {
                IndexWriter indexWriter = getOrCreateIndexWriter(subIndex);
                WriterHelper.processDelete(indexWriter, query.getQuery());
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete query [" + query + "]", e);
        }
    }

    /**
     * Opens a new index writer if there is no open one already for the provided sub index.
     *
     * <p>Thread safe.
     */
    protected IndexWriter getOrCreateIndexWriter(final String subIndex) throws SearchEngineException {
        IndexWriter indexWriter = indexWriterBySubIndex.get(subIndex);
        if (indexWriter != null) {
            return indexWriter;
        }
        try {
            return transactionProcessorFactory.doUnderIndexWriterLock(subIndex, new Callable<IndexWriter>() {
                public IndexWriter call() throws Exception {
                    IndexWriter indexWriter = indexWriterBySubIndex.get(subIndex);
                    if (indexWriter != null) {
                        return indexWriter;
                    }
                    indexWriter = indexManager.getIndexWritersManager().openIndexWriter(searchEngine.getSettings(), subIndex);
                    indexWriterBySubIndex.put(subIndex, indexWriter);
                    indexManager.getIndexWritersManager().trackOpenIndexWriter(subIndex, indexWriter);
                    return indexWriter;
                }
            });
        } catch (Exception e) {
            throw new SearchEngineException("Failed to open index writer for sub index [" + subIndex + "]", e);
        }
    }
}