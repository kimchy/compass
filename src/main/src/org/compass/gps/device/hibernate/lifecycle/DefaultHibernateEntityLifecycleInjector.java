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
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.impl.SessionFactoryImpl;

/**
 * Injects lifecycle listeners directly into Hibernate for mirroring operations.
 *
 * <p>By default, registers with plain insert/update/delete listeners, which will be triggered
 * by Hibernate before committing (and up to Hibernate flushing logic). Also allows to be created
 * with setting the <code>registerPostCommitListeneres</code> to <code>true</code> which will cause
 * the insert/update/delete listeneres to be registered as post commit events.
 *
 * @author kimchy
 */
public class DefaultHibernateEntityLifecycleInjector implements HibernateEntityLifecycleInjector {

    protected boolean registerPostCommitListeneres = false;

    protected boolean marshallIds = false;

    protected boolean pendingCascades = true;

    protected boolean processCollection = true;

    public DefaultHibernateEntityLifecycleInjector() {
        this(false);
    }

    /**
     * Creates a new lifecycle injector. Allows to control if the insert/update/delete
     * even listeners will be registered with post commit listeres (flag it <code>true</code>)
     * or with plain post events (triggered based on Hibrenate flushing logic).
     *
     * @param registerPostCommitListeneres <code>true</code> if post commit listeners will be
     *                                     registered. <code>false</code> for plain listeners.
     */
    public DefaultHibernateEntityLifecycleInjector(boolean registerPostCommitListeneres) {
        this.registerPostCommitListeneres = registerPostCommitListeneres;
    }

    /**
     * Should the listener try and marshall ids for the event listener of post insert. Some
     * Hibernate versions won't put the generated ids in the object that is inserted. Defaults
     * to <code>false</code>.
     */
    public void setMarshallIds(boolean marshallIds) {
        this.marshallIds = marshallIds;
    }

    /**
     * Should the listener try and handle pending cascades avoiding trying to save/update relationships in Compass
     * before they were processed by Hibernate. Default to <code>true<code>.
     *
     * <p>Note, if set, might cause Compass event processing to be a *tad* slower.
     */
    public void setPendingCascades(boolean pendingCascades) {
        this.pendingCascades = pendingCascades;
    }

    /**
     * Should the event listener automatically set the processed flag on collections that are created as a result
     * of the marshalling process of Compass. Defaults to <code>true</code>.
     */
    public void setProcessCollection(boolean processCollection) {
        this.processCollection = processCollection;
    }

    public void injectLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        Object hibernateEventListener = doCreateListener(device);

        if (hibernateEventListener instanceof PostInsertEventListener) {
            PostInsertEventListener[] postInsertEventListeners;
            if (registerPostCommitListeneres) {
                postInsertEventListeners = eventListeners.getPostCommitInsertEventListeners();
            } else {
                postInsertEventListeners = eventListeners.getPostInsertEventListeners();
            }
            PostInsertEventListener[] tempPostInsertEventListeners = new PostInsertEventListener[postInsertEventListeners.length + 1];
            System.arraycopy(postInsertEventListeners, 0, tempPostInsertEventListeners, 0, postInsertEventListeners.length);
            tempPostInsertEventListeners[postInsertEventListeners.length] = (PostInsertEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                eventListeners.setPostCommitInsertEventListeners(tempPostInsertEventListeners);
            } else {
                eventListeners.setPostInsertEventListeners(tempPostInsertEventListeners);
            }
        }

        if (hibernateEventListener instanceof PostUpdateEventListener) {
            PostUpdateEventListener[] postUpdateEventListeners;
            if (registerPostCommitListeneres) {
                postUpdateEventListeners = eventListeners.getPostCommitUpdateEventListeners();
            } else {
                postUpdateEventListeners = eventListeners.getPostUpdateEventListeners();
            }
            PostUpdateEventListener[] tempPostUpdateEventListeners = new PostUpdateEventListener[postUpdateEventListeners.length + 1];
            System.arraycopy(postUpdateEventListeners, 0, tempPostUpdateEventListeners, 0, postUpdateEventListeners.length);
            tempPostUpdateEventListeners[postUpdateEventListeners.length] = (PostUpdateEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                eventListeners.setPostCommitUpdateEventListeners(tempPostUpdateEventListeners);
            } else {
                eventListeners.setPostUpdateEventListeners(tempPostUpdateEventListeners);
            }
        }

