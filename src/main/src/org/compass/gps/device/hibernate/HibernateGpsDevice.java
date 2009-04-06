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

package org.compass.gps.device.hibernate;

import java.util.HashMap;
import java.util.Map;

import org.compass.core.util.Assert;
import org.compass.core.util.ClassUtils;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;
import org.compass.gps.device.hibernate.entities.DefaultHibernateEntitiesLocator;
import org.compass.gps.device.hibernate.entities.EntityInformation;
import org.compass.gps.device.hibernate.entities.HibernateEntitiesLocator;
import org.compass.gps.device.hibernate.indexer.HibernateIndexEntitiesIndexer;
import org.compass.gps.device.hibernate.indexer.ScrollableHibernateIndexEntitiesIndexer;
import org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityLifecycleInjector;
import org.compass.gps.device.hibernate.lifecycle.HibernateEntityLifecycleInjector;
import org.compass.gps.device.hibernate.lifecycle.HibernateMirrorFilter;
import org.compass.gps.device.support.parallel.AbstractParallelGpsDevice;
import org.compass.gps.device.support.parallel.IndexEntitiesIndexer;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.SessionFactory;

/**
 * <p>A Hibernate Gps Device.
 *
 * <p>The hibernate device provides support for using jpa to index a database. The path can
 * be viewed as: Database <-> Hibernate <-> Objects <-> Compass::Gps
 * <-> Compass::Core (Search Engine). What it means is that for every object that has both
 * Hibernate and compass mappings, you will be able to index it's data, as well as real time mirroring of
 * data changes.
 *
 * <p>When creating the object, a <code>SessionFactory</code> must be provided to the Device.
 *
 * <p>Indexing uses {@link HibernateEntitiesLocator} to locate all the entities that can be
 * indexed (i.e. entities that have both Compass and Hibernate mappings). The default implementaion
 * used it the {@link org.compass.gps.device.hibernate.entities.DefaultHibernateEntitiesLocator}.
 *
 * <p>The indexing process itself is done through an implementation of
 * {@link HibernateIndexEntitiesIndexer}. It has two different implementation, the
 * {@link org.compass.gps.device.hibernate.indexer.PaginationHibernateIndexEntitiesIndexer} and the
 * {@link org.compass.gps.device.hibernate.indexer.ScrollableHibernateIndexEntitiesIndexer}. The default
 * used is the scrollable indexer.
 *
 * <p>Mirroring is done by injecting lifecycle listeners into Hibernate. It is done using
 * {@link org.compass.gps.device.hibernate.lifecycle.HibernateEntityLifecycleInjector} with
 * a default implementation of {@link org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityLifecycleInjector}
 * when using Hibernate version < 3.2.6 and {@link org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityCollectionLifecycleInjector}
 * when using Hibernate version >= 3.2.6.
 *
 * <p>Mirroring can be turned off using the {@link #setMirrorDataChanges(boolean)} to <code>false</code>.
 * It defaults to <code>true<code>.
 *
 * <p>The device allows for {@link org.compass.gps.device.hibernate.NativeHibernateExtractor} to be set,
 * for applications that use a framework or by themself wrap the actual
 * <code>SessionFactory</code> implementation.
 *
 * <p>The device extends the parallel device provinding supprot for parallel indexing.
 *
 * @author kimchy
 */
public class HibernateGpsDevice extends AbstractParallelGpsDevice implements PassiveMirrorGpsDevice {

    private SessionFactory sessionFactory;

    private boolean mirrorDataChanges = true;

    private int fetchCount = 200;

    private HibernateEntitiesLocator entitiesLocator;

    private HibernateEntityLifecycleInjector lifecycleInjector;

    private boolean ignoreMirrorExceptions;

    private HibernateMirrorFilter mirrorFilter;

    private NativeHibernateExtractor nativeExtractor;

    private HibernateIndexEntitiesIndexer entitiesIndexer;

    private Map<Class, HibernateQueryProvider> queryProviderByClass = new HashMap<Class, HibernateQueryProvider>();

    private Map<String, HibernateQueryProvider> queryProviderByName = new HashMap<String, HibernateQueryProvider>();


    private SessionFactory nativeSessionFactory;

    public HibernateGpsDevice() {

    }

    public HibernateGpsDevice(String name, SessionFactory sessionFactory) {
        setName(name);
        setSessionFactory(sessionFactory);
    }

