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

package org.compass.core.lucene.engine.merge.scheduler;

import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.SerialMergeScheduler;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * A provider for Lucene {@link org.apache.lucene.index.SerialMergeScheduler}.
 *
 * @author kimchy
 */
public class SerialMergeSchedulerProvider implements MergeSchedulerProvider {

    /**
     * Creates a new {@link org.apache.lucene.index.SerialMergeScheduler} and returns it.
     */
    public MergeScheduler create(LuceneSearchEngineIndexManager indexManager, CompassSettings settings) throws SearchEngineException {
        return new SerialMergeScheduler();
    }
}
