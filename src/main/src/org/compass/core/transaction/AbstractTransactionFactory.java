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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public abstract class AbstractTransactionFactory implements TransactionFactory {

    protected final Log log = LogFactory.getLog(getClass());

    protected Compass compass;

    protected boolean commitBeforeCompletion;

    private boolean disableAutoJoinSession = false;

    public void configure(Compass compass, CompassSettings settings) throws CompassException {
        this.compass = compass;
        this.commitBeforeCompletion = settings.getSettingAsBoolean(
                CompassEnvironment.Transaction.COMMIT_BEFORE_COMPLETION, false);
        disableAutoJoinSession = settings.getSettingAsBoolean(CompassEnvironment.Transaction.DISABLE_AUTO_JOIN_SESSION, false);
        doConfigure(settings);
    }

    protected void doConfigure(CompassSettings settings) {

    }

    public CompassTransaction tryJoinExistingTransaction(InternalCompassSession session) throws CompassException {
        if (disableAutoJoinSession) {
            return null;
        }
        if (!isWithinExistingTransaction(session)) {
            return null;
        }
        return beginTransaction(session);
    }

    protected abstract boolean isWithinExistingTransaction(InternalCompassSession session) throws CompassException;

    public CompassTransaction beginTransaction(InternalCompassSession session)
            throws CompassException {

        CompassSession boundSession = getTransactionBoundSession();
        InternalCompassTransaction tr;
        if (boundSession == null || boundSession != session) {
            tr = doBeginTransaction(session);
            doBindSessionToTransaction(tr, session);
        } else {
            tr = doContinueTransaction(session);
        }
        tr.setBegun(true);
        return tr;
    }

    protected abstract InternalCompassTransaction doBeginTransaction(InternalCompassSession session) throws CompassException;

    protected abstract InternalCompassTransaction doContinueTransaction(InternalCompassSession session)
            throws CompassException;

    protected abstract void doBindSessionToTransaction(CompassTransaction tr, CompassSession session) throws CompassException;
}
