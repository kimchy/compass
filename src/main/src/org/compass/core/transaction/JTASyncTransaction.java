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
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.spi.InternalCompassSession;

/**
 * Implements a basic transaction strategy for JTA transactions. Instances check
 * to see if there is an existing JTA transaction. If none exists, a new
 * transaction is started. If one exists, all work is done in the existing
 * context.
 *
 * @author kimchy
 */
public class JTASyncTransaction extends AbstractJTATransaction {

    private boolean commitBeforeCompletion;

    public JTASyncTransaction(UserTransaction ut, boolean commitBeforeCompletion, TransactionFactory transactionFactory) {
        super(ut, transactionFactory);
        this.commitBeforeCompletion = commitBeforeCompletion;
    }


    protected void doBindToTransaction(Transaction tx, InternalCompassSession session, boolean newTransaction) throws Exception {
        tx.registerSynchronization(new JTATransactionSynchronization(session, tx, newTransaction, commitBeforeCompletion, transactionFactory));
    }

    private static class JTATransactionSynchronization implements Synchronization {

        private static final Log log = LogFactory.getLog(JTATransactionSynchronization.class);

        private InternalCompassSession session;

        private Transaction tx;

        private boolean compassControlledJtaTransaction;

        private boolean commitBeforeCompletion;

        private TransactionFactory transactionFactory;

        public JTATransactionSynchronization(InternalCompassSession session, Transaction tx,
                                             boolean compassControlledJtaTransaction, boolean commitBeforeCompletion,
                                             TransactionFactory transactionFactory) {
            this.session = session;
            this.tx = tx;
            this.compassControlledJtaTransaction = compassControlledJtaTransaction;
            this.commitBeforeCompletion = commitBeforeCompletion;
            this.transactionFactory = transactionFactory;
        }

        public void beforeCompletion() {
            if (!commitBeforeCompletion) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Committing compass transaction using JTA synchronization beforeCompletion on thread [" +
                        Thread.currentThread().getName() + "]");
            }
            session.getSearchEngine().commit(true);
        }

        public void afterCompletion(int status) {
            try {
                if (!commitBeforeCompletion) {
                    if (status == Status.STATUS_COMMITTED) {
                        if (log.isDebugEnabled()) {
                            log.debug("Committing compass transaction using JTA synchronization afterCompletion on thread [" +
                                    Thread.currentThread().getName() + "]");
                        }
                        session.getSearchEngine().commit(true);
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Rolling back compass transaction using JTA synchronization afterCompletion on thread [" +
                                    Thread.currentThread().getName() + "] with status [" + status + "]");
                        }
                        session.getSearchEngine().rollback();
                    }
                }
            } catch (Exception e) {
                // TODO swallow??????
                log.error("Exception occured when sync with transaction", e);
            } finally {
                session.evictAll();
                ((JTASyncTransactionFactory) transactionFactory).unbindSessionFromTransaction(tx, session);
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
