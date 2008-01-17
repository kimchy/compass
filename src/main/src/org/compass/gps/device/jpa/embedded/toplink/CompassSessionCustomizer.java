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

package org.compass.gps.device.jpa.embedded.toplink;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

import oracle.toplink.essentials.descriptors.ClassDescriptor;
import oracle.toplink.essentials.ejb.cmp3.EntityManagerFactoryProvider;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerFactoryImpl;
import oracle.toplink.essentials.internal.ejb.cmp3.EntityManagerSetupImpl;
import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.threetier.ServerSession;
import oracle.toplink.essentials.tools.sessionconfiguration.SessionCustomizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.transaction.JTASyncTransactionFactory;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.lifecycle.TopLinkEssentialsJpaEntityLifecycleInjector;

/**
 * @author kimchy
 */
public class CompassSessionCustomizer implements SessionCustomizer {

    private static final Log log = LogFactory.getLog(CompassSessionCustomizer.class);

    private static final String COMPASS_PREFIX = "compass";

    private static final String COMPASS_GPS_INDEX_PREFIX = "gps.index.";


    public static final String REINDEX_ON_STARTUP = "compass.toplink.reindexOnStartup";

    public static final String REGISTER_REMOTE_COMMIT_LISTENER = "compass.toplink.registerRemoteCommitListener";

    public static final String INDEX_QUERY_PREFIX = "compass.toplink.indexQuery.";

    public static final String COMPASS_CONFIG_LOCATION = "compass.toplink.config";

    public static final String COMPASS_SESSION_CUSTOMIZER = "compass.toplink.session.customizer";

