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

package org.compass.gps.device.hibernate;

import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.FieldInvoker;
import org.compass.gps.CompassGpsException;
import org.compass.gps.PassiveMirrorGpsDevice;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.event.*;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.tuple.EntityMetamodel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A {@link HibernateGpsDevice} which works with hibernate 3.
 * <p/>
 * You must either set the Hibernate <code>Configuration</code> or the
 * <code>SessionFactory</code> to be used by the device. Note that if the
 * <code>Configuration</code> is supplied, when the device <code>start</code>
 * is called, a new <code>SessionFactory</code> will be built.
 * <p/>
 * Provides support for real time index updates using the new Hibernate 3 event
 * system. The device uses the <code>PostInsertEventListener</code>,
 * <code>PostUpdateEventListener</code>, and
 * <code>PostDeleteEventListener</code> events.
 *
 * @author kimchy
 */
public class Hibernate3GpsDevice extends AbstractHibernateGpsDevice implements PassiveMirrorGpsDevice {

    private class Hibernate3SessionWrapper implements HibernateSessionWrapper {

        private SessionFactory sessionFactory;

        private Session session;

        private Transaction tr;

        public Hibernate3SessionWrapper(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public Session getSession() {
            return session;
        }

        public void open() throws HibernateGpsDeviceException {
            try {
                session = sessionFactory.openSession();
            } catch (HibernateException e) {
                throw new HibernateGpsDeviceException(buildMessage("Failed to open session to fetch data"), e);
            }
            try {
                tr = session.beginTransaction();
            } catch (HibernateException e) {
                throw new HibernateGpsDeviceException(buildMessage("Failed to begin transaction to fetch data"), e);
            }
        }

        public void close() {
            if (tr != null) {
                try {
                    tr.commit();
                } catch (HibernateException e) {
                    throw new HibernateGpsDeviceException("Failed to commit hibernate transaction");
                }
            }
            try {
                session.close();
            } catch (HibernateException e) {
                log.error("Failed to close Hibernate session", e);
            }
        }

        public void closeOnError() {
            if (tr != null) {
                try {
                    tr.rollback();
                } catch (HibernateException e1) {
                    log.error(buildMessage("Failed to rollback hibernate transaction"), e1);
                }
            }
            try {
                session.close();
            } catch (HibernateException e) {
                log.error(buildMessage("Failed to close Hibernate session"), e);
            }
        }
    }

    private class Hibernate3GpsDevicePostInsert implements PostInsertEventListener {
        private static final long serialVersionUID = 3544677273799308593L;

        private PostInsertEventListener postInsertEventListener;

        public Hibernate3GpsDevicePostInsert(PostInsertEventListener postInsertEventListener) {
            this.postInsertEventListener = postInsertEventListener;
        }

        public void onPostInsert(final PostInsertEvent postInsertEvent) {

            if (this.postInsertEventListener != null) {
                this.postInsertEventListener.onPostInsert(postInsertEvent);
            }

            if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }

            final Object entity = postInsertEvent.getEntity();
            if (!compassGps.hasMappingForEntityForMirror((entity.getClass()))) {
                return;
            }

