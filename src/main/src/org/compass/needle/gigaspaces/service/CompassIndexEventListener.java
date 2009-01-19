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

package org.compass.needle.gigaspaces.service;

import com.gigaspaces.events.NotifyActionType;
import com.gigaspaces.events.batching.BatchRemoteEvent;
import com.j_spaces.core.client.EntryArrivedRemoteEvent;
import org.compass.core.Compass;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTemplate;
import org.compass.core.mapping.Cascade;
import org.compass.core.spi.InternalCompassSession;
import org.openspaces.core.GigaSpace;
import org.openspaces.events.SpaceDataEventListener;
import org.springframework.transaction.TransactionStatus;

/**
 * A space data event listener that should be registered with a notify container (probably with all its notify flag
 * set: write, update, take, and lease expiration). Using the provided event it either index the given object
 * (if it is a write or an update) or deletes it from the index (if it is a take or lease expiration).
 *
 * <p>The Compass instance is passed, probably configured to store the index on the Space it listens for
 * notificaitons on in a collocated manner.
 *
 * <p>Note, batching should probably be used to increase indexing perfomance.
 *
 * @author kimchy
 */
public class CompassIndexEventListener implements SpaceDataEventListener {

    private CompassTemplate compassTemplate;

    public CompassIndexEventListener(Compass compass) {
        this.compassTemplate = new CompassTemplate(compass);
    }

    public void onEvent(final Object event, final GigaSpace gigaSpace, final TransactionStatus transactionStatus, final Object source) {
        if (event instanceof Object[]) {
            final Object[] events = (Object[]) event;
            final BatchRemoteEvent batchRemoteEvent = (BatchRemoteEvent) source;
            compassTemplate.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    for (int i = 0; i < events.length; i++) {
                        performOperation(session, events[i], (EntryArrivedRemoteEvent) batchRemoteEvent.getEvents()[i]);
                    }
                }
            });
        } else {
            final EntryArrivedRemoteEvent remoteEvent = (EntryArrivedRemoteEvent) source;
            compassTemplate.execute(new CompassCallbackWithoutResult() {
                protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                    performOperation(session, event, remoteEvent);
                }
            });
        }
    }

    private void performOperation(CompassSession session, Object event, EntryArrivedRemoteEvent remoteEvent) {
        if (remoteEvent.getNotifyActionType() == NotifyActionType.NOTIFY_LEASE_EXPIRATION || remoteEvent.getNotifyActionType() == NotifyActionType.NOTIFY_TAKE) {
            if (((InternalCompassSession) session).getMapping().hasMappingForClass(event.getClass(), Cascade.DELETE)) {
                session.delete(event);
            }
        } else
        if (remoteEvent.getNotifyActionType() == NotifyActionType.NOTIFY_UPDATE || remoteEvent.getNotifyActionType() == NotifyActionType.NOTIFY_WRITE) {
            if (((InternalCompassSession) session).getMapping().hasMappingForClass(event.getClass(), Cascade.SAVE)) {
                session.save(event);
            }
        }
    }
}
