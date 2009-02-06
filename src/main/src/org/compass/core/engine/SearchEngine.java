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

package org.compass.core.engine;

import org.compass.core.Resource;

/**
 * A search engine absraction above the actual search engine implementation.
 * Works with resources and properties for data, and ResourceMapping,
 * PropertyMapping, and ResourceIdMapping.
 * <p/>
 * All the search engine operations the are needed should be provided by the
 * SearchEngine abstraction (save, delete, load, get, find).
 * <p/>
 * The search engine abstraction also acts as a Property and Resource factory,
 * creating the actual implementations.
 * <p/>
 * The search engine must provide supprot for transactional operations, though
 * in practice, it can be a non transactional search engine. If it is a non
 * transactional search engine, it must be documented as such.
 *
 * @author kimchy
 * @see org.compass.core.Resource
 * @see org.compass.core.Property
 * @see org.compass.core.mapping.ResourceMapping
 * @see org.compass.core.mapping.ResourcePropertyMapping
 * @see org.compass.core.mapping.ResourceIdMappingProvider
 */
public interface SearchEngine {

    SearchEngineFactory getSearchEngineFactory();

    /**
     * Indicates that the search engine will be used for read only operations. Allowing to optimize
     * search and read.
     */
    void setReadOnly();

    /**
     * Returns <code>true</code> if the session is read only.
     *
     * @see #setReadOnly()
     */
    boolean isReadOnly();

    /**
     * Returns <code>true</code> if the search engine was used (up until now)
     * for read only operations.
     */
    boolean onlyReadOperations();

    /**
     * Begins the search engine transaction, using the configured transaction
     * isolation.
     */
    void begin() throws SearchEngineException;

    /**
     * Prepares the transaction for a commit. The first phase of a two phase
     * commit operation.
     */
    void prepare() throws SearchEngineException;

    /**
     * Commits the transaction. If onePhase is set to <code>true</code>,
     * commits the transaction by executing the two phases in the two phase
     * commit operation. If it is set to <code>false</code>, executes the
     * second phase of the two phase commit operation (and must be called after
     * <code>prepare</code>).
     */
    void commit(boolean onePhase) throws SearchEngineException;

    /**
     * Returns <code>true</code> if Compass is within a running transaction.
     */
    boolean isWithinTransaction() throws SearchEngineException;

    /**
     * Rolls back the current transaction. Can be called before any phase of the
     * commit operation was executed, and after the first phase of the two phase
     * commit operation (the <code>prepare</code> operation).
     */
    void rollback() throws SearchEngineException;

    /**
     * Flushes the current transaction. Tries to wait till all operations are applied to the index.
     */
    void flush() throws SearchEngineException;

    /**
     * Flushes the current transaction, and make all the operations perfomed on it until now "viewable"
     * to other transactions. The operations performed before the flush commit operation will not rollback
     * on rollback operation.
     */
    void flushCommit(String ... aliases) throws SearchEngineException;

    /**
     * Was this transaction rolled back
     */
    boolean wasRolledBack() throws SearchEngineException;

    /**
     * Check if this transaction was successfully committed. This method could
     * return <code>false</code> even after successful invocation of
     * <code>commit()</code>.
     */
    boolean wasCommitted() throws SearchEngineException;

    /**
     * Closes and disposes of the search engine.
     */
    void close() throws SearchEngineException;

    /**
     * Creates a new query builder.
     */
    SearchEngineQueryBuilder queryBuilder() throws SearchEngineException;

    /**
     * Creates a new query filter builder.
     */
    SearchEngineQueryFilterBuilder queryFilterBuilder() throws SearchEngineException;

    /**
     * Returns an analyzer helper for the search engine.
     */
    SearchEngineAnalyzerHelper analyzerHelper();

    /**
     * Creates the resource in the index file under the given alias.
     */
    void create(Resource resource) throws SearchEngineException;

    /**
     * Saves the given resource under the given resource.
     */
    void save(Resource resource) throws SearchEngineException;

    /**
     * Deletes the resource, which has the property ids. The ResourceMapping is fetched
     * according to the alias.
     */
    void delete(Resource resource) throws SearchEngineException;

    /**
     * Deletes all the resources that match the provided query.
     */
    void delete(SearchEngineQuery query) throws SearchEngineException;

    /**
     * Loads a resource for the given resource that holds the properties AND the
     * alias, under the specified alias. Throws an exception if the resource if
     * not found.
     */
    Resource load(Resource idResource) throws SearchEngineException;

    /**
     * Returns a resource for the given resource that holds the properties AND
     * the alias, under the specified alias. Retrurns null if the resource is
     * not found.
     */
    Resource get(Resource idResource) throws SearchEngineException;

    /**
     * Returns term frequencies.
     */
    SearchEngineTermFrequencies termFreq(String[] propertyNames, int size, SearchEngineInternalSearch internalSearch);

    /**
     * Returns a search engine internal implementation.
     */
    SearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException;
}