    public void customize(Session session) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Compass embedded TopLink Essentials support enabled, initializing for session [" + session + "]");
        }
        PersistenceUnitInfo persistenceUnitInfo = findPersistenceUnitInfo(session);
        if (persistenceUnitInfo == null) {
            throw new CompassException("Failed to find Persistence Unit Info");
        }

        Map<Object, Object> toplinkProps = new HashMap();
        toplinkProps.putAll(persistenceUnitInfo.getProperties());
        toplinkProps.putAll(session.getProperties());

        String sessionCustomizer = (String) toplinkProps.get(COMPASS_SESSION_CUSTOMIZER);
        if (sessionCustomizer != null) {
            ((SessionCustomizer) ClassUtils.forName(sessionCustomizer, persistenceUnitInfo.getClassLoader()).newInstance()).customize(session);
        }

        Properties compassProperties = new Properties();
        //noinspection unchecked
        for (Map.Entry entry : toplinkProps.entrySet()) {
            if (!(entry.getKey() instanceof String)) {
                continue;
            }
            String key = (String) entry.getKey();
            if (key.startsWith(COMPASS_PREFIX)) {
                compassProperties.put(entry.getKey(), entry.getValue());
            }
            if (key.startsWith(COMPASS_GPS_INDEX_PREFIX)) {
                compassProperties.put(entry.getKey(), entry.getValue());
            }
        }
        if (compassProperties.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No Compass properties found in configuraiton, disabling Compass");
            }
            return;
        }

        CompassConfiguration compassConfiguration = CompassConfigurationFactory.newConfiguration();
        // use the same class loader of the persistence info to load Compass classes
        compassConfiguration.setClassLoader(persistenceUnitInfo.getClassLoader());
        CompassSettings settings = compassConfiguration.getSettings();
        settings.addSettings(compassProperties);

        String configLocation = (String) compassProperties.get(COMPASS_CONFIG_LOCATION);
        if (configLocation != null) {
            compassConfiguration.configure(configLocation);
        }

        Map descriptors = session.getDescriptors();
        for (Object o : descriptors.values()) {
            ClassDescriptor classDescriptor = (ClassDescriptor) o;
            Class mappedClass = classDescriptor.getJavaClass();
            compassConfiguration.tryAddClass(mappedClass);
        }

        // create some default settings

        String transactionFactory = (String) compassProperties.get(CompassEnvironment.Transaction.FACTORY);
        boolean toplinkControlledTransaction;
        if (transactionFactory == null) {
            if (persistenceUnitInfo.getTransactionType() == PersistenceUnitTransactionType.JTA) {
                transactionFactory = JTASyncTransactionFactory.class.getName();
                toplinkControlledTransaction = false;
            } else {
                transactionFactory = LocalTransactionFactory.class.getName();
                toplinkControlledTransaction = true;
            }
            settings.setSetting(CompassEnvironment.Transaction.FACTORY, transactionFactory);
        } else {
            // JPA is not controlling the transaction (using JTA Sync or XA), don't commit/rollback
            // with Toplink transaction listeners
            toplinkControlledTransaction = false;
        }

        // if the settings is configured to use local transaciton, disable thread bound setting since
        // we are using Toplink to managed transaction scope (using user objects on the em) and not thread locals
        // will only be taken into account when using local transactions
        if (settings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
            // if no emf is defined
            settings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, true);
        }

        Compass compass = compassConfiguration.buildCompass();

        boolean commitBeforeCompletion = settings.getSettingAsBoolean(CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, false);

        // extract index properties so they will be used
        Properties indexProps = new Properties();
        for (Map.Entry entry : compassProperties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(COMPASS_GPS_INDEX_PREFIX)) {
                indexProps.put(key.substring(COMPASS_GPS_INDEX_PREFIX.length()), entry.getValue());
            }
        }

        // start an internal JPA device and Gps for mirroring
        EntityManagerFactory emf = new EntityManagerFactoryImpl((ServerSession) session);

        JpaGpsDevice jpaGpsDevice = new JpaGpsDevice(DefaultJpaCompassGps.JPA_DEVICE_NAME, emf);
        jpaGpsDevice.setMirrorDataChanges(true);
        jpaGpsDevice.setInjectEntityLifecycleListener(true);
        for (Map.Entry entry : compassProperties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(INDEX_QUERY_PREFIX)) {
                String entityName = key.substring(INDEX_QUERY_PREFIX.length());
                String selectQuery = (String) entry.getValue();
                jpaGpsDevice.setIndexSelectQuery(entityName, selectQuery);
            }
        }

        TopLinkEssentialsJpaEntityLifecycleInjector lifecycleInjector = new TopLinkEssentialsJpaEntityLifecycleInjector();
        lifecycleInjector.setEventListener(new EmbeddedToplinkEventListener(jpaGpsDevice));
        jpaGpsDevice.setLifecycleInjector(lifecycleInjector);

        DefaultJpaCompassGps jpaCompassGps = new DefaultJpaCompassGps();
        jpaCompassGps.setCompass(compass);
        jpaCompassGps.addGpsDevice(jpaGpsDevice);

        // before we start the Gps, open and close a broker
        emf.createEntityManager().close();

        jpaCompassGps.start();

        String reindexOnStartup = (String) compassProperties.get(REINDEX_ON_STARTUP);
        if ("true".equalsIgnoreCase(reindexOnStartup)) {
            log.info("Indexing the database on startup...");
            jpaCompassGps.index();
            log.info("Finished the database on startup");
        }

        session.getEventManager().addListener(new CompassSessionEventListener(compass, jpaCompassGps,
                commitBeforeCompletion, toplinkControlledTransaction, indexProps));

        if (log.isDebugEnabled()) {
            log.debug("Compass embedded TopLink Essentials support active");
        }
    }

    protected PersistenceUnitInfo findPersistenceUnitInfo(Session session) {
        String sessionName = session.getName();
        int index = sessionName.indexOf('-');
        while (index != -1) {
            String urlAndName = sessionName.substring(0, index) + sessionName.substring(index + 1);
            if (log.isDebugEnabled()) {
                log.debug("Trying to find PersistenceInfo using [" + urlAndName + "]");
            }
            EntityManagerSetupImpl emSetup = EntityManagerFactoryProvider.getEntityManagerSetupImpl(urlAndName);
            if (emSetup != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found PersistenceInfo using [" + urlAndName + "]");
                }
                return emSetup.getPersistenceUnitInfo();
            }
            index = sessionName.indexOf('-', index + 1);
        }
        return null;
    }
}
