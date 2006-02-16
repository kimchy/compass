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

package org.compass.core.impl;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LuceneSubIndexInfo;
import org.apache.lucene.store.Directory;
import org.compass.core.Compass;
import org.compass.core.CompassCallback;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.cache.first.FirstLevelCache;
import org.compass.core.cache.first.FirstLevelCacheFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.id.IdentifierGenerator;
import org.compass.core.id.UUIDGenerator;
import org.compass.core.jndi.CompassObjectFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.manager.LuceneScheduledSearchEngineIndexManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.optimizer.LuceneSearchEngineOptimizer;
import org.compass.core.lucene.engine.optimizer.LuceneSearchEngineScheduledOptimizer;
import org.compass.core.lucene.engine.store.LuceneSearchEngineStore;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.transaction.TransactionFactory;
import org.compass.core.transaction.TransactionFactoryFactory;

/**
 * @author kimchy
 */
public class DefaultCompass implements InternalCompass {

    private static final Log log = LogFactory.getLog(DefaultCompass.class);

    private static final long serialVersionUID = 3256446884762891059L;

    private static final IdentifierGenerator UUID_GENERATOR = new UUIDGenerator();

    private final String name;

    private String uuid;

    private CompassMapping mapping;

    private SearchEngineFactory searchEngineFactory;

    private TransactionFactory transactionFactory;

    private ConverterLookup converterLookup;

    private CompassMetaData compassMetaData;

    private PropertyNamingStrategy propertyNamingStrategy;

    protected CompassSettings settings;

