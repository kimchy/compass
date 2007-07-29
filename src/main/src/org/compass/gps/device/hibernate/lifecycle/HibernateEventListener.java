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

package org.compass.gps.device.hibernate.lifecycle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;

/**
 * A default implementation for Hibernate lifecycle callbacks.
 *
 * @author kimchy
 * @see org.compass.gps.device.hibernate.lifecycle.DefaultHibernateEntityLifecycleInjector
 */
public class HibernateEventListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener {

    private Log log = LogFactory.getLog(HibernateEventListener.class);

    private HibernateGpsDevice device;

    private HibernateMirrorFilter mirrorFilter;

    public HibernateEventListener(HibernateGpsDevice device) {
        this.device = device;
        this.mirrorFilter = device.getMirrorFilter();
    }

    public void onPostInsert(final PostInsertEvent postInsertEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postInsertEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.CREATE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterInsert(postInsertEvent)) {
                return;
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(device.buildMessage("Creating [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    ((InternalCompassSession) session).getMarshallingStrategy().marshallIds(entity,
                            postInsertEvent.getId());
                    session.create(entity);
                }
            });
        } catch (Exception e) {
            if (device.isIgnoreMirrorExceptions()) {
                log.error(device.buildMessage("Failed while creating [" + entity + "]"), e);
            } else {
                throw new HibernateGpsDeviceException(device.buildMessage("Failed while creating [" + entity + "]"), e);
            }
        }
    }

    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postUpdateEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.SAVE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterUpdate(postUpdateEvent)) {
                return;
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(device.buildMessage("Updating [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    session.save(entity);
                }
            });
        } catch (Exception e) {
            if (device.isIgnoreMirrorExceptions()) {
                log.error(device.buildMessage("Failed while updating [" + entity + "]"), e);
            } else {
                throw new HibernateGpsDeviceException(device.buildMessage("Failed while updating [" + entity + "]"), e);
            }
        }
    }

    public void onPostDelete(PostDeleteEvent postDeleteEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postDeleteEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), CascadeMapping.Cascade.DELETE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterDelete(postDeleteEvent)) {
                return;
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug(device.buildMessage("Deleting [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    session.delete(entity);
                }
            });
        } catch (Exception e) {
            if (device.isIgnoreMirrorExceptions()) {
                log.error(device.buildMessage("Failed while deleting [" + entity + "]"), e);
            } else {
                throw new HibernateGpsDeviceException(device.buildMessage("Failed while deleting [" + entity + "]"), e);
            }
        }
    }
}
