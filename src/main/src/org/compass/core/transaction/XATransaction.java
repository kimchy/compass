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

import javax.transaction.Transaction;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.engine.SearchEngine;
import org.compass.core.spi.InternalCompassSession;

/**
 * <p>Allows for Compass to particiapte in a two phase commit transaction using
 * JTA.
 *
 * <p>Enlists an {@link javax.transaction.xa.XAResource} Compass implementation
 * with a current JTA {@link javax.transaction.Transaction}.
 *
 * <p>Transaction management is done by {@link org.compass.core.transaction.XATransactionFactory}
 * so there is no need to implement suspend and resume (works the same way
 * {@link org.compass.core.transaction.JTASyncTransaction} does).
 *
 * @author kimchy
 */
public class XATransaction extends AbstractJTATransaction {

    public XATransaction(UserTransaction ut, TransactionFactory transactionFactory) {
        super(ut, transactionFactory);
    }

    protected void doBindToTransaction(Transaction tx, InternalCompassSession session, boolean newTransaction) throws Exception {
        tx.enlistResource(new CompassXAResource(session));
    }

    private static class CompassXAResource implements XAResource {

        private static final Log log = LogFactory.getLog(CompassXAResource.class);

        private InternalCompassSession session;

        private SearchEngine searchEngine;

        public CompassXAResource(InternalCompassSession session) {
            this.session = session;
            this.searchEngine = session.getSearchEngine();
        }

        public int getTransactionTimeout() throws XAException {
            return 0;
        }

        public boolean setTransactionTimeout(int i) throws XAException {
            // TODO support transaction timeout
            return false;
        }

        public boolean isSameRM(XAResource xares) throws XAException {
            return (xares != null && xares instanceof CompassXAResource && session == ((CompassXAResource) xares).session);
        }

        public Xid[] recover(int i) throws XAException {
            return null;
        }

        public void forget(Xid xid) throws XAException {
            session.close();
        }

        public void start(Xid xid, int flags) throws XAException {
            switch (flags) {
                case TMJOIN:
                case TMRESUME:
                    // no need to resume anything, the outer
                    // transaction factory manages it
                case TMNOFLAGS:
                    // no need to start the transaction here
                    // since we already started it in the base class
                default:
                    break;
            }
        }

        public void end(Xid xid, int flags) throws XAException {
            // nothing here to do
            switch (flags) {
                case TMSUSPEND:
                    break;
                case TMFAIL:
                    session.unbindTransaction();
                    break;
                case TMSUCCESS:
                    session.unbindTransaction();
                    break;
            }
        }

        public int prepare(Xid xid) throws XAException {
            try {
                searchEngine.prepare();
            } catch (Exception e) {
                log.error("Failed to prepare transaction [" + xid + "]", e);
                throw new XAException(e.getMessage());
            }
            if (searchEngine.onlyReadOperations()) {
                commit(xid, false);
                return XA_RDONLY;
            }
            return XA_OK;
        }

        public void commit(Xid xid, boolean onePhase) throws XAException {
            if (searchEngine.wasRolledBack()) {
                throw new XAException(XAException.XA_RBROLLBACK);
            }
            try {
                searchEngine.commit(onePhase);
            } catch (Exception e) {
                log.error("Failed to commit transaction [" + xid + "]", e);
                throw new XAException(e.getMessage());
            } finally {
                session.unbindTransaction();
            }
        }

        public void rollback(Xid xid) throws XAException {
            try {
                searchEngine.rollback();
            } catch (Exception e) {
                log.error("Failed to rollback transaction [" + xid + "]", e);
                throw new XAException(e.getMessage());
            } finally {
                session.unbindTransaction();
            }
        }
    }
}
