/*
 * Copyright 2004-2008 the original author or authors.
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
 * Agressive optimzer optimizes the index down to a single segment once it has more that the configured
 * merge factor segments. The merge factor can be configured using {@link org.compass.core.lucene.LuceneEnvironment.Optimizer.Aggressive#MERGE_FACTOR}
 * (which deftauls to <code>10</code>).
 *
 * <p>Forced optimization will optimize the index regardless of the number of segments.
 *
 * @author kimchy
 */
public class AggressiveOptimizer extends AbstractIndexInfoOptimizer implements CompassConfigurable {

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

    protected boolean doNeedOptimizing(String subIndex, LuceneSubIndexInfo indexInfo) {
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
        if (!doNeedOptimizing(subIndex, indexInfo)) {
            return;
        }
        doForceOptimize(subIndex, indexInfo);
    }

    protected void doForceOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException {
        LuceneSearchEngineIndexManager indexManager = getSearchEngineFactory().getLuceneIndexManager();
        IndexWriter indexWriter = null;
        Directory dir;
        try {
            if (log.isDebugEnabled()) {
                log.debug("Optimizing sub-index [" + subIndex + "]");
            }
            long time = System.currentTimeMillis();
            dir = indexManager.getStore().openDirectory(subIndex);
            indexWriter = indexManager.openIndexWriter(indexManager.getSettings().getSettings(), dir, false);
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
            try {
                if (indexWriter != null) {
                    indexWriter.close();
                }
            } catch (IOException e) {
                log.warn("Failed to close index writer for sub index [" + subIndex + "]", e);
            }
        }
    }
}
