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

package org.compass.spring.transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.AbstractTransaction;
import org.compass.core.transaction.TransactionException;
import org.compass.core.transaction.TransactionFactory;
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

    private InternalCompassSession session;

    public SpringSyncTransaction(TransactionFactory transactionFactory) {
        super(transactionFactory);
    }

    public void begin(PlatformTransactionManager transactionManager, InternalCompassSession session, boolean commitBeforeCompletion) {

        this.session = session;
        this.transactionManager = transactionManager;

        // the factory called begin, so we are in charge, if we were not, than
        // it would not call begin (we are in charge of the COMPASS transaction,
        // the spring one is handled later)
        controllingNewTransaction = true;

        if (transactionManager != null) {
            DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
            transactionDefinition.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_REQUIRED);
            int timeout = session.getSettings().getSettingAsInt(CompassEnvironment.Transaction.TRANSACTION_TIMEOUT, -1);
            if (timeout != -1) {
                transactionDefinition.setTimeout(timeout);
            }
            transactionDefinition.setReadOnly(session.isReadOnly());
            status = transactionManager.getTransaction(transactionDefinition);
        }

        session.getSearchEngine().begin();

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
            sync = new SpringTransactionSynchronization(session, status.isNewTransaction(), commitBeforeCompletion, transactionFactory);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Joining Spring transaction, and starting a new compass transaction on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            sync = new SpringTransactionSynchronization(session, false, commitBeforeCompletion, transactionFactory);
        }
        TransactionSynchronizationManager.registerSynchronization(sync);

        setBegun(true);
    }

    /**
     * Called by factory when already in a running compass transaction
     */
    public void join(InternalCompassSession session) throws CompassException {
        this.session = session;
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
                log.debug("Committing Spring transaction controlled by compass on thread ["
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

    public CompassSession getSession() {
        return this.session;
    }

    public static class SpringTransactionSynchronization implements TransactionSynchronization {

        private InternalCompassSession session;

        private boolean compassControledTransaction;

        private boolean commitBeforeCompletion;

        private TransactionFactory transactionFactory;

        public SpringTransactionSynchronization(InternalCompassSession session, boolean compassControledTransaction,
                                                boolean commitBeforeCompletion, TransactionFactory transactionFactory) {
            this.session = session;
            this.compassControledTransaction = compassControledTransaction;
            this.commitBeforeCompletion = commitBeforeCompletion;
            this.transactionFactory = transactionFactory;
        }

        public InternalCompassSession getSession() {
            return this.session;
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
                log.debug("Committing compass transaction using Spring synchronization beforeCompletion on thread [" +
                        Thread.currentThread().getName() + "]");
            }
            session.getSearchEngine().commit(true);
        }

        public void afterCommit() {
            
        }

        public void afterCompletion(int status) {
            try {
                if (status == STATUS_COMMITTED) {
                    if (!commitBeforeCompletion) {
                        if (log.isDebugEnabled()) {
                            log.debug("Committing compass transaction using Spring synchronization afterCompletion on thread [" +
                                    Thread.currentThread().getName() + "]");
                        }
                        session.getSearchEngine().commit(true);
                    }
                } else {
                    // in case of STATUS_ROLLBACK or STATUS_UNKNOWN
                    if (log.isDebugEnabled()) {
                        log.debug("Rolling back compass transaction using Spring synchronization afterCompletion on thread [" +
                                Thread.currentThread().getName() + "]");
                    }
                    session.getSearchEngine().rollback();
                }
            } catch (Exception e) {
                log.error("Exception occured when sync with transaction", e);
                // TODO swallow??????
            } finally {
                ((SpringSyncTransactionFactory) transactionFactory).unbindSessionFromTransaction(this, session);
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
