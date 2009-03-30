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

package org.compass.gps.device.jpa.embedded.openjpa;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;
import javax.persistence.EntityNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.conf.OpenJPAConfiguration;
import org.apache.openjpa.conf.OpenJPAConfigurationImpl;
import org.apache.openjpa.conf.OpenJPAVersion;
import org.apache.openjpa.event.BeginTransactionListener;
import org.apache.openjpa.event.BrokerFactoryEvent;
import org.apache.openjpa.event.BrokerFactoryListener;
import org.apache.openjpa.event.EndTransactionListener;
import org.apache.openjpa.event.RemoteCommitEvent;
import org.apache.openjpa.event.RemoteCommitListener;
import org.apache.openjpa.event.TransactionEvent;
import org.apache.openjpa.kernel.Broker;
import org.apache.openjpa.kernel.BrokerFactory;
import org.apache.openjpa.lib.conf.AbstractProductDerivation;
import org.apache.openjpa.lib.conf.Configuration;
import org.apache.openjpa.lib.conf.ConfigurationProvider;
import org.apache.openjpa.persistence.EntityManagerFactoryImpl;
import org.apache.openjpa.persistence.Extent;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAEntityManagerFactory;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.util.OpenJPAId;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.transaction.JTASyncTransactionFactory;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.embedded.DefaultJpaCompassGps;
import org.compass.gps.device.jpa.lifecycle.OpenJPAJpaEntityLifecycleInjector;

/**
 * An OpenJPA Compass product derivation. Simply by adding the Compass jar to the class path this product
 * derivation will be registered with OpenJPA. This derivation will not add Compass support to OpenJPA if
 * no Compass setting is set. The single required setting (for example, within the persistence.xml) is
 * Compass engine connection setting ({@link org.compass.core.config.CompassEnvironment#CONNECTION}.
 *
 * <p>The embedded Open JPA support uses Compass GPS and adds an "embedded" Compass, or adds a searchable
 * feature to Open JPA by registering a {@link org.compass.core.Compass} instance and a {@link org.compass.gps.device.jpa.JpaGpsDevice}
 * instance with OpenJPA. It registers mirroring listeners (after delete/store/persist) to automatically
 * mirror changes done through OpenJPA to the Compass index. It also registeres transaction listeners
 * to synchronize between Compass transactions and Open JPA transactions.
 *
 * <p>Use {@link org.compass.gps.device.jpa.embedded.openjpa.OpenJPAHelper} in order to access the registered
 * {@link org.compass.core.Compass} instnace and {@link org.compass.gps.device.jpa.JpaGpsDevice} instance assigned
 * to the {@link javax.persistence.EntityManagerFactory}. Also use it to get {@link org.compass.core.CompassSession}
 * assigned to an {@link javax.persistence.EntityManager}.
 *
 * <p>The Compass instnace used for mirroring can be configured by adding <code>compass</code> prefixed settings.
 * Additional settings that only control the Compass instnace created for indexing should be set using
 * <code>gps.index.compass.</code>. For more information on indexing and mirroring Compass please check
 * {@link org.compass.gps.impl.SingleCompassGps}.
 *
 * <p>Specific properties that this plugin can use:
 * <ul>
 * <li>compass.openjpa.reindexOnStartup: Set it to <code>true</code> in order to perform full reindex of the database on startup.
 * Defaults to <code>false</code>.</li>
 * <li>compass.openjpa.registerRemoteCommitListener: Set it to <code>true</code> in order to register for remote commits
 * notifications. Defaults to <code>false</code>.</li>
 * <li>compass.openjpa.indexQuery.[entity name/class]: Specific select query that will be used to perform the indexing
 * for the mentioned specific entity name / class. Note, before calling {@link org.compass.gps.CompassGps#index()} there
 * is an option the programmatically control this.</li>
 * <li>compass.openjpa.config: A classpath that points to Compass configuration.</li>
 * </ul>
 *
 * @author kimchy
 */
public class CompassProductDerivation extends AbstractProductDerivation {

    private static final Log log = LogFactory.getLog(CompassProductDerivation.class);

    public static final String COMPASS_USER_OBJECT_KEY = CompassProductDerivation.class.getName() + ".compass";

