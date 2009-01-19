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

import org.apache.lucene.index.LuceneSubIndexInfo;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * @author kimchy
 */
public abstract class AbstractIndexInfoOptimizer extends AbstractOptimizer {

    protected void doOptimize(String subIndex) throws SearchEngineException {
        LuceneSubIndexInfo indexInfo = doGetIndexInfo(subIndex);
        if (indexInfo == null) {
            return;
        }
        doOptimize(subIndex, indexInfo);
    }

    protected void doForceOptimize(String subIndex) throws SearchEngineException {
        LuceneSubIndexInfo indexInfo = doGetIndexInfo(subIndex);
        if (indexInfo == null) {
            return;
        }
        doForceOptimize(subIndex, indexInfo);
    }

    protected LuceneSubIndexInfo doGetIndexInfo(String subIndex) {
        LuceneSearchEngineIndexManager indexManager = getSearchEngineFactory().getLuceneIndexManager();
        LuceneSubIndexInfo indexInfo;
        try {
            indexInfo = LuceneSubIndexInfo.getIndexInfo(subIndex, indexManager);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read index info for sub index [" + subIndex + "]", e);
        }
        if (indexInfo == null) {
            // no index data, simply continue
            return null;
        }
        if (!isRunning()) {
            return null;
        }
        return indexInfo;
    }

    protected abstract void doOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException;

    protected abstract void doForceOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException;
}
