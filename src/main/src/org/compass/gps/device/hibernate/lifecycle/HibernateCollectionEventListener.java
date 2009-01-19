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

package org.compass.gps.device.hibernate.lifecycle;

import java.io.Serializable;

import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.mapping.Cascade;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.engine.EntityEntry;
import org.hibernate.event.AbstractCollectionEvent;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRemoveEvent;
import org.hibernate.event.PostCollectionRemoveEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PostCollectionUpdateEventListener;

/**
 * @author kimchy
 */
public class HibernateCollectionEventListener extends HibernateEventListener implements PostCollectionRecreateEventListener,
        PostCollectionRemoveEventListener, PostCollectionUpdateEventListener {

    public HibernateCollectionEventListener(HibernateGpsDevice device, boolean marshallIds, boolean pendingCascades, boolean processCollections) {
        super(device, marshallIds, pendingCascades, processCollections);
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

    private void processCollectionEvent(final AbstractCollectionEvent event) {
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

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterCollection(event)) {
                return;
            }
        }

        final CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), Cascade.SAVE)) {
            return;
        }

        Serializable id = getId(entity, event);
        if (id == null) {
            log.warn("Unable to reindex entity on collection change, id cannot be extracted: " + event.getAffectedOwnerEntityName());
            return;
        }
        try {
            if (log.isTraceEnabled()) {
                log.trace(device.buildMessage("Updating [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    doUpdate(session, compassGps, entity, event.getSession());
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

    private Serializable getId(Object entity, AbstractCollectionEvent event) {
        Serializable id = event.getAffectedOwnerIdOrNull();
        if (id == null) {
            //most likely this recovery is unnecessary since Hibernate Core probably try that
            EntityEntry entityEntry = event.getSession().getPersistenceContext().getEntry(entity);
            id = entityEntry == null ? null : entityEntry.getId();
        }
        return id;
    }

}