    public static final String COMPASS_SESSION_USER_OBJECT_KEY = CompassProductDerivation.class.getName() + ".compassSession";

    public static final String COMPASS_TRANSACTION_USER_OBJECT_KEY = CompassProductDerivation.class.getName() + ".compassTransaction";

    public static final String COMPASS_GPS_USER_OBJECT_KEY = CompassProductDerivation.class.getName() + ".gps";

    public static final String COMPASS_INDEX_SETTINGS_USER_OBJECT_KEY = CompassProductDerivation.class.getName() + ".indexprops";

    private static final String COMPASS_PREFIX = "compass";

    private static final String COMPASS_GPS_INDEX_PREFIX = "gps.index.";


    public static final String REINDEX_ON_STARTUP = "compass.openjpa.reindexOnStartup";

    public static final String REGISTER_REMOTE_COMMIT_LISTENER = "compass.openjpa.registerRemoteCommitListener";

    public static final String INDEX_QUERY_PREFIX = "compass.openjpa.indexQuery.";

    public static final String COMPASS_CONFIG_LOCATION = "compass.openjpa.config";

    // this is only used when installed in a pre-1.0 version of OpenJPA
    private static final Map<OpenJPAConfiguration, CompassProductDerivation> derivations
            = new IdentityHashMap<OpenJPAConfiguration, CompassProductDerivation>();


    private Compass compass;

    private DefaultJpaCompassGps jpaCompassGps;

    private boolean commitBeforeCompletion;

    private boolean openJpaControlledTransaction;

    private Properties compassProperties;

    public int getType() {
        return TYPE_FEATURE;
    }

    @Override
    public boolean beforeConfigurationLoad(Configuration config) {
        if (!(config instanceof OpenJPAConfiguration)) {
            return false;
        }
        final OpenJPAConfigurationImpl openJpaConfig = (OpenJPAConfigurationImpl) config;

        // Compass can make use of changed object IDs when receiving remote
        // commit events; reset the default setting to true.
        openJpaConfig.remoteProviderPlugin.setTransmitPersistedObjectIds(true);

        // In 0.x releases of OpenJPA, the BrokerFactoryEventManager does not exist.
        // This check will prevent us from triggering a NoSuchMethodError.
        if (!isReleasedVersion()) {
            openJpaConfig.getLog(OpenJPAConfiguration.LOG_RUNTIME).warn(
                    "Compass cannot automatically install itself into pre-1.0 versions of OpenJPA. To complete "
                            + "Compass installation, you must invoke CompassProductDerivation.installCompass().");
            derivations.put(openJpaConfig, this);
            return false;
        }

        openJpaConfig.getBrokerFactoryEventManager().addListener(new BrokerFactoryListener() {
            public void afterBrokerFactoryCreate(BrokerFactoryEvent event) {
                installIntoFactory(event.getBrokerFactory());
            }

            public void eventFired(BrokerFactoryEvent event) {
                if (event.getEventType() == BrokerFactoryEvent.BROKER_FACTORY_CREATED)
                    afterBrokerFactoryCreate(event);
            }
        });
        return false;
    }

    /**
     * @deprecated This is only needed for pre-1.0 versions of OpenJPA.
     */
    public static void installCompass(BrokerFactory factory) {
        if (factory.getUserObject(COMPASS_USER_OBJECT_KEY) != null)
            return;

        CompassProductDerivation derivation = derivations.get(factory.getConfiguration());
        if (derivation == null)
            throw new IllegalStateException("no CompassProductDerivation instance registered for this configuration");
        derivation.installIntoFactory(factory);
    }

    @Override
    public boolean beforeConfigurationConstruct(ConfigurationProvider cp) {
        //noinspection unchecked
        compassProperties = new Properties();
        Map<String, Object> openJpaProps = cp.getProperties();
        loadCompassProps(openJpaProps);
        return super.beforeConfigurationConstruct(cp);
    }

