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

    CopyFromHolder beforeCopyFrom(String subContext, Directory[] dirs) throws SearchEngineException;

    void afterSuccessfulCopyFrom(String subContext, CopyFromHolder holder) throws SearchEngineException;

    void afterFailedCopyFrom(String subContext, CopyFromHolder holder) throws SearchEngineException;

    void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager);

    /**
     * Closes the given directory.
     */
    void close();
}
