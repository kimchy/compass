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

import org.apache.lucene.index.LuceneSegmentsMerger;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

import java.io.IOException;

/**
 * @author kimchy
 */
public class AdaptiveOptimizer extends AbstractLuceneSearchEngineOptimizer implements CompassConfigurable {

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

    public boolean needOptimizing(String subIndex, LuceneSubIndexInfo indexInfo) {
        if (indexInfo.size() >= mergeFactor) {
            if (log.isDebugEnabled()) {
                log.debug("Need to optimize sub-index [" + subIndex + "]. Number of segments " + indexInfo.size()
                        + " is larger than [" + mergeFactor + "]");
            }
            return true;
        }
        return false;
    }

    protected void doOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException {
        if (indexInfo.size() < mergeFactor) {
            return;
        }
        // find the number of segments to merge
        int threshold = mergeFactor - 1;
        long count = 0;
        for (int i = indexInfo.size() - 1; i >= threshold; i--) {
            count += indexInfo.info(i).docCount();
        }
        int mergeFromSegment = 0;
        for (mergeFromSegment = threshold; mergeFromSegment > 0; mergeFromSegment--) {
            LuceneSubIndexInfo.LuceneSegmentInfo segmentInfo = indexInfo.info(mergeFromSegment - 1);
            if (count >= segmentInfo.docCount()) {
                count += segmentInfo.docCount();
            } else {
                break;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Optimizing sub-index [" + subIndex + "] from [" + mergeFromSegment + "]");
        }
        LuceneSearchEngineIndexManager indexManager = getSearchEngineFactory().getLuceneIndexManager();
        LuceneSegmentsMerger segmentsMerger = null;
        try {
            long time = System.currentTimeMillis();
            Directory dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
            segmentsMerger = new LuceneSegmentsMerger(subIndex, dir, true, getSearchEngineFactory().getLuceneSettings());
            long lockTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            segmentsMerger.mergeFromSegment(mergeFromSegment);
            long mergeTime = System.currentTimeMillis() - time;
            time = System.currentTimeMillis();
            segmentsMerger.commit();
            long commitTime = System.currentTimeMillis() - time;
            if (log.isDebugEnabled()) {
                log.debug("Optimization of sub-index [" + subIndex + "] took [" + (commitTime + mergeTime + lockTime)
                        + "ms], Locking took [" + lockTime + "ms], merge took [" + mergeTime + "ms], and commit took ["
                        + commitTime + "ms].");
            }
            indexManager.clearCache(subIndex);
        } catch (IOException e) {
            if (e.getMessage().startsWith("Lock obtain")) {
                log.warn("Failed to obtain lock on sub-index [" + subIndex + "], will do it next time.");
            } else {
                throw new SearchEngineException("Failed to optimize sub-index [" + subIndex + "]", e);
            }
        } finally {
            if (segmentsMerger != null) {
                try {
                    segmentsMerger.close();
                } catch (IOException e) {
                    log.warn("Failed to close optimizer segment merger, ignoring", e);
                }
            }
        }
    }
}
