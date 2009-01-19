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

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * The Adaptive optimizer uses the configured {@link org.compass.core.lucene.LuceneEnvironment.Optimizer.Adaptive#MERGE_FACTOR}
 * (which defaults to <code>10</code>). Basically, ends up calling Lucene {@link org.apache.lucene.index.IndexWriter#optimize(int)}
 * with the provided merge factor which optimizes the index up to the given merge factor.
 *
 * <p>Forced optimization ends up optimizing the sub index up to a single segmend, overriding the merge factor (just
 * for that call, with a merge factor of <code>1</code>).
 *
 * @author kimchy
 */
public class AdaptiveOptimizer extends AbstractOptimizer implements CompassConfigurable {

    private int mergeFactor;

    public void configure(CompassSettings settings) throws CompassException {
        mergeFactor = settings.getSettingAsInt(LuceneEnvironment.Optimizer.Adaptive.MERGE_FACTOR, 10);
    }

    public boolean canBeScheduled() {
        return true;
    }

    protected boolean isOptimizeOnlyIfIndexChanged() {
        return false;
    }

    protected void doForceOptimize(String subIndex) throws SearchEngineException {
        doOptimize(subIndex, 1);
    }

    protected void doOptimize(String subIndex) throws SearchEngineException {
        doOptimize(subIndex, mergeFactor);
    }

    protected void doOptimize(String subIndex, int mergeFactor) throws SearchEngineException {
        if (log.isDebugEnabled()) {
            log.debug("Optimizing sub-index [" + subIndex + "] with mergeFactor [" + mergeFactor + "]");
        }
        long time = System.currentTimeMillis();
        LuceneSearchEngineIndexManager indexManager = (LuceneSearchEngineIndexManager) getSearchEngineFactory().getIndexManager();
        IndexWriter indexWriter;
        try {
            indexWriter = indexManager.openIndexWriter(indexManager.getSettings().getSettings(), subIndex);
        } catch (LockObtainFailedException e) {
            log.debug("Failed to obtain lock in order to optimizer, will try next time...");
            return;
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open index writer for optimization for sub index [" + subIndex + "]", e);
        }

        try {
            indexWriter.optimize(mergeFactor);
        } catch (Exception e) {
            try {
                Directory dir = indexManager.getStore().openDirectory(subIndex);
                if (IndexReader.isLocked(dir)) {
                    IndexReader.unlock(dir);
                }
            } catch (Exception e1) {
                // do nothing
            }
        } finally {
            try {
                indexWriter.close();
            } catch (Exception e) {
                // ignore
            }
        }
        long optimizeTime = System.currentTimeMillis() - time;

        if (log.isDebugEnabled()) {
            log.debug("Optimization of sub-index [" + subIndex + "] took [" + (optimizeTime) + "ms]");
        }
    }
}
