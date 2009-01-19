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

package org.compass.gps.device.jpa.embedded.toplink;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import oracle.toplink.essentials.sessions.Session;
import oracle.toplink.essentials.sessions.SessionEvent;
import oracle.toplink.essentials.sessions.SessionEventAdapter;
import oracle.toplink.essentials.sessions.UnitOfWork;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.gps.device.jpa.embedded.JpaCompassGps;

/**
 * A TopLink SessionEventListener that uses the transaction callback methods
 * to sync between them and Compass transaction mechanism.
 *
 * @author kimchy
 */
public class CompassSessionEventListener extends SessionEventAdapter {

    private static final Log log = LogFactory.getLog(CompassSessionEventListener.class);

    private Compass compass;

    private JpaCompassGps jpaCompassGps;

    private Properties indexSettings;

    private boolean commitBeforeCompletion;

    private boolean toplinkControlledTransaction;

    private ConcurrentHashMap<Session, CompassSessionHolder> sessionsHolders = new ConcurrentHashMap<Session, CompassSessionHolder>();

    public CompassSessionEventListener(Compass compass, JpaCompassGps jpaCompassGps,
                                       boolean commitBeforeCompletion, boolean toplinkControlledTransaction,
                                       Properties indexSettings) {
        this.compass = compass;
        this.jpaCompassGps = jpaCompassGps;
        this.commitBeforeCompletion = commitBeforeCompletion;
        this.toplinkControlledTransaction = toplinkControlledTransaction;
        this.indexSettings = indexSettings;
    }

    public Compass getCompass() {
        return compass;
    }

    public JpaCompassGps getJpaCompassGps() {
        return jpaCompassGps;
    }

    public Properties getIndexSettings() {
        return indexSettings;
    }

    public CompassSession getCurrentCompassSession(Session session) {
        if (session.isUnitOfWork()) {
            Session parentSession = ((UnitOfWork) session).getParent();
            CompassSessionHolder sessionHolder = sessionsHolders.get(parentSession);
            if (sessionHolder != null) {
                return sessionHolder.session;
            } else {
                return beginCompassSessionAndTx(parentSession).session;
            }
        } else {
            CompassSessionHolder sessionHolder = sessionsHolders.get(session);
            if (sessionHolder != null) {
                return sessionHolder.session;
            } else {
                return beginCompassSessionAndTx(session).session;
            }
        }
    }

    public void postBeginTransaction(SessionEvent event) {
        Session session = findSession(event);
        beginCompassSessionAndTx(session);
    }

    public void preCommitTransaction(SessionEvent event) {
        if (commitBeforeCompletion) {
            commit(findSession(event));
        }
    }

    public void postCommitTransaction(SessionEvent event) {
        if (!commitBeforeCompletion) {
            commit(findSession(event));
        }
    }

    public void postRollbackTransaction(SessionEvent event) {
        rollback(findSession(event));
    }

    private CompassSessionHolder beginCompassSessionAndTx(Session session) {
        CompassSession compassSession = compass.openSession();
        CompassTransaction tr = compassSession.beginTransaction();
        CompassSessionHolder sessionHolder = new CompassSessionHolder(compassSession, tr);
        sessionsHolders.put(session, sessionHolder);
        return sessionHolder;
    }

    private void commit(Session session) {
        CompassSessionHolder holder = sessionsHolders.remove(session);
        if (toplinkControlledTransaction) {
            try {
                holder.tr.commit();
            } finally {
                holder.session.close();
            }
        }
    }

    private void rollback(Session session) {
        CompassSessionHolder holder = sessionsHolders.remove(session);
        if (holder == null) {
            return;
        }
        if (toplinkControlledTransaction) {
            try {
                holder.tr.rollback();
            } finally {
                holder.session.close();
            }
        }
    }

    private Session findSession(SessionEvent event) {
        return event.getSession();
    }

    protected void finalize() throws Throwable {
        super.finalize();
        //TODO: is there a better place to close this?
        if (log.isInfoEnabled()) {
            log.info("Compass embedded TopLink shutting down");
        }
        if (jpaCompassGps.isRunning()) {
            jpaCompassGps.stop();
        }
        compass.close();
    }

    private class CompassSessionHolder {

        CompassSession session;

        CompassTransaction tr;

        public CompassSessionHolder(CompassSession session, CompassTransaction tr) {
            this.session = session;
            this.tr = tr;
        }
    }
}