    protected void doStart() throws CompassGpsException {
        Assert.notNull(sessionFactory, buildMessage("Must set Hibernate SessionFactory"));

        nativeSessionFactory = sessionFactory;
        if (nativeExtractor != null) {
            nativeSessionFactory = nativeExtractor.extractNative(sessionFactory);
            if (nativeSessionFactory == null) {
                throw new HibernateGpsDeviceException(buildMessage("Native SessionFactory extractor returned null"));
            }
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using native SessionFactory [" + nativeSessionFactory.getClass().getName() + "] extracted by [" + nativeExtractor.getClass().getName() + "]"));
            }
        }

        if (entitiesLocator == null) {
            entitiesLocator = new DefaultHibernateEntitiesLocator();
        }
        if (log.isDebugEnabled()) {
            log.debug(buildMessage("Using index entityLocator [" + entitiesLocator.getClass().getName() + "]"));
        }

        if (mirrorDataChanges) {
            if (lifecycleInjector == null) {
                try {
                    ClassUtils.forName("org.hibernate.event.PostCollectionRecreateEventListener", compassGps.getMirrorCompass().getSettings().getClassLoader());
                    lifecycleInjector = (HibernateEntityLifecycleInjector) ClassUtils.forName("org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityCollectionLifecycleInjector",
                            compassGps.getMirrorCompass().getSettings().getClassLoader()).newInstance();
                } catch (Exception e) {
                    lifecycleInjector = new DefaultHibernateEntityLifecycleInjector();
                }
            }
            if (log.isDebugEnabled()) {
                log.debug(buildMessage("Using lifecycleInjector [" + lifecycleInjector.getClass().getName() + "]"));
            }
            lifecycleInjector.injectLifecycle(nativeSessionFactory, this);
        }