            try {
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Creating [" + entity + "]"));
                }
                compassGps.executeForMirror(new CompassCallbackWithoutResult() {
                    protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                        ((InternalCompassSession) session).getMarshallingStrategy().marshallIds(entity,
                                postInsertEvent.getId());
                        session.create(entity);
                    }
                });
            } catch (Exception e) {
                log.error(buildMessage("Failed while creating [" + entity + "]"), e);
            }
        }
    }

    private class Hibernate3GpsDevicePostUpdate implements PostUpdateEventListener {
        private static final long serialVersionUID = 3833181428363113528L;

        private PostUpdateEventListener postUpdateEventListener;

        public Hibernate3GpsDevicePostUpdate(PostUpdateEventListener postUpdateEventListener) {
            this.postUpdateEventListener = postUpdateEventListener;
        }

        public void onPostUpdate(final PostUpdateEvent postUpdateEvent) {

            if (this.postUpdateEventListener != null) {
                this.postUpdateEventListener.onPostUpdate(postUpdateEvent);
            }

            if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }

            final Object entity = postUpdateEvent.getEntity();
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
                log.error(buildMessage("Failed while updating [" + entity + "]"), e);
            }
        }
    }

    private class Hibernate3GpsDevicePostDelete implements PostDeleteEventListener {

        private static final long serialVersionUID = 3258126955726385720L;

        private PostDeleteEventListener postDeleteEventListener;

        public Hibernate3GpsDevicePostDelete(PostDeleteEventListener postDeleteEventListener) {
            this.postDeleteEventListener = postDeleteEventListener;
        }

        public void onPostDelete(final PostDeleteEvent postDeleteEvent) {

            if (this.postDeleteEventListener != null) {
                this.postDeleteEventListener.onPostDelete(postDeleteEvent);
            }

            if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
                return;
            }

            final Object entity = postDeleteEvent.getEntity();
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
                log.error(buildMessage("Failed while deleting [" + entity + "]"), e);
            }
        }
    }

    private boolean mirrorDataChanges = true;

    private SessionFactory sessionFactory;

    private Configuration configuration;

    public Hibernate3GpsDevice() {

    }

    public Hibernate3GpsDevice(String name, SessionFactory sessionFactory) {
        setName(name);
        this.sessionFactory = sessionFactory;
    }

    public Hibernate3GpsDevice(String name, Configuration configuration) {
        setName(name);
        this.configuration = configuration;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    protected void doStart() throws CompassGpsException {
        super.doStart();
        if (sessionFactory == null) {
            if (configuration == null) {
                throw new HibernateGpsDeviceException(buildMessage("Must set configuration or sessionFactory"));
            }
            try {
                sessionFactory = configuration.buildSessionFactory();
            } catch (HibernateException e) {
                throw new HibernateGpsDeviceException(buildMessage("Failed to create session factory"), e);
            }
        }
            if (isMirrorDataChanges()) {
                SessionFactory actualSessionFactory = doGetActualSessionFactory();

                try {
                    ClassUtils.forName("org.hibernate.event.SessionEventListenerConfig");
                    registerEventsForHibernate30(actualSessionFactory);
                } catch (ClassNotFoundException e) {
                    registerEventsForHibernate31(actualSessionFactory);
                }
            }
    }

    protected void doStop() throws CompassGpsException {
    }

    /**
     * A helper method that returns the actual session factory for event
     * registration. Can be used by subclasses if the
     * <code>SessionFactory</code> is proxied.
     */
    protected SessionFactory doGetActualSessionFactory() {
        return this.sessionFactory;
    }

    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    protected HibernateSessionWrapper doGetHibernateSessionWrapper() {
        return new Hibernate3SessionWrapper(sessionFactory);
    }

    protected HibernateClassInfo[] doGetHibernateClassesInfo() throws HibernateGpsDeviceException {
        ArrayList classesToIndex = new ArrayList();
        try {
            Map allClassMetaData = sessionFactory.getAllClassMetadata();
            for (Iterator it = allClassMetaData.keySet().iterator(); it.hasNext();) {
                String entityname = (String) it.next();
                ClassMetadata classMetadata = (ClassMetadata) allClassMetaData.get(entityname);
                // if it is inherited, do not add it to the classes to index, since the "from [entity]"
                // query for the base class will return results for this class as well
                if (isInherited(classMetadata)) {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("entity [" + entityname + "] is inherited, filtering it out"));
                    }
                    continue;
                }
                if (isFilteredForIndex(entityname)) {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("entity [" + entityname + "] is marked to be filtered, filtering it out"));
                    }
                    continue;
                }
                if (compassGps.hasMappingForEntityForIndex((entityname))) {
                    classesToIndex.add(entityname);
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("entity [" + entityname + "] will be indexed"));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("entity [" + entityname + "] does not have compass mapping, filtering it out"));
                    }
                }
            }
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed to fetch all class meta data"), e);
        }

        HibernateClassInfo[] infos = new HibernateClassInfo[classesToIndex.size()];
        Session session;
        try {
            session = sessionFactory.openSession();
        } catch (HibernateException e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed to open session to fetch counts"), e);
        }
        Transaction tr = null;
        try {
            session.setFlushMode(FlushMode.NEVER);
            tr = session.beginTransaction();
            for (int i = 0; i < infos.length; i++) {
                infos[i] = new HibernateClassInfo();
                infos[i].entityname = (String) classesToIndex.get(i);
                infos[i].count = ((Integer) session.createQuery("select count(*) from " + infos[i].entityname)
                        .uniqueResult()).intValue();
            }
            tr.commit();
        } catch (Exception e) {
            if (tr != null) {
                try {
                    tr.rollback();
                } catch (HibernateException e1) {
                    log.error(buildMessage("Failed to rollback hibernate transaction"), e1);
                }
            }
            throw new HibernateGpsDeviceException(buildMessage("Failed to fetch counts"), e);
        } finally {
            try {
                session.close();
            } catch (HibernateException e) {
                log.error(buildMessage("Failed to close hibernate session"), e);
            }
        }

        return infos;
    }

    protected List doGetObjects(HibernateClassInfo info, int from, int count, HibernateSessionWrapper sessionWrapper)
            throws HibernateGpsDeviceException {
        List values;
        Session session = ((Hibernate3SessionWrapper) sessionWrapper).getSession();
        try {
            Query query = session.createQuery("from " + info.entityname).setFirstResult(from).setMaxResults(count);
            values = query.list();
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed to open session to fetch data for class ["
                    + info.entityname + "]"), e);
        }
        return values;
    }

    private void registerEventsForHibernate30(SessionFactory sessionFactory) {
        try {
            Class sessionEventListenerConfigClass = ClassUtils.forName("org.hibernate.event.SessionEventListenerConfig");

            FieldInvoker sessionFactorySessionEventListenerConfig =
                    new FieldInvoker(SessionFactoryImpl.class, "sessionEventListenerConfig").prepare();
            Object sessionEventListenerConfig = sessionFactorySessionEventListenerConfig.get(sessionFactory);

            FieldInvoker eventListener = new FieldInvoker(sessionEventListenerConfigClass, "postInsertEventListener").prepare();
            eventListener.set(sessionEventListenerConfig,
                    new Hibernate3GpsDevicePostInsert((PostInsertEventListener) eventListener.get(sessionEventListenerConfig)));
            eventListener = new FieldInvoker(sessionEventListenerConfigClass, "postUpdateEventListener").prepare();
            eventListener.set(sessionEventListenerConfig,
                    new Hibernate3GpsDevicePostUpdate((PostUpdateEventListener) eventListener.get(sessionEventListenerConfig)));
            eventListener = new FieldInvoker(sessionEventListenerConfigClass, "postDeleteEventListener").prepare();
            eventListener.set(sessionEventListenerConfig,
                    new Hibernate3GpsDevicePostDelete((PostDeleteEventListener) eventListener.get(sessionEventListenerConfig)));
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(
                    buildMessage("Failed to inject compass gps device events into hibernate 3.0 session factory"), e);
        }
    }

    private void registerEventsForHibernate31(SessionFactory sessionFactory) {
        try {
            Class eventListenersClass = ClassUtils.forName("org.hibernate.event.EventListeners");

            Object eventListeners =
                    new FieldInvoker(SessionFactoryImpl.class, "eventListeners").prepare().get(sessionFactory);

            FieldInvoker eventListener = new FieldInvoker(eventListenersClass, "postInsertEventListeners").prepare();
            PostInsertEventListener[] postInsertEventListener = (PostInsertEventListener[]) eventListener.get(eventListeners);
            PostInsertEventListener[] tempPostInsertEventListener = new PostInsertEventListener[postInsertEventListener.length + 1];
            System.arraycopy(postInsertEventListener, 0, tempPostInsertEventListener, 0, postInsertEventListener.length);
            tempPostInsertEventListener[postInsertEventListener.length] = new Hibernate3GpsDevicePostInsert(null);
            eventListener.set(eventListeners, tempPostInsertEventListener);

            eventListener = new FieldInvoker(eventListenersClass, "postUpdateEventListeners").prepare();
            PostUpdateEventListener[] postUpdateEventListener = (PostUpdateEventListener[]) eventListener.get(eventListeners);
            PostUpdateEventListener[] tempPostUpdateEventListener = new PostUpdateEventListener[postUpdateEventListener.length + 1];
            System.arraycopy(postUpdateEventListener, 0, tempPostUpdateEventListener, 0, postUpdateEventListener.length);
            tempPostUpdateEventListener[postUpdateEventListener.length] = new Hibernate3GpsDevicePostUpdate(null);
            eventListener.set(eventListeners, tempPostUpdateEventListener);

            eventListener = new FieldInvoker(eventListenersClass, "postDeleteEventListeners").prepare();
            PostDeleteEventListener[] postDeleteEventListener = (PostDeleteEventListener[]) eventListener.get(eventListeners);
            PostDeleteEventListener[] tempPostDeleteEventListener = new PostDeleteEventListener[postDeleteEventListener.length + 1];
            System.arraycopy(postDeleteEventListener, 0, tempPostDeleteEventListener, 0, postDeleteEventListener.length);
            tempPostDeleteEventListener[postDeleteEventListener.length] = new Hibernate3GpsDevicePostDelete(null);
            eventListener.set(eventListeners, tempPostDeleteEventListener);

        } catch (Exception e) {
            throw new HibernateGpsDeviceException(
                    buildMessage("Failed to inject compass gps device events into hibernate 3.1 session factory"), e);
        }
    }

    protected boolean isInherited(ClassMetadata classMetadata) throws HibernateGpsDeviceException {
        try {
            ClassUtils.forName("org.hibernate.event.SessionEventListenerConfig");
            return isInherited30(classMetadata);
        } catch (ClassNotFoundException e) {
            return isInherited31(classMetadata);
        }
    }

    private boolean isInherited30(ClassMetadata classMetadata) throws HibernateGpsDeviceException {
        try {
            Class basicEntityPersisterClass = ClassUtils.forName("org.hibernate.persister.entity.BasicEntityPersister");
            EntityMetamodel entityMetamodel =
                    (EntityMetamodel) new FieldInvoker(basicEntityPersisterClass, "entityMetamodel").prepare().get(classMetadata);
            return entityMetamodel.isInherited();
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(
                    buildMessage("Failed to check for inheritence for 3.0"), e);
        }
    }

    private boolean isInherited31(ClassMetadata classMetadata) throws HibernateGpsDeviceException {
        try {
            Class abstractEntityPersisterClass = ClassUtils.forName("org.hibernate.persister.entity.AbstractEntityPersister");
            EntityMetamodel entityMetamodel =
                    (EntityMetamodel) new FieldInvoker(abstractEntityPersisterClass, "entityMetamodel").prepare().get(classMetadata);
            return entityMetamodel.isInherited();
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(
                    buildMessage("Failed to check for inheritence for 3.1"), e);
        }
    }
}
