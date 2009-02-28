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

package org.compass.core.lucene.engine.transaction.readcommitted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.DefaultLuceneSearchEngineHits;
import org.compass.core.lucene.engine.EmptyLuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.engine.transaction.support.AbstractConcurrentTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.job.DeleteByQueryTransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.DeleteTransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.FlushCommitTransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.UpdateTransactionJob;
import org.compass.core.lucene.search.CacheableMultiReader;
import org.compass.core.lucene.support.ChainedFilter;
import org.compass.core.lucene.support.ResourceHelper;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.context.TransactionalCallable;
import org.compass.core.util.StringUtils;

/**
 * Read committed transaction processor allows to isolate changes done during a transaction from other
 * transactions until commit. It also allows for load/get/find operations to take into account changes
 * done during the current transaction. This means that a delete that occurs during a transaction will
 * be filtered out if a search is executed within the same transaction just after the delete.
 *
 * @author kimchy
 */
public class ReadCommittedTransactionProcessor extends AbstractConcurrentTransactionProcessor {

    private static final Log logger = LogFactory.getLog(ReadCommittedTransactionProcessor.class);

    private final TransIndexManager transIndexManager;

    private final Map<String, IndexWriter> indexWriterBySubIndex;

    private final BitSetByAliasFilter filter;

    private final Map<String, LuceneIndexHolder> indexHoldersBySubIndex;

    public ReadCommittedTransactionProcessor(LuceneSearchEngine searchEngine) {
        super(logger, searchEngine, true, searchEngine.getSearchEngineFactory().getIndexManager().supportsConcurrentOperations());
        if (isConcurrentOperations()) {
            indexWriterBySubIndex = new ConcurrentHashMap<String, IndexWriter>();
            indexHoldersBySubIndex = new ConcurrentHashMap<String, LuceneIndexHolder>();
        } else {
            indexWriterBySubIndex = new HashMap<String, IndexWriter>();
            indexHoldersBySubIndex = new HashMap<String, LuceneIndexHolder>();
        }
        this.filter = new BitSetByAliasFilter(isConcurrentOperations());
        this.transIndexManager = new TransIndexManager(searchEngine.getSearchEngineFactory(), isConcurrentOperations());
        this.transIndexManager.configure(searchEngine.getSettings());
    }

    public String getName() {
        return LuceneEnvironment.Transaction.Processor.ReadCommitted.NAME;
    }

    protected String[] getDirtySubIndexes() {
        return indexWriterBySubIndex.keySet().toArray(new String[indexWriterBySubIndex.keySet().size()]);
    }

    @Override
    public void begin() throws SearchEngineException {
        this.filter.clear();
    }

