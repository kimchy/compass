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

package org.compass.core.engine.spellcheck;

import org.compass.core.CompassQuery;
import org.compass.core.engine.SearchEngineException;

/**
 * The spell check manager allows to perform spell check index level operations. It also
 * allows to perform operations using the spell check, for example to suggest words for
 * a given word.
 *
 * <p>The spell check manager can be configured using {@link org.compass.core.lucene.LuceneEnvironment.SpellCheck}
 * settings.
 *
 * @author kimchy
 */
public interface SearchEngineSpellCheckManager {

    /**
     * Starts the spell check manager. Will start a scheduled task to refresh the cached
     * searchers and readers over the spell check index. Will also schedule, if configured,
     * a schediled rebuild of the spell check index.
     */
    void start();

    /**
     * Stops any scheduled tasks started by the {@link #start()} method.
     */
    void stop();

    /**
     * Returns <code>true</code> if a rebuild of the spell check index is required.
     */
    boolean isRebuildNeeded() throws SearchEngineException;

    /**
     * Returns <code>true</code> if a rebuild of the spell index for the given sub
     * index is required.
     */
    boolean isRebuildNeeded(String subIndex) throws SearchEngineException;

    /**
     * Same as {@link #rebuild()} but executes the rebuild of each sub index using Compass
     * built in thread pool
     */
    boolean concurrentRebuild() throws SearchEngineException;

    /**
     * Rebuilds the spell check index. Won't rebuild specific sub indexes if it is not needed.
     */
    boolean rebuild() throws SearchEngineException;

    /**
     * Rebuilds the spell check index for the given sub index. Won't rebuild if it is not needed.
     */
    boolean rebuild(String subIndex) throws SearchEngineException;

    /**
     * Same as {@link #refresh()} but executes the refresh of each sub index using Compass
     * built in thread pool.
     */
    void concurrentRefresh() throws SearchEngineException;

    /**
     * Refresh the internal readers and searches that work with the spell index.
     */
    void refresh() throws SearchEngineException;

    /**
     * Refresh the internal readers and searches for the given sub index that work with the spell index.
     */
    void refresh(String subIndex) throws SearchEngineException;

    /**
     * Deletes the spell check index.
     */
    void deleteIndex() throws SearchEngineException;

    /**
     * Deletes the spell check index for the given sub index.
     */
    void deleteIndex(String subIndex) throws SearchEngineException;

    /**
     * Creates a suggest builder allowing to suggest words for the given word.
     */
    SearchEngineSpellCheckSuggestBuilder suggestBuilder(String word);

    /**
     * Suggests a query based on the given query. Note, if the query was replaced/sugested
     * then the {@link org.compass.core.CompassQuery#isSuggested()} flag will be set to
     * <code>true</code>.
     */
    CompassQuery suggest(CompassQuery query);
}
