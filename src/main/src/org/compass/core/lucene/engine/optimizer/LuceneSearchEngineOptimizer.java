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

import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * Responsible for optimizing the search engine.
 *
 * @author kimchy
 */
public interface LuceneSearchEngineOptimizer extends SearchEngineOptimizer {

    /**
     * Injected with the actual search engine factory upon construction or Compass startup.
     */
    void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory);

    /**
     * Can the optimizer be scheduled or not.
     */
    boolean canBeScheduled();
}
