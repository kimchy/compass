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

package org.compass.core.impl;

import java.lang.ref.WeakReference;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.ResourceFactory;
import org.compass.core.cache.first.FirstLevelCache;
import org.compass.core.cache.first.FirstLevelCacheFactory;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.engine.spi.InternalSearchEngineFactory;
import org.compass.core.events.CompassEventManager;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.id.IdentifierGenerator;
import org.compass.core.id.UUIDGenerator;
import org.compass.core.jndi.CompassObjectFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.transaction.TransactionException;
import org.compass.core.transaction.TransactionFactory;
import org.compass.core.transaction.TransactionFactoryFactory;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.transaction.context.TransactionContextCallback;
import org.compass.core.transaction.context.TransactionContextCallbackWithTr;

/**
 * @author kimchy
 */
public class DefaultCompass implements InternalCompass {

    private static final Log logger = LogFactory.getLog(DefaultCompass.class);

    private static final long serialVersionUID = 3256446884762891059L;

    private static final IdentifierGenerator UUID_GENERATOR = new UUIDGenerator();

    private final String name;

    private String uuid;

    private final CompassMapping mapping;

    private final InternalSearchEngineFactory searchEngineFactory;

    private final TransactionFactory transactionFactory;

    private final LocalTransactionFactory localTransactionFactory;

    private final ConverterLookup converterLookup;

    private final CompassMetaData compassMetaData;

    private final PropertyNamingStrategy propertyNamingStrategy;

    private final ExecutorManager executorManager;

    private final CompassEventManager eventManager;

    protected final CompassSettings settings;

    private final FirstLevelCacheFactory firstLevelCacheFactory;

    private volatile ShutdownThread shutdownThread;

    private final boolean duplicate;

    private volatile boolean closed = false;

