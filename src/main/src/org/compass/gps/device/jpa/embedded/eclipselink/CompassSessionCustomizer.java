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

package org.compass.gps.device.jpa.embedded.eclipselink;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;

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
import org.compass.gps.device.jpa.JtaEntityManagerWrapper;
import org.compass.gps.device.jpa.ResourceLocalEntityManagerWrapper;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.lifecycle.EclipseLinkJpaEntityLifecycleInjector;
import org.eclipse.persistence.Version;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider;
import org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.server.ServerSession;

/**
 * A EclipseLink <code>SessionCustomizer</code> allowing to integrate in an "embedded" mode
 * Compass with EclipseLink. The single required setting (for example, within the <code>persistence.xml</code>
 * file) is the Compass connection property ({@link org.compass.core.config.CompassEnvironment#CONNECTION}
 * and at least one Searchable class mapped out of the classes mapped in EclipseLink.
 *
 * <p>The embedded EclipseLink support uses Compass GPS and adds an "embedded" Compass, or adds a searchable
 * feature to EclipseLink by registering a {@link org.compass.core.Compass} instance and a {@link org.compass.gps.device.jpa.JpaGpsDevice}
 * instance with EclipseLink. It registers mirroring listeners (after delete/store/persist) to automatically
 * mirror changes done through EclipseLink to the Compass index. It also registeres an event listener
 * ({@link org.compass.gps.device.hibernate.embedded.CompassEventListener} to syncronize with transactions.
 *
 * <p>Use {@link EclipseLinkHelper} in order to access the <code>Compass</code> instance or the
 * <code>JpaGpsDevice</code> instance attached to a given entity manager.
 *
 * <p>The Compass instnace used for mirroring can be configured by adding <code>compass</code> prefixed settings.
 * Additional settings that only control the Compass instnace created for indexing should be set using
 * <code>gps.index.compass.</code>. For more information on indexing and mirroring Compass please check
 * {@link org.compass.gps.impl.SingleCompassGps}.
 *
 * <p>This customizer tries to find the persistence info in order to read the properties out of it. In order
 * for it to find it, it uses the naming convention EclipseLink has at naming Sessions. Note, if you change the
 * name of the Session using EclipseLink setting, this customizer will not be able to operate.
 *
 * <p>This session customizer will also identify if the persistence info is configured to work with JTA or
 * with RESOURCE LOCAL transaction and adjust itsefl accordingly. If JTA is used, it will automatically
 * use Compass {@link org.compass.core.transaction.JTASyncTransactionFactory} and if RESOURCE LOCAL is used it will automatically use
 * {@link org.compass.core.transaction.LocalTransactionFactory}. Note, this is only set if the transaction factory is not explicitly set
 * using Compass settings.
 *
 * <p>Specific properties that this plugin can use:
 * <ul>
 * <li>compass.EclipseLink.indexQuery.[entity name/class]: Specific select query that will be used to perform the indexing
 * for the mentioned specific entity name / class. Note, before calling {@link org.compass.gps.CompassGps#index()} there
 * is an option the programmatically control this.</li>
 * <li>compass.EclipseLink.config: A classpath that points to Compass configuration.</li>
 * <li>compass.EclipseLink.session.customizer: If there is another EclipseLink <code>SessionCustomizer</code> that needs
 * to be applied, its class FQN should be specified with this setting.</li>
 * </ul>
 *
 * @author kimchy
 */
public class CompassSessionCustomizer implements SessionCustomizer {

    private static final Log log = LogFactory.getLog(CompassSessionCustomizer.class);

    private static final String COMPASS_PREFIX = "compass";

    private static final String COMPASS_GPS_INDEX_PREFIX = "gps.index.";

    public static final String INDEX_QUERY_PREFIX = "compass.eclipselink.indexQuery.";

    public static final String COMPASS_CONFIG_LOCATION = "compass.eclipselink.config";

    public static final String COMPASS_SESSION_CUSTOMIZER = "compass.eclipselink.session.customizer";

