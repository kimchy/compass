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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.jndi.NamingHelper;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.*;

/**
 * Factory for <code>JTATransaction</code>.
 * 
 * @see JTASyncTransaction
 * @author kimchy
 */
public class JTASyncTransactionFactory extends AbstractTransactionFactory {

	private static final Log log = LogFactory.getLog(JTASyncTransactionFactory.class);

	private static final String DEFAULT_USER_TRANSACTION_NAME = "java:comp/UserTransaction";

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

	public InternalCompassTransaction doBeginTransaction(InternalCompassSession session,
			TransactionIsolation transactionIsolation) throws CompassException {
        JTASyncTransaction tx = new JTASyncTransaction(getUserTransaction());
		tx.begin(session, transactionManager, transactionIsolation, commitBeforeCompletion);
		return tx;
	}

	protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session) throws CompassException {
        JTASyncTransaction tx = new JTASyncTransaction(getUserTransaction());
        tx.join();
        return tx;
    }

	protected CompassSession doGetTransactionBoundSession(CompassSessionHolder holder) throws CompassException {
		UserTransaction ut = getUserTransaction();
		try {
			if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
				return null;
			}
			Transaction tr = transactionManager.getTransaction();
			return holder.getSession(tr);
		} catch (SystemException e) {
			throw new TransactionException("Failed to fetch transaction bound session", e);
		}
	}

	protected void doBindSessionToTransaction(CompassSessionHolder holder, CompassSession session)
			throws CompassException {
		try {
			Transaction tx = transactionManager.getTransaction();
			holder.addSession(tx, session);
		} catch (SystemException e) {
			throw new TransactionException("Failed to bind session to transcation", e);
		}
	}

    private UserTransaction getUserTransaction() throws TransactionException {
        if (userTransaction != null) {
            return userTransaction;
        }
        return lookupUserTransaction();
    }

    private UserTransaction lookupUserTransaction() throws TransactionException {
		UserTransaction ut;
		if (log.isDebugEnabled()) {
			log.debug("Looking for UserTransaction under [" + utName + "]");
		}
		try {
			ut = (UserTransaction) context.lookup(utName);
		} catch (NamingException ne) {
			throw new TransactionException("Could not find UserTransaction in JNDI under [" + utName + "]", ne);
		}
		if (ut == null) {
			throw new TransactionException("A naming service lookup returned null under [" + utName + "]");
		}
		return ut;
	}
}
