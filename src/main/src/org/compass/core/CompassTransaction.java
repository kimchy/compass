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

package org.compass.core;

/**
 * Allows the application to define units of work, while maintaining abstraction
 * from the underlying transaction implementation (eg. JTA, Local).
 * 
 * @see org.compass.core.CompassSession#beginTransaction()
 * @see org.compass.core.transaction.TransactionFactory
 * 
 * @author kimchy
 */
public interface CompassTransaction {

	/**
	 * Ends the current unit of work. The transaction will be commited only if
	 * it was initiated by the current transcation.
	 * 
	 * @throws CompassException
	 */
	void commit() throws CompassException;

	/**
	 * Force the underlying transaction to roll back.
	 * 
	 * @throws CompassException
	 */
	void rollback() throws CompassException;

	/**
	 * Was this transaction rolled back or set to rollback only?
	 * 
	 * @return If the transaction was rolled backed
	 * @throws CompassException
	 */
	boolean wasRolledBack() throws CompassException;

	/**
	 * Check if this transaction was successfully committed. This method could
	 * return <code>false</code> even after successful invocation of
	 * <code>commit()</code>.
	 * 
	 * @return If the transaction was committed
	 * @throws CompassException
	 */
	boolean wasCommitted() throws CompassException;

    /**
     * Returns the current Compass transaction associated with this transaction.
     */
    CompassSession getSession();
}
