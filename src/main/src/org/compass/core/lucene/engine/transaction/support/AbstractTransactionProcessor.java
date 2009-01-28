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

package org.compass.core.lucene.engine.transaction.support;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.search.CacheableMultiReader;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;

/**
 * A base class for all Lucene based transactions. Provides helper methods for
 * Lucene index transaction management, and default state management for the
 * transcational operations.
 *
 * @author kimchy
 */
public abstract class AbstractTransactionProcessor implements TransactionProcessor {

    protected final Log logger;

    protected final LuceneSearchEngine searchEngine;

    protected final LuceneSearchEngineFactory searchEngineFactory;

    protected final LuceneSearchEngineIndexManager indexManager;

    protected final CompassMapping mapping;

    protected AbstractTransactionProcessor(Log logger, LuceneSearchEngine searchEngine) {
        this.logger = logger;
        this.searchEngine = searchEngine;
        this.searchEngineFactory = searchEngine.getSearchEngineFactory();
        this.indexManager = searchEngineFactory.getLuceneIndexManager();
        this.mapping = searchEngine.getSearchEngineFactory().getMapping();
    }

    protected ResourceMapping getResourceMapping(String alias) {
        return mapping.getRootMappingByAlias(alias);
    }

    protected LuceneSearchEngineInternalSearch buildInternalSearch(String[] subIndexes, String[] aliases, boolean useFieldCache) throws SearchEngineException {
        ArrayList<LuceneIndexHolder> indexHoldersToClose = new ArrayList<LuceneIndexHolder>();
        try {
            String[] calcSubIndexes = indexManager.getStore().calcSubIndexes(subIndexes, aliases);
            // currenly we disable search by multireader and do it with multisearcher so field cache
            // will work correclty (as we won't create a new "outer" reader each time, which will cause
            // the field cache to invalidate each time
            if (!useFieldCache) {
                ArrayList<IndexReader> readers = new ArrayList<IndexReader>(calcSubIndexes.length);
                LuceneIndexHolder lastNonEmptyIndexHolder = null;
                for (String subIndex : calcSubIndexes) {
                    LuceneIndexHolder indexHolder = indexManager.getIndexHoldersCache().getHolder(subIndex);
                    indexHoldersToClose.add(indexHolder);
                    if (indexHolder.getIndexReader().numDocs() > 0) {
                        readers.add(indexHolder.getIndexReader());
                        lastNonEmptyIndexHolder = indexHolder;
                    }
                }
                if (readers.size() == 0) {
                    return new LuceneSearchEngineInternalSearch(searchEngine, indexHoldersToClose);
                }
                // if we have just one reader, no need to create a multi reader on top of it
                if (readers.size() == 1) {
                    return new LuceneSearchEngineInternalSearch(searchEngine, lastNonEmptyIndexHolder, indexHoldersToClose);
                }
                MultiReader reader = new CacheableMultiReader(readers.toArray(new IndexReader[readers.size()]), false);
                IndexSearcher searcher = indexManager.openIndexSearcher(reader);
                return new LuceneSearchEngineInternalSearch(searchEngine, reader, searcher, indexHoldersToClose);
            } else {
                ArrayList<IndexSearcher> searchers = new ArrayList<IndexSearcher>(calcSubIndexes.length);
                LuceneIndexHolder lastNonEmptyIndexHolder = null;
                for (String subIndex : calcSubIndexes) {
                    LuceneIndexHolder indexHolder = indexManager.getIndexHoldersCache().getHolder(subIndex);
                    indexHoldersToClose.add(indexHolder);
                    if (indexHolder.getIndexReader().numDocs() > 0) {
                        searchers.add(indexHolder.getIndexSearcher());
                        lastNonEmptyIndexHolder = indexHolder;
                    }
                }
                if (searchers.size() == 0) {
                    return new LuceneSearchEngineInternalSearch(searchEngine, indexHoldersToClose);
                }
                // if we have just one reader, no need to create a multi reader on top of it
                if (searchers.size() == 1) {
                    return new LuceneSearchEngineInternalSearch(searchEngine, lastNonEmptyIndexHolder, indexHoldersToClose);
                }
                MultiSearcher searcher = indexManager.openMultiSearcher(searchers.toArray(new IndexSearcher[searchers.size()]));
                return new LuceneSearchEngineInternalSearch(searchEngine, searcher, indexHoldersToClose);
            }
        } catch (Exception e) {
            for (LuceneIndexHolder indexHolder : indexHoldersToClose) {
                indexHolder.release();
            }
            throw new SearchEngineException("Failed to open Lucene reader/searcher", e);
        }
    }

    protected Hits findByQuery(LuceneSearchEngineInternalSearch internalSearch,
                               LuceneSearchEngineQuery searchEngineQuery, Filter filter) throws SearchEngineException {
        Query query = searchEngineQuery.getQuery();
        if (searchEngineQuery.isRewrite()) {
            try {
                query = query.rewrite(internalSearch.getReader());
            } catch (IOException e) {
                throw new SearchEngineException("Failed to rewrite query [" + query.toString() + "]", e);
            }
        }
        Sort sort = searchEngineQuery.getSort();
        Hits hits;
        try {
            if (filter == null) {
                hits = internalSearch.getSearcher().search(query, sort);
            } else {
                hits = internalSearch.getSearcher().search(query, filter, sort);
            }
        } catch (IOException e) {
            throw new SearchEngineException("Failed to search with query [" + query + "]", e);
        }
        return hits;
    }

    protected boolean isInvalidateCacheOnCommit() {
        return searchEngine.getSettings().getSettingAsBoolean(LuceneEnvironment.Transaction.CLEAR_CACHE_ON_COMMIT, true);
    }

    /**
     * Returns the concatanation of {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor#PREFIX} +
     * {@link #getName()}  + "." + <code>settingName</code>.
     */
    protected final String getSettingName(String settingName) {
        return LuceneEnvironment.Transaction.Processor.PREFIX + getName() + "." + settingName;
    }
}
