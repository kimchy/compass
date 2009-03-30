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

package org.compass.gps.device.hibernate.embedded;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassConfigurationFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.transaction.JTASyncTransactionFactory;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.util.ClassUtils;
import org.compass.gps.device.hibernate.lifecycle.HibernateMirrorFilter;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.engine.EntityEntry;
import org.hibernate.event.*;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Value;
import org.hibernate.transaction.CMTTransactionFactory;
import org.hibernate.transaction.JTATransactionFactory;

/**
 * An Hibernate event listener allowing to run Compass embedded within Hibernate. The embedded mode
 * will allow to automatically (with minimal configuration) get Compass configured to mirror changes
 * done through Hibernate to the search engine, as well as simply indexing the whole database content.
 *
 * <p>Configuration of this listener is simple:
 * <pre>
 * &lt;hibernate-configuration&gt;
 *      &lt;session-factory&gt;
 *
 *          &lt;event type="post-update"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *          &lt;event type="post-insert"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *          &lt;event type="post-delete"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *          &lt;event type="post-collection-recreate"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *          &lt;event type="post-collection-remove"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *          &lt;event type="post-collection-update"&gt;
 *              &lt;listener class="org.compass.gps.device.hibernate.embedded.CompassEventListener"/&gt;
 *          &lt;/event&gt;
 *
 *      &lt;/session-factory&gt;
 * &lt;/hibernate-configuration&gt;
 * </pre>
 *
 * <p>When using Hiberante annotations or entity manager Compass also contains Hibernate search event class
 * so it will be automatically detected. In such a case, there is no need for the event listener configuration.
 *
 * <p>Once the above configuration is set, then Compass is "installed" within Hibernate. In order to enable
 * Compass, the search engine connection url must be set using Hibernate properties configuration. For example:
 * <code>&lt;property name="compass.engine.connection"&gt;testindex&lt;/property&gt;</code>.
 *
 * <p>Compass will automatically go over the mapped classes in Hibernate and will check if they have Compass
 * mappings. If they do, they will be added to the searchable entities. If no such searchable classes are found,
 * this listener will perform no operations.
 *
 * <p>Compass additional configuration can be set using typical Hiberante properties configuration using the
 * <code>compass.</code> prefix. If using an external Compass configuration file is preferred, then the
 * <code>compass.hibernate.config</code> can be configured and point to the location of a Compass configuration
 * file.
 *
 * <p>Embedded Compass also allows to use {@link org.compass.gps.CompassGps#index()} in order to complely reindex
 * the database. See {@link org.compass.gps.device.hibernate.embedded.HibernateHelper} for more information. In
 * order to configure the Compass instance that will be used to index the database, the <code>gps.index.</code>
 * can be used.
 *
 * <p>Transaction management is automatically bounded to Hibernate by using Compass local transaction. If other
 * transaction strategies are used (such as JTA Sync or XA) then the Compass transaction will be bounded to them
 * and not the Hibernate transaction.
 *
 * <p>A user defined {@link org.compass.gps.device.hibernate.lifecycle.HibernateMirrorFilter} can be used to
 * filter out mirror operations. In order to configure one, the <code>compass.hibernate.mirrorFilter</code>
 * can be used with the implementation class FQN.
 *
 * <p>In order to get the {@link Compass} instnace bounded to this Hibernate configuration, the
 * {@link HibernateHelper} can be used. This is mainly used in order to perform search operations on the
 * index and get a Compass Gps in order to reindex the database.
 *
 * @author kimchy
 */
