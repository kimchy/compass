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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * @author kimchy
 */
public class OptimizerTemplate {

    private static Log log = LogFactory.getLog(OptimizerTemplate.class);

    private boolean canceled;

    private LuceneSearchEngineOptimizer optimizer;

    public OptimizerTemplate(LuceneSearchEngineOptimizer optimizer, SearchEngineIndexManager indexManager) {
        this.optimizer = optimizer;
    }

    public void optimize() {
        LuceneSearchEngineIndexManager indexManager = optimizer.getSearchEngineFactory().getLuceneIndexManager();
        String[] subIndexes = indexManager.getStore().getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            try {
                if (canceled) {
                    break;
                }
                boolean needOptimizing = optimizer.needOptimizing(subIndexes[i]);
                if (!needOptimizing) {
                    continue;
                }
                LuceneSubIndexInfo indexInfo = LuceneSubIndexInfo.getIndexInfo(subIndexes[i], indexManager);
                needOptimizing = optimizer.needOptimizing(subIndexes[i], indexInfo);
                if (canceled) {
                    break;
                }
                if (needOptimizing) {
                    optimizer.optimize(subIndexes[i], indexInfo);
                }
            } catch (Exception e) {
                log.error("Failed to optimize index for sub-index [" + subIndexes[i] + "]", e);
            }
        }

    }

    public boolean needOptimizing() {
        LuceneSearchEngineIndexManager indexManager = optimizer.getSearchEngineFactory().getLuceneIndexManager();
        String[] subIndexes = indexManager.getStore().getSubIndexes();
        for (int i = 0; i < subIndexes.length; i++) {
            try {
                if (canceled) {
                    break;
                }
                boolean needOptimizing = optimizer.needOptimizing(subIndexes[i]);
                if (!needOptimizing) {
                    continue;
                }
                LuceneSubIndexInfo indexInfo = LuceneSubIndexInfo.getIndexInfo(subIndexes[i], indexManager);
                needOptimizing = optimizer.needOptimizing(subIndexes[i], indexInfo);
                if (needOptimizing) {
                    return true;
                }
            } catch (Exception e) {
                log.error("Failed to check if index need optimizing for sub-index [" + subIndexes[i] + "]", e);
            }
        }
        return false;
    }

    public void cancel() {
        this.canceled = true;
    }
}
