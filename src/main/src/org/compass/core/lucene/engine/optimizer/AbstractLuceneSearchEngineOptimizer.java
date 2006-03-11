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
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.store.Directory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;

/**
 * @author kimchy
 */
public abstract class AbstractLuceneSearchEngineOptimizer implements LuceneSearchEngineOptimizer {

    protected final Log log = LogFactory.getLog(getClass());

    private LuceneSearchEngineFactory searchEngineFactory;

    private boolean isRunning = false;

    private HashMap subIndexVersion = new HashMap();

    private HashMap tempSubIndexVersion = new HashMap();

    private OptimizerTemplate optimizerTemplate;

    public void start() throws SearchEngineException {
        if (isRunning) {
            throw new IllegalStateException("Optimizer is already running");
        }
        if (log.isDebugEnabled()) {
            log.debug("Starting Optimizer");
        }
        optimizerTemplate = new OptimizerTemplate(this);
        doStart();
        isRunning = true;
    }

    protected void doStart() throws SearchEngineException {

    }

    public void stop() throws SearchEngineException {
        if (!isRunning) {
            throw new IllegalStateException("Optimizer is is not running");
        }
        if (log.isDebugEnabled()) {
            log.debug("Stopping Optimizer");
        }
        doStop();
        isRunning = false;
    }

    protected void doStop() throws SearchEngineException {

    }

    public boolean isRunning() {
        return isRunning;
    }

    public void optimize() throws SearchEngineException {
        optimizerTemplate.optimize();
    }

    public void optimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException {
        doOptimize(subIndex, indexInfo);
        subIndexVersion.put(subIndex, tempSubIndexVersion.get(subIndex));
    }

    protected abstract void doOptimize(String subIndex, LuceneSubIndexInfo indexInfo) throws SearchEngineException;

    public boolean needOptimization() throws SearchEngineException {
        return optimizerTemplate.needOptimizing();
    }

    protected abstract boolean isOptimizeOnlyIfIndexChanged();

    public boolean needOptimizing(String subIndex) throws SearchEngineException {
        if (!isOptimizeOnlyIfIndexChanged()) {
            return true;
        }
        Long version = (Long) subIndexVersion.get(subIndex);
        Directory directory = searchEngineFactory.getLuceneIndexManager().getStore()
                .getDirectoryBySubIndex(subIndex, false);
        long actualVersion;
        try {
            actualVersion = IndexReader.getCurrentVersion(directory);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to read index version for optimization", e);
        }
        // it is the first time we run it, or the index was changed
        if (version == null || actualVersion > version.longValue()) {
            tempSubIndexVersion.put(subIndex, new Long(actualVersion));
            return true;
        }
        return false;
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public OptimizerTemplate getOptimizerTemplate() {
        return this.optimizerTemplate;
    }
}
