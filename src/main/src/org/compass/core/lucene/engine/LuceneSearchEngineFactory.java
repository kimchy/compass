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

package org.compass.core.lucene.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.engine.spi.InternalSearchEngineFactory;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.LuceneResourceFactory;
import org.compass.core.lucene.engine.all.AllTermsCache;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.engine.highlighter.LuceneHighlighterManager;
import org.compass.core.lucene.engine.indexdeletionpolicy.IndexDeletionPolicyFactory;
import org.compass.core.lucene.engine.manager.DefaultLuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.optimizer.LuceneSearchEngineOptimizerManager;
import org.compass.core.lucene.engine.query.LuceneSearchEngineQueryBuilder;
import org.compass.core.lucene.engine.query.LuceneSearchEngineQueryFilterBuilder;
import org.compass.core.lucene.engine.queryparser.LuceneQueryParserManager;
import org.compass.core.lucene.engine.similarity.LuceneSimilarityManager;
import org.compass.core.lucene.engine.spellcheck.DefaultLuceneSpellCheckManager;
import org.compass.core.lucene.engine.spellcheck.InternalLuceneSearchEngineSpellCheckManager;
import org.compass.core.lucene.engine.store.DefaultLuceneSearchEngineStore;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.lucene.engine.transaction.TransactionProcessorManager;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class LuceneSearchEngineFactory implements InternalSearchEngineFactory {

    private static final Log log = LogFactory.getLog(LuceneSearchEngineFactory.class);

    private final CompassMapping mapping;

    private final PropertyNamingStrategy propertyNamingStrategy;

    private final LuceneSettings luceneSettings;

    private final ResourceFactory resourceFactory;

    private final SearchEngineOptimizer searchEngineOptimizer;

    private InternalLuceneSearchEngineSpellCheckManager spellCheckManager;

    private final LuceneSearchEngineIndexManager indexManager;

    private final ExecutorManager executorManager;

    private TransactionContext transactionContext;

    private final TransactionProcessorManager transactionProcessorManager;

    private final LuceneAnalyzerManager analyzerManager;

    private final LuceneSimilarityManager similarityManager;

    private LuceneHighlighterManager highlighterManager;

    private final LuceneQueryParserManager queryParserManager;

    private final IndexDeletionPolicyFactory indexDeletionPolicyManager;

    private final AllTermsCache allTermsCache;

    private CompassSettings settings;

    private SearchEngineEventManager eventManager = new SearchEngineEventManager();

    // debug holders

    private final boolean debug;

    public LuceneSearchEngineFactory(PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings,
                                     CompassMapping mapping, ExecutorManager executorManager) {
        this.debug = settings.getSettingAsBoolean(CompassEnvironment.DEBUG, false);
        this.propertyNamingStrategy = propertyNamingStrategy;
        this.mapping = mapping;
        this.executorManager = executorManager;
        this.settings = settings;
        this.luceneSettings = new LuceneSettings();
        luceneSettings.configure(settings);

        resourceFactory = new LuceneResourceFactory(this);

        // build the analyzers
        analyzerManager = new LuceneAnalyzerManager(settings, mapping);

        // build the search engine store
        LuceneSearchEngineStore searchEngineStore = new DefaultLuceneSearchEngineStore(this, settings, mapping);
        indexManager = new DefaultLuceneSearchEngineIndexManager(this, searchEngineStore);

        // build the index deletion policy manager
        indexDeletionPolicyManager = new IndexDeletionPolicyFactory(indexManager);
        indexDeletionPolicyManager.configure(settings);

        try {
            ClassUtils.forName("org.apache.lucene.search.highlight.Highlighter", settings.getClassLoader());
            highlighterManager = new LuceneHighlighterManager();
            highlighterManager.configure(settings);
        } catch (ClassNotFoundException e1) {
            log.info("Not using highlighter - no highlighter jar included.");
        }

        searchEngineOptimizer = new LuceneSearchEngineOptimizerManager(this);
        ((CompassConfigurable) searchEngineOptimizer).configure(settings);

        if (settings.getSettingAsBoolean(LuceneEnvironment.SpellCheck.ENABLE, false)) {
            spellCheckManager = (InternalLuceneSearchEngineSpellCheckManager) settings.getSettingAsInstance(LuceneEnvironment.SpellCheck.CLASS, DefaultLuceneSpellCheckManager.class.getName());
            spellCheckManager.configure(this, settings, mapping);
        }

        // build the query parsers
        queryParserManager = new LuceneQueryParserManager(this);
        queryParserManager.configure(settings);

        // build the similarity manager
        similarityManager = new LuceneSimilarityManager();
        similarityManager.configure(settings);

        // build the transaction processor manager
        transactionProcessorManager = new TransactionProcessorManager(this);

        this.allTermsCache = new AllTermsCache(settings, mapping);
    }

    public void close() throws SearchEngineException {
        transactionProcessorManager.close();
        analyzerManager.close();
        if (spellCheckManager != null) {
            spellCheckManager.close();
        }
        indexManager.close();
    }

    public boolean isDebug() {
        return debug;
    }

    public SearchEngine openSearchEngine(RuntimeCompassSettings runtimeSettings) {
        return new LuceneSearchEngine(runtimeSettings, this);
    }

    public SearchEngineQueryBuilder queryBuilder() throws SearchEngineException {
        return new LuceneSearchEngineQueryBuilder(this);
    }

    public SearchEngineQueryFilterBuilder queryFilterBuilder() throws SearchEngineException {
        return new LuceneSearchEngineQueryFilterBuilder();
    }

    public TransactionContext getTransactionContext() {
        return transactionContext;
    }

    public ExecutorManager getExecutorManager() {
        return this.executorManager;
    }

    public void setTransactionContext(TransactionContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    public void start() {
        searchEngineOptimizer.start();
        indexManager.start();
        if (spellCheckManager != null) {
            spellCheckManager.start();
        }
    }

    public void stop() {
        searchEngineOptimizer.stop();
        indexManager.stop();
        if (spellCheckManager != null) {
            spellCheckManager.stop();
        }
    }

    public ResourceFactory getResourceFactory() {
        return this.resourceFactory;
    }

    public String getAliasProperty() {
        return luceneSettings.getAliasProperty();
    }

    public String getExtendedAliasProperty() {
        return luceneSettings.getExtendedAliasProperty();
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

    public SearchEngineSpellCheckManager getSpellCheckManager() {
        return this.spellCheckManager;
    }

    public SearchEngineIndexManager getIndexManager() {
        return indexManager;
    }

    public LuceneSearchEngineIndexManager getLuceneIndexManager() {
        return this.indexManager;
    }

    public TransactionProcessorManager getTransactionProcessorManager() {
        return this.transactionProcessorManager;
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

    public LuceneSimilarityManager getSimilarityManager() {
        return similarityManager;
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

    public LuceneQueryParserManager getQueryParserManager() {
        return this.queryParserManager;
    }

    public IndexDeletionPolicyFactory getIndexDeletionPolicyManager() {
        return indexDeletionPolicyManager;
    }

    public AllTermsCache getAllTermsCache() {
        return allTermsCache;
    }
}
