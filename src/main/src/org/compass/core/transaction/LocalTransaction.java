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

package org.compass.core.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */

public class LocalTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(LocalTransaction.class);

    private static final int UNKNOWN = -1;

    private static final int STARTED = 0;

    private static final int COMMIT = 1;

    private static final int ROLLBACK = 2;

    private int state;

    private InternalCompassSession session;

    private InternalCompass compass;

    public LocalTransaction(InternalCompassSession session, TransactionFactory transactionFactory) {
        super(transactionFactory);
        state = UNKNOWN;
        this.session = session;
        this.compass = session.getCompass();
    }

    public void join(InternalCompassSession session) throws CompassException {
        this.session = session;
        if (log.isDebugEnabled()) {
            log.debug("Joining an existing local transcation on thread [" + Thread.currentThread().getName() +
                    "] Compass [" + System.identityHashCode(compass) + "] Session [" + System.identityHashCode(session) + "]");
        }
    }

    public void begin() throws CompassException {
        if (log.isDebugEnabled()) {
            log.debug("Starting a new local transcation on thread [" + Thread.currentThread().getName() +
                    "] Compass [" + System.identityHashCode(compass) + "] Session [" + System.identityHashCode(session) + "]");
        }
        session.getSearchEngine().begin();
        state = STARTED;
    }

    protected void doCommit() throws CompassException {
        if (session.getSearchEngine().wasRolledBack()) {
            // don't do anything, since it was rolled back already
        }

        if (state == UNKNOWN) {
            log.debug("Not committing the transaction since within a local transaction on thread ["
                    + Thread.currentThread().getName() + "] Compass [" + System.identityHashCode(compass)
                    + "] Session [" + System.identityHashCode(session) + "]");
            return;
        }

        // commit called by the high level local transaction
        if (log.isDebugEnabled()) {
            log.debug("Committing local transaction on thread [" + Thread.currentThread().getName() +
                    "] Compass [" + System.identityHashCode(compass) + "] Session [" + System.identityHashCode(session) + "]");
        }

        session.evictAll();
        session.getSearchEngine().commit(true);
        ((LocalTransactionFactory) transactionFactory).unbindSessionFromTransaction(this, session);
        state = COMMIT;
    }

    protected void doRollback() throws CompassException {
        if (session.getSearchEngine().wasRolledBack()) {
            // don't do anything, since it was rolled back already
        }
        
        if (state == UNKNOWN) {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back local transaction, which exists within another local transaction "
                        + " on thread [" + Thread.currentThread().getName() +
                        "] Compass [" + System.identityHashCode(compass) + "] Session [" + System.identityHashCode(session) + "]");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Rolling back local transaction on thread [" + Thread.currentThread().getName() +
                        "] Compass [" + System.identityHashCode(compass) + "] Session [" + System.identityHashCode(session) + "]");
            }

            ((LocalTransactionFactory) transactionFactory).unbindSessionFromTransaction(this, session);
        }

        state = ROLLBACK;
        session.evictAll();
        session.getSearchEngine().rollback();
    }

    public boolean wasRolledBack() throws CompassException {
        return session.getSearchEngine().wasRolledBack();
    }

    public boolean wasCommitted() throws CompassException {
        return session.getSearchEngine().wasCommitted();
    }

    public CompassSession getSession() {
        return this.session;
    }
}
