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

package org.compass.gps.device.jpa;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.compass.core.util.Assert;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.jpa.entities.JpaEntitiesLocator;
import org.compass.gps.device.jpa.entities.JpaEntitiesLocatorDetector;
import org.compass.gps.device.jpa.extractor.NativeJpaExtractor;
import org.compass.gps.device.jpa.extractor.NativeJpaHelper;
import org.compass.gps.device.jpa.indexer.JpaIndexEntitiesIndexer;
import org.compass.gps.device.jpa.indexer.JpaIndexEntitiesIndexerDetector;
import org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjector;
import org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjectorDetector;
import org.compass.gps.device.jpa.queryprovider.DefaultJpaQueryProvider;
import org.compass.gps.device.jpa.queryprovider.JpaQueryProvider;
import org.compass.gps.device.support.parallel.AbstractParallelGpsDevice;
import org.compass.gps.device.support.parallel.IndexEntitiesIndexer;
import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * <p>A Java Persistence API Gps Device (EJB3 Persistence).
 *
 * <p>The jpa device provides support for using jpa to index a database. The path can
 * be viewed as: Database <-> EntityManager(JPA) <-> Objects <-> Compass::Gps
 * <-> Compass::Core (Search Engine). What it means is that for every object that has both
 * jpa and compass mappings, you will be able to index it's data, as well as real time mirroring of
 * data changes.
 *
 * <p>When creating the object, an <code>EntityManagerFactory</code> must be provided to the Device.
 *
 * <p>Indexing uses {@link JpaEntitiesLocator} to locate all the entities that can be
 * indexed (i.e. entities that have both Compass and JPA mappings). Most of the time
 * the {@link org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator} is enough, but
 * special JPA implementation one can be provided. If none is provided, the device will use the {@link
 * JpaEntitiesLocatorDetector} to auto detect the correct locator (which defaults to the ({@link
 * org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator}).
 *
 * <p>The indexing process itself is done through an implementation of
 * {@link org.compass.gps.device.jpa.indexer.JpaIndexEntitiesIndexer}. There are several implemenations
 * for it including a default one that uses plain JPA APIs. Specific implementations (such as Hibernate
 * and OpenJPA) are used for better performance.
 *
 * <p>Mirroring can be done in two ways. The first one is using JPA official API, implemeting
 * an Entity Lifecycle listener and specifing it for each entity class via annotations. Compass
 * comes with helper base clases for it, {@link AbstractCompassJpaEntityListener} and
 * {@link AbstractDeviceJpaEntityListener}. As far as integrating Compass with JPA for mirroring,
 * this is the less preferable way. The second option for mirroring is to use the
 * {@link JpaEntityLifecycleInjector}, which will use the internal JPA implementation to
 * inject global lifecycle event listerens (sadly, there is no option to do that with the
 * <code>EntityManagerFactory</code> API). If the {@link #setInjectEntityLifecycleListener(boolean)} is
 * set to <code>true</code> (defaults to <code>false</code>), the device will try to use the injector to
 * inject global event listeners. If no {@link JpaEntityLifecycleInjector} is defined, the device will
 * try to autodetect the injector based on the current support for specific JPA implementations using
 * the {@link JpaEntityLifecycleInjectorDetector}. See its javadoc for a list of the current JPA
 * implementations supported.
 *
 * <p>Mirroring can be turned off using the {@link #setMirrorDataChanges(boolean)} to <code>false</code>.
 * It defaults to <code>true<code>.
 *
 * <p>The device allows for {@link NativeJpaExtractor} to be set, for applications
 * that use a framework or by themself wrap the actual <code>EntityManagerFactory</code> implementation.
 *
 * <p>For advance usage, the device allows for {@link EntityManagerWrapper} to be set,
 * allowing to control the creation of <code>EntityManager</code>s, and transactions.
 * The {@link DefaultEntityManagerWrapper} should suffice for most cases.
 *
 * <p>The device extends the parallel device provinding supprot for parallel indexing.
 *
 * @author kimchy
 */
public class JpaGpsDevice extends AbstractParallelGpsDevice implements PassiveMirrorGpsDevice {

    /**
     * Creates a new JpaGpsDevice. Note that its name ({@link #setName(String)}  and
     * entity manager factory ({@link #setEntityManagerFactory(javax.persistence.EntityManagerFactory)}
     * must be set.
     */
    public JpaGpsDevice() {

    }

    /**
     * Creates a new device with a specific name and an entity manager factory.
     */
    public JpaGpsDevice(String name, EntityManagerFactory entityManagerFactory) {
        setName(name);
        setEntityManagerFactory(entityManagerFactory);
    }

    private boolean mirrorDataChanges = true;

    private int fetchCount = 200;

    private EntityManagerFactory entityManagerFactory;

    private EntityManagerWrapper entityManagerWrapper;

    private JpaEntityLifecycleInjector lifecycleInjector;

    private boolean injectEntityLifecycleListener;

    private NativeJpaExtractor nativeJpaExtractor;

    private EntityManagerFactory nativeEntityManagerFactory;

    private JpaEntitiesLocator entitiesLocator;

    private Map<Class<?>, JpaQueryProvider> queryProviderByClass = new HashMap<Class<?>, JpaQueryProvider>();

    private Map<String, JpaQueryProvider> queryProviderByName = new HashMap<String, JpaQueryProvider>();

    private JpaIndexEntitiesIndexer entitiesIndexer;

    protected void doStart() throws CompassGpsException {
        Assert.notNull(entityManagerFactory, buildMessage("Must set JPA EntityManagerFactory"));
        if (entityManagerWrapper == null) {
            entityManagerWrapper = new DefaultEntityManagerWrapper();
        }
        entityManagerWrapper.setUp(entityManagerFactory);

        nativeEntityManagerFactory = entityManagerFactory;
        if (nativeJpaExtractor != null) {
            nativeEntityManagerFactory = nativeJpaExtractor.extractNative(nativeEntityManagerFactory);
            if (nativeEntityManagerFactory == null) {
                throw new JpaGpsDeviceException(buildMessage("Native EntityManager extractor returned null"));
            }
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using native EntityManagerFactory ["
                        + nativeEntityManagerFactory.getClass().getName() + "] extracted by ["
                        + nativeJpaExtractor.getClass().getName() + "]"));
            }
        } else {
            nativeEntityManagerFactory = NativeJpaHelper.extractNativeJpa(entityManagerFactory);
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using native EntityManagerFactory ["
                        + nativeEntityManagerFactory.getClass().getName() + "] using default extractor"));
            }
        }

        if (entitiesLocator == null) {
            entitiesLocator = JpaEntitiesLocatorDetector.detectLocator(nativeEntityManagerFactory, compassGps.getMirrorCompass().getSettings());
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using index entityLocator [" + entitiesLocator.getClass().getName() + "]"));
            }
        }

        injectLifecycle();

        if (entitiesIndexer == null) {
            entitiesIndexer = JpaIndexEntitiesIndexerDetector.detectEntitiesIndexer(nativeEntityManagerFactory, compassGps.getMirrorCompass().getSettings());
        }
        if (log.isDebugEnabled()) {
            log.debug(buildMessage("Using entities indexer [" + entitiesIndexer.getClass().getName() + "]"));
        }
        entitiesIndexer.setJpaGpsDevice(this);
    }

    protected void doStop() throws CompassGpsException {
        removeLifecycle();
    }

    @Override
    public void refresh() throws CompassGpsException {
        if (lifecycleInjector != null && lifecycleInjector.requireRefresh()) {
            removeLifecycle();
            injectLifecycle();
        }
    }

    protected IndexEntity[] doGetIndexEntities() throws CompassGpsException {
        EntityInformation[] entitiesInformation = entitiesLocator.locate(nativeEntityManagerFactory, this);
        // apply specific select statements
        for (EntityInformation entityInformation : entitiesInformation) {
            if (queryProviderByClass.get(entityInformation.getEntityClass()) != null) {
                entityInformation.setQueryProvider(queryProviderByClass.get(entityInformation.getEntityClass()));
            }
            if (queryProviderByName.get(entityInformation.getName()) != null) {
                entityInformation.setQueryProvider(queryProviderByName.get(entityInformation.getName()));
            }
        }
        return entitiesInformation;
    }

    protected IndexEntitiesIndexer doGetIndexEntitiesIndexer() {
        return entitiesIndexer;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return this.entityManagerFactory;
    }

    public EntityManagerFactory getNativeEntityManagerFactory() {
        return this.nativeEntityManagerFactory;
    }

    /**
     * @see org.compass.gps.MirrorDataChangesGpsDevice#isMirrorDataChanges()
     */
    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    /**
     * @see org.compass.gps.MirrorDataChangesGpsDevice#setMirrorDataChanges(boolean)
     */
    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    /**
     * Sets the Jpa <code>EntityManagerFactory</code>. This is manadatory for the Jpa device.
     *
     * @param entityManagerFactory The entity manager factory the device will use.
     */
    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    /**
     * Sets the Entity Manager factory wrapper to control the entity manager operations. This is optional since the
     * device has sensible defaults for it.
     *
     * @param entityManagerWrapper The entity manager wrapper to control the manager operations.
     */
    public void setEntityManagerWrapper(EntityManagerWrapper entityManagerWrapper) {
        this.entityManagerWrapper = entityManagerWrapper;
    }

    /**
     * Returns the Entity Manager factory wrapper to control the entity manager operations.
     */
    public EntityManagerWrapper getEntityManagerWrapper() {
        return entityManagerWrapper;
    }

    /**
     * <p>Sets a specialized native entity manager factory extractor.
     * For applications that use a framework or by themself wrap the actual
     * <code>EntityManagerFactory</code> implementation.
     *
     * The native extractor is mainly used for specialized {@link JpaEntityLifecycleInjector}
     * and {@link JpaEntitiesLocator}.
     */
    public void setNativeExtractor(NativeJpaExtractor nativeJpaExtractor) {
        this.nativeJpaExtractor = nativeJpaExtractor;
    }

    /**
     * Returns the native extractor.
     */
    public NativeJpaExtractor getNativeJpaExtractor() {
        return nativeJpaExtractor;
    }

    /**
     * Sets if the device should try and automatically inject global entity lifecycle
     * listeners using either the provided {@link JpaEntityLifecycleInjector}, or if not
     * set, using the {@link JpaEntityLifecycleInjectorDetector}. Defaults to <code>false</code>.
     */
    public void setInjectEntityLifecycleListener(boolean injectEntityLifecycleListener) {
        this.injectEntityLifecycleListener = injectEntityLifecycleListener;
    }

    /**
     * If the {@link #setLifecycleInjector(org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjector)} is
     * set to <code>true</code>, the global lifecycle injector that will be used to inject global lifecycle
     * event listerens to the underlying implementation of the <code>EntityManagerFactory</code>. If not set,
     * the {@link JpaEntitiesLocatorDetector} will be used to auto-detect it.
     */
    public void setLifecycleInjector(JpaEntityLifecycleInjector lifecycleInjector) {
        this.lifecycleInjector = lifecycleInjector;
    }

    /**
     * Sets a specific enteties locator, which is responsible for locating enteties
     * that need to be indexed. Not a required parameter, since will use the
     * {@link JpaEntitiesLocatorDetector} to auto detect that correct one.
     */
    public void setEntitiesLocator(JpaEntitiesLocator entitiesLocator) {
        this.entitiesLocator = entitiesLocator;
    }

    /**
     * Sets the fetch count for the indexing process. A large number will perform the indexing faster,
     * but will consume more memory. Defaults to <code>200</code>.
     */
    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    /**
     * Returns the fetch count for the indexing process. A large number will perform the indexing faster,
     * but will consume more memory. Default to <code>200</code>.
     */
    public int getFetchCount() {
        return this.fetchCount;
    }

    /**
     * <p>Sets a specific select statement for the index process of the given
     * entity class. The same as {@link #setIndexQueryProvider(Class,JpaQueryProvider)}
     * using {@link org.compass.gps.device.jpa.queryprovider.DefaultJpaQueryProvider}.
     *
     * <p>Certain JPA implementations have specific query providers with possible
     * enhanced functionality in regards to indexing. When using this method
     * instead of providing their specific implementation might mean less functionality
     * from the indexer.
     *
     * <p>Note, this information is used when the device starts.
     *
     * @param entityClass The Entity class to associate the select query with
     * @param selectQuery The select query to execute when indexing the given entity
     */
    public void setIndexSelectQuery(Class<?> entityClass, String selectQuery) {
        setIndexQueryProvider(entityClass, new DefaultJpaQueryProvider(selectQuery));
    }

    /**
     * Sets a specific select statement for the index process of the given
     * entity name. The same as {@link #setIndexQueryProvider(String,JpaQueryProvider)}
     * using {@link org.compass.gps.device.jpa.queryprovider.DefaultJpaQueryProvider}.
     *
     * <p>Certain JPA implementations have specific query providers with possible
     * enhanced functionality in regards to indexing. When using this method
     * instead of providing their specific implementation might mean less functionality
     * from the indexer.
     *
     * <p>Note, this information is used when the device starts.
     *
     * @param entityName  The entity name to associate the select query with
     * @param selectQuery The select query to execute when indexing the given entity
     */
    public void setIndexSelectQuery(String entityName, String selectQuery) {
        setIndexQueryProvider(entityName, new DefaultJpaQueryProvider(selectQuery));
    }

    /**
     * <p>Sets a specific query provider for the index process of the given entity class.
     * <p>Note, this information is used when the device starts.
     *
     * @param entityClass   The Entity class to associate the query provider with
     * @param queryProvider The query provider to execute when indexing the given entity
     */
    public void setIndexQueryProvider(Class<?> entityClass, JpaQueryProvider queryProvider) {
        queryProviderByClass.put(entityClass, queryProvider);
    }

    /**
     * <p>Sets a specific query provider for the index process of the given entity name.
     * <p>Note, this information is used when the device starts.
     *
     * @param entityName    The Entity name to associate the query provider with
     * @param queryProvider The query provider to execute when indexing the given entity
     */
    public void setIndexQueryProvider(String entityName, JpaQueryProvider queryProvider) {
        queryProviderByName.put(entityName, queryProvider);
    }

    /**
     * <p>Sets an index entity info that will control how the given entity will
     * be indexed.
     */
    public void setIndexEntityInfo(JpaIndexEntityInfo indexEntityInfo) {
        if (indexEntityInfo.getEntityName() == null) {
            throw new IllegalArgumentException("entityName must not be null");
        }
        setIndexQueryProvider(indexEntityInfo.getEntityName(), indexEntityInfo.getQueryProvider());
    }

    /**
     * Sets a custom entities indexer that will be used to index the data. By default will
     * be detected automatically based on the actual implemenation of JPA used and will try
     * to use it.
     */
    public void setEntitiesIndexer(JpaIndexEntitiesIndexer entitiesIndexer) {
        this.entitiesIndexer = entitiesIndexer;
    }

    private void injectLifecycle() {
        if (injectEntityLifecycleListener && mirrorDataChanges) {
            if (lifecycleInjector == null) {
                lifecycleInjector = JpaEntityLifecycleInjectorDetector.detectInjector(nativeEntityManagerFactory, compassGps.getMirrorCompass().getSettings());
            }
            if (lifecycleInjector == null) {
                throw new JpaGpsDeviceException(buildMessage("Failed to locate lifecycleInjector"));
            }
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using lifecycleInjector [" + lifecycleInjector.getClass().getName() + "]"));
            }
            lifecycleInjector.injectLifecycle(nativeEntityManagerFactory, this);
        }
    }

    private void removeLifecycle() {
        if (injectEntityLifecycleListener && mirrorDataChanges) {
            lifecycleInjector.removeLifecycle(nativeEntityManagerFactory, this);
        }
    }
}