    private void installIntoFactory(BrokerFactory factory) {
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

        OpenJPAConfiguration openJpaConfig = factory.getConfiguration();

        CompassConfiguration compassConfiguration = CompassConfigurationFactory.newConfiguration();
        CompassSettings settings = compassConfiguration.getSettings();
        settings.addSettings(compassProperties);

        String configLocation = (String) compassProperties.get(COMPASS_CONFIG_LOCATION);
        if (configLocation != null) {
            compassConfiguration.configure(configLocation);
        }

        Collection<Class> classes = openJpaConfig.getMetaDataRepositoryInstance().loadPersistentTypes(true, null);
        for (Class jpaClass : classes) {
            compassConfiguration.tryAddClass(jpaClass);
        }

        OpenJPAEntityManagerFactory emf = toEntityManagerFactory(factory);

        // create some default settings

        String transactionFactory = (String) compassProperties.get(CompassEnvironment.Transaction.FACTORY);
        if (transactionFactory == null) {
            OpenJPAEntityManager em = emf.createEntityManager();
            boolean isJTA = em.isManaged();
            em.close();
            if (isJTA) {
                transactionFactory = JTASyncTransactionFactory.class.getName();
                openJpaControlledTransaction = false;
            } else {
                transactionFactory = LocalTransactionFactory.class.getName();
                openJpaControlledTransaction = true;
            }
            settings.setSetting(CompassEnvironment.Transaction.FACTORY, transactionFactory);
        } else {
            // JPA is not controlling the transaction (using JTA Sync or XA), don't commit/rollback
            // with OpenJPA transaction listeners
            openJpaControlledTransaction = false;
        }

        // if the settings is configured to use local transaciton, disable thread bound setting since
        // we are using OpenJPA to managed transaction scope (using user objects on the em) and not thread locals
        // will only be taken into account when using local transactions
        if (settings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
            // if no emf is defined
            settings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, true);
        }

        compass = compassConfiguration.buildCompass();

        commitBeforeCompletion = settings.getSettingAsBoolean(CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, false);

        factory.putUserObject(COMPASS_USER_OBJECT_KEY, compass);

        registerListeners(factory);

