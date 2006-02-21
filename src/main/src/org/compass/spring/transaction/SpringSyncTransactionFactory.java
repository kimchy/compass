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

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.config.CompassSettings;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.transaction.AbstractTransactionFactory;
import org.compass.core.transaction.CompassSessionHolder;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.TransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Iterator;
import java.util.List;

public class SpringSyncTransactionFactory extends AbstractTransactionFactory {

	private static ThreadLocal transactionManagerHolder = new ThreadLocal();

	private PlatformTransactionManager transactionManager;

	public static void setTransactionManager(PlatformTransactionManager transactionManager) {
		transactionManagerHolder.set(transactionManager);
	}

	protected void doConfigure(CompassSettings settings) {
		this.transactionManager = (PlatformTransactionManager) transactionManagerHolder.get();
	}

	protected InternalCompassTransaction doBeginTransaction(InternalCompassSession session,
			TransactionIsolation transactionIsolation) throws CompassException {
		SpringSyncTransaction tr = new SpringSyncTransaction();
		// transaction manager might be null, we rely then on the fact that the
		// transaction started before
		tr.begin(transactionManager, session, transactionIsolation, commitBeforeCompletion);
		return tr;
	}

	protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session)
			throws CompassException {
        SpringSyncTransaction tr = new SpringSyncTransaction();
		tr.join();
        return tr;
    }

	protected CompassSession doGetTransactionBoundSession(CompassSessionHolder holder) throws CompassException {
		TransactionSynchronization sync = lookupTransactionSynchronization();
		if (sync == null) {
			return null;
		}
		return holder.getSession(sync);
	}

	protected void doBindSessionToTransaction(CompassSessionHolder holder, CompassSession session)
			throws CompassException {
		TransactionSynchronization sync = lookupTransactionSynchronization();
		if (sync == null) {
			throw new TransactionException("Failed to find compass registered spring synchronization");
		}
		holder.addSession(sync, session);
	}

	private TransactionSynchronization lookupTransactionSynchronization() throws TransactionException {
		if (!TransactionSynchronizationManager.isSynchronizationActive()) {
			if (transactionManager == null) {
				throw new TransactionException(
						"Either spring trnasction synchronization is not active, or a spring transaction has not been started, "
								+ "you might want to check if transacitonManager is set on LocalCompassBean configuration, so compass can start one by itself");
			} else {
				return null;
			}
		}
		List syncs = TransactionSynchronizationManager.getSynchronizations();
		for (Iterator it = syncs.iterator(); it.hasNext();) {
			Object sync = it.next();
			if (sync instanceof SpringSyncTransaction.SpringTransactionSynchronization) {
				return (TransactionSynchronization) sync;
			}
		}
		return null;
	}
}