public class CompassEventListener implements PostDeleteEventListener, PostInsertEventListener, PostUpdateEventListener,
        PostCollectionRecreateEventListener, PostCollectionRemoveEventListener, PostCollectionUpdateEventListener,
        Initializable {

    public final static Log log = LogFactory.getLog(CompassEventListener.class);

    public static final String COMPASS_PREFIX = "compass";

    public static final String COMPASS_GPS_INDEX_PREFIX = "gps.index.";

    public static final String COMPASS_CONFIG_LOCATION = "compass.hibernate.config";

    public static final String COMPASS_MIRROR_FILTER = "compass.hibernate.mirrorFilter";

    public static final String COMPASS_PROCESS_COLLECTIONS = "compass.hibernate.processCollections";

    private static ThreadLocal<WeakHashMap<Configuration, CompassHolder>> contexts = new ThreadLocal<WeakHashMap<Configuration, CompassHolder>>();

    private CompassHolder compassHolder;

    private boolean processCollections = true;

    public void initialize(Configuration cfg) {
        compassHolder = getCompassHolder(cfg);
    }

    public Compass getCompass() {
        return this.compassHolder.compass;
    }

    public Properties getIndexSettings() {
        return this.compassHolder.indexSettings;
    }

    public void onPostDelete(PostDeleteEvent event) {
        if (compassHolder == null) {
            return;
        }
        Object entity = event.getEntity();
        if (!hasMappingForEntity(entity.getClass(), Cascade.DELETE)) {
            return;
        }
        if (compassHolder.mirrorFilter != null) {
            if (compassHolder.mirrorFilter.shouldFilterDelete(event)) {
                return;
            }
        }
        TransactionSyncHolder holder = getOrCreateHolder(event.getSession());
        if (log.isTraceEnabled()) {
            log.trace("Deleting [" + entity + "]");
        }
        holder.session.delete(entity);
        afterOperation(holder);
    }

    public void onPostInsert(PostInsertEvent event) {
        if (compassHolder == null) {
            return;
        }
        Object entity = event.getEntity();
        if (!hasMappingForEntity(entity.getClass(), Cascade.CREATE)) {
            return;
        }
        if (compassHolder.mirrorFilter != null) {
            if (compassHolder.mirrorFilter.shouldFilterInsert(event)) {
                return;
            }
        }
        TransactionSyncHolder holder = getOrCreateHolder(event.getSession());
        if (log.isTraceEnabled()) {
            log.trace("Creating [" + entity + "]");
        }
        Collection<CollectionEntry> collectionsBefore = null;
        if (processCollections) {
            collectionsBefore = new HashSet<CollectionEntry>(event.getSession().getPersistenceContext().getCollectionEntries().values());
        }
        holder.session.create(entity);
        if (processCollections) {
            Collection<CollectionEntry> collectionsAfter = event.getSession().getPersistenceContext().getCollectionEntries().values();
            for (CollectionEntry collection : collectionsAfter) {
                if (!collectionsBefore.contains(collection)) {
                    collection.setProcessed(true);
                }
            }
        }
        afterOperation(holder);
    }

    public void onPostUpdate(PostUpdateEvent event) {
        if (compassHolder == null) {
            return;
        }
        Object entity = event.getEntity();
        if (!hasMappingForEntity(entity.getClass(), Cascade.SAVE)) {
            return;
        }
        if (compassHolder.mirrorFilter != null) {
            if (compassHolder.mirrorFilter.shouldFilterUpdate(event)) {
                return;
            }
        }
        TransactionSyncHolder holder = getOrCreateHolder(event.getSession());
        if (log.isTraceEnabled()) {
            log.trace("Updating [" + entity + "]");
        }
        Collection<CollectionEntry> collectionsBefore = null;
        if (processCollections) {
            collectionsBefore = new HashSet<CollectionEntry>(event.getSession().getPersistenceContext().getCollectionEntries().values());
        }
        holder.session.save(entity);
        if (processCollections) {
            Collection<CollectionEntry> collectionsAfter = event.getSession().getPersistenceContext().getCollectionEntries().values();
            for (CollectionEntry collection : collectionsAfter) {
                if (!collectionsBefore.contains(collection)) {
                    collection.setProcessed(true);
                }
            }
        }
        afterOperation(holder);
    }

    public void onPostRecreateCollection(PostCollectionRecreateEvent postCollectionRecreateEvent) {
        processCollectionEvent(postCollectionRecreateEvent);
    }

    public void onPostRemoveCollection(PostCollectionRemoveEvent postCollectionRemoveEvent) {
        processCollectionEvent(postCollectionRemoveEvent);
    }

    public void onPostUpdateCollection(PostCollectionUpdateEvent postCollectionUpdateEvent) {
        processCollectionEvent(postCollectionUpdateEvent);
    }

    private void processCollectionEvent(AbstractCollectionEvent event) {
        if (compassHolder == null) {
            return;
        }
        final Object entity = event.getAffectedOwnerOrNull();
        if (entity == null) {
            //Hibernate cannot determine every single time the owner especially incase detached objects are involved
            // or property-ref is used
            //Should log really but we don't know if we're interested in this collection for indexing
            return;
        }

        CollectionEntry collectionEntry = event.getSession().getPersistenceContext().getCollectionEntry(event.getCollection());
        if (collectionEntry != null && collectionEntry.getLoadedPersister() == null) {
            // ignore this entry, since Hibernate will cause NPE when doing SAVE
            // TODO is there a better way to solve this?
            return;
        }

        if (!hasMappingForEntity(entity.getClass(), Cascade.SAVE)) {
            return;
        }

        Serializable id = getId(entity, event);
        if (id == null) {
            log.warn("Unable to reindex entity on collection change, id cannot be extracted: " + event.getAffectedOwnerEntityName());
            return;
        }

        if (compassHolder.mirrorFilter != null) {
            if (compassHolder.mirrorFilter.shouldFilterCollection(event)) {
                return;
            }
        }
        TransactionSyncHolder holder = getOrCreateHolder(event.getSession());
        if (log.isTraceEnabled()) {
            log.trace("Updating [" + entity + "]");
        }
        holder.session.save(entity);
        afterOperation(holder);
    }

    private Serializable getId(Object entity, AbstractCollectionEvent event) {
        Serializable id = event.getAffectedOwnerIdOrNull();
        if (id == null) {
            //most likely this recovery is unnecessary since Hibernate Core probably try that
            EntityEntry entityEntry = event.getSession().getPersistenceContext().getEntry(entity);
            id = entityEntry == null ? null : entityEntry.getId();
        }
        return id;
    }

    private TransactionSyncHolder getOrCreateHolder(EventSource session) {
        if (session.isTransactionInProgress()) {
            Transaction transaction = session.getTransaction();
            TransactionSyncHolder holder = compassHolder.syncHolderPerTx.get(transaction);
            if (holder == null) {
                holder = new TransactionSyncHolder();
                holder.session = compassHolder.compass.openSession();
                holder.tr = holder.session.beginTransaction();
                holder.transacted = true;
                transaction.registerSynchronization(new CompassEmbeddedSyncronization(holder, transaction));
                compassHolder.syncHolderPerTx.put(transaction, holder);
            }
            return holder;
        } else {
            TransactionSyncHolder holder = new TransactionSyncHolder();
            holder.session = compassHolder.compass.openSession();
            holder.tr = holder.session.beginTransaction();
            holder.transacted = false;
            return holder;
        }
    }

    private void afterOperation(TransactionSyncHolder holder) {
        if (holder.transacted) {
            return;
        }
        holder.tr.commit();
        holder.session.close();
    }

    private CompassHolder getCompassHolder(Configuration cfg) {
        WeakHashMap<Configuration, CompassHolder> contextMap = contexts.get();
        if (contextMap == null) {
            contextMap = new WeakHashMap<Configuration, CompassHolder>(2);
            contexts.set(contextMap);
        }
        CompassHolder compassHolder = contextMap.get(cfg);
        if (compassHolder == null) {
            compassHolder = initCompassHolder(cfg);
            if (compassHolder != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Regsitering new Compass Holder [" + compassHolder + "]");
                }
                contextMap.put(cfg, compassHolder);
            }
        }
        return compassHolder;
    }

    private CompassHolder initCompassHolder(Configuration cfg) {
        Properties compassProperties = new Properties();
        //noinspection unchecked
        Properties props = cfg.getProperties();
        for (Map.Entry entry : props.entrySet()) {
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
                log.debug("No Compass properties defined, disabling Compass");
            }
            return null;
        }
        if (compassProperties.getProperty(CompassEnvironment.CONNECTION) == null) {
            if (log.isDebugEnabled()) {
                log.debug("No Compass [" + CompassEnvironment.CONNECTION + "] property defined, disabling Compass");
            }
            return null;
        }

        processCollections = compassProperties.getProperty(COMPASS_PROCESS_COLLECTIONS, "true").equalsIgnoreCase("true");

        CompassConfiguration compassConfiguration = CompassConfigurationFactory.newConfiguration();
        CompassSettings settings = compassConfiguration.getSettings();
        settings.addSettings(compassProperties);

        String configLocation = (String) compassProperties.get(COMPASS_CONFIG_LOCATION);
        if (configLocation != null) {
            compassConfiguration.configure(configLocation);
        }

        boolean atleastOneClassAdded = false;
        for (Iterator it = cfg.getClassMappings(); it.hasNext();) {
            PersistentClass clazz = (PersistentClass) it.next();
            Class<?> mappedClass = clazz.getMappedClass();
            for (Iterator propIt = clazz.getPropertyIterator(); propIt.hasNext();) {
                Property prop = (Property) propIt.next();
                Value value = prop.getValue();
                if (value instanceof Component) {
                    Component component = (Component) value;
                    try {
                        atleastOneClassAdded |= compassConfiguration.tryAddClass(ClassUtils.forName(component.getComponentClassName(), settings.getClassLoader()));
                    } catch (ClassNotFoundException e) {
                        log.warn("Failed to load component class [" + component.getComponentClassName() + "]", e);
                    }
                }
            }
            Value idValue = clazz.getIdentifierProperty().getValue();
            if (idValue instanceof Component) {
                Component component = (Component) idValue;
                try {
                    atleastOneClassAdded |= compassConfiguration.tryAddClass(ClassUtils.forName(component.getComponentClassName(), settings.getClassLoader()));
                } catch (ClassNotFoundException e) {
                    log.warn("Failed to load component class [" + component.getComponentClassName() + "]", e);
                }
            }
            atleastOneClassAdded |= compassConfiguration.tryAddClass(mappedClass);
        }
        if (!atleastOneClassAdded) {
            if (log.isDebugEnabled()) {
                log.debug("No searchable class mappings found in Hibernate class mappings, disabling Compass");
            }
            return null;
        }

        CompassHolder compassHolder = new CompassHolder();
        compassHolder.compassProperties = compassProperties;

        compassHolder.commitBeforeCompletion = settings.getSettingAsBoolean(CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, false);


        String transactionFactory = (String) compassProperties.get(CompassEnvironment.Transaction.FACTORY);
        if (transactionFactory == null) {
            String hibernateTransactionStrategy = cfg.getProperty(Environment.TRANSACTION_STRATEGY);
            if (CMTTransactionFactory.class.getName().equals(hibernateTransactionStrategy) || JTATransactionFactory.class.getName().equals(hibernateTransactionStrategy)) {
                // hibernate is configured with JTA, automatically configure Compass to use its JTASync (by default)
                compassHolder.hibernateControlledTransaction = false;
                compassConfiguration.setSetting(CompassEnvironment.Transaction.FACTORY, JTASyncTransactionFactory.class.getName());
            } else {
                // Hibernate JDBC transaction manager, let Compass use the local transaction manager
                compassHolder.hibernateControlledTransaction = true;
                // if the settings is configured to use local transaciton, disable thread bound setting since
                // we are using Hibernate to managed transaction scope (using the transaction to holder map) and not thread locals
                if (settings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
                    settings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, true);
                }
            }
        } else if (LocalTransactionFactory.class.getName().equals(transactionFactory)) {
            compassHolder.hibernateControlledTransaction = true;
            // if the settings is configured to use local transaciton, disable thread bound setting since
            // we are using Hibernate to managed transaction scope (using the transaction to holder map) and not thread locals
            if (settings.getSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION) == null) {
                settings.setBooleanSetting(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, true);
            }
        } else {
            // Hibernate is not controlling the transaction (using JTA Sync or XA), don't commit/rollback
            // with Hibernate transaction listeners
            compassHolder.hibernateControlledTransaction = false;
        }

        compassHolder.indexSettings = new Properties();
        for (Map.Entry entry : compassProperties.entrySet()) {
            String key = (String) entry.getKey();
            if (key.startsWith(COMPASS_GPS_INDEX_PREFIX)) {
                compassHolder.indexSettings.put(key.substring(COMPASS_GPS_INDEX_PREFIX.length()), entry.getValue());
            }
        }

        String mirrorFilterClass = compassHolder.compassProperties.getProperty(COMPASS_MIRROR_FILTER);
        if (mirrorFilterClass != null) {
            try {
                compassHolder.mirrorFilter = (HibernateMirrorFilter) ClassUtils.forName(mirrorFilterClass, compassConfiguration.getSettings().getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new CompassException("Failed to create mirror filter [" + mirrorFilterClass + "]", e);
            }
        }

        compassHolder.compass = compassConfiguration.buildCompass();

        return compassHolder;
    }

    private boolean hasMappingForEntity(Class clazz, Cascade cascade) {
        ResourceMapping resourceMapping = ((InternalCompass) compassHolder.compass).getMapping().getRootMappingByClass(clazz);
        if (resourceMapping != null) {
            return true;
        }
        resourceMapping = ((InternalCompass) compassHolder.compass).getMapping().getNonRootMappingByClass(clazz);
        if (resourceMapping == null) {
            return false;
        }
        return resourceMapping.operationAllowed(cascade);
    }


    private class CompassHolder {

        ConcurrentHashMap<Transaction, TransactionSyncHolder> syncHolderPerTx = new ConcurrentHashMap<Transaction, TransactionSyncHolder>();

        Properties compassProperties;

        Properties indexSettings;

        boolean commitBeforeCompletion;

        boolean hibernateControlledTransaction;

        HibernateMirrorFilter mirrorFilter;

        Compass compass;
    }

    private class TransactionSyncHolder {

        public CompassSession session;

        public CompassTransaction tr;

        public boolean transacted;
    }

    private class CompassEmbeddedSyncronization implements Synchronization {

        private Transaction transaction;

        private TransactionSyncHolder holder;

        private CompassEmbeddedSyncronization(TransactionSyncHolder holder, Transaction transaction) {
            this.holder = holder;
            this.transaction = transaction;
        }

        public void beforeCompletion() {
            if (!compassHolder.commitBeforeCompletion) {
                return;
            }
            if (holder.session.isClosed()) {
                return;
            }
            if (compassHolder.hibernateControlledTransaction) {
                if (log.isTraceEnabled()) {
                    log.trace("Committing compass transaction using Hibernate synchronization beforeCompletion on thread [" +
                            Thread.currentThread().getName() + "]");
                }
                holder.tr.commit();
            }
        }

        public void afterCompletion(int status) {
            try {
                if (holder.session.isClosed()) {
                    return;
                }
                if (!compassHolder.commitBeforeCompletion) {
                    if (compassHolder.hibernateControlledTransaction) {
                        try {
                            if (status == Status.STATUS_COMMITTED) {
                                if (log.isTraceEnabled()) {
                                    log.trace("Committing compass transaction using Hibernate synchronization afterCompletion on thread [" +
                                            Thread.currentThread().getName() + "]");
                                }
                                holder.tr.commit();
                            } else {
                                if (log.isTraceEnabled()) {
                                    log.trace("Rolling back compass transaction using Hibernate synchronization afterCompletion on thread [" +
                                            Thread.currentThread().getName() + "]");
                                }
                                holder.tr.rollback();
                            }
                        } finally {
                            holder.session.close();
                        }
                    }
                }
            } catch (Exception e) {
                // TODO swallow??????
                log.error("Exception occured when sync with transaction", e);
            } finally {
                compassHolder.syncHolderPerTx.remove(transaction);
            }
        }
    }
}
