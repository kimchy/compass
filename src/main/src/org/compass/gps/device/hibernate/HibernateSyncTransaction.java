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

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.AbstractTransaction;
import org.compass.core.transaction.TransactionException;
import org.compass.core.transaction.TransactionFactory;
import org.hibernate.FlushMode;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;

/**
 * @author kimchy
 */
public class HibernateSyncTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(HibernateSyncTransaction.class);

    private SessionFactory sessionFactory;

    /**
     * Did we start the Hibernate transaction
     */
    private boolean newTransaction;

    /**
     * Is this the up most level controlling the Compass transaction
     */
    private boolean controllingNewTransaction = false;

    private InternalCompassSession session;

    private boolean commitFailed;

    private boolean commitBeforeCompletion;

    private Transaction transaction;

    public HibernateSyncTransaction(SessionFactory sessionFactory, boolean commitBeforeCompletion, TransactionFactory transactionFactory) {
        super(transactionFactory);
        this.sessionFactory = sessionFactory;
        this.commitBeforeCompletion = commitBeforeCompletion;
    }

    public void begin(InternalCompassSession session) throws CompassException {
        this.session = session;
        try {
            controllingNewTransaction = true;
            SessionImplementor hibernateSession = ((SessionImplementor) sessionFactory.getCurrentSession());
            newTransaction = !hibernateSession.isTransactionInProgress();
            transaction = sessionFactory.getCurrentSession().getTransaction();
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Beginning new Hibernate transaction, and a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
                session.getSearchEngine().begin();
                if (session.isReadOnly()) {
                    hibernateSession.setFlushMode(FlushMode.MANUAL);
                }
                transaction.begin();
            } else {
                // joining an exisiting transaction
                session.getSearchEngine().begin();
                if (log.isDebugEnabled()) {
                    log.debug("Joining an existing Hibernate transaction, starting a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
            }
            transaction.registerSynchronization(new HibernateTransactionSynchronization(session, transaction, newTransaction, commitBeforeCompletion, transactionFactory));
        } catch (Exception e) {
            throw new TransactionException("Begin failed with exception", e);
        }
        setBegun(true);
    }

    /**
     * Called by the factory when joining an already running compass transaction
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
                log.debug("Not committing Hibernate transaction since compass does not control it on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            return;
        }

        if (newTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Committing Hibernate transaction controlled by compass on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            try {
                transaction.commit();
            } catch (Exception e) {
                commitFailed = true;
                // so the transaction is already rolled back, by Hibernate spec
                throw new TransactionException("Commit failed", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Commit called, let Hibernate synchronization commit the transaciton on thread ["
                        + Thread.currentThread().getName() + "]");
            }
        }
    }

    protected void doRollback() throws CompassException {

        try {
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling back Hibernate transaction controlled by compass on thread [" + Thread.currentThread().getName() + "]");
                }
                if (!commitFailed)
                    transaction.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Marking Hibernate transaction as rolled back since compass controlls it on thread [" +
                            Thread.currentThread().getName() + "]");
                }
                // no way to mark a transaction as rolled back, assume throwing the exception will make it
                // transaction.setRollbackOnly();
            }
        } catch (Exception e) {
            throw new TransactionException("Rollback failed with exception", e);
        }
    }

    public boolean wasRolledBack() throws TransactionException {

        if (!isBegun())
            return false;
        if (commitFailed)
            return true;

        return transaction.wasRolledBack();
    }

    public boolean wasCommitted() throws TransactionException {

        if (!isBegun() || commitFailed)
            return false;

        return transaction.wasCommitted();
    }

    public CompassSession getSession() {
        return this.session;
    }

    private static class HibernateTransactionSynchronization implements Synchronization {

        private static final Log log = LogFactory.getLog(HibernateTransactionSynchronization.class);

        private InternalCompassSession session;

        private Transaction tx;

        private boolean compassControlledHibernateTransaction;

        private boolean commitBeforeCompletion;

        private TransactionFactory transactionFactory;

        public HibernateTransactionSynchronization(InternalCompassSession session, Transaction tx,
                                                   boolean compassControlledHibernateTransaction, boolean commitBeforeCompletion,
                                                   TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            this.session = session;
            this.tx = tx;
            this.compassControlledHibernateTransaction = compassControlledHibernateTransaction;
            this.commitBeforeCompletion = commitBeforeCompletion;
        }

        public void beforeCompletion() {
            if (!commitBeforeCompletion) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Committing compass transaction using Hibernate synchronization beforeCompletion on thread [" +
                        Thread.currentThread().getName() + "]");
            }
            session.getSearchEngine().commit(true);
        }

        public void afterCompletion(int status) {
            try {
                if (!commitBeforeCompletion) {
                    if (status == Status.STATUS_COMMITTED) {
                        if (log.isDebugEnabled()) {
                            log.debug("Committing compass transaction using Hibernate synchronization afterCompletion on thread [" +
                                    Thread.currentThread().getName() + "]");
                        }
                        session.getSearchEngine().commit(true);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Rolling back compass transaction using Hibernate synchronization afterCompletion on thread [" +
                                    Thread.currentThread().getName() + "]");
                        }
                        session.getSearchEngine().rollback();
                    }
                }
            } catch (Exception e) {
                // TODO swallow??????
                log.error("Exception occured when sync with transaction", e);
            } finally {
                session.evictAll();
                ((HibernateSyncTransactionFactory) transactionFactory).unbindSessionFromTransaction(tx, session);
                // close the session AFTER we cleared it from the transaction,
                // so it will be actually closed (and only if we are not
                // controlling the trnasction)
                if (!compassControlledHibernateTransaction) {
                    session.close();
                }
            }
        }
    }

}