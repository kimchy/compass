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
 * An abstraction on top of Lucene {@link org.apache.lucene.store.Directory} handling.
 *
 * @author kimchy
 */
public interface DirectoryStore {

    /**
     * Opens a new {@link org.apache.lucene.store.Directory} for the given sub context and sub index.
     */
    Directory open(String subContext, String subIndex) throws SearchEngineException;

    /**
     * Lists all the sub indexes for the given sub context. Retruns <code>null</code> if the sub context
     * directory does not even exists.
     *
     * <p>Throws an {@link UnsupportedOperationException} when the directory store does not support listing
     * sub indexes. 
     */
    String[] listSubIndexes(String subContext) throws SearchEngineException, UnsupportedOperationException;

    /**
     * Returns <code>true</code> if the inex exists, <code>false</code> if it does not. Can return
     * <code>null</code> which then will cause the default checking to apply.
     */
    Boolean indexExists(Directory dir) throws SearchEngineException;

    /**
     * If applicable, deletes the given directory.
     */
    void deleteIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException;

    /**
     * If applicable, cleans the given directory. Notes, this will be called right before the directory
     * will be closed. And then a create index will be done.
     */
    void cleanIndex(Directory dir, String subContext, String subIndex) throws SearchEngineException;

    /**
     * Closes the given directory.
     */
    void closeDirectory(Directory dir, String subContext, String subIndex) throws SearchEngineException;

    /**
     * Perform any scheduled tasks that are needed on the given directory.
     */
    void performScheduledTasks(Directory dir, String subContext, String subIndex) throws SearchEngineException;

    CopyFromHolder beforeCopyFrom(String subContext, String subIndex, Directory dir) throws SearchEngineException;

    void afterSuccessfulCopyFrom(String subContext, String subIndex, CopyFromHolder holder) throws SearchEngineException;

    void afterFailedCopyFrom(String subContext, String subIndex, CopyFromHolder holder) throws SearchEngineException;

    void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager);

    /**
     * Closes the given directory.
     */
    void close();

    /**
     * Returns <code>true</code> if a transaction needs to be started when performing operations
     * with this store.
     */
    boolean requiresAsyncTransactionalContext();

    /**
     * Returns the suggested compound file format usage.
     */
    boolean suggestedUseCompoundFile();

    /**
     * Returns <code>true</code> if this store supports concurrent operations.
     */
    boolean supportsConcurrentOperations();

    /**
     * Retruns <code>true</code> if this store supports concurrent commits. If set to <code>true</code>,
     * commits will be perfomed on different threads concurrently.
     */
    boolean supportsConcurrentCommits();

    /**
     * Returns the suggested index deletion policy for the given store. Will be applied if not
     * explicitly configured. Can return <code>null</code> if globabl settings should be applied.
     */
    String suggestedIndexDeletionPolicy();
}
