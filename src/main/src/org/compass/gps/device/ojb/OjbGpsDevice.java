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

package org.compass.gps.device.ojb;

import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ojb.broker.PBLifeCycleEvent;
import org.apache.ojb.broker.PBLifeCycleListener;
import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;
import org.compass.gps.device.AbstractGpsDevice;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * An ObJectRelationalBridge (OJB) device, provides support for using ojb and
 * ojb mapping files to index a database. The path can be views as: Database <->
 * OJB <-> Objects <-> Compass::Gps <-> Compass::Core (Search Engine). What it
 * means is that for every object that has both ojb and compass mappings, you
 * will be able to index it's data, as well as real time mirroring of data
 * changes.
 * <p/>
 * Indexing the data (using the <code>index()</code> operation) requires the
 * <code>batchPersistentBroker</code> property to be set, before the
 * <code>index()</code> operation is called.
 * <p/>
 * Real-time mirroring of data changes requires to use the
 * {@link OjbGpsDevice#attachLifecycleListeners(PersistenceBroker)} to let the
 * device listen for any data changes, and
 * {@link OjbGpsDevice#removeLifecycleListeners(PersistenceBroker)} to remove
 * the listener. Since the lifecycle listener can only be set on the instance
 * level, and not the factory level (why would you do that, ojb developers?),
 * attach and remove must be called every time a PersistentBroker is
 * instantiated. You can use the
 * {@link OjbGpsDeviceUtils#attachPersistenceBrokerForIndex(org.compass.gps.CompassGpsDevice, org.apache.ojb.broker.PersistenceBroker)}
 * and
 * {@link OjbGpsDeviceUtils#removePersistenceBrokerForMirror(org.compass.gps.CompassGpsDevice, org.apache.ojb.broker.PersistenceBroker)}
 * as helper methods if attache to a generic device is required (which must be
 * the OjbGpsDevice).
 * <p/>
 * Since the real time mirroring and the event listener registration sounds like
 * an aspect for Ojb aware classes/methods, Compass::Spring utilizes spring
 * support for OJB and aspects for a much simpler event registration, please see
 * Compass::Spring for more documentation.
 *
 * @author kimchy
 */
public class OjbGpsDevice extends AbstractGpsDevice implements PassiveMirrorGpsDevice {

    protected static Log log = LogFactory.getLog(OjbGpsDevice.class);

    private boolean mirrorDataChanges = true;

    private PersistenceBroker indexPersistenceBroker;

    private CompassGpsPBLifecycleListener lifecycleListener;

    public OjbGpsDevice() {

    }

    public OjbGpsDevice(String name, PersistenceBroker indexPersistenceBroker) {
        setName(name);
        this.indexPersistenceBroker = indexPersistenceBroker;
    }

    protected void doIndex(CompassSession session) throws CompassGpsException {
        final PersistenceBroker persistenceBroker = doGetIndexPersistentBroker();
        if (persistenceBroker == null) {
            throw new OjbGpsDeviceException(buildMessage("Must set the index persistent broker"));
        }
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database"));
        }
        final ResourceMapping[] resourceMappings = ((InternalCompass) compassGps.getIndexCompass()).getMapping().getRootMappings();

        for (int i = 0; i < resourceMappings.length; i++) {
            if (!(resourceMappings[i] instanceof ClassMapping)) {
                continue;
            }
            final ClassMapping classMapping = (ClassMapping) resourceMappings[i];
            final Class clazz = classMapping.getClazz();
            if (isFilteredForIndex(clazz.getName())) {
                continue;
            }
            try {
                QueryByCriteria query = new QueryByCriteria(clazz);
                Collection datas = null;
                try {
                    datas = persistenceBroker.getCollectionByQuery(query);
                } catch (Exception e) {
                    // no mapping for the class in ojb
                }
                if (datas == null) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Indexing alias [" + classMapping.getAlias()
                            + "] with object count [" + datas.size() + "]"));
                }
                for (Iterator it = datas.iterator(); it.hasNext();) {
                    session.create(it.next());
                }
            } catch (Exception e) {
                log.error(buildMessage("Failed to index the database"), e);
                throw new OjbGpsDeviceException(buildMessage("Failed to index the database"), e);
            }
        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Finished indexing the database"));
        }
    }

    /**
     * A method which can be used by derived classes to supply the persistent
     * broker by a means of a centrelized registry (for example).
     */
    protected PersistenceBroker doGetIndexPersistentBroker() throws CompassGpsException {
        return getIndexPersistenceBroker();
    }

    protected void doStart() throws CompassGpsException {
        lifecycleListener = new CompassGpsPBLifecycleListener(compassGps, this);
    }

    protected void doStop() throws CompassGpsException {
    }

    /**
     * Attached the OjbGpsDevice lifecycle listener to the instance of the
     * persistence broker.
     *
     * @param pb The persistence broker
     */
    public void attachLifecycleListeners(PersistenceBroker pb) {
        pb.addListener(lifecycleListener);
    }

    /**
     * Removed the OjbGpsDevice lifecycle listener from the instance of the
     * persistence broker.
     *
     * @param pb The persistence broker
     */
    public void removeLifecycleListeners(PersistenceBroker pb) {
        pb.removeListener(lifecycleListener);
    }

    /**
     * Returns the batch persistence broker used for indexing.
     */
    public PersistenceBroker getIndexPersistenceBroker() {
        return indexPersistenceBroker;
    }

    /**
     * Sets the batch persistence broker used for indexing.
     */
    public void setIndexPersistenceBroker(PersistenceBroker indexPersistenceBroker) {
        this.indexPersistenceBroker = indexPersistenceBroker;
    }

    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    private class CompassGpsPBLifecycleListener implements PBLifeCycleListener {

        private OjbGpsDevice ojbGpsDevice;

        private CompassGpsInterfaceDevice compassGps;

        public CompassGpsPBLifecycleListener(CompassGpsInterfaceDevice compassGps, OjbGpsDevice ojbGpsDevice) {
            this.compassGps = compassGps;
            this.ojbGpsDevice = ojbGpsDevice;
        }

        public void beforeInsert(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
        }

        public void afterInsert(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
            if (!ojbGpsDevice.shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }
            final Object entity = lifeCycleEvent.getTarget();
            if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.CREATE)) {
                return;
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("{" + ojbGpsDevice.getName() + "}: Creating [" + entity + "]");
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        session.create(entity);
                    }
                });
            } catch (Exception e) {
                log.error("{" + ojbGpsDevice.getName() + "}: Failed while creating [" + entity + "]", e);
            }
        }

        public void beforeUpdate(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
        }

        public void afterUpdate(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
            if (!ojbGpsDevice.shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }
            final Object entity = lifeCycleEvent.getTarget();
            if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.SAVE)) {
                return;
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("{" + ojbGpsDevice.getName() + "}: Updating [" + entity + "]");
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        session.save(entity);
                    }
                });
            } catch (Exception e) {
                log.error("{" + ojbGpsDevice.getName() + "}: Failed while updating [" + entity + "]", e);
            }
        }

        public void beforeDelete(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
        }

        public void afterDelete(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
            if (!ojbGpsDevice.shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }
            final Object entity = lifeCycleEvent.getTarget();
            if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.DELETE)) {
                return;
            }
            try {
                if (log.isDebugEnabled()) {
                    log.debug("{" + ojbGpsDevice.getName() + "}: Deleting [" + entity + "]");
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        session.delete(entity);
                    }
                });
            } catch (Exception e) {
                log.error("{" + ojbGpsDevice.getName() + "}: Failed while deleting [" + entity + "]", e);
            }
        }

        public void afterLookup(PBLifeCycleEvent lifeCycleEvent) throws PersistenceBrokerException {
        }

    }

}
