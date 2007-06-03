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

package org.compass.core.lucene.engine.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.RAMTransLog;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TransIndex;
import org.apache.lucene.index.TransLog;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineInternalSearch;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.DefaultLuceneSearchEngineHits;
import org.compass.core.lucene.engine.EmptyLuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.util.ChainedFilter;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.FieldInvoker;
import org.compass.core.util.StringUtils;

/**
 * A better implementation of the read committed transaction support. Uses the
 * {@link org.apache.lucene.index.TransIndex} as the transactional index, which
 * means that it does not uses Lucene public API's, as well as Lucene segments
 * management (must use a
 * {@link org.compass.core.engine.SearchEngineOptimizer}
 * implementation) when using this transaction.
 * <p/>
 * The transactional data is saved (by the TransIndex) in a RAM based engine,
 * and is used when performing find operations with the actual index using
 * Lucene "Multi" readers and searchers support. Deleted resources are save in
 * memory and filtered out from the search results using lucene filtering
 * mechanism. The commit operation adds the transactional data to the original
 * index, and deletes the marked resources for deletions.
 * <p/>
 * By using the TransIndex, the transaction support two phase commits (see the
 * {@link org.apache.lucene.index.TransIndex} documentation).
 * <p/>
 * The transaction allows paralell searching, and single delete/create
 * threads/processess to happen (Using different transaction instances). Note
 * that even when performing dirty operations (delete/create), other
 * thread/processess can perform searchers.
 * <p/>
 * There is no performance penalty when performing pure find operations, the
 * implementation is samrt enough and only creates the transactional supprot
 * when needed (i.e, when using dirty operations - create / delete).
 *
 * @author kimchy
 */
