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

package org.compass.gps.device.jpa.lifecycle;

import java.util.ArrayList;
import javax.persistence.EntityManagerFactory;

import org.compass.gps.device.jpa.AbstractDeviceJpaEntityListener;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.hibernate.ejb.HibernateEntityManagerFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * Injects lifecycle listeners directly into Hibernate for mirroring operations.
 *
 * @author kimchy
 */
public class HibernateJpaEntityLifecycleInjector implements JpaEntityLifecycleInjector {

    private class HibernateEventListener extends AbstractDeviceJpaEntityListener implements PostInsertEventListener,
            PostUpdateEventListener, PostDeleteEventListener {

        private JpaGpsDevice device;

        public HibernateEventListener(JpaGpsDevice device) {
            this.device = device;
        }

        protected JpaGpsDevice getDevice() {
            return this.device;
        }

        public void onPostInsert(PostInsertEvent postInsertEvent) {
            postPersist(postInsertEvent.getEntity());
        }

        public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
            postUpdate(postUpdateEvent.getEntity());
        }

        public void onPostDelete(PostDeleteEvent postDeleteEvent) {
            postRemove(postDeleteEvent.getEntity());
        }
    }

    public void injectLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device)
            throws JpaGpsDeviceException {

        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) entityManagerFactory;
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();
        EventListeners eventListeners = sessionFactory.getEventListeners();

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


    public void removeLifecycle(EntityManagerFactory entityManagerFactory, JpaGpsDevice device) throws JpaGpsDeviceException {
        HibernateEntityManagerFactory hibernateEntityManagerFactory =
                (HibernateEntityManagerFactory) entityManagerFactory;
        SessionFactoryImpl sessionFactory = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();
        EventListeners eventListeners = sessionFactory.getEventListeners();

        PostInsertEventListener[] postInsertEventListeners = eventListeners.getPostInsertEventListeners();
        ArrayList<PostInsertEventListener> tempPostInsertEventListeners = new ArrayList<PostInsertEventListener>();
        for (PostInsertEventListener postInsertEventListener : postInsertEventListeners) {
            if (!(postInsertEventListener instanceof HibernateEventListener)) {
                tempPostInsertEventListeners.add(postInsertEventListener);
            }
        }
        eventListeners.setPostInsertEventListeners(tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));

        PostUpdateEventListener[] postUpdateEventListeners = eventListeners.getPostUpdateEventListeners();
        ArrayList<PostUpdateEventListener> tempPostUpdateEventListeners = new ArrayList<PostUpdateEventListener>();
        for (PostUpdateEventListener postUpdateEventListener : postUpdateEventListeners) {
            if (!(postUpdateEventListener instanceof HibernateEventListener)) {
                tempPostUpdateEventListeners.add(postUpdateEventListener);
            }
        }
        eventListeners.setPostUpdateEventListeners(tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));

        PostDeleteEventListener[] postDeleteEventListeners = eventListeners.getPostDeleteEventListeners();
        ArrayList<PostDeleteEventListener> tempPostDeleteEventListeners = new ArrayList<PostDeleteEventListener>();
        for (PostDeleteEventListener postDeleteEventListener : postDeleteEventListeners) {
            if (!(postDeleteEventListener instanceof HibernateEventListener)) {
                tempPostDeleteEventListeners.add(postDeleteEventListener);
            }
        }
        eventListeners.setPostDeleteEventListeners(tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
    }
}