        if (hibernateEventListener instanceof PostDeleteEventListener) {
            PostDeleteEventListener[] postDeleteEventListeners;
            if (registerPostCommitListeneres) {
                postDeleteEventListeners = eventListeners.getPostCommitDeleteEventListeners();
            } else {
                postDeleteEventListeners = eventListeners.getPostDeleteEventListeners();
            }
            PostDeleteEventListener[] tempPostDeleteEventListeners = new PostDeleteEventListener[postDeleteEventListeners.length + 1];
            System.arraycopy(postDeleteEventListeners, 0, tempPostDeleteEventListeners, 0, postDeleteEventListeners.length);
            tempPostDeleteEventListeners[postDeleteEventListeners.length] = (PostDeleteEventListener) hibernateEventListener;
            if (registerPostCommitListeneres) {
                eventListeners.setPostCommitDeleteEventListeners(tempPostDeleteEventListeners);
            } else {
                eventListeners.setPostDeleteEventListeners(tempPostDeleteEventListeners);
            }
        }
    }

    public void removeLifecycle(SessionFactory sessionFactory, HibernateGpsDevice device) throws HibernateGpsDeviceException {

        SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
        EventListeners eventListeners = sessionFactoryImpl.getEventListeners();

        PostInsertEventListener[] postInsertEventListeners;
        if (registerPostCommitListeneres) {
            postInsertEventListeners = eventListeners.getPostCommitInsertEventListeners();
        } else {
            postInsertEventListeners = eventListeners.getPostInsertEventListeners();
        }
        ArrayList tempPostInsertEventListeners = new ArrayList();
        for (int i = 0; i < postInsertEventListeners.length; i++) {
            PostInsertEventListener postInsertEventListener = postInsertEventListeners[i];
            if (!(postInsertEventListener instanceof HibernateEventListener)) {
                tempPostInsertEventListeners.add(postInsertEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            eventListeners.setPostCommitInsertEventListeners((PostInsertEventListener[]) tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));
        } else {
            eventListeners.setPostInsertEventListeners((PostInsertEventListener[]) tempPostInsertEventListeners.toArray(new PostInsertEventListener[tempPostInsertEventListeners.size()]));
        }

        PostUpdateEventListener[] postUpdateEventListeners;
        if (registerPostCommitListeneres) {
            postUpdateEventListeners = eventListeners.getPostCommitUpdateEventListeners();
        } else {
            postUpdateEventListeners = eventListeners.getPostUpdateEventListeners();
        }
        ArrayList tempPostUpdateEventListeners = new ArrayList();
        for (int i = 0; i < postUpdateEventListeners.length; i++) {
            PostUpdateEventListener postUpdateEventListener = postUpdateEventListeners[i];
            if (!(postUpdateEventListener instanceof HibernateEventListener)) {
                tempPostUpdateEventListeners.add(postUpdateEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            eventListeners.setPostCommitUpdateEventListeners((PostUpdateEventListener[]) tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));
        } else {
            eventListeners.setPostUpdateEventListeners((PostUpdateEventListener[]) tempPostUpdateEventListeners.toArray(new PostUpdateEventListener[tempPostUpdateEventListeners.size()]));
        }

        PostDeleteEventListener[] postDeleteEventListeners;
        if (registerPostCommitListeneres) {
            postDeleteEventListeners = eventListeners.getPostCommitDeleteEventListeners();
        } else {
            postDeleteEventListeners = eventListeners.getPostDeleteEventListeners();
        }
        ArrayList tempPostDeleteEventListeners = new ArrayList();
        for (int i = 0; i < postDeleteEventListeners.length; i++) {
            PostDeleteEventListener postDeleteEventListener = postDeleteEventListeners[i];
            if (!(postDeleteEventListener instanceof HibernateEventListener)) {
                tempPostDeleteEventListeners.add(postDeleteEventListener);
            }
        }
        if (registerPostCommitListeneres) {
            eventListeners.setPostCommitDeleteEventListeners((PostDeleteEventListener[]) tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
        } else {
            eventListeners.setPostDeleteEventListeners((PostDeleteEventListener[]) tempPostDeleteEventListeners.toArray(new PostDeleteEventListener[tempPostDeleteEventListeners.size()]));
        }
    }

    protected Object doCreateListener(HibernateGpsDevice device) {
        return new HibernateEventListener(device, marshallIds, pendingCascades, processCollection);
    }
}