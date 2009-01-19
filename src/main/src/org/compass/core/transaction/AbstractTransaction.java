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

import org.compass.core.CompassException;
import org.compass.core.engine.SearchEngine;
import org.compass.core.spi.InternalCompassSession;

public abstract class AbstractTransaction implements InternalCompassTransaction {

    private boolean begun;

    protected final TransactionFactory transactionFactory;

    protected AbstractTransaction(TransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public void setBegun(boolean begun) {
        this.begun = begun;
    }

    public boolean isBegun() {
        return begun;
    }

    public void commit() throws CompassException {
        if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }

        doCommit();
    }

    public void rollback() throws CompassException {
        if (!begun) {
            throw new TransactionException("Transaction not successfully started");
        }

        doRollback();
    }

    public SearchEngine getSearchEngine() {
        return ((InternalCompassSession) getSession()).getSearchEngine();
    }

    protected abstract void doCommit() throws CompassException;

    protected abstract void doRollback() throws CompassException;

}
