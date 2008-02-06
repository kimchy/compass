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

package org.compass.core.lucene.engine.transaction.readcommitted;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.Directory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.DefaultLuceneSearchEngineHits;
import org.compass.core.lucene.engine.EmptyLuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.engine.transaction.AbstractTransaction;
import org.compass.core.lucene.util.ChainedFilter;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.StringUtils;

/**
 * Read Committed transaction support. Allows to perform operations within a transaction and all
 * operations will be "viewable" to the ongoing transaction, even search.
 *
 * @author kimchy
 */
public class ReadCommittedTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(ReadCommittedTransaction.class);

    private TransIndexManager transIndexManager;

    private Map<String, IndexWriter> indexWriterBySubIndex = new HashMap<String, IndexWriter>();

    private BitSetByAliasFilter filter;

    private Map<String, LuceneIndexHolder> indexHoldersBySubIndex = new HashMap<String, LuceneIndexHolder>();

    protected void doBegin() throws SearchEngineException {
        this.transIndexManager = new TransIndexManager(searchEngine.getSearchEngineFactory());
        this.transIndexManager.configure(searchEngine.getSettings());
        this.filter = new BitSetByAliasFilter();
    }

    protected void doRollback() throws SearchEngineException {
        releaseHolders();
        SearchEngineException lastException = null;
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
                lastException = new SearchEngineException("Failed to rollbacl sub index [" + entry.getKey() + "]", e);
            }
        }
        if (lastException != null) {
            throw lastException;
        }
    }

    protected void doPrepare() throws SearchEngineException {
        releaseHolders();
        try {
            transIndexManager.commit();
        } catch (IOException e) {
            throw new SearchEngineException("Failed to prepare transactional index", e);
        }
    }

    protected void doCommit(boolean onePhase) throws SearchEngineException {
        if (onePhase) {
            doPrepare();
        }
        for (Map.Entry<String, IndexWriter> entry : indexWriterBySubIndex.entrySet()) {
            String subIndex = entry.getKey();
            // onlt add indexes if there is a transactional index
            try {
                if (transIndexManager.hasTransIndex(subIndex)) {
                    Directory transDir = transIndexManager.getDirectory(subIndex);
                    entry.getValue().addIndexesNoOptimize(new Directory[]{transDir});
                }
                entry.getValue().close();
            } catch (IOException e) {
                Directory dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
                try {
                    if (IndexReader.isLocked(dir)) {
                        IndexReader.unlock(dir);
                    }
                } catch (Exception e1) {
                    log.warn("Failed to check for locks or unlock failed commit for sub index [" + subIndex + "]", e);
                }
                throw new SearchEngineException("Failed add transaction index to sub index [" + subIndex + "]", e);
            }
            if (indexManager.getSettings().isClearCacheOnCommit()) {
                indexManager.refreshCache(subIndex);
            }
            try {
                transIndexManager.close(subIndex);
            } catch (IOException e) {
                log.warn("Failed to close transactional index for sub index [" + subIndex + "], ignoring", e);
            }
        }
    }

    protected LuceneSearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        ArrayList<LuceneIndexHolder> indexHoldersToClose = new ArrayList<LuceneIndexHolder>();
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(subIndexes, aliases);
            ArrayList<IndexSearcher> searchers = new ArrayList<IndexSearcher>();
            for (String subIndex : calcSubIndexes) {
                LuceneIndexHolder indexHolder = indexHoldersBySubIndex.get(subIndex);
                if (indexHolder == null) {
                    indexHolder = indexManager.openIndexHolderBySubIndex(subIndex);
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

    protected LuceneSearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        LuceneSearchEngineInternalSearch internalSearch =
                (LuceneSearchEngineInternalSearch) internalSearch(query.getSubIndexes(), query.getAliases());
        if (internalSearch.isEmpty()) {
            return new EmptyLuceneSearchEngineHits();
        }
        Filter qFilter = null;
        if (filter.hasDeletes()) {
            if (query.getFilter() == null) {
                qFilter = filter;
            } else {
                qFilter = new ChainedFilter(new Filter[]{filter, query.getFilter().getFilter()}, ChainedFilter.ChainedFilterType.AND);
            }
        } else {
            if (query.getFilter() != null) {
                qFilter = query.getFilter().getFilter();
            }
        }
        Hits hits = findByQuery(internalSearch, query, qFilter);
        return new DefaultLuceneSearchEngineHits(hits, searchEngine, query, internalSearch);
    }

    public Resource[] get(ResourceKey resourceKey) throws SearchEngineException {
        Searcher indexSearcher = null;
        IndexReader indexReader = null;
        LuceneIndexHolder indexHolder = null;
        boolean releaseHolder = false;
        boolean closeReaderAndSearcher = false;
        try {
            String subIndex = resourceKey.getSubIndex();
            indexHolder = indexHoldersBySubIndex.get(subIndex);
            if (indexHolder == null) {
                indexHolder = indexManager.openIndexHolderBySubIndex(subIndex);
                releaseHolder = true;
            } else {
                releaseHolder = false;
            }
            if (transIndexManager.hasTransIndex(subIndex)) {
                closeReaderAndSearcher = true;
                indexReader = new MultiReader(new IndexReader[]{indexHolder.getIndexReader(), transIndexManager.getReader(subIndex)}, false);
                // note, we need to create a multi searcher here instead of a searcher ontop of the MultiReader
                // since our filter relies on specific reader per searcher
                indexSearcher = new MultiSearcher(new Searcher[]{new IndexSearcher(indexHolder.getIndexReader()), transIndexManager.getSearcher(subIndex)});
            } else {
                indexReader = indexHolder.getIndexReader();
                indexSearcher = indexHolder.getIndexSearcher();
            }
            if (filter.hasDeletes()) {
                // TODO we can do better with HitCollector
                Query query = LuceneUtils.buildResourceLoadQuery(resourceKey);
                Hits hits = indexSearcher.search(query, filter);
                return LuceneUtils.hitsToResourceArray(hits, searchEngine);
            } else {
                Term t = new Term(resourceKey.getUIDPath(), resourceKey.buildUID());
                TermDocs termDocs = null;
                try {
                    termDocs = indexReader.termDocs(t);
                    if (termDocs != null) {
                        return LuceneUtils.hitsToResourceArray(termDocs, indexReader, searchEngine);
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

    protected void doCreate(InternalResource resource, Analyzer analyzer) throws SearchEngineException {
        try {
            openIndexWriterIfNeeded(resource.getSubIndex());
            transIndexManager.create(resource, analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource for alias [" + resource.getAlias()
                    + "] and resource " + resource, e);
        }

    }

    protected void doDelete(ResourceKey resourceKey) throws SearchEngineException {
        try {
            openIndexWriterIfNeeded(resourceKey.getSubIndex());

            LuceneIndexHolder indexHolder = indexHoldersBySubIndex.get(resourceKey.getSubIndex());
            if (indexHolder == null) {
                indexManager.refreshCache(resourceKey.getSubIndex());
                indexHolder = indexManager.openIndexHolderBySubIndex(resourceKey.getSubIndex());
                indexHoldersBySubIndex.put(resourceKey.getSubIndex(), indexHolder);
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

            // delete from the original index (autoCommit is false, so won't be committed
            indexWriterBySubIndex.get(resourceKey.getSubIndex()).deleteDocuments(deleteTerm);

            // and delete it (if there) from the transactional index
            transIndexManager.delete(resourceKey);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]", e);
        }
    }

    public void flush() throws SearchEngineException {
        // TODO maybe flush here the trans index manager?
    }

    protected void openIndexWriterIfNeeded(String subIndex) throws IOException {
        if (indexWriterBySubIndex.containsKey(subIndex)) {
            return;
        }
        IndexWriter indexWriter = indexManager.openIndexWriter(subIndex, false);
        indexWriterBySubIndex.put(subIndex, indexWriter);
    }

    private void releaseHolders() {
        for (LuceneIndexHolder indexHolder : indexHoldersBySubIndex.values()) {
            indexHolder.release();
        }
        indexHoldersBySubIndex.clear();
    }
}
