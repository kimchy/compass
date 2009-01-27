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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.jndi.NamingHelper;
import org.compass.core.spi.InternalCompassSession;

/**
 * A base class for JTA transaction strategies. Associates a {@link org.compass.core.CompassSession}
 * with the JTA {@link javax.transaction.Transaction} and uses it as the basis for begin / continue
 * a transaction and for transaction boudn session management.
 *
 * @author kimchy
 */
public abstract class AbstractJTATransactionFactory extends AbstractTransactionFactory {

    private static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

    private transient Map<Transaction, CompassSession> currentSessionMap = new ConcurrentHashMap<Transaction, CompassSession>();

    private InitialContext context;

    private String utName;

    private UserTransaction userTransaction;

    private TransactionManager transactionManager;

    public void doConfigure(CompassSettings settings) throws CompassException {

        try {
            context = NamingHelper.getInitialContext(settings);
        } catch (NamingException ne) {
            throw new CompassException("Could not obtain initial context", ne);
        }

        utName = settings.getSetting(CompassEnvironment.Transaction.USER_TRANSACTION);

        TransactionManagerLookup lookup = TransactionManagerLookupFactory.getTransactionManagerLookup(settings);
        if (lookup != null) {
            transactionManager = lookup.getTransactionManager(settings);
            if (transactionManager == null) {
                throw new CompassException("Failed to find transaction manager");
            }
            if (utName == null)
                utName = lookup.getUserTransactionName();
        } else {
            throw new CompassException("Must register a transaction manager lookup using the "
                    + CompassEnvironment.Transaction.MANAGER_LOOKUP + " property");
        }

        if (utName == null) {
            utName = DEFAULT_USER_TRANSACTION_NAME;
        }

        boolean cacheUserTransaction = settings.getSettingAsBoolean(CompassEnvironment.Transaction.CACHE_USER_TRANSACTION, true);
        if (cacheUserTransaction) {
            if (log.isDebugEnabled()) {
                log.debug("Caching JTA UserTransaction from Jndi [" + utName + "]");
            }
            userTransaction = lookupUserTransaction();
        }
    }


    protected boolean isWithinExistingTransaction(InternalCompassSession session) throws CompassException {
        try {
            return getUserTransaction().getStatus() != Status.STATUS_NO_TRANSACTION;
        } catch (SystemException e) {
            throw new CompassException("Failed to get staus on JTA transaciton", e);
        }
    }

    public CompassSession getTransactionBoundSession() throws CompassException {
        UserTransaction ut = getUserTransaction();
        try {
            if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
                return null;
            }
            Transaction tr = transactionManager.getTransaction();
            return currentSessionMap.get(tr);
        } catch (SystemException e) {
            throw new TransactionException("Failed to fetch transaction bound session", e);
        }
    }

    protected void doBindSessionToTransaction(CompassTransaction tr, CompassSession session) throws CompassException {
        try {
            Transaction tx = transactionManager.getTransaction();
            currentSessionMap.put(tx, session);
        } catch (SystemException e) {
            throw new TransactionException("Failed to bind session to transcation", e);
        }
    }

    public void unbindSessionFromTransaction(Transaction tx, CompassSession session) {
        ((InternalCompassSession) session).unbindTransaction();
        currentSessionMap.remove(tx);
    }

    public TransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    public UserTransaction getUserTransaction() throws TransactionException {
        if (userTransaction != null) {
            return userTransaction;
        }
        return lookupUserTransaction();
    }

    private UserTransaction lookupUserTransaction() throws TransactionException {
        if (log.isDebugEnabled()) {
            log.debug("Looking for UserTransaction under [" + utName + "]");
        }
        UserTransaction ut;
        try {
            ut = (UserTransaction) context.lookup(utName);
            ut.getStatus();
        } catch (NamingException ne) {
            ut = null;
        } catch (IllegalStateException e) {
            ut = null;
        } catch (SystemException e) {
            ut = null;
        }
        if (ut == null) {
            if (log.isInfoEnabled()) {
                log.info("Failed to locate a UserTransaction under [" + utName + "], creating UserTransactionAdapter in its place");
            }
            ut = new UserTransactionAdapter(transactionManager);
        }
        return ut;
    }

}
