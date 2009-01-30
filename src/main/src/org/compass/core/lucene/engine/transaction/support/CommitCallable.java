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

package org.compass.core.lucene.engine.transaction.support;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * A simple callable that commits an index writer for a given sub index.
 *
 * @author kimchy
 */
public class CommitCallable implements Callable {

    private static final Log logger = LogFactory.getLog(CommitCallable.class);

    private final LuceneSearchEngineIndexManager indexManager;

    private final String subIndex;

    private final IndexWriter indexWriter;

    private final boolean invalidateCacheOnCommit;

    public CommitCallable(LuceneSearchEngineIndexManager indexManager, String subIndex, IndexWriter indexWriter, boolean invalidateCacheOnCommit) {
        this.indexManager = indexManager;
        this.subIndex = subIndex;
        this.indexWriter = indexWriter;
        this.invalidateCacheOnCommit = invalidateCacheOnCommit;
    }

    public Object call() throws Exception {
        try {
            indexWriter.commit();
            indexWriter.close();
        } catch (IOException e) {
            Directory dir = indexManager.getStore().openDirectory(subIndex);
            try {
                if (IndexWriter.isLocked(dir)) {
                    IndexWriter.unlock(dir);
                }
            } catch (Exception e1) {
                logger.warn("Failed to check for locks or unlock failed commit for sub index [" + subIndex + "]", e);
            }
            throw new SearchEngineException("Failed commit transaction sub index [" + subIndex + "]", e);
        } finally {
            indexManager.getIndexWritersManager().trackCloseIndexWriter(subIndex, indexWriter);
        }
        if (invalidateCacheOnCommit) {
            if (logger.isTraceEnabled()) {
                logger.trace("Invalidating cache after commit for sub index [" + subIndex + "]");
            }
            indexManager.getIndexHoldersCache().invalidateCache(subIndex);
        }
        return null;
    }
}
