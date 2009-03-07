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

package org.compass.core.lucene.engine.store;

import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;

/**
 * Manages the mapping between aliases and their repective sub indexes. A Acts
 * as an abstration layer on top of the actual Lucene <code>Directory</code>
 * creation.
 *
 * @author kimchy
 */
public interface LuceneSearchEngineStore {

    /**
     * Closes the store.
     */
    void close();

    /**
     * Performs any scheduled tasks, managed by the index manager.
     */
    void performScheduledTasks();

    /**
     * Returns all the sub indexes defined within the store.
     */
    String[] getSubIndexes();

    /**
     * Returns <code>true</code> if the sub index exists.
     */
    boolean subIndexExists(String subIndex);

    /**
     * Returns the number of aliases that map to the given sub index.
     */
    int getNumberOfAliasesBySubIndex(String subIndex);

    /**
     * Returns the list of aliases that are mapped for a given sub index.
     */
    String[] getAliasesBySubIndex(String subIndex);

    /**
     * Returns the directory that match the given sub index.
     */
    Directory openDirectory(String subIndex) throws SearchEngineException;

    /**
     * Returns the directory that match the given sub index.
     */
    Directory openDirectory(String subContext, String subIndex) throws SearchEngineException;

    /**
     * Returns <code>true</code> if any sub index is locked.
     */
    boolean isLocked() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the sub index is locked (both Lucene write and commit locks).
     */
    boolean isLocked(String subIndex) throws SearchEngineException;

    /**
     * Releases all the locks on all the sub indexes.
     */
    void releaseLocks() throws SearchEngineException;

    /**
     * Releases the lock for the given sub index.
     */
    void releaseLock(String subIndex) throws SearchEngineException;

    /**
     * Deletes the index.
     */
    void deleteIndex() throws SearchEngineException;

    /**
     * Deletes the index for the given sub index.
     */
    void deleteIndex(String subIndex) throws SearchEngineException;

    /**
     * Deletes the index for the given sub context and sub index.
     */
    void deleteIndex(String subContext, String subIndex) throws SearchEngineException;

    /**
     * Cleans the sub index.
     */
    void cleanIndex(String subIndex) throws SearchEngineException;

    /**
     * Creates the index (if it is already exists, delets it first).
     */
    void createIndex() throws SearchEngineException;

    /**
     * Verify that the index exists. If the index exists, nothing happens, if it
     * does not, the index is created.
     */
    boolean verifyIndex() throws SearchEngineException;

    /**
     * Returns <code>true</code> if one of the sub indexes index does exists.
     */
    boolean indexExists() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the sub index index does exists as a legal Lucene index.
     */
    boolean indexExists(String subIndex) throws SearchEngineException;

    /**
     * Returns the sub indexes the intersect with the given sub indexes and aliases
     * provided. The types are translated to aliases and retuned as well.
     */
    String[] calcSubIndexes(String[] subIndexes, String[] aliases, Class[] types);

    /**
     * Returns the sub indexes that intersect with the given sub indexes, aliases
     * and types. Types are translated to the matching aliases. Any extending aliases
     * of the given aliases (or types) are added as well.
     */
    String[] polyCalcSubIndexes(String[] subIndexes, String[] aliases, Class[] types);

    /**
     * Returns the sub indexes that intersect with the given sub indexes and
     * aliases provided. If the sub indexes and aliases are <code>null</code>,
     * return all the sub indexes.
     */
    String[] calcSubIndexes(String[] subIndexes, String[] aliases);

    /**
     * Copies the index from the given store into the current store.
     */
    void copyFrom(String subContext, String subIndex, LuceneSearchEngineStore searchEngineStore) throws SearchEngineException;

    /**
     * Copies the index from the given store into the current store.
     */
    void copyFrom(String subIndex, LuceneSearchEngineStore searchEngineStore) throws SearchEngineException;

    /**
     * Returns the default sub context associated with this store.
     */
    String getDefaultSubContext();

    /**
     * A callback to register event listeners when a {@link SearchEngine} is
     * created.
     *
     * @param searchEngine The search engine created
     * @param eventManager The event manager to register events with
     */
    void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager);

    /**
     * Returns <code>true</code> if a transaction needs to be started when performing operations
     * with this store.
     */
    boolean requiresAsyncTransactionalContext();

    /**
     * Returns <code>true</code> if this store supports concurrent operations.
     */
    boolean supportsConcurrentOperations();

    /**
     * Returns <code>true</code> if this store supprots concurrent commits.
     */
    boolean supportsConcurrentCommits();

    /**
     * Should we use the compound file format or not.
     */
    boolean isUseCompoundFile();

    /**
     * Returns the suggested index deletion policy for the given store. Will be applied if not
     * explicitly configured. Can return <code>null</code> if globabl settings should be applied.
     */
    String suggestedIndexDeletionPolicy();
}
