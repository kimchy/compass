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

package org.compass.core.lucene.engine.optimizer;

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * Responsible for optimizing the search engine.
 * 
 * @author kimchy
 */
public interface LuceneSearchEngineOptimizer extends SearchEngineOptimizer {

    void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory);

    LuceneSearchEngineFactory getSearchEngineFactory();

    /**
     * Returns the optimizer template associated with this optimizer.
     */
    OptimizerTemplate getOptimizerTemplate();

    /**
     * Can the optimizer be scheduled or not.
     */
    boolean canBeScheduled();
    
    /**
     * Should the index be optimized only after the index was changed. The
     * operation of checking if the index was changed or not is much faster than
     * the {@link #needOptimizing(String, LuceneSubIndexInfo)} operation, and it
     * can speed up optimization check time.
     * <p>
     * It will return <code>false</code> if there is not need for optmizing,
     * and <code>true</code> to continue and check with the
     * {@link #needOptimizing(String, LuceneSubIndexInfo)} method.
     */
    boolean needOptimizing(String subIndex) throws SearchEngineException;

    /**
     * Checks if the index requires optimizing operation.
     * 
     * @param subIndex
     *            The sub-index to check.
     * @param indexInfo
     *            The index structire information.
     * @return <code>true</code> if the index need to be optimized.
     */
    boolean needOptimizing(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException;

    /**
     * Optmizes the sub-index.
     * 
     * @param subIndex
     *            The sub-index to optimize.
     * @param indexInfo
     *            The index structure information.
     */
    void optimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException;

}
