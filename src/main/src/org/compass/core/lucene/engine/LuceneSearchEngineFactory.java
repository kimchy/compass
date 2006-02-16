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

package org.compass.core.lucene.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.engine.highlighter.LuceneHighlighterManager;
import org.compass.core.lucene.engine.manager.DefaultLuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.optimizer.AdaptiveOptimizer;
import org.compass.core.lucene.engine.optimizer.LuceneSearchEngineOptimizer;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStoreFactory;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class LuceneSearchEngineFactory implements SearchEngineFactory {

    static {
        // set the segment reader to be a compass class
        System.setProperty("org.apache.lucene.SegmentReader.class", "org.apache.lucene.index.CompassSegmentReader");
    }

    private static final Log log = LogFactory.getLog(LuceneSearchEngineFactory.class);

    private CompassMapping mapping;

    private PropertyNamingStrategy propertyNamingStrategy;

    private LuceneSettings luceneSettings;

    private LuceneSearchEngineOptimizer searchEngineOptimizer;

    private LuceneSearchEngineIndexManager indexManager;

    private LuceneAnalyzerManager analyzerManager;

    private LuceneHighlighterManager highlighterManager;

    private CompassSettings settings;

    private SearchEngineEventManager eventManager = new SearchEngineEventManager();

    public LuceneSearchEngineFactory(PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings,
                                     CompassMapping mapping) {
        this.propertyNamingStrategy = propertyNamingStrategy;
        this.mapping = mapping;
        this.settings = settings;
        this.luceneSettings = new LuceneSettings();
        luceneSettings.configure(settings);
        configure(settings, mapping);
    }

    public void close() throws SearchEngineException {
        indexManager.close();
    }

    public SearchEngine openSearchEngine() {
        return new LuceneSearchEngine(this);
    }

    private void configure(CompassSettings settings, CompassMapping mapping) {
        // build the analyzers
        analyzerManager = new LuceneAnalyzerManager();
        analyzerManager.configure(settings, mapping, luceneSettings);

        // build the search engine store
        String subContext = settings.getSetting(CompassEnvironment.CONNECTION_SUB_CONTEXT, "index");
        LuceneSearchEngineStore searchEngineStore = LuceneSearchEngineStoreFactory.createStore(luceneSettings
                .getConnection(), subContext);
        searchEngineStore.configure(this, settings, mapping);
        indexManager = new DefaultLuceneSearchEngineIndexManager(this, searchEngineStore);

        try {
            ClassUtils.forName("org.apache.lucene.search.highlight.Highlighter");
            highlighterManager = new LuceneHighlighterManager();
            highlighterManager.configure(settings);
        } catch (ClassNotFoundException e1) {
            log.info("Not using highlighter - no highlighter jar included.");
        }

        // build the optimizer and start it
        String optimizerClassSetting = settings.getSetting(LuceneEnvironment.Optimizer.TYPE, AdaptiveOptimizer.class
                .getName());
        if (log.isDebugEnabled()) {
            log.debug("Using optimizer [" + optimizerClassSetting + "]");
        }
        try {
            Class optimizerClass = ClassUtils.forName(optimizerClassSetting);
            searchEngineOptimizer = (LuceneSearchEngineOptimizer) optimizerClass.newInstance();
            if (searchEngineOptimizer instanceof CompassConfigurable) {
                ((CompassConfigurable) searchEngineOptimizer).configure(settings);
            }
        } catch (Exception e) {
            throw new SearchEngineException("Can't find optimizer class [" + optimizerClassSetting + "]", e);
        }
        searchEngineOptimizer.setSearchEngineFactory(this);
    }

    public String getAliasProperty() {
        return luceneSettings.getAliasProperty();
    }

    public String getAllProperty() {
        return luceneSettings.getAllProperty();
    }

    public CompassMapping getMapping() {
        return this.mapping;
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return propertyNamingStrategy;
    }

    public SearchEngineOptimizer getOptimizer() {
        return searchEngineOptimizer;
    }

    public void setOptimizer(LuceneSearchEngineOptimizer searchEngineOptimizer) {
        this.searchEngineOptimizer = searchEngineOptimizer;
    }

    public SearchEngineIndexManager getIndexManager() {
        return indexManager;
    }

    public void setIndexManager(LuceneSearchEngineIndexManager indexManager) {
        this.indexManager = indexManager;
    }

    public LuceneSearchEngineIndexManager getLuceneIndexManager() {
        return this.indexManager;
    }

    public LuceneSettings getLuceneSettings() {
        return luceneSettings;
    }

    public CompassSettings getSettings() {
        return settings;
    }

    public LuceneAnalyzerManager getAnalyzerManager() {
        return analyzerManager;
    }

    public SearchEngineEventManager getEventManager() {
        return this.eventManager;
    }

    public LuceneHighlighterManager getHighlighterManager() throws SearchEngineException {
        if (highlighterManager == null) {
            throw new SearchEngineException("Trying to use highlighter, but no highlighter jar included");
        }
        return highlighterManager;
    }
}
