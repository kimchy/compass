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

package org.compass.core.lucene.engine.transaction.lucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.DefaultLuceneSearchEngineHits;
import org.compass.core.lucene.engine.EmptyLuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.engine.transaction.AbstractTransaction;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.context.TransactionalCallable;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(LuceneTransaction.class);

    private Map<String, IndexWriter> indexWriterBySubIndex = new HashMap<String, IndexWriter>();

    protected void doBegin() throws SearchEngineException {
        // nothing to do here
    }

    protected void doRollback() throws SearchEngineException {
        SearchEngineException exception = null;
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            try {
                entry.getValue().abort();
            } catch (IOException e) {
                Directory dir = indexManager.getStore().getDirectoryBySubIndex(entry.getKey(), false);
                try {
                    if (IndexReader.isLocked(dir)) {
                        IndexReader.unlock(dir);
                    }
                } catch (Exception e1) {
                    log.warn("Failed to check for locks or unlock failed commit for sub index [" + entry.getKey() + "]", e);
                }
                exception = new SearchEngineException("Failed to rollback transaction for sub index [" + entry.getKey() + "]", e);
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    protected void doPrepare() throws SearchEngineException {
        // same as flush
        flush();
    }

    protected void doCommit(boolean onePhase) throws SearchEngineException {
        if (indexWriterBySubIndex.isEmpty()) {
            return;
        }
        ArrayList<Callable<Object>> prepareCallables = new ArrayList<Callable<Object>>();
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            prepareCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new CommitCallable(entry.getKey(), entry.getValue())));
        }
        indexManager.getExecutorManager().invokeAllWithLimitBailOnException(prepareCallables, 1);
    }

    public void flush() throws SearchEngineException {
        if (indexWriterBySubIndex.isEmpty()) {
            return;
        }
        ArrayList<Callable<Object>> prepareCallables = new ArrayList<Callable<Object>>();
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            prepareCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new FlushCallable(entry.getKey(), entry.getValue())));
        }
        indexManager.getExecutorManager().invokeAllWithLimitBailOnException(prepareCallables, 1);
    }

    protected LuceneSearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        LuceneSearchEngineInternalSearch internalSearch =
                (LuceneSearchEngineInternalSearch) internalSearch(query.getSubIndexes(), query.getAliases());
        if (internalSearch.isEmpty()) {
            return new EmptyLuceneSearchEngineHits();
        }
        Filter qFilter = null;
        if (query.getFilter() != null) {
            qFilter = query.getFilter().getFilter();
        }
        Hits hits = findByQuery(internalSearch, query, qFilter);
        return new DefaultLuceneSearchEngineHits(hits, searchEngine, query, internalSearch);
    }

    protected LuceneSearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        ArrayList<LuceneIndexHolder> indexHoldersToClose = new ArrayList<LuceneIndexHolder>();
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(subIndexes, aliases);
            ArrayList<IndexSearcher> searchers = new ArrayList<IndexSearcher>();
            for (String subIndex : calcSubIndexes) {
                LuceneIndexHolder indexHolder = indexManager.openIndexHolderBySubIndex(subIndex);
                indexHoldersToClose.add(indexHolder);
                if (indexHolder.getIndexReader().numDocs() > 0) {
                    searchers.add(indexHolder.getIndexSearcher());
                }
            }
            if (searchers.size() == 0) {
                return new LuceneSearchEngineInternalSearch(null, null);
            }
            MultiSearcher indexSeracher = new MultiSearcher(searchers.toArray(new Searcher[searchers.size()]));
            return new LuceneSearchEngineInternalSearch(indexSeracher, indexHoldersToClose);
        } catch (IOException e) {
            for (LuceneIndexHolder indexHolder : indexHoldersToClose) {
                indexHolder.release();
            }
            throw new SearchEngineException("Failed to open Lucene reader/searcher", e);
        }
    }

    public Resource[] get(ResourceKey resourceKey) throws SearchEngineException {
        LuceneIndexHolder indexHolder = indexManager.openIndexHolderBySubIndex(resourceKey.getSubIndex());
        try {
            Term t = new Term(resourceKey.getUIDPath(), resourceKey.buildUID());
            TermDocs termDocs = null;
            try {
                termDocs = indexHolder.getIndexReader().termDocs(t);
                if (termDocs != null) {
                    return LuceneUtils.hitsToResourceArray(termDocs, indexHolder.getIndexReader(), searchEngine);
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
        } finally {
            indexHolder.release();
        }
    }

    protected void doCreate(InternalResource resource, Analyzer analyzer) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resource.getSubIndex());
            indexWriter.addDocument(((LuceneResource) resource).getDocument(), analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource for alias [" + resource.getAlias()
                    + "] and resource " + resource, e);
        }
    }

    protected void doDelete(ResourceKey resourceKey) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resourceKey.getSubIndex());
            indexWriter.deleteDocuments(new Term(resourceKey.getUIDPath(), resourceKey.buildUID()));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]", e);
        }
    }

    protected void doUpdate(InternalResource resource, Analyzer analyzer) throws SearchEngineException {
        try {
            IndexWriter indexWriter = getOrCreateIndexWriter(resource.getSubIndex());
            indexWriter.updateDocument(new Term(resource.resourceKey().getUIDPath(), resource.resourceKey().buildUID()), ((LuceneResource) resource).getDocument(), analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to update resource for alias [" + resource.getAlias()
                    + "] and resource " + resource, e);
        }
    }

    protected IndexWriter getOrCreateIndexWriter(String subIndex) throws IOException {
        IndexWriter indexWriter = indexWriterBySubIndex.get(subIndex);
        if (indexWriter != null) {
            return indexWriter;
        }
        indexWriter = indexManager.openIndexWriter(searchEngine.getSettings(), subIndex, false);
        indexWriterBySubIndex.put(subIndex, indexWriter);
        return indexWriter;
    }

    private class FlushCallable implements Callable {

        private String subIndex;

        private IndexWriter indexWriter;

        private FlushCallable(String subIndex, IndexWriter indexWriter) {
            this.subIndex = subIndex;
            this.indexWriter = indexWriter;
        }

        public Object call() throws Exception {
            indexWriter.flush();
            return null;
        }
    }

    private class CommitCallable implements Callable {

        private String subIndex;

        private IndexWriter indexWriter;

        private CommitCallable(String subIndex, IndexWriter indexWriter) {
            this.subIndex = subIndex;
            this.indexWriter = indexWriter;
        }

        public Object call() throws Exception {
            try {
                indexWriter.close();
            } catch (IOException e) {
                Directory dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
                try {
                    if (IndexReader.isLocked(dir)) {
                        IndexReader.unlock(dir);
                    }
                } catch (Exception e1) {
                    log.warn("Failed to check for locks or unlock failed commit for sub index [" + subIndex + "]", e);
                }
                throw new SearchEngineException("Failed commit transaction sub index [" + subIndex + "]", e);
            }
            if (indexManager.getSettings().isClearCacheOnCommit()) {
                indexManager.refreshCache(subIndex);
            }
            return null;
        }
    }
}
