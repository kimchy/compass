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

package org.compass.core.lucene.engine.optimizer;

import org.compass.core.engine.SearchEngineException;

/**
 * An internal Lucene interface for managing index optimizations. 
 *
 * @author kimchy
 */
public interface LuceneSearchEngineOptimizer {

    /**
     * Can the optimizer be scheduled or not.
     */
    boolean canBeScheduled();

    /**
     * Optimizes the search engine index if it requires optimization.
     *
     * @throws org.compass.core.engine.SearchEngineException
     */
    void optimize() throws SearchEngineException;

    /**
     * Forces an optimization to occur and maintain to the number of "mergeFactor" segments.
     */
    void optimize(int maxNumberOfSegments) throws SearchEngineException;

    /**
     * Optimizes the sub index if it requires optimization.
     *
     * @param subIndex The sub index to optimize
     */
    void optimize(String subIndex) throws SearchEngineException;

    /**
     * Forces an optimization to occur for the specific sub index and maintain to the number of "mergeFactor" segments.
     */
    void optimize(String subIndex, int maxNumberOfSegments) throws SearchEngineException;
}