        if (entitiesIndexer == null) {
            entitiesIndexer = new ScrollableHibernateIndexEntitiesIndexer();
        }
        if (log.isDebugEnabled()) {
            log.debug(buildMessage("Using entities indexer [" + entitiesIndexer.getClass().getName() + "]"));
        }
        entitiesIndexer.setHibernateGpsDevice(this);
    }

    protected void doStop() throws CompassGpsException {
        if (mirrorDataChanges) {
            lifecycleInjector.removeLifecycle(nativeSessionFactory, this);
        }
    }

    protected IndexEntity[] doGetIndexEntities() throws CompassGpsException {
        EntityInformation[] entitiesInformation = entitiesLocator.locate(nativeSessionFactory, this);
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

    /**
     * Sets the Hibernate <code>SessionFactory</code> to be used before the start operation.
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
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
     * @see org.compass.gps.MirrorDataChangesGpsDevice#isMirrorDataChanges()
     */
    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    /**
     * Should exceptions be ignored during the mirroring operations (the Hibernate event listeners).
     * Defaults to <code>false</code>.
     */
    public boolean isIgnoreMirrorExceptions() {
        return ignoreMirrorExceptions;
    }

    /**
     * Should exceptions be ignored during the mirroring operations (the Hibernate event listeners).
     * Defaults to <code>false</code>.
     */
    public void setIgnoreMirrorExceptions(boolean ignoreMirrorExceptions) {
        this.ignoreMirrorExceptions = ignoreMirrorExceptions;
    }

    /**
     * @see org.compass.gps.MirrorDataChangesGpsDevice#setMirrorDataChanges(boolean)
     */
    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    /**
     * Sets a pluggable index entities locator allowing to control the indexes entties that
     * will be used. Defaults to {@link org.compass.gps.device.hibernate.entities.DefaultHibernateEntitiesLocator}.
     */
    public void setEntitiesLocator(HibernateEntitiesLocator entitiesLocator) {
        this.entitiesLocator = entitiesLocator;
    }

    /**
     * Returns mirroring filter that can filter hibernate mirror events. If no mirror filter is set
     * no filtering will happen.
     */
    public HibernateMirrorFilter getMirrorFilter() {
        return mirrorFilter;
    }

    /**
     * Sets a mirroring filter that can filter hibernate mirror events. If no mirror filter is set
     * no filtering will happen.
     *
     * @param mirrorFilter The mirror filter handler
     */
    public void setMirrorFilter(HibernateMirrorFilter mirrorFilter) {
        this.mirrorFilter = mirrorFilter;
    }

    /**
     * Sets a native Hibernate extractor to work with frameworks that wrap the actual
     * SessionFactory.
     */
    public void setNativeExtractor(NativeHibernateExtractor nativeExtractor) {
        this.nativeExtractor = nativeExtractor;
    }

    /**
     * Sets a custom entities indexer allowing to control the indexing process.
     * Defaults to {@link org.compass.gps.device.hibernate.indexer.PaginationHibernateIndexEntitiesIndexer}.
     */
    public void setEntitiesIndexer(HibernateIndexEntitiesIndexer entitiesIndexer) {
        this.entitiesIndexer = entitiesIndexer;
    }

    /**
     * Sets a custom lifecycle injector controlling the injection of Hibernate lifecycle
     * listeners for mirroring operations. Defaults to {@link org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityLifecycleInjector}.
     */
    public void setLifecycleInjector(HibernateEntityLifecycleInjector lifecycleInjector) {
        this.lifecycleInjector = lifecycleInjector;
    }

    /**
     * <p>Sets a specific select statement for the index process of the given
     * entity class.
     *
     * <p>Note, when using {@link org.compass.gps.device.hibernate.indexer.ScrollableHibernateIndexEntitiesIndexer}
     * it is preferable not to use this mehotd, instead use
     * {@link #setIndexQueryProvider(Class, HibernateQueryProvider)} and return a
     * Hibernate <code>Criteria</code> object instead.
     *
     * <p>Note, this information is used when the device starts.
     *
     * @param entityClass The Entity class to associate the select query with
     * @param selectQuery The select query to execute when indexing the given entity
     */
    public void setIndexSelectQuery(Class entityClass, String selectQuery) {
        setIndexQueryProvider(entityClass, new DefaultHibernateQueryProvider(selectQuery));
    }

    /**
     * Sets a specific select statement for the index process of the given
     * entity name.
     *
     * <p>Note, when using {@link org.compass.gps.device.hibernate.indexer.ScrollableHibernateIndexEntitiesIndexer}
     * it is preferable not to use this mehotd, instead use
     * {@link #setIndexQueryProvider(String, HibernateQueryProvider)} and return a
     * Hibernate <code>Criteria</code> object instead.
     *
     * <p>Note, this information is used when the device starts.
     *
     * @param entityName  The entity name to associate the select query with
     * @param selectQuery The select query to execute when indexing the given entity
     */
    public void setIndexSelectQuery(String entityName, String selectQuery) {
        setIndexQueryProvider(entityName, new DefaultHibernateQueryProvider(selectQuery));
    }

    /**
     * Sets a specific query provider for the index process of the given entity class.
     * <p>Note, this information is used when the device starts.
     *
     * @param entityClass   The Entity class to associate the query provider with
     * @param queryProvider The query provider to execute when indexing the given entity
     */
    public void setIndexQueryProvider(Class entityClass, HibernateQueryProvider queryProvider) {
        queryProviderByClass.put(entityClass, queryProvider);
    }

    /**
     * Sets a specific query provider for the index process of the given entity name.
     * <p>Note, this information is used when the device starts.
     *
     * @param entityName    The Entity name to associate the query provider with
     * @param queryProvider The query provider to execute when indexing the given entity
     */
    public void setIndexQueryProvider(String entityName, HibernateQueryProvider queryProvider) {
        queryProviderByName.put(entityName, queryProvider);
    }

    /**
     * Allows to set {@link org.compass.gps.device.hibernate.HibernateEntityIndexInfo} which results
     * in calling {@link #setIndexQueryProvider(String, HibernateQueryProvider)}.
     */
    public void setindexEntityInfo(HibernateEntityIndexInfo indexInfo) {
        if (indexInfo.getEntityName() == null) {
            throw new IllegalArgumentException("entityName must be provided");
        }
        setIndexQueryProvider(indexInfo.getEntityName(), indexInfo.getQueryProvider());
    }

    /**
     * Allows to set an array of {@link org.compass.gps.device.hibernate.HibernateEntityIndexInfo} which results
     * in calling {@link #setIndexQueryProvider(String, HibernateQueryProvider)}.
     */
    public void setindexEntityInfos(HibernateEntityIndexInfo[] indexInfos) {
        for (HibernateEntityIndexInfo indexInfo : indexInfos) {
            if (indexInfo.getEntityName() == null) {
                throw new IllegalArgumentException("entityName must be provided");
            }
            setIndexQueryProvider(indexInfo.getEntityName(), indexInfo.getQueryProvider());
        }
    }

    /**
     * Returns a native Hibernate extractor to work with frameworks that wrap the actual
     * SessionFactory.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
