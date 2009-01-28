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

import org.compass.core.ResourceFactory;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.transaction.context.TransactionContext;

/**
 * A factory class that creates search engines and search engine optimizers.
 * 
 * @author kimchy
 */
public interface SearchEngineFactory {

    /**
     * Opens/Creates a light weight search engine to perform search engine
     * operations.
     * 
     * @return A new search engine session.
     */
    SearchEngine openSearchEngine(RuntimeCompassSettings runtimeSettings);

    /**
     * Creates a new query builder.
     */
    SearchEngineQueryBuilder queryBuilder() throws SearchEngineException;

    /**
     * Creates a new query filter builder.
     */
    SearchEngineQueryFilterBuilder queryFilterBuilder() throws SearchEngineException;
    
    /**
     * Returns a resource factory allowing to create resources and properties.
     */
    ResourceFactory getResourceFactory();

    /**
     * Returns the index manager.
     *
     * @return the search engine index manager.
     */
    SearchEngineIndexManager getIndexManager();

    /**
     * Returns the property naming strategy used by the search engine to create
     * hidden properties.
     * 
     * @return The property naming strategy used.
     */
    PropertyNamingStrategy getPropertyNamingStrategy();

    /**
     * Returns the serach engine optimizer that was created by the factory.
     * 
     * @return The search engine optimizer
     */
    SearchEngineOptimizer getOptimizer();

    /**
     * Rerturns the specll checker manager (if enabled).
     */
    SearchEngineSpellCheckManager getSpellCheckManager();

    /**
     */
    SearchEngineEventManager getEventManager();

    /**
     * Returns a transactional context that operations that (usually) operate on a different
     * thread or outside of a transactional context should use.
     */
    TransactionContext getTransactionContext();

    /**
     * Returns an executor manager allowing to execute tasks in an async manner as well as
     * schedule tasks.
     */
    ExecutorManager getExecutorManager();

    /**
     * Closes the factory.
     * 
     * @throws SearchEngineException
     */
    void close() throws SearchEngineException;

    /**
     * Returns the name of the alias property.
     *
     * @return The name of the alias property.
     */
    String getAliasProperty();

    /**
     * Returns the name of the extending alias property name.
     */
    String getExtendedAliasProperty();

    /**
     * Returns the name for the all property.
     *
     * @return The name of the all property.
     */
    String getAllProperty();

    /**
     * Returns the compass mappings.
     */
    CompassMapping getMapping();
}