        // extract index properties so they will be used
        Properties indexProps = new Properties();
        for (Map.Entry entry : compassProperties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(COMPASS_GPS_INDEX_PREFIX)) {
                indexProps.put(key.substring(COMPASS_GPS_INDEX_PREFIX.length()), entry.getValue());
            }
        }
        factory.putUserObject(COMPASS_INDEX_SETTINGS_USER_OBJECT_KEY, indexProps);

        // start an internal JPA device and Gps for mirroring

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

        OpenJPAJpaEntityLifecycleInjector lifecycleInjector = new OpenJPAJpaEntityLifecycleInjector();
        lifecycleInjector.setEventListener(new EmbeddedOpenJPAEventListener(jpaGpsDevice));
        jpaGpsDevice.setLifecycleInjector(lifecycleInjector);

        jpaCompassGps = new DefaultJpaCompassGps();
        jpaCompassGps.setCompass(compass);
        jpaCompassGps.addGpsDevice(jpaGpsDevice);

        // before we start the Gps, open and close a broker
        emf.createEntityManager().close();

        jpaCompassGps.start();

        String reindexOnStartup = (String) compassProperties.get(REINDEX_ON_STARTUP);
        if ("true".equalsIgnoreCase(reindexOnStartup)) {
            jpaCompassGps.index();
        }

        factory.putUserObject(COMPASS_GPS_USER_OBJECT_KEY, jpaCompassGps);
    }

    private void loadCompassProps(Map<String, Object> openJpaProps) {
        for (Map.Entry<String, Object> entry : openJpaProps.entrySet()) {
            if (entry.getKey().startsWith(COMPASS_PREFIX)) {
                compassProperties.put(entry.getKey(), entry.getValue());
            }
            if (entry.getKey().startsWith(COMPASS_GPS_INDEX_PREFIX)) {
                compassProperties.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private OpenJPAEntityManagerFactory toEntityManagerFactory(BrokerFactory factory) {
        try {
            Class cls;
            try {
                cls = Class.forName("org.apache.openjpa.persistence.JPAFacadeHelper");
            } catch (ClassNotFoundException e) {
                cls = OpenJPAPersistence.class;
            }
            return (OpenJPAEntityManagerFactory)
                    cls.getMethod("toEntityManagerFactory", BrokerFactory.class).invoke(null, factory);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException)
                throw (RuntimeException) e.getCause();
            else
                throw new RuntimeException(e);
        }
    }

    protected void registerListeners(BrokerFactory brokerFactory) {

        brokerFactory.addTransactionListener(new BeginTransactionListener() {
            public void afterBegin(TransactionEvent transactionEvent) {
                Broker broker = (Broker) transactionEvent.getSource();
                CompassSession session = compass.openSession();
                broker.putUserObject(COMPASS_SESSION_USER_OBJECT_KEY, session);
                CompassTransaction tr = session.beginTransaction();
                broker.putUserObject(COMPASS_TRANSACTION_USER_OBJECT_KEY, tr);
            }
        });

        brokerFactory.addTransactionListener(new EndTransactionListener() {
            public void beforeCommit(TransactionEvent transactionEvent) {
                if (commitBeforeCompletion) {
                    commit(transactionEvent);
                }
            }

            public void afterCommit(TransactionEvent transactionEvent) {
                // TODO maybe beforeCommit should occur here (when using jdbc)
            }

            public void afterRollback(TransactionEvent transactionEvent) {
            }

            public void afterStateTransitions(TransactionEvent transactionEvent) {
            }

            public void afterCommitComplete(TransactionEvent transactionEvent) {
                if (!commitBeforeCompletion) {
                    commit(transactionEvent);
                }
            }

            public void afterRollbackComplete(TransactionEvent transactionEvent) {
                rollback(transactionEvent);
            }

            private void commit(TransactionEvent trEvent) {
                Broker broker = (Broker) trEvent.getSource();
                CompassTransaction tr = (CompassTransaction) broker.getUserObject(COMPASS_TRANSACTION_USER_OBJECT_KEY);
                CompassSession session = (CompassSession) broker.getUserObject(COMPASS_SESSION_USER_OBJECT_KEY);
                try {
                    if (openJpaControlledTransaction) {
                        try {
                            tr.commit();
                        } finally {
                            session.close();
                        }
                    }
                } finally {
                    broker.putUserObject(COMPASS_TRANSACTION_USER_OBJECT_KEY, null);
                    broker.putUserObject(COMPASS_SESSION_USER_OBJECT_KEY, null);
                }
            }

            private void rollback(TransactionEvent trEvent) {
                Broker broker = (Broker) trEvent.getSource();
                CompassTransaction tr = (CompassTransaction) broker.getUserObject(COMPASS_TRANSACTION_USER_OBJECT_KEY);
                CompassSession session = (CompassSession) broker.getUserObject(COMPASS_SESSION_USER_OBJECT_KEY);
                try {
                    if (openJpaControlledTransaction) {
                        try {
                            tr.rollback();
                        } finally {
                            session.close();
                        }
                    }
                } finally {
                    broker.putUserObject(COMPASS_TRANSACTION_USER_OBJECT_KEY, null);
                    broker.putUserObject(COMPASS_SESSION_USER_OBJECT_KEY, null);
                }
            }
        });

        String registerRemoteCommitListener = (String) compassProperties.get(REGISTER_REMOTE_COMMIT_LISTENER);
        if ("true".equalsIgnoreCase(registerRemoteCommitListener)) {
            brokerFactory.getConfiguration().getRemoteCommitEventManager().addListener(new CompassRemoteCommitListener(
                    toEntityManagerFactory(brokerFactory), compass));
        }
    }

    public void beforeConfigurationClose(Configuration configuration) {
        if (jpaCompassGps != null) {
            jpaCompassGps.stop();
        }
        if (compass != null) {
            compass.close();
        }
    }

    private static class CompassRemoteCommitListener implements RemoteCommitListener {

        private static final Log log = LogFactory.getLog(CompassRemoteCommitListener.class);

        private final EntityManagerFactoryImpl emf;

        private final Compass compass;

        private CompassRemoteCommitListener(OpenJPAEntityManagerFactory emf, Compass compass) {
            // casting to EMFImpl so that this code can work with pre-1.0 and post-1.0 versions
            // of OpenJPA.
            this.emf = (EntityManagerFactoryImpl) emf;
            this.compass = compass;
        }

        @SuppressWarnings({"unchecked"})
        public void afterCommit(RemoteCommitEvent event) {
            OpenJPAEntityManager em = emf.createEntityManager();
            CompassSession session = compass.openSession();
            CompassTransaction tr = null;
            try {
                tr = session.beginTransaction();
                switch (event.getPayloadType()) {
                    case RemoteCommitEvent.PAYLOAD_OIDS:
                        reindexTypesByName(event.getPersistedTypeNames(), em, session);
                        reindexObjectsById(event.getUpdatedObjectIds(), em, session);
                        deleteObjectsById(event.getDeletedObjectIds(), session);
                        break;
                    case RemoteCommitEvent.PAYLOAD_OIDS_WITH_ADDS:
                        reindexObjectsById(event.getPersistedObjectIds(), em, session);
                        reindexObjectsById(event.getUpdatedObjectIds(), em, session);
                        deleteObjectsById(event.getDeletedObjectIds(), session);
                        break;
                    case RemoteCommitEvent.PAYLOAD_EXTENTS:
                        Collection c = new HashSet();
                        c.addAll(event.getPersistedTypeNames());
                        c.addAll(event.getUpdatedTypeNames());
                        c.addAll(event.getDeletedTypeNames());
                        reindexTypesByName(c, em, session);
                        break;
                    case RemoteCommitEvent.PAYLOAD_LOCAL_STALE_DETECTION:
                        reindexObjectsById(event.getUpdatedObjectIds(), em, session);
                        break;
                    default:
                        log.warn("Unknown remote commit event type [" + event.getPayloadType() + "], ignoring...");
                }
                tr.commit();
            } catch (Exception e) {
                log.error("Failed to perform remote commit syncronization", e);
                if (tr != null) {
                    tr.rollback();
                }
            } finally {
                if (session != null) {
                    session.close();
                }
                if (em != null) {
                    em.close();
                }
            }
        }

        @SuppressWarnings({"unchecked"})
        private void reindexObjectsById(Collection oids, OpenJPAEntityManager em, CompassSession session) {
            for (OpenJPAId oid : (Collection<OpenJPAId>) oids) {
                reindexOid(oid, em, session);
            }
        }

        @SuppressWarnings({"unchecked"})
        private void deleteObjectsById(Collection oids, CompassSession session) {
            for (OpenJPAId oid : (Collection<OpenJPAId>) oids) {
                delete(oid, session);
            }
        }

        @SuppressWarnings({"unchecked"})
        private void reindexTypesByName(Collection typeNames, OpenJPAEntityManager em, CompassSession session) {
            ClassLoader loader = emf.getConfiguration().getClassResolverInstance().getClassLoader(null, null);
            for (String typeName : (Collection<String>) typeNames) {
                try {
                    Class cls = Class.forName(typeName, true, loader);
                    // delete all objects matching the given type
                    session.delete(session.queryBuilder().matchAll().setTypes(new Class[]{cls}));
                    Extent extent = em.createExtent(cls, true);
                    for (Object o : extent.list()) {
                        reindex(o, session);
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Failed to find class", e);
                }
            }
        }

        @SuppressWarnings({"unchecked"})
        private void reindexOid(OpenJPAId oid, OpenJPAEntityManager em, CompassSession session) {
            try {
                Object o = em.find(oid.getType(), oid.getIdObject());
                reindex(o, session);
            } catch (EntityNotFoundException e) {
                delete(oid, session);
            }
        }

        private void reindex(Object o, CompassSession session) {
            session.save(o);
        }

        private void delete(OpenJPAId oid, CompassSession session) {
            Class cls = oid.getType();
            Object id = oid.getIdObject();
            session.delete(cls, id);
        }

        public void close() {
        }
    }

    public static boolean isReleasedVersion() {
        if (OpenJPAVersion.MAJOR_RELEASE < 1)
            return false;

        if (OpenJPAVersion.MAJOR_RELEASE == 1
                && OpenJPAVersion.MINOR_RELEASE == 0
                && OpenJPAVersion.PATCH_RELEASE == 0) {
            // OpenJPA changed things during the 1.0.0-SNAPSHOT
            // release period.
            try {
                Class.forName("org.apache.openjpa.event.BrokerFactoryEvent",
                        false, OpenJPAVersion.class.getClassLoader());
                return true;
            } catch (ClassNotFoundException cnfe) {
                return false;
            }
        }

        return true;
    }
}
