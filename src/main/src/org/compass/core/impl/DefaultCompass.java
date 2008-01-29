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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.cache.first.FirstLevelCache;
import org.compass.core.cache.first.FirstLevelCacheFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.id.IdentifierGenerator;
import org.compass.core.id.UUIDGenerator;
import org.compass.core.jndi.CompassObjectFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.manager.ScheduledLuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.optimizer.LuceneSearchEngineOptimizer;
import org.compass.core.lucene.engine.optimizer.ScheduledLuceneSearchEngineOptimizer;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.InternalCompass;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.transaction.TransactionException;
import org.compass.core.transaction.TransactionFactory;
import org.compass.core.transaction.TransactionFactoryFactory;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.transaction.context.TransactionContextCallback;

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

    private LocalTransactionFactory localTransactionFactory;

    private ConverterLookup converterLookup;

    private CompassMetaData compassMetaData;

    private PropertyNamingStrategy propertyNamingStrategy;

    protected CompassSettings settings;

    private FirstLevelCacheFactory firstLevelCacheFactory;

    private volatile boolean closed = false;

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

        searchEngineFactory.setTransactionContext(new CompassTransactionContext(this));
        this.searchEngineFactory = searchEngineFactory;

        // build the transaction factory
        transactionFactory = TransactionFactoryFactory.createTransactionFactory(this, settings);
        localTransactionFactory = TransactionFactoryFactory.createLocalTransactionFactory(this, settings);

        // wrap optimizer with transaction, and create scheduled one
        LuceneSearchEngineOptimizer optimizer = (LuceneSearchEngineOptimizer) searchEngineFactory.getOptimizer();
        if (optimizer.canBeScheduled()) {
            boolean scheduledOptimizer = settings.getSettingAsBoolean(LuceneEnvironment.Optimizer.SCHEDULE, true);
            if (scheduledOptimizer) {
                optimizer = new ScheduledLuceneSearchEngineOptimizer(optimizer);
            }
        }
        optimizer.setSearchEngineFactory(searchEngineFactory);
        searchEngineFactory.setOptimizer(optimizer);

        // wrap the index manager with a scheduled one and a transactional aspect
        LuceneSearchEngineIndexManager indexManager = searchEngineFactory.getLuceneIndexManager();
        indexManager = new ScheduledLuceneSearchEngineIndexManager(indexManager);
        searchEngineFactory.setIndexManager(indexManager);

        firstLevelCacheFactory = new FirstLevelCacheFactory();
        firstLevelCacheFactory.configure(settings);

        indexManager.verifyIndex();

        if (!duplicate) {
            indexManager.start();
            searchEngineFactory.getOptimizer().start();
        }
    }

    public Compass clone(CompassSettings addedSettings) {
        CompassSettings copySettings = settings.copy();
        copySettings.addSettings(addedSettings);
        return new DefaultCompass(mapping, converterLookup, compassMetaData, propertyNamingStrategy, copySettings, true);
    }

    public String getName() {
        return this.name;
    }

    public CompassMapping getMapping() {
        checkClosed();
        return this.mapping;
    }

    public CompassSession openSession() {
        return openSession(true);
    }

    public CompassSession openSession(boolean allowCreate) {
        checkClosed();
        CompassSession session = transactionFactory.getTransactionBoundSession();

        if (session != null) {
            return session;
        }

        if (!allowCreate) {
            return null;
        }

        FirstLevelCache firstLevelCache = firstLevelCacheFactory.createFirstLevelCache();
        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(getSettings());
        return new DefaultCompassSession(runtimeSettings, this, searchEngineFactory.openSearchEngine(runtimeSettings), firstLevelCache);
    }

    public void close() {
        if (closed) {
            return;
        }
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

    public boolean isClosed() {
        return this.closed;
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

    public LocalTransactionFactory getLocalTransactionFactory() {
        return this.localTransactionFactory;
    }

    public ConverterLookup getConverterLookup() {
        return converterLookup;
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return propertyNamingStrategy;
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

    private void checkClosed() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("Compass already closed");
        }
    }

    private static class CompassTransactionContext implements TransactionContext {

        private Compass compass;

        public CompassTransactionContext(Compass compass) {
            this.compass = compass;
        }

        public <T> T execute(TransactionContextCallback<T> callback) throws TransactionException {
            CompassSession session = compass.openSession();
            CompassTransaction tx = null;
            try {
                tx = session.beginTransaction();
                T result = callback.doInTransaction(tx);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        log.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw e;
            } catch (Error err) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        log.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw err;
            } finally {
                session.close();
            }
        }
    }
}
