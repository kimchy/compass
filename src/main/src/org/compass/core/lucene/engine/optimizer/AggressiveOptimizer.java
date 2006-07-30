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

import java.io.IOException;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * @author kimchy
 */
public class AggressiveOptimizer extends AbstractLuceneSearchEngineOptimizer implements CompassConfigurable {

    private long mergeFactor;

    public void configure(CompassSettings settings) throws CompassException {
        mergeFactor = settings.getSettingAsLong(LuceneEnvironment.Optimizer.Aggressive.MERGE_FACTOR, 10);
    }

    public boolean canBeScheduled() {
        return true;
    }

    protected boolean isOptimizeOnlyIfIndexChanged() {
        return true;
    }

    public boolean doNeedOptimizing(String subIndex, LuceneSubIndexInfo indexInfo) {
        if (indexInfo.size() >= mergeFactor) {
            if (log.isDebugEnabled()) {
                log.debug("Need to optimize sub-index [" + subIndex + "]. Optimizing " + indexInfo.size()
                        + " segments into one segment.");
            }
            return true;
        }
        return false;
    }

    protected void doOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException {
        LuceneSearchEngineIndexManager indexManager = getSearchEngineFactory().getLuceneIndexManager();
        IndexWriter indexWriter = null;
        Directory dir = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Optimizing sub-index [" + subIndex + "]");
            }
            long time = System.currentTimeMillis();
            dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
            indexWriter = indexManager.openIndexWriter(dir, false);
            long lockTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            indexWriter.optimize();
            long optimizeTime = System.currentTimeMillis() - time;
            if (log.isDebugEnabled()) {
                log.debug("Optimization of sub-index [" + subIndex + "] took [" + (optimizeTime + lockTime)
                        + "ms], Locking took [" + lockTime + "ms], and optimization took [" + optimizeTime + "ms].");
            }
        } catch (IOException e) {
            if (e.getMessage().startsWith("Lock obtain")) {
                log.warn("Failed to obtain lock on sub-index [" + subIndex + "], will do it next time.");
            } else {
                throw new SearchEngineException("Failed to optimize sub-index [" + subIndex + "]", e);
            }
        } finally {
            indexManager.closeIndexWriter(subIndex, indexWriter, dir);
        }
    }
}
