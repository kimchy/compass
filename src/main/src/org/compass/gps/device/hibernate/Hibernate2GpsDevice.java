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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.hibernate.FlushMode;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.SessionFactory;
import net.sf.hibernate.Transaction;
import net.sf.hibernate.cfg.Configuration;

import org.compass.gps.CompassGpsException;

/**
 * A {@link HibernateGpsDevice} which
 * works with hibernate 2.
 * <p/>
 * You must either set the Hibernate <code>Configuration</code> or the
 * <code>SessionFactory</code> to be used by the device. Note that if the
 * <code>Configuration</code> is supplied, when the device <code>start</code>
 * is called, a new <code>SessionFactory</code> will be built.
 * <p/>
 * Note: Only provides the <code>index()</code> operation. No real time index
 * updated are performed since the Hiberante <code>Interceptor</code> provides
 * null values for newly created objects with generated ids.
 *
 * @author kimchy
 */
public class Hibernate2GpsDevice extends AbstractHibernateGpsDevice {

    private SessionFactory sessionFactory;

    private Configuration configuration;

    private class Hibernate2SessionWrapper implements HibernateSessionWrapper {

        private SessionFactory sessionFactory;

        private Session session;

        private Transaction tr;

        public Hibernate2SessionWrapper(SessionFactory sessionFactory) {
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

    public Hibernate2GpsDevice() {

    }

    public Hibernate2GpsDevice(String name, SessionFactory sessionFactory) {
        setName(name);
        this.sessionFactory = sessionFactory;
    }

    public Hibernate2GpsDevice(String name, Configuration configuration) {
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
    }

    protected void doStop() throws CompassGpsException {
    }

    protected HibernateSessionWrapper doGetHibernateSessionWrapper() {
        return new Hibernate2SessionWrapper(sessionFactory);
    }

    protected HibernateEntityInfo[] doGetHibernateEntetiesInfo() throws HibernateGpsDeviceException {
        ArrayList classesToIndex = new ArrayList();
        try {
            Map allClassMetaData = sessionFactory.getAllClassMetadata();
            for (Iterator it = allClassMetaData.keySet().iterator(); it.hasNext();) {
                Class clazz = (Class) it.next();
                if (isFilteredForIndex(clazz.getName())) {
                    continue;
                }
                if (compassGps.hasMappingForEntityForIndex(clazz)) {
                    classesToIndex.add(clazz);
                }
            }
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed to fetch all class meta data"), e);
        }

        HibernateEntityInfo[] infos = new HibernateEntityInfo[classesToIndex.size()];
        for (int i = 0; i < infos.length; i++) {
            infos[i] = new HibernateEntityInfo();
            infos[i].entityname = ((Class) classesToIndex.get(i)).getName();
            infos[i].selectQuery = "from " + infos[i].entityname;
        }

        return infos;
    }

    protected List doGetObjects(HibernateEntityInfo info, int from, int count, HibernateSessionWrapper sessionWrapper)
            throws HibernateGpsDeviceException {
        List values;
        Session session = ((Hibernate2SessionWrapper) sessionWrapper).getSession();
        try {
            Query query = session.createQuery(info.selectQuery).setFirstResult(from).setMaxResults(count);
            values = query.list();
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed to open session to fetch data for class ["
                    + info.entityname + "]"), e);
        }
        return values;
    }
}
