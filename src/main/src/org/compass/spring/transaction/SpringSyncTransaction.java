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

package org.compass.spring.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.AbstractTransaction;
import org.compass.core.transaction.CompassSessionHolder;
import org.compass.core.transaction.TransactionException;
import org.compass.core.transaction.TransactionSessionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class SpringSyncTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(SpringSyncTransaction.class);

    private TransactionStatus status;

    /**
     * This flag means if this transaction controls the COMPASS transaction (i.e. it is the top level compass
     * transaction)
     */
    private boolean controllingNewTransaction = false;

    private boolean commitFailed;

    private PlatformTransactionManager transactionManager;

    public void begin(PlatformTransactionManager transactionManager, InternalCompassSession session,
                      TransactionIsolation transactionIsolation, boolean commitBeforeCompletion) {

        this.transactionManager = transactionManager;

        // the factory called begin, so we are in charge, if we were not, than
        // it would not call begin (we are in charge of the COMPASS transaction,
        // the spring one is handled later)
        controllingNewTransaction = true;

        if (transactionManager != null) {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
            boolean readOnly = false;
            if (transactionIsolation == TransactionIsolation.READ_ONLY_READ_COMMITTED) {
                readOnly = true;
            }
            transactionDefinition.setReadOnly(readOnly);
            status = transactionManager.getTransaction(transactionDefinition);
        }

        session.getSearchEngine().begin(transactionIsolation);

        SpringTransactionSynchronization sync;
        if (transactionManager != null) {
            if (log.isDebugEnabled()) {
                if (status.isNewTransaction()) {
                    log.debug("Beginning new Spring transaction, and a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                } else {
                    log.debug("Joining Spring transaction, and starting a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
            }
            sync = new SpringTransactionSynchronization(session, status.isNewTransaction(), commitBeforeCompletion);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Joining Spring transaction, and starting a new compass transaction on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            sync = new SpringTransactionSynchronization(session, false, commitBeforeCompletion);
        }
        TransactionSynchronizationManager.registerSynchronization(sync);

        setBegun(true);
    }

    /**
     * Called by factory when already in a running compass transaction
     */
    public void join() throws CompassException {
        controllingNewTransaction = false;
        if (log.isDebugEnabled()) {
            log.debug("Joining an existing compass transcation on thread [" + Thread.currentThread().getName() + "]");
        }
    }

    protected void doCommit() throws CompassException {
        if (!controllingNewTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Not committing transaction since compass does not control it on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            return;
        }

        if (transactionManager == null) {
            // do nothing, it could only get here if the spring transaction was
            // started and we synch on it
            return;
        }

        if (status.isNewTransaction()) {
            if (log.isDebugEnabled()) {
                log.debug("Committing Spring transaction controlled by compass on therad ["
                        + Thread.currentThread().getName() + "]");
            }
            try {
                transactionManager.commit(status);
            } catch (Exception e) {
                commitFailed = true;
                // so the transaction is already rolled back, by JTA spec
                throw new TransactionException("Commit failed", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Commit called, let Spring synchronization commit the transaciton on thread ["
                        + Thread.currentThread().getName() + "]");
            }
        }
    }

    protected void doRollback() throws CompassException {

        if (transactionManager == null) {
            // do nothing, it could only get here if the spring transaction was
            // started and we synch on it
            return;
        }

        try {
            if (status.isNewTransaction()) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling back Spring transaction controlled by compass on thread ["
                            + Thread.currentThread().getName() + "]");
                }
                if (!commitFailed)
                    transactionManager.rollback(status);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Marking Spring transaction as rolled back since compass controlls it on thread [" +
                            Thread.currentThread().getName() + "]");
                }
                status.setRollbackOnly();
            }
        } catch (Exception e) {
            throw new TransactionException("Rollback failed with exception", e);
        }
    }

    public boolean wasRolledBack() throws CompassException {
        throw new TransactionException("Not supported");
    }

    public boolean wasCommitted() throws CompassException {
        throw new TransactionException("Not supported");
    }

    public static class SpringTransactionSynchronization implements TransactionSynchronization {

        private InternalCompassSession session;

        private boolean compassControledTransaction;

        private boolean commitBeforeCompletion;

        public SpringTransactionSynchronization(InternalCompassSession session, boolean compassControledTransaction,
                                                boolean commitBeforeCompletion) {
            this.session = session;
            this.compassControledTransaction = compassControledTransaction;
            this.commitBeforeCompletion = commitBeforeCompletion;
        }

        public void suspend() {
        }

        public void resume() {
        }

        public void beforeCommit(boolean readOnly) {
        }

        public void beforeCompletion() {
            if (!commitBeforeCompletion) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Committing compass transaction using Spring synchronization beforeCompletion on therad [" +
                        Thread.currentThread().getName() + "]");
            }
            session.getSearchEngine().commit(true);
        }

        public void afterCompletion(int status) {
            try {
                if (status == STATUS_COMMITTED) {
                    if (log.isDebugEnabled()) {
                        log.debug("Committing compass transaction using Spring synchronization afterCompletion on therad [" +
                                Thread.currentThread().getName() + "]");
                    }
                    if (!commitBeforeCompletion) {
                        session.getSearchEngine().commit(true);
                    }
                } else if (status == STATUS_ROLLED_BACK) {
                    if (log.isDebugEnabled()) {
                        log.debug("Rolling back compass transaction using Spring synchronization afterCompletion on therad [" +
                                Thread.currentThread().getName() + "]");
                    }
                    session.getSearchEngine().rollback();
                }
            } catch (Exception e) {
                log.error("Exception occured when sync with transaction", e);
                // TODO swallow??????
            } finally {
                CompassSessionHolder holder = TransactionSessionManager.getHolder(session.getCompass());
                holder.removeSession(this);
                session.evictAll();
                // close the session AFTER we cleared it from the transaction,
                // so it will be actually closed. Also close it only if we do
                // not contoll the transaction
                // (otherwise it will be closed by the calling template)
                if (!compassControledTransaction) {
                    session.close();
                }
            }
        }

    }

}
