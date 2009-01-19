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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.mapping.Cascade;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.event.EventSource;
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

    protected final Log log = LogFactory.getLog(getClass());

    protected final HibernateGpsDevice device;

    protected final HibernateMirrorFilter mirrorFilter;

    protected final boolean marshallIds;

    protected final boolean pendingCascades;

    protected final boolean processCollections;

    private Map<Object, Collection> pendingCreate = Collections.synchronizedMap(new IdentityHashMap<Object, Collection>());

    private Map<Object, Collection> pendingSave = Collections.synchronizedMap(new IdentityHashMap<Object, Collection>());

    public HibernateEventListener(HibernateGpsDevice device, boolean marshallIds, boolean pendingCascades, boolean processCollections) {
        this.device = device;
        this.mirrorFilter = device.getMirrorFilter();
        this.marshallIds = marshallIds;
        this.pendingCascades = pendingCascades;
        this.processCollections = processCollections;
    }

    public void onPostInsert(final PostInsertEvent postInsertEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        final CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postInsertEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), Cascade.CREATE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterInsert(postInsertEvent)) {
                return;
            }
        }

        try {
            if (log.isTraceEnabled()) {
                log.trace(device.buildMessage("Creating [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    doInsert(session, postInsertEvent, entity, compassGps);
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

    public void onPostUpdate(final PostUpdateEvent postUpdateEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        final CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postUpdateEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), Cascade.SAVE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterUpdate(postUpdateEvent)) {
                return;
            }
        }

        Collection<CollectionEntry> collectionsBefore = null;
        if (processCollections) {
            collectionsBefore = new HashSet<CollectionEntry>(postUpdateEvent.getSession().getPersistenceContext().getCollectionEntries().values());
        }
        try {
            if (log.isTraceEnabled()) {
                log.trace(device.buildMessage("Updating [" + entity + "]"));
            }
            compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    doUpdate(session, compassGps, entity, postUpdateEvent.getSession());
                }
            });
        } catch (Exception e) {
            if (device.isIgnoreMirrorExceptions()) {
                log.error(device.buildMessage("Failed while updating [" + entity + "]"), e);
            } else {
                throw new HibernateGpsDeviceException(device.buildMessage("Failed while updating [" + entity + "]"), e);
            }
        } finally {
            if (processCollections) {
                Collection<CollectionEntry> collectionsAfter = postUpdateEvent.getSession().getPersistenceContext().getCollectionEntries().values();
                for (CollectionEntry collection : collectionsAfter) {
                    if (!collectionsBefore.contains(collection)) {
                        collection.setProcessed(true);
                    }
                }
            }
        }
    }

    public void onPostDelete(PostDeleteEvent postDeleteEvent) {
        if (!device.shouldMirrorDataChanges() || device.isPerformingIndexOperation()) {
            return;
        }

        CompassGpsInterfaceDevice compassGps = (CompassGpsInterfaceDevice) device.getGps();

        final Object entity = postDeleteEvent.getEntity();
        if (!compassGps.hasMappingForEntityForMirror(entity.getClass(), Cascade.DELETE)) {
            return;
        }

        if (mirrorFilter != null) {
            if (mirrorFilter.shouldFilterDelete(postDeleteEvent)) {
                return;
            }
        }

        try {
            if (log.isTraceEnabled()) {
                log.trace(device.buildMessage("Deleting [" + entity + "]"));
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

    protected void doInsert(CompassSession session, PostInsertEvent postInsertEvent, Object entity, CompassGpsInterfaceDevice compassGps) {
        if (marshallIds) {
            Serializable id = postInsertEvent.getId();
            postInsertEvent.getPersister().setIdentifier(entity, id, postInsertEvent.getSession().getEntityMode());
        }
        Collection<CollectionEntry> collectionsBefore = null;
        if (processCollections) {
            collectionsBefore = new HashSet<CollectionEntry>(postInsertEvent.getSession().getPersistenceContext().getCollectionEntries().values());
        }
        if (pendingCascades) {
            HibernateEventListenerUtils.registerRemovalHook(postInsertEvent.getSession(), pendingCreate, entity);
            Collection dependencies = HibernateEventListenerUtils.getUnpersistedCascades(compassGps, entity,
                    (SessionFactoryImplementor) device.getSessionFactory(), Cascade.CREATE, new HashSet());
            if (!dependencies.isEmpty()) {
                pendingCreate.put(entity, dependencies);
            } else {
                dependencies = HibernateEventListenerUtils.getAssociatedDependencies(entity, pendingCreate);
                if (!dependencies.isEmpty()) {
                    pendingCreate.put(entity, dependencies);
                } else {
                    session.create(entity);
                }
            }

            HibernateEventListenerUtils.persistPending(session, entity, pendingCreate, true);
        } else {
            session.create(entity);
        }
        if (processCollections) {
            Collection<CollectionEntry> collectionsAfter = postInsertEvent.getSession().getPersistenceContext().getCollectionEntries().values();
            for (CollectionEntry collection : collectionsAfter) {
                if (!collectionsBefore.contains(collection)) {
                    collection.setProcessed(true);
                }
            }
        }
    }

    protected void doUpdate(CompassSession session, CompassGpsInterfaceDevice compassGps, Object entity, EventSource eventSource) {
        if (pendingCascades) {
            HibernateEventListenerUtils.registerRemovalHook(eventSource, pendingSave, entity);
            Collection dependencies = HibernateEventListenerUtils.getUnpersistedCascades(compassGps, entity,
                    (SessionFactoryImplementor) device.getSessionFactory(), Cascade.SAVE, new HashSet());
            if (!dependencies.isEmpty()) {
                pendingSave.put(entity, dependencies);
            } else {
                dependencies = HibernateEventListenerUtils.getAssociatedDependencies(entity, pendingSave);
                if (!dependencies.isEmpty()) {
                    pendingSave.put(entity, dependencies);
                } else {
                    session.save(entity);
                }
            }

            HibernateEventListenerUtils.persistPending(session, entity, pendingSave, false);
        } else {
            session.save(entity);
        }
    }
}
