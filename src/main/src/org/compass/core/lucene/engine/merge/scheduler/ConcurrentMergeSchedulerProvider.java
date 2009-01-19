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

import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.MergeScheduler;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * A provider for Lucene {@link org.apache.lucene.index.ConcurrentMergeScheduler} allowing to configure
 * using {@link org.compass.core.lucene.LuceneEnvironment.MergeScheduler.Concurrent}.
 *
 * @author kimchy
 */
public class ConcurrentMergeSchedulerProvider implements MergeSchedulerProvider {

    /**
     * Returns Lucene {@link org.apache.lucene.index.ConcurrentMergeScheduler} allowing to configure
     * using {@link org.compass.core.lucene.LuceneEnvironment.MergeScheduler.Concurrent}.
     */
    public MergeScheduler create(LuceneSearchEngineIndexManager indexManager, CompassSettings settings) throws SearchEngineException {
        ConcurrentMergeScheduler mergeScheduler = new ConcurrentMergeScheduler();
        mergeScheduler.setMaxThreadCount(settings.getSettingAsInt(LuceneEnvironment.MergeScheduler.Concurrent.MAX_THREAD_COUNT, 3));
        mergeScheduler.setMergeThreadPriority(settings.getSettingAsInt(LuceneEnvironment.MergeScheduler.Concurrent.THREAD_PRIORITY, Thread.NORM_PRIORITY));
        return mergeScheduler;
    }
}
