package org.compass.core.impl;

import java.util.ArrayList;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassIndexSession;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSearchSession;
import org.compass.core.CompassSession;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.engine.SearchEngineIndexManager;
import org.compass.core.engine.SearchEngineOptimizer;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.spellcheck.SearchEngineSpellCheckManager;
import org.compass.core.events.CompassEventManager;
import org.compass.core.events.RebuildEventListener;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.InternalCompass;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.transaction.TransactionFactory;

/**
 * A wrapper around an actual implemenation of {@link Compass} that allows to rebuild it
 * after changes that are perfomed on the {@link #getConfig()} configuration.
 *
 * @author kimchy
 */
public class RefreshableCompass implements InternalCompass {

    private static final Log logger = LogFactory.getLog(RefreshableCompass.class);

    private final CompassConfiguration config;

    private volatile InternalCompass compass;

    private List<RebuildEventListener> rebuildEventListeners = new ArrayList<RebuildEventListener>();

    public RefreshableCompass(CompassConfiguration config, InternalCompass compass) {
        this.config = config;
        this.compass = compass;
    }

    public CompassConfiguration getConfig() {
        return config;
    }

    public synchronized void rebuild() throws CompassException {
        compass.stop();
        config.getSettings().addSettings(compass.getSettings());
        InternalCompass rebuiltCompass;
        try {
            rebuiltCompass = (InternalCompass) config.buildCompass();
        } catch (RuntimeException e) {
            compass.start();
            throw e;
        }
        // do the switch
        Compass oldCompass = compass;
        compass = rebuiltCompass;

        oldCompass.getSearchEngineIndexManager().clearCache();

        long sleepBeforeClose = getConfig().getSettings().getSettingAsTimeInMillis(CompassEnvironment.Rebuild.SLEEP_BEFORE_CLOSE, CompassEnvironment.Rebuild.DEFAULT_SLEEP_BEFORE_CLOSE);        // schedule Compass to be closed
        if (sleepBeforeClose <= 0) {
            oldCompass.close();
        } else {
            Thread t = new Thread(new CloseCompassRunnable(oldCompass, sleepBeforeClose), "Close Compass");
            t.start();
        }

        for (RebuildEventListener eventListener : rebuildEventListeners) {
            eventListener.onCompassRebuild(compass);
        }
    }

    public Compass clone(CompassSettings addedSettings) {
        InternalCompass clonedCompass = (InternalCompass) compass.clone(addedSettings);
        return new RefreshableCompass(config, clonedCompass);
    }

    public synchronized void addRebuildEventListener(RebuildEventListener eventListener) {
        rebuildEventListeners.add(eventListener);
    }

    public synchronized void removeRebuildEventListener(RebuildEventListener eventListener) {
        rebuildEventListeners.remove(eventListener);
    }

    // Delegate Methods

    public void start() {
        compass.start();
    }

    public void stop() {
        compass.stop();
    }

    public CompassSession openSession(boolean allowCreate, boolean checkClosed) {
        return compass.openSession(allowCreate, checkClosed);
    }

    public CompassSession openSession() throws CompassException {
        return compass.openSession();
    }

    public CompassSearchSession openSearchSession() throws CompassException {
        return compass.openSearchSession();
    }

    public CompassIndexSession openIndexSession() throws CompassException {
        return compass.openIndexSession();
    }

    public String getName() {
        return compass.getName();
    }

    public CompassSettings getSettings() {
        return compass.getSettings();
    }

    public CompassMapping getMapping() {
        return compass.getMapping();
    }

    public ExecutorManager getExecutorManager() {
        return compass.getExecutorManager();
    }

    public CompassMetaData getMetaData() {
        return compass.getMetaData();
    }

    public SearchEngineFactory getSearchEngineFactory() {
        return compass.getSearchEngineFactory();
    }

    public TransactionFactory getTransactionFactory() {
        return compass.getTransactionFactory();
    }

    public LocalTransactionFactory getLocalTransactionFactory() {
        return compass.getLocalTransactionFactory();
    }

    public ConverterLookup getConverterLookup() {
        return compass.getConverterLookup();
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return compass.getPropertyNamingStrategy();
    }

    public CompassEventManager getEventManager() {
        return compass.getEventManager();
    }

    public void close() throws CompassException {
        compass.close();
    }

    public CompassQueryBuilder queryBuilder() throws CompassException {
        return compass.queryBuilder();
    }

    public CompassQueryFilterBuilder queryFilterBuilder() throws CompassException {
        return compass.queryFilterBuilder();
    }

    public ResourceFactory getResourceFactory() {
        return compass.getResourceFactory();
    }

    public SearchEngineOptimizer getSearchEngineOptimizer() {
        return compass.getSearchEngineOptimizer();
    }

    public SearchEngineIndexManager getSearchEngineIndexManager() {
        return compass.getSearchEngineIndexManager();
    }

    public SearchEngineSpellCheckManager getSpellCheckManager() {
        return compass.getSpellCheckManager();
    }

    public boolean isClosed() {
        return compass.isClosed();
    }

    public Reference getReference() throws NamingException {
        return compass.getReference();
    }

    private class CloseCompassRunnable implements Runnable {

        private final Compass compass;

        private final long timeout;

        private CloseCompassRunnable(Compass compass, long timeout) {
            this.compass = compass;
            this.timeout = timeout;
        }

        public void run() {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                // do nothing
            }
            try {
                compass.close();
            } catch (Exception e) {
                logger.error("Failed to close original Compass after rebuild", e);
            }
        }
    }
}
