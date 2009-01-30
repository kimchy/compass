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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.transaction.context.TransactionContextCallback;

/**
 * @author kimchy
 */
public class DefaultLuceneSearchEngineOptimizer implements LuceneSearchEngineOptimizer, CompassConfigurable, SearchEngineFactoryAware {

    private final static Log logger = LogFactory.getLog(DefaultLuceneSearchEngineOptimizer.class);

    private LuceneSearchEngineFactory searchEngineFactory;

    private int maxNumberOfSegments;

    private CompassSettings settings;

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        maxNumberOfSegments = settings.getSettingAsInt(LuceneEnvironment.Optimizer.MAX_NUMBER_OF_SEGMENTS, 10);
    }

    public boolean canBeScheduled() {
        return true;
    }

    public void optimize() throws SearchEngineException {
        optimize(maxNumberOfSegments);
    }

    public void optimize(int maxNumberOfSegments) {
        for (String subIndex : searchEngineFactory.getLuceneIndexManager().getSubIndexes()) {
            optimize(subIndex, maxNumberOfSegments);
        }
    }

    public void optimize(String subIndex) throws SearchEngineException {
        optimize(subIndex, maxNumberOfSegments);
    }

    public void optimize(final String subIndex, final int maxNumberOfSegments) {
        searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Object>() {
            public Object doInTransaction() throws CompassException {
                doOptimize(subIndex, maxNumberOfSegments);
                searchEngineFactory.getLuceneIndexManager().getIndexHoldersCache().refreshCache(subIndex);
                return null;
            }
        });
    }

    protected void doOptimize(String subIndex, int maxNumberOfSegments) throws SearchEngineException {
        // we first check if we need to optimize at all, so we won't obtain a write lock needlessly
        LuceneSubIndexInfo indexInfo = doGetIndexInfo(subIndex);
        if (indexInfo.size() < maxNumberOfSegments) {
            return;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Optimizing sub-index [" + subIndex + "] with maxNumberOfSegments [" + maxNumberOfSegments + "] and currentNumberOfSegments[" + indexInfo.size() + "]");
        }
        long time = System.currentTimeMillis();
        IndexWriter indexWriter;
        try {
            indexWriter = searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().openIndexWriter(settings, subIndex);
            searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackOpenIndexWriter(subIndex, indexWriter);
        } catch (LockObtainFailedException e) {
            logger.debug("Failed to obtain lock in order to optimizer, will try next time...");
            return;
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open index writer for optimization for sub index [" + subIndex + "]", e);
        }

        try {
            indexWriter.optimize(maxNumberOfSegments);
        } catch (Exception e) {
            try {
                Directory dir = searchEngineFactory.getLuceneIndexManager().getDirectory(subIndex);
                if (IndexWriter.isLocked(dir)) {
                    IndexWriter.unlock(dir);
                }
            } catch (Exception e1) {
                // do nothing
            }
        } finally {
            try {
                indexWriter.close();
            } catch (Exception e) {
                // ignore
            } finally {
                searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackCloseIndexWriter(subIndex, indexWriter);
            }
        }
        long optimizeTime = System.currentTimeMillis() - time;

        if (logger.isDebugEnabled()) {
            logger.debug("Optimization of sub-index [" + subIndex + "] took [" + (optimizeTime) + "ms]");
        }
    }

    protected LuceneSubIndexInfo doGetIndexInfo(String subIndex) throws SearchEngineException {
        try {
            return LuceneSubIndexInfo.getIndexInfo(subIndex, searchEngineFactory.getLuceneIndexManager());
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read index info for sub index [" + subIndex + "]", e);
        }
    }

}
