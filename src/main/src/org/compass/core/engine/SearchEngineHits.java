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
 * Holds hits returned by a find operation of a search engine.
 * 
 * @see org.compass.core.engine.SearchEngineQuery#hits(SearchEngine)
 * 
 * @author kimchy
 */
public interface SearchEngineHits {

    /**
     * Returns that maps to the n'th hit.
     */
    Resource getResource(int n) throws SearchEngineException;

    /**
     * Returns the number of hits.
     */
    int getLength();

    /**
     * Returns the score of the n'th hit. Can be a value between 0 and 1,
     * normalised by the highest scoring hit.
     */
    float score(int i) throws SearchEngineException;

    /**
     * Returns an highlighter for the hits.
     */
    SearchEngineHighlighter getHighlighter() throws SearchEngineException;

    /**
     * Closes the hits object. Note that it is an optional operation since it
     * will be closed transperantly when the transaction is closed.
     * <p>
     * It is provided for more controlled resource management
     * </p>
     */
    void close() throws SearchEngineException;
}
