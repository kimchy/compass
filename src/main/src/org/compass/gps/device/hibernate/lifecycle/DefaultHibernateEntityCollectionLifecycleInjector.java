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

import java.util.ArrayList;

import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.hibernate.SessionFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRemoveEventListener;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * @author kimchy
 */
public class DefaultHibernateEntityCollectionLifecycleInjector extends DefaultHibernateEntityLifecycleInjector {

    private Object eventListener;

    public DefaultHibernateEntityCollectionLifecycleInjector() {
        super();
    }

    public DefaultHibernateEntityCollectionLifecycleInjector(boolean registerPostCommitListeneres) {
        super(registerPostCommitListeneres);
    }

    public void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {
        super.injectLifecycle(sessionFactory, device);

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        if (registerPostCommitListeneres) {
            return;
        }

        if (eventListener instanceof PostCollectionRecreateEventListener) {
            PostCollectionRecreateEventListener[] listeners = eventListeners.getPostCollectionRecreateEventListeners();
            PostCollectionRecreateEventListener[] tempListeners = new PostCollectionRecreateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRecreateEventListener) eventListener;
            eventListeners.setPostCollectionRecreateEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionRemoveEventListener) {
            PostCollectionRemoveEventListener[] listeners = eventListeners.getPostCollectionRemoveEventListeners();
            PostCollectionRemoveEventListener[] tempListeners = new PostCollectionRemoveEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionRemoveEventListener) eventListener;
            eventListeners.setPostCollectionRemoveEventListeners(tempListeners);
        }

        if (eventListener instanceof PostCollectionUpdateEventListener) {
            PostCollectionUpdateEventListener[] listeners = eventListeners.getPostCollectionUpdateEventListeners();
            PostCollectionUpdateEventListener[] tempListeners = new PostCollectionUpdateEventListener[listeners.length + 1];
            System.arraycopy(listeners, 0, tempListeners, 0, listeners.length);
            tempListeners[listeners.length] = (PostCollectionUpdateEventListener) eventListener;
            eventListeners.setPostCollectionUpdateEventListeners(tempListeners);
        }
    }

    public void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {
        super.removeLifecycle(sessionFactory, device);

        if (registerPostCommitListeneres) {
            return;
        }

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        PostCollectionRecreateEventListener[] postCollectionRecreateEventListeners = eventListeners.getPostCollectionRecreateEventListeners();
        ArrayList<PostCollectionRecreateEventListener> tempPostCollectionRecreateEventListeners = new ArrayList<PostCollectionRecreateEventListener>();
        for (PostCollectionRecreateEventListener postCollectionRecreateEventListener : postCollectionRecreateEventListeners) {
            if (!(postCollectionRecreateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRecreateEventListeners.add(postCollectionRecreateEventListener);
            }
        }
        eventListeners.setPostCollectionRecreateEventListeners(tempPostCollectionRecreateEventListeners.toArray(new PostCollectionRecreateEventListener[tempPostCollectionRecreateEventListeners.size()]));

        PostCollectionUpdateEventListener[] postCollectionUpdateEventListeners = eventListeners.getPostCollectionUpdateEventListeners();
        ArrayList<PostCollectionUpdateEventListener> tempPostCollectionUpdateEventListeners = new ArrayList<PostCollectionUpdateEventListener>();
        for (PostCollectionUpdateEventListener postCollectionUpdateEventListener : postCollectionUpdateEventListeners) {
            if (!(postCollectionUpdateEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionUpdateEventListeners.add(postCollectionUpdateEventListener);
            }
        }
        eventListeners.setPostCollectionUpdateEventListeners(tempPostCollectionUpdateEventListeners.toArray(new PostCollectionUpdateEventListener[tempPostCollectionUpdateEventListeners.size()]));

        PostCollectionRemoveEventListener[] postCollectionRemoveEventListeners = eventListeners.getPostCollectionRemoveEventListeners();
        ArrayList<PostCollectionRemoveEventListener> tempPostCollectionRemoveEventListeners = new ArrayList<PostCollectionRemoveEventListener>();
        for (PostCollectionRemoveEventListener postCollectionRemoveEventListener : postCollectionRemoveEventListeners) {
            if (!(postCollectionRemoveEventListener instanceof HibernateCollectionEventListener)) {
                tempPostCollectionRemoveEventListeners.add(postCollectionRemoveEventListener);
            }
        }
        eventListeners.setPostCollectionRemoveEventListeners(tempPostCollectionRemoveEventListeners.toArray(new PostCollectionRemoveEventListener[tempPostCollectionRemoveEventListeners.size()]));


        eventListener = null;
    }

    protected Object doCreateListener(HibernateGpsDevice device) {
        eventListener = new HibernateCollectionEventListener(device, marshallIds, pendingCascades, processCollection);
        return eventListener;
    }
}
