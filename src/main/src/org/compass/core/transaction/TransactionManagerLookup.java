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

import javax.transaction.TransactionManager;

import org.compass.core.config.CompassSettings;

/**
 * Concrete implementations locate and return the JTA
 * <code>TransactionManager</code>.
 * <p>
 * Initial implementation taken from Hibernate.
 * 
 * @author kimchy
 */
public interface TransactionManagerLookup {

    /**
     * Obtain the JTA <tt>TransactionManager</tt>
     */
    public TransactionManager getTransactionManager(CompassSettings settings) throws TransactionException;

    /**
     * Return the JNDI name of the JTA <code>UserTransaction</code> or
     * <code>null</code> (optional operation).
     */
    public String getUserTransactionName();

}
