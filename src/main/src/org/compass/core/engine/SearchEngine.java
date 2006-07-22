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

package org.compass.core.engine;

import java.io.Reader;

import org.compass.core.CompassTermInfoVector;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.ResourcePropertyMapping;

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

    /**
     * Returns a null value that represents no entry in the search engine.
     * Usefull when the system needs to store an actual data entry, but for it
     * to represent a business null value.
     */
    String getNullValue();

    /**
     * Returns true if the value is marked as a null value.
     */
    boolean isNullValue(String value);

    /**
     * Creates a resource, that is used with the actual Search Engine
     * implementation.
     */
    Resource createResource(String alias) throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String value, ResourcePropertyMapping mapping)
            throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String name, String value, ResourcePropertyMapping mapping)
            throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String name, String value, Property.Store store, Property.Index index)
            throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine. The
     * available values for the store and index parameters are provided in the
     * Property interface (Property.Store, Property.Index, Property.TermVector).
     */
    Property createProperty(String name, String value, Property.Store store, Property.Index index,
                            Property.TermVector termVector) throws SearchEngineException;

    /**
     * Creates a property (TEXT type) for the specified reader.
     */
    Property createProperty(String name, Reader value) throws SearchEngineException;

    /**
     * Creates a property (indexed, and not stored) for the specified reader.
     */
    Property createProperty(String name, Reader value, Property.TermVector termVector) throws SearchEngineException;

    /**
     * Creates a binary property.
     */
    Property createProperty(String name, byte[] value, Property.Store store) throws SearchEngineException;

    /**
     * Begins the search engine transaction, using the configured transaction
     * isolation.
     */
    void begin() throws SearchEngineException;

    /**
     * Begins the search engine transaction using the given transaction
     * isolation.
     */
    void begin(TransactionIsolation transactionIsolation) throws SearchEngineException;

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
     * Rolls back the current transaction. Can be called before any phase of the
     * commit operation was executed, and after the first phase of the two phase
     * commit operation (the <code>prepare</code> operation).
     */
    void rollback() throws SearchEngineException;

    /**
     * Flushed the current transaction. Currently only works with batch insert transaction.
     */
    void flush() throws SearchEngineException;

    /**
     * Was this transaction rolled back
     */
    public boolean wasRolledBack() throws SearchEngineException;

    /**
     * Check if this transaction was successfully committed. This method could
     * return <code>false</code> even after successful invocation of
     * <code>commit()</code>.
     */
    public boolean wasCommitted() throws SearchEngineException;

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
     * Deletes the resource that it's ids match the given array of ids under the
     * given alias.
     */
    void delete(String[] ids, String alias) throws SearchEngineException;

    /**
     * Deletes the resource that it's ids match the given array of properties
     * under the given alias.
     */
    void delete(Property[] ids, String alias) throws SearchEngineException;

    /**
     * Deletes the resource, which has the property ids. The ResourceMapping is fetched
     * according to the alias.
     */
    void delete(Resource resource) throws SearchEngineException;

    /**
     * Loads a resource for the given array of string ids, under the specified
     * alias. Throws an exception if the resource is not found.
     */
    Resource load(String[] ids, String alias) throws SearchEngineException;

    /**
     * Loads a resource for the given resource that holds the properties AND the
     * alias, under the specified alias. Throws an exception if the resource if
     * not found.
     */
    Resource load(Resource idResource) throws SearchEngineException;

    /**
     * Loads a resource for the given array of properties, under the specified
     * alias. Throws an exception if the resource if not found.
     */
    Resource load(Property[] ids, String alias) throws SearchEngineException;

    /**
     * Returns a resource for the given resource that holds the properties AND
     * the alias, under the specified alias. Retrurns null if the resource is
     * not found.
     */
    Resource get(Resource idResource) throws SearchEngineException;

    /**
     * Returns a resource for the given array of string ids, under the specified
     * alias. Retrurns null if the resource is not found.
     */
    Resource get(String[] ids, String alias) throws SearchEngineException;

    /**
     * Returns a resource for the given array of properties, under the specified
     * alias. Return null if it is not found.
     */
    Resource get(Property[] ids, String alias) throws SearchEngineException;

    SearchEngineHighlighter highlighter(SearchEngineQuery query) throws SearchEngineException;

    CompassTermInfoVector[] getTermInfos(Resource resource) throws SearchEngineException;

    CompassTermInfoVector getTermInfo(Resource resource, String propertyName) throws SearchEngineException;
}
