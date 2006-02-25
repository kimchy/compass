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

package org.compass.core.engine;

/**
 * Optimizes search engine index data.
 * <p>
 * Using it, one can controll the lifecycle of the optimizer using the
 * <code>start()</code> and <code>stop()</code> methods (note that does not
 * mean that it will start a scheduled optimizer, it depends on the
 * configuration supplied).
 * <p>
 * You can also check if the search engine required optimization using the
 * <code>needOptimization()</code> method, and run the optimization process
 * using the <code>optimize()</code> method.
 * 
 * @author kimchy
 */
public interface SearchEngineOptimizer {

    /**
     * Starts the given optimizer. Will start a scheduled optimizer if
     * configured.
     * 
     * @throws SearchEngineException
     */
    void start() throws SearchEngineException;

    /**
     * Stops the given optimizer. Will stop the scheduled optimizer if
     * configured.
     * <p>
     * Note that if the optimizer is stopped while optimizing, it might take
     * some time till the optimizer will actually stop.
     * 
     * @throws SearchEngineException
     */
    void stop() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the optimizer is running.
     * 
     * @return <code>true</code> if the optimizer is running
     */
    boolean isRunning();

    /**
     * Returns <code>true</code> if the search engine index required
     * optimization (depends on the optimizer configured).
     * 
     * @return <code>true</code> if the search engine index requires optimization.
     * @throws SearchEngineException
     */
    boolean needOptimization() throws SearchEngineException;

    /**
     * Optimizes the search engine index if it requires optimization.
     * 
     * @throws SearchEngineException
     */
    void optimize() throws SearchEngineException;
}
