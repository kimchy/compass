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

import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;

/**
 * @author kimchy
 */
public abstract class AbstractTransactionFactory implements TransactionFactory {

    protected Compass compass;

    protected boolean commitBeforeCompletion;

    public void configure(Compass compass, CompassSettings settings) throws CompassException {
        this.compass = compass;
        this.commitBeforeCompletion = settings.getSettingAsBoolean(
                CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, false);
        doConfigure(settings);
    }

    protected void doConfigure(CompassSettings settings) {

    }

    public CompassTransaction beginTransaction(InternalCompassSession session, TransactionIsolation transactionIsolation)
            throws CompassException {
        // bind the holder if not exists
        CompassSessionHolder holder = TransactionSessionManager.getHolder(compass);
        if (holder == null) {
            holder = new CompassSessionHolder();
            TransactionSessionManager.bindHolder(compass, holder);
        }

        CompassSession boundSession = getTransactionBoundSession();
        InternalCompassTransaction tr;
        if (boundSession == null || boundSession != session) {
            tr = doBeginTransaction(session, transactionIsolation);
            doBindSessionToTransaction(holder, session);
        } else {
            tr = doContinueTransaction(session);
        }
        tr.setBegun(true);
        return tr;
    }

    protected abstract InternalCompassTransaction doBeginTransaction(InternalCompassSession session,
                                                                     TransactionIsolation transactionIsolation) throws CompassException;

    protected abstract InternalCompassTransaction doContinueTransaction(InternalCompassSession session)
            throws CompassException;

    public CompassSession getTransactionBoundSession() throws CompassException {
        CompassSessionHolder holder = TransactionSessionManager.getHolder(compass);
        if (holder == null || holder.isEmpty()) {
            return null;
        }
        return doGetTransactionBoundSession(holder);
    }

    protected abstract CompassSession doGetTransactionBoundSession(CompassSessionHolder holder) throws CompassException;

    protected abstract void doBindSessionToTransaction(CompassSessionHolder holder, CompassSession session)
            throws CompassException;

    public Compass getCompass() {
        return compass;
    }
}
