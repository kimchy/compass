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

package org.compass.gps.device.hibernate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.AbstractTransactionFactory;
import org.compass.core.transaction.InternalCompassTransaction;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;

/**
 * <p>Integrates with Hibernate transaction managemnet abstraction and Compass transactions. Uses Hibernate
 * support for "context session" including its support for registration of synchronizations with the current
 * transaction.
 *
 * <p>Will start a Hibernate transaction of none exists, and will join an existing one if one is already
 * in progress. If the Hibernate transcation is started by this transaction factory, it will also be committed
 * by it.
 *
 * <p>In order to use the transaction factory, it must be configured with Compass, as well as calling
 * {@link #setSessionFactory(org.hibernate.SessionFactory)} before the Compass instance is created.
 *
 * @author kimchy
 */
public class HibernateSyncTransactionFactory extends AbstractTransactionFactory {

    private static ThreadLocal<SessionFactory> sessionFactoryHolder = new ThreadLocal<SessionFactory>();

    private static String sessionFactoryKey = HibernateSyncTransactionFactory.class.getName();

    private SessionFactory sessionFactory;

    private transient Map<Transaction, CompassSession> currentSessionMap = new ConcurrentHashMap<Transaction, CompassSession>();

    public static void setSessionFactory(SessionFactory sessionFactory) {
        sessionFactoryHolder.set(sessionFactory);
    }

    protected void doConfigure(CompassSettings settings) {
        this.sessionFactory = sessionFactoryHolder.get();
        if (sessionFactory == null) {
            sessionFactory = (SessionFactory) settings.getRegistry(sessionFactoryKey);
        }
        if (sessionFactory != null) {
            settings.setRegistry(sessionFactoryKey, sessionFactory);
        }
        sessionFactoryHolder.set(null);
    }


    protected boolean isWithinExistingTransaction(InternalCompassSession session) throws CompassException {
        return ((SessionImplementor) sessionFactory.getCurrentSession()).isTransactionInProgress();
    }

    protected InternalCompassTransaction doBeginTransaction(InternalCompassSession session) throws CompassException {
        HibernateSyncTransaction tr = new HibernateSyncTransaction(sessionFactory, commitBeforeCompletion, this);
        tr.begin(session);
        return tr;
    }

    protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session)
            throws CompassException {
        HibernateSyncTransaction tr = new HibernateSyncTransaction(sessionFactory, commitBeforeCompletion, this);
        tr.join(session);
        return tr;
    }

    public CompassSession getTransactionBoundSession() throws CompassException {
        Session session = sessionFactory.getCurrentSession();
        if (!((SessionImplementor) session).isTransactionInProgress()) {
            return null;
        }
        return currentSessionMap.get(session.getTransaction());
    }

    protected void doBindSessionToTransaction(CompassTransaction tr, CompassSession session) throws CompassException {
        currentSessionMap.put(sessionFactory.getCurrentSession().getTransaction(), session);
    }

    public void unbindSessionFromTransaction(Transaction transaction, CompassSession session) {
        ((InternalCompassSession) session).unbindTransaction();
        currentSessionMap.remove(transaction);
    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }
}