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

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.mapping.CompassMapping;

/**
 * Manages the mapping between aliases and their repective sub indexes. A Acts
 * as an abstration layer on top of the actual Lucene <code>Directory</code>
 * creation.
 * 
 * @author kimchy
 */
public interface LuceneSearchEngineStore {

	public static interface LuceneStoreCallback {
		Object doWithStore(Directory dir) throws IOException;
	}

	/**
	 * Configures the store.
	 */
	void configure(LuceneSearchEngineFactory searchEngineFactory, CompassSettings settings, CompassMapping mapping);

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
	 * Returns the number of aliases that map to the given sub index.
	 */
	int getNumberOfAliasesBySubIndex(String subIndex);

	/**
	 * Closes the given directory.
	 */
	void closeDirectory(String subIndex, Directory dir) throws SearchEngineException;

	/**
	 * Returns the directory that match the given sub index.
	 */
	Directory getDirectoryBySubIndex(String subIndex, boolean create) throws SearchEngineException;

    /**
     * Returns <code>true</code> if the sub index is locked (both Lucene write and commit locks).
     */
    boolean isLocked(String subIndex) throws SearchEngineException;

    /**
	 * Deletes the index.
	 */
	void deleteIndex() throws SearchEngineException;

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
	 * Returns <code>true</code> if one of the sub indexes index does not
	 * exists.
	 */
	boolean indexExists() throws SearchEngineException;

	/**
	 * Returns the sub indexes that intersect with the given sub indexes and
	 * aliases provided. If the sub indexes and aliases are <code>null</code>,
	 * return all the sub indexes.
	 */
	String[] calcSubIndexes(String[] subIndexes, String[] aliases);

	/**
	 * Copies the index from the given store into the current store.
	 * 
	 * @param searchEngineStore
	 *            The store to copy from
	 * @throws SearchEngineException
	 */
	void copyFrom(LuceneSearchEngineStore searchEngineStore) throws SearchEngineException;

	/**
	 * A callback to register event listeners when a {@link SearchEngine} is
	 * created.
	 * 
	 * @param searchEngine
	 *            The search engine created
	 * @param eventManager
	 *            The event manager to register events with
	 */
	void registerEventListeners(SearchEngine searchEngine, SearchEngineEventManager eventManager);

	/**
	 * Returns the lucene settings.
	 */
	LuceneSettings getLuceneSettings();

}