    public void customize(Session session) throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Compass embedded EclipseLink support enabled, initializing for session [" + session + "]");
        }
        PersistenceUnitInfo persistenceUnitInfo = findPersistenceUnitInfo(session);
        if (persistenceUnitInfo == null) {
            throw new CompassException("Failed to find Persistence Unit Info");
        }

        Map<Object, Object> eclipselinkProps = new HashMap();
        eclipselinkProps.putAll(persistenceUnitInfo.getProperties());
        eclipselinkProps.putAll(session.getProperties());

        String sessionCustomizer = (String) eclipselinkProps.get(COMPASS_SESSION_CUSTOMIZER);
        if (sessionCustomizer != null) {
            ((SessionCustomizer) ClassUtils.forName(sessionCustomizer, persistenceUnitInfo.getClassLoader()).newInstance()).customize(session);
        }

        Properties compassProperties = new Properties();
        //noinspection unchecked
        for (Map.Entry entry : eclipselinkProps.entrySet()) {
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
        if (compassProperties.getProperty(CompassEnvironment.CONNECTION) == null) {
            if (log.isDebugEnabled()) {
                log.debug("No Compass [" + CompassEnvironment.CONNECTION + "] property defined, disabling Compass");
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
        boolean eclipselinkControlledTransaction;
        if (transactionFactory == null) {
            if (persistenceUnitInfo.getTransactionType() == PersistenceUnitTransactionType.JTA) {
                transactionFactory = JTASyncTransactionFactory.class.getName();
                eclipselinkControlledTransaction = false;
            } else {
                transactionFactory = LocalTransactionFactory.class.getName();
                eclipselinkControlledTransaction = true;
            }
            settings.setSetting(CompassEnvironment.Transaction.FACTORY, transactionFactory);
        } else {
            // JPA is not controlling the transaction (using JTA Sync or XA), don't commit/rollback
            // with EclipseLink transaction listeners
            eclipselinkControlledTransaction = false;
        }

        // if the settings is configured to use local transaciton, disable thread bound setting since
        // we are using EclipseLink to managed transaction scope (using user objects on the em) and not thread locals
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

        EclipseLinkJpaEntityLifecycleInjector lifecycleInjector = new EclipseLinkJpaEntityLifecycleInjector();
        lifecycleInjector.setEventListener(new EclipseLinkEventListener(jpaGpsDevice));
        jpaGpsDevice.setLifecycleInjector(lifecycleInjector);

        // set explicitly the EntityManagerWrapper since EclipseLink rollback the transaction on EntityManager#getTransaction
        // which makes it useless when using DefaultEntityManagerWrapper
        if (persistenceUnitInfo.getTransactionType() == PersistenceUnitTransactionType.JTA) {
            jpaGpsDevice.setEntityManagerWrapper(new JtaEntityManagerWrapper());
        } else {
            jpaGpsDevice.setEntityManagerWrapper(new ResourceLocalEntityManagerWrapper());
        }

        DefaultJpaCompassGps jpaCompassGps = new DefaultJpaCompassGps();
        jpaCompassGps.setCompass(compass);
        jpaCompassGps.addGpsDevice(jpaGpsDevice);

        // before we start the Gps, open and close a broker
        emf.createEntityManager().close();

        jpaCompassGps.start();

        session.getEventManager().addListener(new CompassSessionEventListener(compass, jpaCompassGps,
                commitBeforeCompletion, eclipselinkControlledTransaction, indexProps));

        if (log.isDebugEnabled()) {
            log.debug("Compass embedded EclipseLink support active");
        }
    }

    protected PersistenceUnitInfo findPersistenceUnitInfo(Session session) {
        String sessionName = session.getName();
        if (Version.getVersion().compareTo("2.3.0") <= 0) {
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
        } else {
            EntityManagerSetupImpl emSetup = EntityManagerFactoryProvider
                    .getEntityManagerSetupImpl(sessionName);
            if (emSetup != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found PersistenceInfo using [" + sessionName + "]");
                }
                return emSetup.getPersistenceUnitInfo();
            }
        }
        return null;
    }
}
