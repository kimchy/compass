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

package org.compass.gps.device.jdo;

import javax.jdo.PersistenceManagerFactory;
import javax.jdo.listener.DeleteLifecycleListener;
import javax.jdo.listener.InstanceLifecycleEvent;
import javax.jdo.listener.StoreLifecycleListener;

import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;

/**
 * Adds real time monitoring on top of JDO 1 support (see
 * {@link org.compass.gps.device.jdo.JdoGpsDevice}), using JDO 2
 * lifecycle events.
 * 
 * @author kimchy
 * 
 */
public class Jdo2GpsDevice extends JdoGpsDevice implements PassiveMirrorGpsDevice {

    private boolean mirrorDataChanges = true;

    private boolean ignoreMirrorExceptions;

    public Jdo2GpsDevice() {
        super();
    }

    public Jdo2GpsDevice(String name, PersistenceManagerFactory persistenceManagerFactory) {
        super(name, persistenceManagerFactory);
    }

    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    /**
     * Should exceptions be ignored during the mirroring operations (the JDO 2 event listeners).
     * Defaults to <code>false</code>.
     */
    public boolean isIgnoreMirrorExceptions() {
        return ignoreMirrorExceptions;
    }

    /**
     * Should exceptions be ignored during the mirroring operations (the JDO 2 event listeners).
     * Defaults to <code>false</code>.
     */
    public void setIgnoreMirrorExceptions(boolean ignoreMirrorExceptions) {
        this.ignoreMirrorExceptions = ignoreMirrorExceptions;
    }

    protected void doStart() throws CompassGpsException {
        super.doStart();
        if (isMirrorDataChanges()) {
            // TODO if there is a way to get the persistence classes, we can
            // narrow it down with the OSEM classes and provide them instead of
            // null
            persistenceManagerFactory.addInstanceLifecycleListener(new JdoGpsInstanceLifecycleListener(), null);
        }
    }

    private class JdoGpsInstanceLifecycleListener implements DeleteLifecycleListener, StoreLifecycleListener {

        public JdoGpsInstanceLifecycleListener() {
        }

        public void preDelete(InstanceLifecycleEvent event) {
        }

        public void preStore(InstanceLifecycleEvent event) {
        }

        public void postDelete(InstanceLifecycleEvent event) {
            if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }

            final Object entity = event.getSource();
            if (!compassGps.hasMappingForEntityForMirror((entity.getClass()))) {
                return;
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Deleting [" + entity + "]"));
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        session.delete(entity);
                    }
                });
            } catch (Exception e) {
                if (isIgnoreMirrorExceptions()) {
                    log.error(buildMessage("Failed while deleting [" + entity + "]"), e);
                } else {
                    throw new JdoGpsDeviceException(buildMessage("Failed while deleting [" + entity + "]"), e);
                }
            }
        }

        public void postStore(InstanceLifecycleEvent event) {
            if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }

            final Object entity = event.getSource();
            if (!compassGps.hasMappingForEntityForMirror((entity.getClass()))) {
                return;
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Updating [" + entity + "]"));
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        session.save(entity);
                    }
                });
            } catch (Exception e) {
                if (isIgnoreMirrorExceptions()) {
                    log.error(buildMessage("Failed while updating [" + entity + "]"), e);
                } else {
                    throw new JdoGpsDeviceException(buildMessage("Failed while updating [" + entity + "]"), e);
                }
            }
        }
    }
}
