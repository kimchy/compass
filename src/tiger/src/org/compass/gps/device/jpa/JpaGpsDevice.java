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

package org.compass.gps.device.jpa;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.compass.core.CompassSession;
import org.compass.core.util.Assert;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;
import org.compass.gps.device.AbstractGpsDevice;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.jpa.entities.JpaEntitiesLocator;
import org.compass.gps.device.jpa.entities.JpaEntitiesLocatorDetector;
import org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjector;
import org.compass.gps.device.jpa.lifecycle.JpaEntityLifecycleInjectorDetector;

/**
 * A Java Persistence API Gps Device (EJB3 Persistence).
 * <p/>
 * The jpa device provides support for using jpa to index a database. The path can
 * be viewed as: Database <-> EntityManager(JPA) <-> Objects <-> Compass::Gps
 * <-> Compass::Core (Search Engine). What it means is that for every object that has both
 * jpa and compass mappings, you will be able to index it's data, as well as real time mirroring of
 * data changes.
 * <p/>
 * When creating the object, an <code>EntityManagerFactory</code> must be provided to the Device.
 * <p/>
 * Indexing uses {@link JpaEntitiesLocator} to locate all the entities that can be
 * indexed (i.e. entities that have both Compass and JPA mappings). Most of the time
 * the {@link org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator} is enough, but
 * special JPA implementation one can be provided. If none is provided, the device will use the {@link
 * JpaEntitiesLocatorDetector} to auto detect the correct locator (which defaults to the ({@link
 * org.compass.gps.device.jpa.entities.DefaultJpaEntitiesLocator}).
 * <p/>
 * Mirroring can be done in two ways. The first one is using JPA official API, implemeting
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
 * <p/>
 * Mirroring can be turned off using the {@link #setMirrorDataChanges(boolean)} to <code>false</code>.
 * It defaults to <code>true<code>.
 * <p/>
 * The device allows for {@link NativeEntityManagerFactoryExtractor} to be set, for applications
 * that use a framework or by themself wrap the actual <code>EntityManagerFactory</code> implementation.
 * <p/>
 * For advance usage, the device allows for {@link EntityManagerWrapper} to be set,
 * allowing to control the creation of <code>EntityManager</code>s, and transactions.
 * The {@link DefaultEntityManagerWrapper} should suffice for most cases.
 *
 * @author kimchy
 */
public class JpaGpsDevice extends AbstractGpsDevice implements PassiveMirrorGpsDevice {

    /**
     * Creates a new JpaGpsDevice. Note that its name ({@link #setName(String)}  and
     * entity manager factory ({@link #setEntityManagerFactory(javax.persistence.EntityManagerFactory)}
     * must be set.
     */
    public JpaGpsDevice() {

    }

    /**
     * Creates a new device with a specific name and an entity manager factory.
     *
     * @param name
     * @param entityManagerFactory
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

    private NativeEntityManagerFactoryExtractor nativeEntityManagerFactoryExtractor;

    private JpaEntitiesLocator entitiesLocator;

    private EntityInformation[] entetiesInformation;

    protected void doStart() throws CompassGpsException {
        Assert.notNull(entityManagerFactory, buildMessage("Must set JPA EntityManagerFactory"));
        if (entityManagerWrapper == null) {
            entityManagerWrapper = new DefaultEntityManagerWrapper();
        }
        entityManagerWrapper.setUp(entityManagerFactory);

        EntityManagerFactory nativeEntityManagerFactory = entityManagerFactory;
        if (nativeEntityManagerFactoryExtractor != null) {
            nativeEntityManagerFactory = nativeEntityManagerFactoryExtractor.extractNative(nativeEntityManagerFactory);
        }

        if (entitiesLocator == null) {
            entitiesLocator = JpaEntitiesLocatorDetector.detectLocator(nativeEntityManagerFactory);
        }
        entetiesInformation = entitiesLocator.locate(nativeEntityManagerFactory, this);

        if (injectEntityLifecycleListener && mirrorDataChanges) {
            if (lifecycleInjector == null) {
                lifecycleInjector = JpaEntityLifecycleInjectorDetector.detectInjector(nativeEntityManagerFactory);
            }
            if (lifecycleInjector == null) {
                throw new JpaGpsDeviceException("Failed to locate lifecycleInjector");
            }
            lifecycleInjector.injectLifecycle(nativeEntityManagerFactory, this);
        }
    }

    protected void doIndex(CompassSession session) throws CompassGpsException {
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database"));
        }
        for (EntityInformation entityInformation : entetiesInformation) {
            try {
                int current = 0;
                entityManagerWrapper.open();
                EntityManager entityManager = entityManagerWrapper.getEntityManager();
                while (true) {
                    Query query = entityManager.createQuery(entityInformation.getSelectQuery());
                    query.setFirstResult(current);
                    query.setMaxResults(current + fetchCount);
                    List results = query.getResultList();
                    for (Object result : results) {
                        session.create(result);
                    }
                    session.evictAll();
                    entityManager.clear();
                    if (results.size() < fetchCount) {
                        break;
                    }
                    current += fetchCount;
                }
                entityManagerWrapper.close();
            } catch (Exception e) {
                log.error(buildMessage("Failed to index the database"), e);
                entityManagerWrapper.closeOnError();
                if (!(e instanceof JpaGpsDeviceException)) {
                    throw new JpaGpsDeviceException(buildMessage("Failed to index the database"), e);
                }
                throw (JpaGpsDeviceException) e;
            }
        }
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
     * Sets the Entity Manager factory wrapper to control the entity manager operations. This is mandatory since the
     * device has sensible defaults for it.
     *
     * @param entityManagerWrapper The entity manager wrapper to control the manager operations.
     */
    public void setEntityManagerWrapper(EntityManagerWrapper entityManagerWrapper) {
        this.entityManagerWrapper = entityManagerWrapper;
    }

    /**
     * Sets a specialized native entity manager factory extractor.
     * For applications that use a framework or by themself wrap the actual
     * <code>EntityManagerFactory</code> implementation.
     * <p/>
     * The native extractor is mainly used for specialized {@link JpaEntityLifecycleInjector}
     * and {@link JpaEntitiesLocator}.
     */
    public void setNativeEntityManagerFactoryExtractor(NativeEntityManagerFactoryExtractor nativeEntityManagerFactoryExtractor) {
        this.nativeEntityManagerFactoryExtractor = nativeEntityManagerFactoryExtractor;
    }

    /**
     * Sets if the device should try and automatically inject global entity lifecycle
     * listeners using either the provided {@link JpaEntityLifecycleInjector}, or if not
     * set, using the {@link JpaEntityLifecycleInjectorDetector}.
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
     *
     * @param fetchCount
     */
    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }
}
