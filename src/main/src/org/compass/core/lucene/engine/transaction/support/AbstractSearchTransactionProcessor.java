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

import org.apache.commons.logging.Log;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.DefaultLuceneSearchEngineHits;
import org.compass.core.lucene.engine.EmptyLuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneIndexHolder;
import org.compass.core.lucene.support.ResourceHelper;
import org.compass.core.spi.ResourceKey;

/**
 * A base class that does all the search based operations <b>without</b> visibility for on going
 * changes within the transaction.
 *
 * @author kimchy
 */
public abstract class AbstractSearchTransactionProcessor extends AbstractTransactionProcessor {

    protected AbstractSearchTransactionProcessor(Log logger, LuceneSearchEngine searchEngine) {
        super(logger, searchEngine);
    }

    protected LuceneSearchEngineHits performFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        LuceneSearchEngineInternalSearch internalSearch = internalSearch(query.getSubIndexes(), query.getAliases());
        if (internalSearch.isEmpty()) {
            return new EmptyLuceneSearchEngineHits(searchEngine, internalSearch);
        }
        Filter qFilter = null;
        if (query.getFilter() != null) {
            qFilter = query.getFilter().getFilter();
        }
        Hits hits = findByQuery(internalSearch, query, qFilter);
        return new DefaultLuceneSearchEngineHits(hits, searchEngine, query, internalSearch);
    }

    protected LuceneSearchEngineInternalSearch performInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        // TODO somehow, we need to find a way to pass the useFieldCache parameter
        return buildInternalSearch(subIndexes, aliases, true);
    }

    protected Resource[] performGet(ResourceKey resourceKey) throws SearchEngineException {
        LuceneIndexHolder indexHolder = indexManager.getIndexHoldersCache().getHolder(resourceKey.getSubIndex());
        try {
            Term t = new Term(resourceKey.getUIDPath(), resourceKey.buildUID());
            TermDocs termDocs = null;
            try {
                termDocs = indexHolder.getIndexReader().termDocs(t);
                if (termDocs != null) {
                    return ResourceHelper.hitsToResourceArray(termDocs, indexHolder.getIndexReader(), searchEngine);
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
}
