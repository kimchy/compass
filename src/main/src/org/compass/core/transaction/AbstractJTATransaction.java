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

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public abstract class AbstractJTATransaction extends AbstractTransaction {

    private static final Log log = LogFactory.getLog(AbstractJTATransaction.class);

    private UserTransaction ut;

    /**
     * Did we start the JTA transaction
     */
    private boolean newTransaction;

    /**
     * Is this the up most level controlling the Compass transaction
     */
    private boolean controllingNewTransaction = false;

    private boolean commitFailed;

    private InternalCompassSession session;

    public AbstractJTATransaction(UserTransaction ut, TransactionFactory transactionFactory) {
        super(transactionFactory);
        this.ut = ut;
    }

    public void begin(InternalCompassSession session, TransactionManager transactionManager) throws CompassException {

        try {
            this.session = session;
            controllingNewTransaction = true;
            newTransaction = ut.getStatus() == Status.STATUS_NO_TRANSACTION;
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Beginning new JTA transaction, and a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "]");
                }
                session.getSearchEngine().begin();

                int timeout = session.getSettings().getSettingAsInt(CompassEnvironment.Transaction.TRANSACTION_TIMEOUT, -1);
                if (timeout != -1) {
                    ut.setTransactionTimeout(timeout);
                }
                ut.begin();
            } else {
                // joining an exisiting transaction
                session.getSearchEngine().begin();
                if (log.isDebugEnabled()) {
                    log.debug("Joining an existing JTA transaction, starting a new compass transaction on thread ["
                            + Thread.currentThread().getName() + "] with status [" + ut.getStatus() + "]");
                }
            }
            javax.transaction.Transaction tx = transactionManager.getTransaction();
            doBindToTransaction(tx, session, newTransaction);
        } catch (Exception e) {
            throw new TransactionException("Begin failed with exception", e);
        }
        setBegun(true);
    }

    protected abstract void doBindToTransaction(javax.transaction.Transaction tx, InternalCompassSession session,
                                                boolean newTransaction) throws Exception;

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
                log.debug("Not committing JTA transaction since compass does not control it on thread ["
                        + Thread.currentThread().getName() + "]");
            }
            return;
        }

        if (newTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Committing JTA transaction controlled by compass on thread ["
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
                log.debug("Commit called, let JTA synchronization commit the transaciton on thread ["
                        + Thread.currentThread().getName() + "]");
            }
        }
    }

    protected void doRollback() throws CompassException {

        try {
            if (newTransaction) {
                if (log.isDebugEnabled()) {
                    log.debug("Rolling back JTA transaction controlled by compass on thread [" + Thread.currentThread().getName() + "]");
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

    public CompassSession getSession() {
        return this.session;
    }
}
