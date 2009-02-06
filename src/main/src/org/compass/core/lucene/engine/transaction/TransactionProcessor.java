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

package org.compass.core.lucene.engine.transaction;

import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * A Lucene transaction interface. All Lucene based transactions must implement
 * the interface for the LuceneSearchEngine to work with them.
 *
 * @author kimchy
 */
public interface TransactionProcessor {

    /**
     * Retuns the name of this transaction processor.
     */
    String getName();

    /**
     * Begin the transaction.
     */
    void begin() throws SearchEngineException;

    /**
     * Prepare the transaction for commit.
     */
    void prepare() throws SearchEngineException;

    /**
     * Commit the trnasction. If <code>onePhase</code> is set to <code>true</code> then
     * should perform both the prepare phase and the commit phase. If it is set to
     * <code>false</code> then just needs to perform the second phase of the commit process.
     */
    void commit(boolean onePhase) throws SearchEngineException;

    /**
     * Rollback the transaction.
     */
    void rollback() throws SearchEngineException;

    /**
     * Flush changes. Note, the implementation needs to strive for changes not to be visible
     * to other transactions.
     */
    void flush() throws SearchEngineException;

    /**
     * Flush changes and make them visible for other transactions. Note, operations performed up until
     * the flush commit was called might not be able to roll back.
     */
    void flushCommit(String ... aliases) throws SearchEngineException;

    /**
     * Creates a resource.
     */
    void create(final InternalResource resource) throws SearchEngineException;

    /**
     * Updates a resource.
     */
    void update(final InternalResource resource) throws SearchEngineException;

    /**
     * Deletes a resource based on the resource key.
     */
    void delete(final ResourceKey resourceKey) throws SearchEngineException;

    /**
     * Delets everything that match the given query.
     */
    void delete(LuceneSearchEngineQuery query) throws SearchEngineException;

    /**
     * Perform a search for the given query and returns the hits for it.
     */
    LuceneSearchEngineHits find(LuceneSearchEngineQuery query) throws SearchEngineException;

    /**
     * Returns the resources tha match a resource key.
     *
     * <p>Note, should usually only return one resource.
     */
    Resource[] get(ResourceKey resourceKey) throws SearchEngineException;

    /**
     * Performs an internal search operation.
     */
    LuceneSearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException;
}
