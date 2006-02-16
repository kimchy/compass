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

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.impl.InternalCompassSession;

/**
 * 
 * @author kimchy
 * 
 */
public class LocalTransactionFactory extends AbstractTransactionFactory {

	protected InternalCompassTransaction doBeginTransaction(InternalCompassSession session,
			TransactionIsolation transactionIsolation) throws CompassException {
		LocalTransaction tx = new LocalTransaction(session, transactionIsolation);
		tx.begin();
		return tx;
	}

	protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session) throws CompassException {
		LocalTransaction tx = new LocalTransaction(session, null);
		tx.join();
		return tx;
	}

	protected CompassSession doGetTransactionBoundSession(CompassSessionHolder holder) throws CompassException {
		return holder.getSession();
	}

	protected void doBindSessionToTransaction(CompassSessionHolder holder, CompassSession session)
			throws CompassException {
		holder.addSession(session);
	}
}
