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

import java.util.ArrayList;

import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.hibernate.SessionFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * Injects lifecycle listeners directly into Hibernate for mirroring operations.
 *
 * @author kimchy
 */
public class DefaultHibernateEntityLifecycleInjector implements HibernateEntityLifecycleInjector {

    public void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        // TODO add the ability to extend the system and register your own event listener (which maybe performs filtering)
        HibernateEventListener hibernateEventListener = new HibernateEventListener(device);

        PostInsertEventListener[] postInsertEventListeners = eventListeners.getPostInsertEventListeners();
        PostInsertEventListener[] tempPostInsertEventListeners = new PostInsertEventListener[postInsertEventListeners.length + 1];
        System.arraycopy(postInsertEventListeners, 0, tempPostInsertEventListeners, 0, postInsertEventListeners.length);
        tempPostInsertEventListeners[postInsertEventListeners.length] = hibernateEventListener;
        eventListeners.setPostInsertEventListeners(tempPostInsertEventListeners);

        PostUpdateEventListener[] postUpdateEventListeners = eventListeners.getPostUpdateEventListeners();
        PostUpdateEventListener[] tempPostUpdateEventListeners = new PostUpdateEventListener[postUpdateEventListeners.length + 1];
        System.arraycopy(postUpdateEventListeners, 0, tempPostUpdateEventListeners, 0, postUpdateEventListeners.length);
        tempPostUpdateEventListeners[postUpdateEventListeners.length] = hibernateEventListener;
        eventListeners.setPostUpdateEventListeners(tempPostUpdateEventListeners);

        PostDeleteEventListener[] postDeleteEventListeners = eventListeners.getPostDeleteEventListeners();
        PostDeleteEventListener[] tempPostDeleteEventListeners = new PostDeleteEventListener[postDeleteEventListeners.length + 1];
        System.arraycopy(postDeleteEventListeners, 0, tempPostDeleteEventListeners, 0, postDeleteEventListeners.length);
        tempPostDeleteEventListeners[postDeleteEventListeners.length] = hibernateEventListener;
        eventListeners.setPostDeleteEventListeners(tempPostDeleteEventListeners);
    }


    public void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        PostInsertEventListener[] postInsertEventListeners = eventListeners.getPostInsertEventListeners();
        ArrayList tempPostInsertEventListeners = new ArrayList();
        for (int i = 0; i < postInsertEventListeners.length; i++) {
            PostInsertEventListener postInsertEventListener = postInsertEventListeners[i];
            if (!(postInsertEventListener instanceof HibernateEventListener)) {
                tempPostInsertEventListeners.add(postInsertEventListener);
            }
        }
        eventListeners.setPostInsertEventListeners((PostInsertEventListener[]) tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));

        PostUpdateEventListener[] postUpdateEventListeners = eventListeners.getPostUpdateEventListeners();
        ArrayList tempPostUpdateEventListeners = new ArrayList();
        for (int i = 0; i < postUpdateEventListeners.length; i++) {
            PostUpdateEventListener postUpdateEventListener = postUpdateEventListeners[i];
            if (!(postUpdateEventListener instanceof HibernateEventListener)) {
                tempPostUpdateEventListeners.add(postUpdateEventListener);
            }
        }
        eventListeners.setPostUpdateEventListeners((PostUpdateEventListener[]) tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));

        PostDeleteEventListener[] postDeleteEventListeners = eventListeners.getPostDeleteEventListeners();
        ArrayList tempPostDeleteEventListeners = new ArrayList();
        for (int i = 0; i < postDeleteEventListeners.length; i++) {
            PostDeleteEventListener postDeleteEventListener = postDeleteEventListeners[i];
            if (!(postDeleteEventListener instanceof HibernateEventListener)) {
                tempPostDeleteEventListeners.add(postDeleteEventListener);
            }
        }
        eventListeners.setPostDeleteEventListeners((PostDeleteEventListener[]) tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
    }
}