public class ReadCommittedTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(ReadCommittedTransaction.class);

    public static class TransIndexWrapper {
        public String subIndex;

        public TransIndex transIndex;

        public Directory dir;
    }

    public class TransIndexManager {

        private HashMap transIndexMap = new HashMap();

        private ArrayList transIndexList = new ArrayList();

        private LuceneSearchEngineIndexManager indexManager;

        private LuceneSettings luceneSettings;

        public TransIndexManager(LuceneSearchEngineFactory searchEngineFactory) {
            this.indexManager = searchEngineFactory.getLuceneIndexManager();
            this.luceneSettings = searchEngineFactory.getLuceneSettings();
        }

        public TransIndexWrapper getTransIndexBySubIndex(String subIndex) {
            return (TransIndexWrapper) transIndexMap.get(subIndex);
        }

        public TransIndexWrapper openTransIndexBySubIndex(String subIndex) throws SearchEngineException {
            TransIndexWrapper wrapper = (TransIndexWrapper) transIndexMap.get(subIndex);
            if (wrapper == null) {
                wrapper = new TransIndexWrapper();
                wrapper.subIndex = subIndex;
                try {
                    wrapper.dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
                    TransLog transLog;
                    try {
                        Class transLogClass = searchEngine.getSettings().getSettingAsClass(LuceneEnvironment.Transaction.TransLog.TYPE, RAMTransLog.class);
                        if (log.isTraceEnabled()) {
                            log.trace("Using Trans Log [" + transLogClass.getName() + "]");
                        }
                        transLog = (TransLog) transLogClass.newInstance();
                    } catch (Exception e) {
                        throw new SearchEngineException("Failed to create transLog", e);
                    }
                    transLog.configure(searchEngine.getSettings());
                    wrapper.transIndex = new TransIndex(subIndex, wrapper.dir, transLog, luceneSettings);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to open index for sub-index [" + subIndex + "]", e);
                }
                transIndexMap.put(subIndex, wrapper);
                transIndexList.add(wrapper);
            }
            return wrapper;
        }

        public void firstPhase() throws SearchEngineException {
            for (int i = 0; i < transIndexList.size(); i++) {
                TransIndexWrapper wrapper = (TransIndexWrapper) transIndexList.get(i);
                try {
                    wrapper.transIndex.firstPhase();
                } catch (IOException ex) {
                    throw new SearchEngineException("Failed in first phase commit from sub-index [" + wrapper.subIndex
                            + "]", ex);
                }
            }
        }

        public void secondPhase() throws SearchEngineException {
            for (int i = 0; i < transIndexList.size(); i++) {
                TransIndexWrapper wrapper = (TransIndexWrapper) transIndexList.get(i);
                try {
                    wrapper.transIndex.secondPhase();
                } catch (IOException ex) {
                    throw new SearchEngineException("Failed in second phase commit from sub-index [" + wrapper.subIndex
                            + "]", ex);
                }
            }
        }

        public void rollback() throws SearchEngineException {
            IOException e = null;
            for (Iterator it = transIndexList.iterator(); it.hasNext();) {
                TransIndexWrapper wrapper = (TransIndexWrapper) it.next();
                try {
                    wrapper.transIndex.rollback();
                } catch (IOException ex) {
                    e = ex;
                }
            }
            if (e != null) {
                throw new SearchEngineException("Failed to rollback", e);
            }
        }

        public void close() throws SearchEngineException {
            Exception e = null;
            for (int i = 0; i < transIndexList.size(); i++) {
                TransIndexWrapper wrapper = (TransIndexWrapper) transIndexList.get(i);
                try {
                    wrapper.transIndex.close();
                } catch (IOException ex) {
                    e = ex;
                }
                try {
                    indexManager.getStore().closeDirectory(wrapper.subIndex, wrapper.dir);
                } catch (Exception ex) {
                    e = ex;
                }
            }
            if (e != null) {
                if (e instanceof SearchEngineException) {
                    throw(SearchEngineException) e;
                }
                throw new SearchEngineException("Failed to close index writers", e);
            }
        }

        public void clear() {
            transIndexList.clear();
            transIndexMap.clear();
        }
    }

    private static FieldInvoker indexReaderDirectoryOwner;

    private static FieldInvoker indexReaderHasChanges;

    static {
        try {
            indexReaderDirectoryOwner = new FieldInvoker(IndexReader.class, "directoryOwner").prepare();
            indexReaderHasChanges = new FieldInvoker(IndexReader.class, "hasChanges").prepare();
        } catch (Exception e) {
            log.error("Failed to read index reader properties", e);
        }
    }

    private BitSetByAliasFilter filter;

    protected TransIndexManager transIndexManager;

    protected void doBegin() throws SearchEngineException {
        transIndexManager = new TransIndexManager(searchEngine.getSearchEngineFactory());
        filter = new BitSetByAliasFilter();
    }

    protected void doPrepare() throws SearchEngineException {
        for (Iterator it = filter.subIndexDeletesIt(); it.hasNext();) {
            String subIndex = (String) it.next();
            BitSetByAliasFilter.IntArray deletes = filter.getDeletesBySubIndex(subIndex);
            if (deletes != null) {
                IndexReader indexReader;
                try {
                    indexReader = transIndexManager.getTransIndexBySubIndex(subIndex).transIndex.getIndexReader();
                    // a hack so the index reader won't acquire a writer lock,
                    // since we already hold it when we locked the writer
                    indexReaderDirectoryOwner.set(indexReader, Boolean.FALSE);
                    for (int j = 0; j < deletes.length; j++) {
                        indexReader.deleteDocument(deletes.array[j]);
                    }
                } catch (Exception ex) {
                    throw new SearchEngineException("Failed to persist deletes for sub-index [" + subIndex + "]");
                }
            }
        }
        transIndexManager.firstPhase();
    }

    protected void doCommit(boolean onePhase) throws SearchEngineException {
        try {
            if (onePhase) {
                doPrepare();
            }
            transIndexManager.secondPhase();
            if (searchEngine.getSearchEngineFactory().getLuceneSettings().isClearCacheOnCommit()) {
                // clear cache here for all the dirty sub indexes
                for (Iterator it = transIndexManager.transIndexMap.keySet().iterator(); it.hasNext();) {
                    String subIndex = (String) it.next();
                    indexManager.clearCache(subIndex);
                }
            }
        } finally {
            transIndexManager.close();
            transIndexManager.clear();
            filter.clear();
        }
    }

    protected void doRollback() throws SearchEngineException {
        for (Iterator it = filter.subIndexDeletesIt(); it.hasNext();) {
            String subIndex = (String) it.next();
            BitSetByAliasFilter.IntArray deletes = filter.getDeletesBySubIndex(subIndex);
            if (deletes != null) {
                IndexReader indexReader;
                try {
                    indexReader = transIndexManager.getTransIndexBySubIndex(subIndex).transIndex.getIndexReader();
                    indexReaderHasChanges.set(indexReader, Boolean.FALSE);
                } catch (Exception ex) {
                    // swallow it
                    log.warn("Failed to mark index reader with no changes, ignoring", ex);
                }
            }
        }
        filter.clear();
        try {
            transIndexManager.rollback();
        } finally {
            transIndexManager.close();
            transIndexManager.clear();
        }
    }

    public void flush() throws SearchEngineException {
        // nothing here
        // we might want to actually perform a flush if the Lucene store allows it (like jdbc).
    }

    protected void doCreate(InternalResource resource) throws SearchEngineException {
        String subIndex = ResourceHelper.computeSubIndex(resource.resourceKey());
        TransIndexWrapper wrapper = transIndexManager.openTransIndexBySubIndex(subIndex);
        try {
            LuceneUtils.applyBoostIfNeeded(resource, searchEngine);
            Analyzer analyzer = analyzerManager.getAnalyzerByResource(resource);
            wrapper.transIndex.addResource(resource, analyzer);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to create resource for alias [" + resource.getAlias()
                    + "] and resource " + resource, e);
        }
    }

    protected void doDelete(ResourceKey resourceKey) throws SearchEngineException {
        String subIndex = ResourceHelper.computeSubIndex(resourceKey);
        TransIndexWrapper wrapper = transIndexManager.openTransIndexBySubIndex(subIndex);

        // mark for deletion in the actual index
        markDelete(wrapper, subIndex, resourceKey, filter);

        // delete it from the transactional data
        try {
            wrapper.transIndex.deleteTransResource(resourceKey);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]", e);
        }
    }

    public Resource[] find(ResourceKey resourceKey) throws SearchEngineException {
        Searcher indexSearcher;
        Hits hits;
        LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder = null;
        try {
            String subIndex = ResourceHelper.computeSubIndex(resourceKey);
            TransIndexWrapper wrapper = transIndexManager.getTransIndexBySubIndex(subIndex);
            if (wrapper == null) {
                indexHolder = indexManager.openIndexHolderBySubIndex(subIndex);
                indexSearcher = indexHolder.getIndexSearcher();
            } else {
                indexSearcher = wrapper.transIndex.getFullIndexSearcher();
            }
            Filter qFilter = null;
            if (filter.hasDeletes()) {
                qFilter = filter;
            }
            hits = findByIds(indexSearcher, subIndex, resourceKey, qFilter);
            return LuceneUtils.hitsToResourceArray(hits, searchEngine);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to find for alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]", e);
        } finally {
            if (indexHolder != null) {
                indexHolder.release();
            }
        }
    }

    protected SearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        ArrayList indexHolders = new ArrayList();
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(subIndexes, aliases);
            ArrayList searchers = new ArrayList();
            for (int i = 0; i < calcSubIndexes.length; i++) {
                String subIndex = calcSubIndexes[i];
                TransIndexWrapper wrapper = transIndexManager.getTransIndexBySubIndex(subIndex);
                if (wrapper == null) {
                    LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder =
                            indexManager.openIndexHolderBySubIndex(subIndex);
                    indexHolders.add(indexHolder);
                    if (indexHolder.getIndexReader().numDocs() > 0) {
                        searchers.add(indexHolder.getIndexSearcher());
                    }
                } else {
                    Searcher[] transSearchers = wrapper.transIndex.getFullIndexSearcherAsArray();
                    for (int j = 0; j < transSearchers.length; j++) {
                        searchers.add(transSearchers[j]);
                    }
                }
            }
            if (searchers.size() == 0) {
                return new LuceneSearchEngineInternalSearch(null, null);
            }
            MultiSearcher indexSeracher = new MultiSearcher((Searcher[]) searchers.toArray(new Searcher[searchers.size()]));
            return new LuceneSearchEngineInternalSearch(indexSeracher, indexHolders);
        } catch (IOException e) {
            for (Iterator it = indexHolders.iterator(); it.hasNext();) {
                LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder =
                        (LuceneSearchEngineIndexManager.LuceneIndexHolder) it.next();
                indexHolder.release();
            }
            throw new SearchEngineException("Failed to open Lucene reader/searcher", e);
        }
    }

    protected SearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
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
        Hits hits = findByQuery(internalSearch.getSearcher(), query, qFilter);
        return new DefaultLuceneSearchEngineHits(hits, searchEngine, query, internalSearch);
    }

    private Hits findByIds(Searcher indexSearcher, String subIndex, ResourceKey resourceKey, Filter filter)
            throws SearchEngineException {
        Property[] ids = resourceKey.getIds();
        Query query;
        int numberOfAliases = indexManager.getStore().getNumberOfAliasesBySubIndex(subIndex);
        if (numberOfAliases == 1 && ids.length == 1) {
            query = new TermQuery(new Term(ids[0].getName(), ids[0].getStringValue()));
        } else {
            BooleanQuery bQuery = new BooleanQuery();
            if (numberOfAliases > 1) {
                String aliasProperty = searchEngine.getSearchEngineFactory().getLuceneSettings().getAliasProperty();
                Term t = new Term(aliasProperty, resourceKey.getAlias());
                bQuery.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
            for (int i = 0; i < ids.length; i++) {
                Term t = new Term(ids[i].getName(), ids[i].getStringValue());
                bQuery.add(new TermQuery(t), BooleanClause.Occur.MUST);
            }
            query = bQuery;
        }
        try {
            if (filter == null) {
                return indexSearcher.search(query);
            } else {
                return indexSearcher.search(query, filter);
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to search for alias [" + resourceKey.getAlias() + "] and properties ["
                    + StringUtils.arrayToCommaDelimitedString(ids) + "]", e);
        }
    }

    private Hits findByQuery(Searcher indexSearcher, LuceneSearchEngineQuery searchEngineQuery, Filter filter)
            throws SearchEngineException {
        Query query = searchEngineQuery.getQuery();
        Sort sort = searchEngineQuery.getSort();
        Hits hits;
        try {
            if (filter == null) {
                hits = indexSearcher.search(query, sort);
            } else {
                hits = indexSearcher.search(query, filter, sort);
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to search with query [" + query + "]", e);
        }
        return hits;
    }

    private void markDelete(TransIndexWrapper wrapper, String subIndex, ResourceKey resourceKey, BitSetByAliasFilter filter)
            throws SearchEngineException {
        Property[] ids = resourceKey.getIds();
        try {
            boolean moreThanOneAliasPerSubIndex =
                    indexManager.getStore().getNumberOfAliasesBySubIndex(subIndex) > 1;
            if (ids.length == 1 && !moreThanOneAliasPerSubIndex) {
                Property id = ids[0];
                Term t = new Term(id.getName(), id.getStringValue());
                TermDocs termDocs = null;
                try {
                    termDocs = wrapper.transIndex.getIndexReader().termDocs(t);
                    if (termDocs != null) {
                        int maxDoc = wrapper.transIndex.getIndexReader().maxDoc();
                        try {
                            while (termDocs.next()) {
                                filter.markDeleteBySubIndex(subIndex, termDocs.doc(), maxDoc);
                            }
                        } catch (IOException e) {
                            throw new SearchEngineException("Failed to iterate data in order to delete", e);
                        }
                    }
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to search for property [" + id + "]", e);
                } finally {
                    try {
                        if (termDocs != null) {
                            termDocs.close();
                        }
                    } catch (IOException e) {
                        // swallow it
                    }
                }
            } else {
                Hits hits = findByIds(wrapper.transIndex.getIndexSearcher(), subIndex, resourceKey, null);
                if (hits.length() != 0) {
                    int maxDoc = wrapper.transIndex.getIndexSearcher().maxDoc();
                    for (int i = 0; i < hits.length(); i++) {
                        int docNum = hits.id(i);
                        filter.markDeleteBySubIndex(subIndex, docNum, maxDoc);
                    }
                }
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to delete", e);
        }
    }

}