    private FirstLevelCacheFactory firstLevelCacheFactory;

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings) throws CompassException {
        this(mapping, converterLookup, compassMetaData, propertyNamingStrategy, settings, false);
    }

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings,
                          boolean duplicate) throws CompassException {

        this(mapping, converterLookup, compassMetaData, propertyNamingStrategy, settings, duplicate,
                new LuceneSearchEngineFactory(propertyNamingStrategy, settings, mapping));
    }

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings,
                          boolean duplicate, LuceneSearchEngineFactory searchEngineFactory) throws CompassException {

        this.mapping = mapping;
        this.converterLookup = converterLookup;
        this.compassMetaData = compassMetaData;
        this.propertyNamingStrategy = propertyNamingStrategy;
        this.name = settings.getSetting(CompassEnvironment.NAME, "default");
        this.settings = settings;

        if (!duplicate) {
            registerJndi();
        }

        this.searchEngineFactory = searchEngineFactory;

        // build the transaction factory
        transactionFactory = TransactionFactoryFactory.createTransactionFactory(this, settings);

        // wrap optimizer with transaction, and create scheduled one
        LuceneSearchEngineOptimizer optimizer = (LuceneSearchEngineOptimizer) searchEngineFactory.getOptimizer();
        optimizer = new TransactionalSearchEngineOptimizer(optimizer, this);
        if (optimizer.canBeScheduled()) {
            boolean scheduledOptimizer = settings.getSettingAsBoolean(LuceneEnvironment.Optimizer.SCHEDULE, true);
            if (scheduledOptimizer) {
                optimizer = new LuceneSearchEngineScheduledOptimizer(optimizer, searchEngineFactory);
            }
        }
        optimizer.setSearchEngineFactory(searchEngineFactory);
        (searchEngineFactory).setOptimizer(optimizer);

        // wrap the index manager with a scheduled one and a transactional aspect
        LuceneSearchEngineIndexManager indexManager = searchEngineFactory.getLuceneIndexManager();
        indexManager = new TransactionalSearchEngineIndexManager(indexManager, this);
        indexManager = new LuceneScheduledSearchEngineIndexManager(indexManager);
        searchEngineFactory.setIndexManager(indexManager);

        firstLevelCacheFactory = new FirstLevelCacheFactory();
        firstLevelCacheFactory.configure(settings);

        indexManager.verifyIndex();
        indexManager.start();

        if (!duplicate) {
            searchEngineFactory.getOptimizer().start();
        }
    }

    public Compass clone(CompassSettings addedSettings) {
        CompassSettings copySettings = settings.copy();
        copySettings.addSettings(addedSettings);
        return new DefaultCompass(mapping, converterLookup, compassMetaData, propertyNamingStrategy, copySettings, true);
    }

    public CompassMapping getMapping() {
        return this.mapping;
    }

    public CompassSession openSession() {
        return openSession(true);
    }

    public CompassSession openSession(boolean allowCreate) {
        CompassSession session = transactionFactory.getTransactionBoundSession();

        if (session != null) {
            return session;
        }

        if (!allowCreate) {
            return null;
        }

        FirstLevelCache firstLevelCache = firstLevelCacheFactory.createFirstLevelCache();
        return new DefaultCompassSession(this, searchEngineFactory.openSearchEngine(), firstLevelCache);
    }

    public void close() {
        log.info("Closing Compass [" + name + "]");
        if (settings.getSettingAsBoolean(CompassEnvironment.Jndi.ENABLE, false)) {
            CompassObjectFactory.removeInstance(uuid, name, settings);
        }
        try {
            searchEngineFactory.getOptimizer().stop();
        } catch (IllegalStateException e) {
            // swallow, thats ok if it is
        }
        searchEngineFactory.close();
        log.info("Closed Compass [" + name + "]");
    }

    // from javax.naming.Referenceable
    public Reference getReference() throws NamingException {
        return new Reference(DefaultCompass.class.getName(), new StringRefAddr("uuid", uuid),
                CompassObjectFactory.class.getName(), null);
    }

    public CompassSettings getSettings() {
        return settings;
    }

    public SearchEngineOptimizer getSearchEngineOptimizer() {
        return searchEngineFactory.getOptimizer();
    }

    public SearchEngineIndexManager getSearchEngineIndexManager() {
        return searchEngineFactory.getIndexManager();
    }

    public SearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public CompassMetaData getMetaData() {
        return compassMetaData;
    }

    public TransactionFactory getTransactionFactory() {
        return transactionFactory;
    }

    public ConverterLookup getConverterLookup() {
        return converterLookup;
    }

    private void registerJndi() throws CompassException {
        if (!settings.getSettingAsBoolean(CompassEnvironment.Jndi.ENABLE, false)) {
            return;
        }
        // JNDI
        try {
            uuid = (String) UUID_GENERATOR.generate();
        } catch (Exception e) {
            throw new CompassException("Could not generate UUID for JNDI binding");
        }

        CompassObjectFactory.addInstance(uuid, name, this, settings);
    }

    private static class TransactionalSearchEngineIndexManager implements LuceneSearchEngineIndexManager {

        private LuceneSearchEngineIndexManager indexManager;

        private CompassTemplate template;

        private TransactionalSearchEngineIndexManager(LuceneSearchEngineIndexManager indexManager, Compass compass) {
            this.indexManager = indexManager;
            this.template = new CompassTemplate(compass);
        }

        public void start() {
            indexManager.start();
        }

        public void stop() {
            indexManager.stop();
        }

        public void close() {
            indexManager.close();
        }

        public void createIndex() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.createIndex();
                }
            });
        }

        public boolean verifyIndex() throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (indexManager.verifyIndex()) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public void deleteIndex() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.deleteIndex();
                }
            });
        }

        public boolean indexExists() throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (indexManager.indexExists()) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public void operate(final IndexOperationCallback callback) throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.operate(callback);
                }
            });
        }

        public void replaceIndex(final SearchEngineIndexManager innerIndexManager, final ReplaceIndexCallback callback) throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.replaceIndex(innerIndexManager, callback);
                }
            });
        }

        public boolean isCached(String subIndex) throws SearchEngineException {
            return indexManager.isCached(subIndex);
        }

        public boolean isCached() throws SearchEngineException {
            return indexManager.isCached();
        }

        public void clearCache(String subIndex) throws SearchEngineException {
            indexManager.clearCache(subIndex);
        }

        public void clearCache() throws SearchEngineException {
            indexManager.clearCache();
        }

        public void notifyAllToClearCache() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.notifyAllToClearCache();
                }
            });
        }

        public void checkAndClearIfNotifiedAllToClearCache() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.checkAndClearIfNotifiedAllToClearCache();
                }
            });
        }

        public boolean isIndexCompound() throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (indexManager.isIndexCompound()) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public boolean isIndexUnCompound() throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (indexManager.isIndexUnCompound()) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public void compoundIndex() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.compoundIndex();
                }
            });
        }

        public void unCompoundIndex() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    indexManager.unCompoundIndex();
                }
            });
        }

        // lucene search engine Index manager

        public LuceneSettings getSettings() {
            return indexManager.getSettings();
        }

        public LuceneSearchEngineStore getStore() {
            return indexManager.getStore();
        }

        public IndexWriter openIndexWriter(Directory dir, boolean create) throws IOException {
            return indexManager.openIndexWriter(dir, create);
        }

        public void closeIndexWriter(IndexWriter indexWriter, Directory dir) throws SearchEngineException {
            indexManager.closeIndexWriter(indexWriter, dir);
        }

        public LuceneIndexHolder openIndexHolderByAlias(String alias) throws SearchEngineException {
            return indexManager.openIndexHolderByAlias(alias);
        }

        public LuceneIndexHolder openIndexHolderBySubIndex(String subIndex) throws SearchEngineException {
            return indexManager.openIndexHolderBySubIndex(subIndex);
        }
    }

    public static class TransactionalSearchEngineOptimizer implements LuceneSearchEngineOptimizer {

        private LuceneSearchEngineOptimizer searchEngineOptimizer;

        private CompassTemplate template;

        public TransactionalSearchEngineOptimizer(LuceneSearchEngineOptimizer searchEngineOptimizer, Compass compass) {
            this.searchEngineOptimizer = searchEngineOptimizer;
            this.template = new CompassTemplate(compass);
        }

        public LuceneSearchEngineOptimizer getWrappedOptimizer() {
            return searchEngineOptimizer;
        }

        public void start() throws SearchEngineException {
            searchEngineOptimizer.start();
        }

        public void stop() throws SearchEngineException {
            searchEngineOptimizer.stop();
        }

        public boolean isRunning() {
            return searchEngineOptimizer.isRunning();
        }

        public boolean needOptimization() throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (searchEngineOptimizer.needOptimization()) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public void optimize() throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    searchEngineOptimizer.optimize();
                }
            });
        }

        public void setSearchEngineFactory(LuceneSearchEngineFactory searchEngineFactory) {
            searchEngineOptimizer.setSearchEngineFactory(searchEngineFactory);
        }

        public LuceneSearchEngineFactory getSearchEngineFactory() {
            return searchEngineOptimizer.getSearchEngineFactory();
        }

        public boolean canBeScheduled() {
            return searchEngineOptimizer.canBeScheduled();
        }

        public boolean needOptimizing(final String subIndex) throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (searchEngineOptimizer.needOptimizing(subIndex)) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public boolean needOptimizing(final String subIndex, final LuceneSubIndexInfo indexInfo) throws SearchEngineException {
            return ((Boolean) template.execute(new CompassCallback() {
                public Object doInCompass(CompassSession session) throws CompassException {
                    return (searchEngineOptimizer.needOptimizing(subIndex, indexInfo)) ? Boolean.TRUE : Boolean.FALSE;
                }
            })).booleanValue();
        }

        public void optimize(final String subIndex, final LuceneSubIndexInfo indexInfo) throws SearchEngineException {
            template.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    searchEngineOptimizer.optimize(subIndex, indexInfo);
                }
            });
        }
    }
}
