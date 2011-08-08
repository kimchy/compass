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

/**
 * @author kimchy
 */
public interface SearchEngineIndexManager {

    public static interface IndexOperationPlan {

        String[] getSubIndexes();

        String[] getAliases();

        Class[] getTypes();
    }

    /**
     * A callback interface that works with.
     *
     * @author kimchy
     */
    public static interface IndexOperationCallback {

        /**
         * First step is called just after the index is locked for any dirty operations.
         * <p>
         * Return <code>true</code> if after the first step, the system should continue
         * to the second step.
         */
        boolean firstStep() throws SearchEngineException;

        /**
         * Second step is called just after the index is locked for read operations
         * (on top of the dirty operations).
         */
        void secondStep() throws SearchEngineException;
    }

    /**
     * A callback to replace the current index.
     *
     * @author kimchy
     */
    public static interface ReplaceIndexCallback {

        /**
         * Provides the ability to be notified when the index can be built
         * during the replace operation. There is no need to actually build the
         * index, if one already exists.
         */
        void buildIndexIfNeeded() throws SearchEngineException;
    }

    /**
     * Starts the index manager
     */
    void start();

    /**
     * Stops / closes the index manager
     */
    void stop();

    /**
     * Returns <code>true</code> if the index manage is running
     */
    boolean isRunning();

    /**
     * Closes the index manager. Used by compass, probably not a good idea to call it.
     */
    void close();

    /**
     * Creates an index data. If exists, deletes it and creates a new one.
     *
     * @throws SearchEngineException
     */
    void createIndex() throws SearchEngineException;

    /**
     * Verify the index data. If exists, does nothing. If it doesn't exists,
     * creates it. Returns <code>true</code> if the index was created. If the index
     * exists, and it's {@link org.compass.core.lucene.LuceneEnvironment.SearchEngineIndex#USE_COMPOUND_FILE}
     * changed it's settings, will compound / un-compound the index accordingly.
     *
     * @throws SearchEngineException
     */
    boolean verifyIndex() throws SearchEngineException;

    /**
     * Deletes the index.
     *
     * @throws SearchEngineException
     */
    void deleteIndex() throws SearchEngineException;

    /**
     * Cleans the index from data (by deleting and creating an empty one).
     */
    void cleanIndex() throws SearchEngineException;

    /**
     * Cleans the index from data (by deleting and creating an empty one).
     */
    void cleanIndex(String subIndex) throws SearchEngineException;

    /**
     * Returns <code>true</code> if the index exists.
     */
    boolean indexExists() throws SearchEngineException;

    /**
     * A general api for index operations. Provides the ability to perform safe operations
     * using the {@link IndexOperationCallback}.
     */
    void operate(IndexOperationCallback callback) throws SearchEngineException;

    /**
     * Replaces the index data that is used by the current instance, with the one that is pointed by
     * the given <code>indexManager</code>. A callback interface can be registered if the index is
     * dynamically created.
     * <p>
     * The replace process is safe, in terms that it will aquire dirty locks and read locks,
     * so the index can be safely replaced while it is being used.
     */
    void replaceIndex(SearchEngineIndexManager indexManager, ReplaceIndexCallback callback) throws SearchEngineException;

    /**
     * Returns <code>true</code> if the sub index is cached.
     */
    boolean isCached(String subIndex) throws SearchEngineException;

    /**
     * Returns <code>true</code> if one of the sub indexes is cached.
     */
    boolean isCached() throws SearchEngineException;

    /**
     * Clears any internal caching done by the index for the specified sub-index. Closes any
     * cached resources.
     */
    void clearCache(String subIndex) throws SearchEngineException;

    /**
     * Clears any internal caching done by the index. Closes any cached resources.
     */
    void clearCache() throws SearchEngineException;

    /**
     * Invalidates any internal caching done by the index for the specified sub-index. More lightweight
     * than {@link #clearCache(String)}.
     */
    void invalidateCache(String subIndex) throws SearchEngineException;

    /**
     * Invalidates any internal caching done by the index. More lightweight than {@link #clearCache()}.
     */
    void invalidateCache() throws SearchEngineException;

    /**
     * Refresh any internal caching done by the index for the specified sub-index.
     */
    void refreshCache(String subIndex) throws SearchEngineException;

    /**
     * Refresh any internal caching done by the index.
     *
     * @throws SearchEngineException
     */
    void refreshCache() throws SearchEngineException;

    /**
     * Notifies all the compass instances that are working with the same index to
     * clear cache.
     *
     * @throws SearchEngineException
     */
    void notifyAllToClearCache() throws SearchEngineException;

    /**
     * Manual check if the notified to clear the cache globally. If it does, will clear the cache.
     *
     * @throws SearchEngineException
     */
    void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException;

    /**
     * Performs scheduled tasks that are usually derived based on the actual index storage used.
     *
     * <p>This API will be called when disabling the automatic scheduler that comes built in with
     * Compass.
     */
    void performScheduledTasks() throws SearchEngineException;

    /**
     * Returns the sub indexes that Compass handles.
     */
    String[] getSubIndexes();

    /**
     * Returns <code>true</code> if the sub index exists.
     */
    boolean subIndexExists(String subIndex);

    /**
     * Releases all the locks held over all the possbile sub indexes.
     */
    void releaseLocks() throws SearchEngineException;

    /**
     * Releases a lock for the given sub index.
     */
    void releaseLock(String subIndex) throws SearchEngineException;

    /**
     * Returns <code>true</code> if one of the sub indexes is locked.
     */
    boolean isLocked() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the given sub index is locked.
     */
    boolean isLocked(String subIndex) throws SearchEngineException;

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
     * Returns <code>true</code> if a transaction needs to be started when performing operations
     * with this store.
     */
    boolean requiresAsyncTransactionalContext();

    /**
     * Returns <code>true</code> if the index store supports concurrent operations.
     */
    boolean supportsConcurrentOperations();

    /**
     * Returns <code>true</code> if the index store supports concurrent commits.
     */
    boolean supportsConcurrentCommits();
}
