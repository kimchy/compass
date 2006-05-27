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

package org.compass.core.transaction;

import javax.transaction.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.spi.InternalCompassSession;

/**
 * Implements a basic transaction strategy for JTA transactions. Instances check
 * to see if there is an existing JTA transaction. If none exists, a new
 * transaction is started. If one exists, all work is done in the existing
 * context. The following properties are used to locate the underlying
 * <code>UserTransaction</code>:<br>
 * <br>
 * <table>
 * <tr>
 * <td><code>compass.jndi.url</code></td>
 * <td>JNDI initial context URL</td>
 * </tr>
 * <tr>
 * <td><code>compass.jndi.class</code></td>
 * <td>JNDI provider class</td>
 * </tr>
 * <tr>
 * <td><code>compass.transaction.userTransactionName</code></td>
 * <td>JNDI name</td>
 * </tr>
 * </table>
 *
 * @author kimchy
 */
public class JTASyncTransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(JTASyncTransaction.class);

    private UserTransaction ut;

    private boolean newTransaction;

    private boolean controllingNewTransaction = false;

    private boolean commitFailed;

    public JTASyncTransaction(UserTransaction ut) {
        this.ut = ut;
    }

    public void begin(InternalCompassSession session, TransactionManager transactionManager,
                      TransactionIsolation transactionIsolation, boolean commitBeforeCompletion) throws CompassException {

        try {
            controllingNewTransaction = true;
            newTransaction = ut.getStatus() == Status.STATUS_NO_TRANSACTION;
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Beginning new JTA transaction, and a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
                session.getSearchEngine().begin(transactionIsolation);
                ut.begin();
            } else {
                // joining an exisiting transaction
                session.getSearchEngine().begin(transactionIsolation);
                if (log.isDebugEnabled()) {
                    log.debug("Joining an existing JTA transaction, starting a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
            }
            javax.transaction.Transaction tx = transactionManager.getTransaction();
            tx.registerSynchronization(new JTATransactionSynchronization(session, tx, newTransaction, commitBeforeCompletion));
        } catch (Exception e) {
            throw new TransactionException("Begin failed with exception", e);
        }

        setBegun(true);
    }

    /**
     * Called by the factory when joining an already running compass transaction
     */
    public void join() throws CompassException {
        controllingNewTransaction = false;
        if (log.isDebugEnabled()) {
            log.debug("Joining an existing compass transcation on therad [" + Thread.currentThread().getName() + "]");
        }
    }

    protected void doCommit() throws CompassException {

        if (!controllingNewTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Not committing JTA transaction since compass does not control it on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            return;
        }

        if (newTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JTA transaction controlled by compass on therad ["
                        + Thread.currentThread().getName() + "]");
            }
            try {
                ut.commit();
            } catch (Exception e) {
                commitFailed = true;
                // so the transaction is already rolled back, by JTA spec
                throw new TransactionException("Commit failed", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Commit called, let JTA synchronization commit the transaciton on therad ["
                        + Thread.currentThread().getName() + "]");
            }
        }
    }

    protected void doRollback() throws CompassException {

        try {
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling back JTA transaction controlled by compass on therad [" + Thread.currentThread().getName() + "]");
                }
                if (!commitFailed)
                    ut.rollback();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Marking JTA transaction as rolled back since compass controlls it on thread [" +
                            Thread.currentThread().getName() + "]");
                }
                ut.setRollbackOnly();
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

        final int status;
        try {
            status = ut.getStatus();
        } catch (SystemException se) {
            throw new TransactionException("Could not determine transaction status", se);
        }
        if (status == Status.STATUS_UNKNOWN) {
            throw new TransactionException("Could not determine transaction status");
        } else {
            return status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLING_BACK
                    || status == Status.STATUS_ROLLEDBACK;
        }
    }

    public boolean wasCommitted() throws TransactionException {

        if (!isBegun() || commitFailed)
            return false;

        final int status;
        try {
            status = ut.getStatus();
        } catch (SystemException se) {
            throw new TransactionException("Could not determine transaction status: ", se);
        }
        if (status == Status.STATUS_UNKNOWN) {
            throw new TransactionException("Could not determine transaction status");
        } else {
            return status == Status.STATUS_COMMITTED;
        }
    }

    private static class JTATransactionSynchronization implements Synchronization {

        private InternalCompassSession session;

        private Transaction tx;

        private boolean compassControlledJtaTransaction;

        private boolean commitBeforeCompletion;

        public JTATransactionSynchronization(InternalCompassSession session, Transaction tx,
                                             boolean compassControlledJtaTransaction, boolean commitBeforeCompletion) {
            this.session = session;
            this.tx = tx;
            this.compassControlledJtaTransaction = compassControlledJtaTransaction;
            this.commitBeforeCompletion = commitBeforeCompletion;
        }

        public void beforeCompletion() {
            if (!commitBeforeCompletion) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Committing compass transaction using JTA synchronization beforeCompletion on therad [" +
                        Thread.currentThread().getName() + "]");
            }
            session.getSearchEngine().commit(true);
        }

        public void afterCompletion(int status) {
            try {
                if (!commitBeforeCompletion) {
                    if (status == Status.STATUS_COMMITTED) {
                        if (log.isDebugEnabled()) {
                            log.debug("Committing compass transaction using JTA synchronization afterCompletion on therad [" +
                                    Thread.currentThread().getName() + "]");
                        }
                        session.getSearchEngine().commit(true);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Rolling back compass transaction using JTA synchronization afterCompletion on therad [" +
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
                CompassSessionHolder holder = TransactionSessionManager.getHolder(session.getCompass());
                holder.removeSession(tx);
                // close the session AFTER we cleared it from the transaction,
                // so it will be actually closed (and only if we are not
                // controlling the trnasction)
                if (!compassControlledJtaTransaction) {
                    session.close();
                }
            }
        }
    }

}