    @Override
    protected void doPrepare() throws SearchEngineException {
        releaseHolders();
        if (indexManager.supportsConcurrentCommits()) {
            ArrayList<Callable<Object>> prepareCallables = new ArrayList<Callable<Object>>();
            for (String subIndex : indexWriterBySubIndex.keySet()) {
                if (!transIndexManager.hasTransIndex(subIndex)) {
                    continue;
                }
                prepareCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new PrepareCallable(subIndex)));
            }
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(prepareCallables, 1);
        } else {
            for (String subIndex : indexWriterBySubIndex.keySet()) {
                if (!transIndexManager.hasTransIndex(subIndex)) {
                    continue;
                }
                try {
                    new PrepareCallable(subIndex).call();
                } catch (SearchEngineException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to prepare transaction for sub index [" + subIndex + "]", e);
                }
            }
        }
    }

    @Override
    protected void doCommit(boolean onePhase) throws SearchEngineException {
        releaseHolders();
        // here, we issue doPrepare since if only one of the sub indexes failed with it, then
        // it should fail.
        if (onePhase) {
            prepare();
        }
        if (indexManager.supportsConcurrentCommits()) {
            ArrayList<Callable<Object>> commitCallables = new ArrayList<Callable<Object>>();
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                commitCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new CommitCallable(entry.getKey(), entry.getValue(), false)));
            }
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(commitCallables, 1);
        } else {
            for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
                try {
                    new CommitCallable(entry.getKey(), entry.getValue(), false).call();
                } catch (SearchEngineException e) {
                    throw e;
                } catch (Exception e) {
                    throw new SearchEngineException("Failed to commit transaction for sub index [" + entry.getKey() + "]", e);
                }
            }
        }
    }

    @Override
    protected void doRollback() throws SearchEngineException {
        releaseHolders();
        SearchEngineException lastException = null;
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            try {
                if (transIndexManager.hasTransIndex(entry.getKey())) {
                    transIndexManager.rollback(entry.getKey());
                    transIndexManager.close(entry.getKey());
                }
            } catch (Exception e) {
                // ignore this exception
            }
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
                lastException = new SearchEngineException("Failed to rollback sub index [" + entry.getKey() + "]", e);
            } finally {
                indexManager.getIndexWritersManager().trackCloseIndexWriter(entry.getKey(), entry.getValue());
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }

    @Override
    protected LuceneSearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        // if there are no ongoing dirty operations, simply return the default one which is faster
        if (indexHoldersBySubIndex.isEmpty() && !transIndexManager.hasTransactions()) {
            // TODO somehow, we need to find a way to pass the useFieldCache parameter
            return super.buildInternalSearch(subIndexes, aliases, true);
        }
        // we have a transaction, take it into account
        ArrayList<LuceneIndexHolder> indexHoldersToClose = new ArrayList<LuceneIndexHolder>();
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(subIndexes, aliases);
            ArrayList<IndexSearcher> searchers = new ArrayList<IndexSearcher>();
            for (String subIndex : calcSubIndexes) {
                LuceneIndexHolder indexHolder = indexHoldersBySubIndex.get(subIndex);
                if (indexHolder == null) {
                    indexHolder = indexManager.getIndexHoldersCache().getHolder(subIndex);
                    indexHoldersToClose.add(indexHolder);
                }
                if (indexHolder.getIndexReader().numDocs() > 0) {
                    searchers.add(indexHolder.getIndexSearcher());
                }
                if (transIndexManager.hasTransIndex(subIndex)) {
                    searchers.add(transIndexManager.getSearcher(subIndex));
                }
            }
            if (searchers.size() == 0) {
                return new LuceneSearchEngineInternalSearch(searchEngine, indexHoldersToClose);
            }
            MultiSearcher indexSeracher = indexManager.openMultiSearcher(searchers.toArray(new Searcher[searchers.size()]));
            return new LuceneSearchEngineInternalSearch(searchEngine, indexSeracher, indexHoldersToClose);
        } catch (IOException e) {
            for (LuceneIndexHolder indexHolder : indexHoldersToClose) {
                indexHolder.release();
            }
            throw new SearchEngineException("Failed to open Lucene reader/searcher", e);
        }
    }

    @Override
    protected LuceneSearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        LuceneSearchEngineInternalSearch internalSearch = internalSearch(query.getSubIndexes(), query.getAliases());
        if (internalSearch.isEmpty()) {
            return new EmptyLuceneSearchEngineHits(searchEngine, internalSearch);
        }
        Filter qFilter = null;
        if (filter.hasDeletes()) {
            if (query.getFilter() == null) {
                qFilter = filter;
            } else {
                qFilter = new ChainedFilter(new Filter[]{filter, query.getFilter().getFilter()}, ChainedFilter.AND);
            }
        } else {
            if (query.getFilter() != null) {
                qFilter = query.getFilter().getFilter();
            }
        }
        Hits hits = findByQuery(internalSearch, query, qFilter);
        return new DefaultLuceneSearchEngineHits(hits, searchEngine, query, internalSearch);
    }

    @Override
    protected Resource[] doGet(ResourceKey resourceKey) throws SearchEngineException {
        Searcher indexSearcher = null;
        IndexReader indexReader = null;
        LuceneIndexHolder indexHolder = null;
        boolean releaseHolder = false;
        boolean closeReaderAndSearcher = false;
        try {
            String subIndex = resourceKey.getSubIndex();
            indexHolder = indexHoldersBySubIndex.get(subIndex);
            if (indexHolder == null) {
                indexHolder = indexManager.getIndexHoldersCache().getHolder(subIndex);
                releaseHolder = true;
            } else {
                releaseHolder = false;
            }
            if (transIndexManager.hasTransIndex(subIndex)) {
                closeReaderAndSearcher = true;
                indexReader = new CacheableMultiReader(new IndexReader[]{indexHolder.getIndexReader(), transIndexManager.getReader(subIndex)}, false);
                // note, we need to create a multi searcher here instead of a searcher ontop of the MultiReader
                // since our filter relies on specific reader per searcher
                indexSearcher = indexManager.openMultiSearcher(new Searcher[]{new IndexSearcher(indexHolder.getIndexReader()), transIndexManager.getSearcher(subIndex)});
            } else {
                indexReader = indexHolder.getIndexReader();
                indexSearcher = indexHolder.getIndexSearcher();
            }
            if (filter.hasDeletes()) {
                // TODO we can do better with HitCollector
                Query query = ResourceHelper.buildResourceLoadQuery(resourceKey);
                Hits hits = indexSearcher.search(query, filter);
                return ResourceHelper.hitsToResourceArray(hits, searchEngine);
            } else {
                Term t = new Term(resourceKey.getUIDPath(), resourceKey.buildUID());
                TermDocs termDocs = null;
                try {
                    termDocs = indexReader.termDocs(t);
                    if (termDocs != null) {
                        return ResourceHelper.hitsToResourceArray(termDocs, indexReader, searchEngine);
                    } else {
                        return new Resource[0];
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to search for property [" + resourceKey + "]", e);
                } finally {
                    try {
                        if (termDocs != null) {
                            termDocs.close();
                        }
                    } catch (IOException e) {
                        // swallow it
                    }
                }
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to find for alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]", e);
        } finally {
            if (indexHolder != null && releaseHolder) {
                indexHolder.release();
            }
            if (closeReaderAndSearcher) {
                try {
                    indexSearcher.close();
                } catch (Exception e) {
                    // ignore
                }
                try {
                    indexReader.close();
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    @Override
    protected void doProcessJob(TransactionJob job) throws SearchEngineException {
        try {
            IndexWriter indexWriter = openIndexWriterIfNeeded(job.getSubIndex());
            if (job instanceof DeleteTransactionJob) {
                DeleteTransactionJob deleteJob = (DeleteTransactionJob) job;
                markDeleted(deleteJob.getResourceKey());
                // delete from the original index (autoCommit is false, so won't be committed)
                job.execute(indexWriter, searchEngineFactory);
                transIndexManager.processJob(job);
            } else if (job instanceof DeleteByQueryTransactionJob) {
                job.execute(indexWriter, searchEngineFactory);
                transIndexManager.processJob(job);
            } else if (job instanceof UpdateTransactionJob) {
                UpdateTransactionJob updateJob = (UpdateTransactionJob) job;
                Term deleteTerm = markDeleted(updateJob.getResource().getResourceKey());
                // delete from the original index (autoCommit is false, so won't be committed
                indexWriter.deleteDocuments(deleteTerm);
                transIndexManager.processJob(job);
            } else if (job instanceof FlushCommitTransactionJob) {
                if (transIndexManager.hasTransIndex(job.getSubIndex())) {
                    transIndexManager.commit(job.getSubIndex());
                    Directory transDir = transIndexManager.getDirectory(job.getSubIndex());
                    indexWriter.addIndexesNoOptimize(new Directory[]{transDir});
                    transIndexManager.close(job.getSubIndex());
                }
                indexWriter.commit();
            } else {
                // Create job
                transIndexManager.processJob(job);
            }
        } catch (Exception e) {
            throw new SearchEngineException("Failed to process job [" + job + "]", e);
        }
    }

    /**
     * Just open an index writer here on the same calling thread so we maintain ordering of operations as well
     * as no need for double check if we created it or not using expensive global locking.
     */
    @Override
    protected void prepareBeforeAsyncDirtyOperation(TransactionJob job) throws SearchEngineException {
        try {
            openIndexWriterIfNeeded(job.getSubIndex());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open writer for sub index [" + job.getSubIndex() + "]", e);
        }
    }

    private Term markDeleted(ResourceKey resourceKey) {
        LuceneIndexHolder indexHolder = indexHoldersBySubIndex.get(resourceKey.getSubIndex());
        if (indexHolder == null) {
            synchronized (indexHoldersBySubIndex) {
                indexHolder = indexHoldersBySubIndex.get(resourceKey.getSubIndex());
                if (indexHolder == null) {
                    // make sure we have a clean cache...
                    indexManager.getIndexHoldersCache().refreshCache(resourceKey.getSubIndex());
                    indexHolder = indexManager.getIndexHoldersCache().getHolder(resourceKey.getSubIndex());
                    indexHoldersBySubIndex.put(resourceKey.getSubIndex(), indexHolder);
                }
            }
        }

        // mark the deleted term in the filter
        Term deleteTerm = new Term(resourceKey.getUIDPath(), resourceKey.buildUID());
        TermDocs termDocs = null;
        try {
            termDocs = indexHolder.getIndexReader().termDocs(deleteTerm);
            if (termDocs != null) {
                int maxDoc = indexHolder.getIndexReader().maxDoc();
                try {
                    while (termDocs.next()) {
                        filter.markDelete(indexHolder.getIndexReader(), termDocs.doc(), maxDoc);
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to iterate data in order to delete", e);
                }
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to search for property [" + resourceKey + "]", e);
        } finally {
            try {
                if (termDocs != null) {
                    termDocs.close();
                }
            } catch (IOException e) {
                // swallow it
            }
        }
        return deleteTerm;
    }

    protected IndexWriter openIndexWriterIfNeeded(String subIndex) throws IOException {
        IndexWriter indexWriter = indexWriterBySubIndex.get(subIndex);
        if (indexWriter != null) {
            return indexWriter;
        }
        indexWriter = indexManager.getIndexWritersManager().openIndexWriter(searchEngine.getSettings(), subIndex);
        indexWriterBySubIndex.put(subIndex, indexWriter);
        indexManager.getIndexWritersManager().trackOpenIndexWriter(subIndex, indexWriter);
        return indexWriter;
    }

    private void releaseHolders() {
        for (LuceneIndexHolder indexHolder : indexHoldersBySubIndex.values()) {
            indexHolder.release();
        }
        indexHoldersBySubIndex.clear();
    }

    private class PrepareCallable implements Callable {

        private String subIndex;

        private PrepareCallable(String subIndex) {
            this.subIndex = subIndex;
        }

        public Object call() throws Exception {
            if (transIndexManager.hasTransIndex(subIndex)) {
                transIndexManager.commit(subIndex);
            }
            return null;
        }
    }

    private class CommitCallable implements Callable {

        private String subIndex;

        private IndexWriter indexWriter;

        private PrepareCallable prepareCallable;

        public CommitCallable(String subIndex, IndexWriter indexWriter, boolean onePhase) {
            this.subIndex = subIndex;
            this.indexWriter = indexWriter;
            if (onePhase) {
                prepareCallable = new PrepareCallable(subIndex);
            }
        }

        public Object call() throws Exception {
            if (prepareCallable != null) {
                prepareCallable.call();
            }
            // onlt add indexes if there is a transactional index
            try {
                if (transIndexManager.hasTransIndex(subIndex)) {
                    Directory transDir = transIndexManager.getDirectory(subIndex);
                    indexWriter.addIndexesNoOptimize(new Directory[]{transDir});
                }
                indexWriter.close();
            } catch (IOException e) {
                Directory dir = indexManager.getStore().openDirectory(subIndex);
                try {
                    if (IndexWriter.isLocked(dir)) {
                        IndexWriter.unlock(dir);
                    }
                } catch (Exception e1) {
                    logger.warn("Failed to check for locks or unlock failed commit for sub index [" + subIndex + "]", e);
                }
                throw new SearchEngineException("Failed to add transaction index to sub index [" + subIndex + "]", e);
            } finally {
                indexManager.getIndexWritersManager().trackCloseIndexWriter(subIndex, indexWriter);
            }
            if (isInvalidateCacheOnCommit()) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Invalidating cache after commit for sub index [" + subIndex + "]");
                }
                indexManager.getIndexHoldersCache().invalidateCache(subIndex);
            }
            try {
                transIndexManager.close(subIndex);
            } catch (IOException e) {
                logger.warn("Failed to close transactional index for sub index [" + subIndex + "], ignoring", e);
            }
            return null;
        }
    }
}
