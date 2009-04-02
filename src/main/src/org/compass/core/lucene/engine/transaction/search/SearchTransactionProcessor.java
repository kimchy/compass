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

package org.compass.core.lucene.engine.transaction.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.transaction.support.AbstractSearchTransactionProcessor;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * A lightweight search only transaction processor.
 *
 * @author kimchy
 */
public class SearchTransactionProcessor extends AbstractSearchTransactionProcessor {

    private static final Log logger = LogFactory.getLog(SearchTransactionProcessor.class);

    public SearchTransactionProcessor(LuceneSearchEngine searchEngine) {
        super(logger, searchEngine);
    }

    public String getName() {
        return LuceneEnvironment.Transaction.Processor.Search.NAME;
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

    public void begin() throws SearchEngineException {
        // nothing to do
    }

    public void prepare() throws SearchEngineException {
        // nothing to do
    }

    public void commit(boolean onePhase) throws SearchEngineException {
        // nothing to do
    }

    public void rollback() throws SearchEngineException {
        // nothing to do
    }

    public void flush() throws SearchEngineException {
        // nothing to do
    }

    public void flushCommit(String ... aliases) throws SearchEngineException {
        // nothing to do here
    }

    public void create(InternalResource resource) throws SearchEngineException {
        throw new SearchEngineException("create can not be perfoemd on a search only session");
    }

    public void update(InternalResource resource) throws SearchEngineException {
        throw new SearchEngineException("update can not be perfoemd on a search only session");
    }

    public void delete(ResourceKey resourceKey) throws SearchEngineException {
        throw new SearchEngineException("delete (resource) can not be perfoemd on a search only session");
    }

    public void delete(LuceneSearchEngineQuery query) throws SearchEngineException {
        throw new SearchEngineException("delete (query) can not be perfoemd on a search only session");
    }
}