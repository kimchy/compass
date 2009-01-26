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
 * Optimizes search engine index data.
 *
 * Can control a scheduled optimizer that will run periodically using {@link #start()} and {@link #stop()}.
 *
 * @author kimchy
 */
public interface SearchEngineOptimizer {

    /**
     * Starts the given optimizer. Will start a scheduled optimizer if
     * configured. If not, does nothing.
     */
    void start() throws SearchEngineException;

    /**
     * Stops the given optimizer. Will stop the scheduled optimizer if
     * configured. If not, does nothing.
     *
     * <p>Note that if the optimizer is stopped while optimizing, it might take
     * some time till the optimizer will actually stop.
     */
    void stop() throws SearchEngineException;

    /**
     * Returns <code>true</code> if the optimizer is running a scheduled optimizer.
     */
    boolean isRunning();

    /**
     * Optimizes the search engine index if it requires optimization. The optimization will be perfomed on all
     * sub indexes and based on configuration. For example, the default optimizer will use the configured
     * <code>maxNumberOfSegments</code> in order to perform the optimization.
     */
    void optimize() throws SearchEngineException;

    /**
     * Optimizes all the sub indexes down to the required maximum number of segments.
     */
    void optimize(int maxNumberOfSegments) throws SearchEngineException;

    /**
     * Optimizes the sub index does to a configured max number of segments.
     */
    void optimize(String subIndex) throws SearchEngineException;

    /**
     * Optimizes a specific sub index down to a required maximum number of segments.
     */
    void optimize(String subIndex, int maxNumberOfSegments) throws SearchEngineException;
}