    private final boolean debug;

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, ExecutorManager executorManager,
                          CompassSettings settings) throws CompassException {
        this(mapping, converterLookup, compassMetaData, propertyNamingStrategy, executorManager, settings, false);
    }

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, ExecutorManager executorManager,
                          CompassSettings settings, boolean duplicate) throws CompassException {

        this(mapping, converterLookup, compassMetaData, propertyNamingStrategy, executorManager, settings, duplicate,
                new LuceneSearchEngineFactory(propertyNamingStrategy, settings, mapping, executorManager));
    }

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, CompassSettings settings,
                          LuceneSearchEngineFactory searchEngineFactory) throws CompassException {
        this(mapping, converterLookup, compassMetaData, propertyNamingStrategy, searchEngineFactory.getExecutorManager(),
                settings, false, searchEngineFactory);
    }

    public DefaultCompass(CompassMapping mapping, ConverterLookup converterLookup, CompassMetaData compassMetaData,
                          PropertyNamingStrategy propertyNamingStrategy, ExecutorManager executorManager,
                          CompassSettings settings, boolean duplicate, LuceneSearchEngineFactory searchEngineFactory) throws CompassException {

        this.mapping = mapping;
        this.converterLookup = converterLookup;
        this.compassMetaData = compassMetaData;
        this.propertyNamingStrategy = propertyNamingStrategy;
        this.executorManager = executorManager;
        this.name = settings.getSetting(CompassEnvironment.NAME, "default");
        this.settings = settings;
        this.duplicate = duplicate;

        this.eventManager = new CompassEventManager(this, mapping);
        eventManager.configure(settings);

        if (!duplicate) {
            registerJndi();
        }

        searchEngineFactory.setTransactionContext(new CompassTransactionContext(this));
        this.searchEngineFactory = searchEngineFactory;

        // build the transaction factory
        transactionFactory = TransactionFactoryFactory.createTransactionFactory(this, settings);
        localTransactionFactory = TransactionFactoryFactory.createLocalTransactionFactory(this, settings);

        firstLevelCacheFactory = new FirstLevelCacheFactory();
        firstLevelCacheFactory.configure(settings);

        searchEngineFactory.getIndexManager().verifyIndex();

        if (!duplicate) {
            start();
        }

        if (settings.getSettingAsBoolean(CompassEnvironment.REGISTER_SHUTDOWN_HOOK, true)) {
            shutdownThread = new ShutdownThread(this);
            if (logger.isDebugEnabled()) {
                logger.debug("Registering shutdown hook [" + System.identityHashCode(shutdownThread) + "]");
            }
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        this.debug = settings.getSettingAsBoolean(CompassEnvironment.DEBUG, false);
    }

    public CompassConfiguration getConfig() {
        throw new UnsupportedOperationException();
    }

    public void rebuild() {
        throw new UnsupportedOperationException();
    }

    public void addRebuildEventListener(RebuildEventListener eventListener) {
        throw new UnsupportedOperationException();
    }

    public void removeRebuildEventListener(RebuildEventListener eventListener) {
        throw new UnsupportedOperationException();
    }

    public Compass clone(CompassSettings addedSettings) {
        CompassSettings copySettings = settings.copy();
        copySettings.addSettings(addedSettings);
        return new DefaultCompass(mapping, converterLookup, compassMetaData, propertyNamingStrategy, executorManager, copySettings, true);
    }

    public String getName() {
        return this.name;
    }

    public CompassQueryBuilder queryBuilder() throws CompassException {
        return new DefaultCompassQueryBuilder(searchEngineFactory.queryBuilder(), this);
    }

    public CompassQueryFilterBuilder queryFilterBuilder() throws CompassException {
        return new DefaultCompassQueryFilterBuilder(searchEngineFactory.queryFilterBuilder(), this);
    }

    public ResourceFactory getResourceFactory() {
        return searchEngineFactory.getResourceFactory();
    }

    public CompassMapping getMapping() {
        return this.mapping;
    }

    public ExecutorManager getExecutorManager() {
        return executorManager;
    }

    public CompassEventManager getEventManager() {
        return this.eventManager;
    }

    public CompassSearchSession openSearchSession() {
        CompassSession session = openSession();
        session.setReadOnly();
        return session;
    }

    public CompassIndexSession openIndexSession() {
        CompassSession session = openSession();
        session.getSettings().setSetting(LuceneEnvironment.Transaction.Processor.TYPE, LuceneEnvironment.Transaction.Processor.Lucene.NAME);
        return session;
    }

    public CompassSession openSession() {
        return openSession(true);
    }

    public CompassSession openSession(boolean allowCreate) {
        return openSession(allowCreate, true);
    }

    public CompassSession openSession(boolean allowCreate, boolean checkClosed) {
        if (checkClosed) {
            checkClosed();
        }
        CompassSession session = transactionFactory.getTransactionBoundSession();

        if (session != null) {
            return new ExistingCompassSession((InternalCompassSession) session);
        }

        if (!allowCreate) {
            return null;
        }

        FirstLevelCache firstLevelCache = firstLevelCacheFactory.createFirstLevelCache();
        RuntimeCompassSettings runtimeSettings = new RuntimeCompassSettings(getSettings());
        return new DefaultCompassSession(runtimeSettings, this, searchEngineFactory.openSearchEngine(runtimeSettings), firstLevelCache);
    }

    public void start() {
        searchEngineFactory.start();
    }

    public void stop() {
        searchEngineFactory.stop();
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        logger.info("Closing Compass [" + name + "]");

        if (shutdownThread != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing shutdown hook");
            }
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
            } catch (IllegalStateException e) {
                // ignore, we are shuttng down
            }
        }

        if (settings.getSettingAsBoolean(CompassEnvironment.Jndi.ENABLE, false) && !duplicate) {
            CompassObjectFactory.removeInstance(uuid, name, settings);
        }
        searchEngineFactory.close();

        if (!duplicate) {
            executorManager.close();
        }

        logger.info("Closed Compass [" + name + "]");
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

    public SearchEngineSpellCheckManager getSpellCheckManager() {
        return searchEngineFactory.getSpellCheckManager();
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

    protected void finalize() throws Throwable {
        super.finalize();
        close();
    }

    public void clearShutdownHook() {
        shutdownThread = null;
    }

    /**
     * A shutdown hook that closes Compass.
     */
    private static class ShutdownThread extends Thread {

        private final WeakReference<Compass> compass;

        public ShutdownThread(Compass compass) {
            this.compass = new WeakReference<Compass>(compass);
        }

        @Override
        public void run() {
            Compass compass = this.compass.get();
            if (compass != null) {
                ((DefaultCompass) compass).clearShutdownHook();
                if (!compass.isClosed()) {
                    compass.close();
                }
            }
        }
    }

    private static class CompassTransactionContext implements TransactionContext {

        private InternalCompass compass;

        public CompassTransactionContext(InternalCompass compass) {
            this.compass = compass;
        }

        public <T> T execute(TransactionContextCallback<T> callback) throws TransactionException {
            // if marked as not requiring transaction context, just execute without starting a transaction.
            if (!compass.getSearchEngineIndexManager().requiresAsyncTransactionalContext()) {
                return callback.doInTransaction();
            }

            CompassSession session = compass.openSession(true, false);
            CompassTransaction tx = null;
            try {
                tx = session.beginTransaction();
                T result = callback.doInTransaction();
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        logger.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw e;
            } catch (Error err) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        logger.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw err;
            } finally {
                session.close();
            }
        }

        public <T> T execute(TransactionContextCallbackWithTr<T> callback) throws TransactionException {
            CompassSession session = compass.openSession(true, false);
            CompassTransaction tx = null;
            try {
                tx = session.beginTransaction();
                T result = callback.doInTransaction((InternalCompassTransaction) tx);
                tx.commit();
                return result;
            } catch (RuntimeException e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        logger.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw e;
            } catch (Error err) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e1) {
                        logger.error("Failed to rollback transaction, ignoring", e1);
                    }
                }
                throw err;
            } finally {
                session.close();
            }
        }
    }
}
