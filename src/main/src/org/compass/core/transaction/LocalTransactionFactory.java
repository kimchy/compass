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

import java.util.HashMap;
import java.util.Map;

import org.compass.core.Compass;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.spi.InternalCompassSession;

/**
 * Local transaction factory provides Compass level transaction managment.
 *
 * <p>By default, when a transaction begins, the {@link org.compass.core.CompassSession} is
 * bounded to a thread local. It will be unbinded from the thread local when the transaction commits/rollsback.
 *
 * @author kimchy
 */
public class LocalTransactionFactory extends AbstractTransactionFactory {

    /**
     * A ThreadLocal maintaining current sessions for the given execution thread.
     * The actual ThreadLocal variable is a java.util.Map to account for
     * the possibility for multiple Compass instances being used during execution
     * of the given thread.
     */
    private static final ThreadLocal<Map<Compass, CompassSession>> context = new ThreadLocal<Map<Compass, CompassSession>>();

    private boolean disableThreadBoundTx = false;

    protected void doConfigure(CompassSettings settings) {
        disableThreadBoundTx = settings.getSettingAsBoolean(CompassEnvironment.Transaction.DISABLE_THREAD_BOUND_LOCAL_TRANSATION, false);
    }
                                                               
    protected boolean isWithinExistingTransaction(InternalCompassSession session) throws CompassException {
        return getTransactionBoundSession() == session;
    }

    protected InternalCompassTransaction doBeginTransaction(InternalCompassSession session) throws CompassException {
        LocalTransaction tx = new LocalTransaction(session, this);
        tx.begin();
        return tx;
    }

    protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session) throws CompassException {
        LocalTransaction tx = new LocalTransaction(session, this);
        tx.join(session);
        return tx;
    }

    public CompassSession getTransactionBoundSession() throws CompassException {
        if (disableThreadBoundTx) {
            return null;
        }
        Map<Compass, CompassSession> sessionMap = sessionMap();
        if (sessionMap == null) {
            return null;
        } else {
            return sessionMap.get(compass);
        }
    }

    public void unbindSessionFromTransaction(LocalTransaction tr, CompassSession session) {
        ((InternalCompassSession) session).unbindTransaction();
        if (disableThreadBoundTx) {
            return;
        }
        Map<Compass, CompassSession> sessionMap = sessionMap();
        if (sessionMap != null) {
            sessionMap.remove(compass);
            if (sessionMap.isEmpty()) {
                context.set(null);
            }
        }
    }

    protected void doBindSessionToTransaction(CompassTransaction tr, CompassSession session) throws CompassException {
        if (disableThreadBoundTx) {
            return;
        }
        Map<Compass, CompassSession> sessionMap = sessionMap();
        if (sessionMap == null) {
            sessionMap = new HashMap<Compass, CompassSession>();
            context.set(sessionMap);
        }
        sessionMap.put(compass, session);
    }

    private static Map<Compass, CompassSession> sessionMap() {
        return context.get();
    }

}
