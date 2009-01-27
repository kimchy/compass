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

package org.compass.spring.transaction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.config.CompassSettings;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.transaction.AbstractTransactionFactory;
import org.compass.core.transaction.InternalCompassTransaction;
import org.compass.core.transaction.TransactionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class SpringSyncTransactionFactory extends AbstractTransactionFactory {

    private static ThreadLocal<PlatformTransactionManager> transactionManagerHolder = new ThreadLocal<PlatformTransactionManager>();

    private static String transactionManagerKey = SpringSyncTransactionFactory.class.getName();

    private PlatformTransactionManager transactionManager;

    private transient Map<TransactionSynchronization, CompassSession> currentSessionMap = new ConcurrentHashMap<TransactionSynchronization, CompassSession>();

    public static void setTransactionManager(PlatformTransactionManager transactionManager) {
        transactionManagerHolder.set(transactionManager);
    }

    protected void doConfigure(CompassSettings settings) {
        this.transactionManager = transactionManagerHolder.get();
        if (transactionManager == null) {
            transactionManager = (PlatformTransactionManager) settings.getRegistry(transactionManagerKey);
        }
        if (transactionManager != null) {
            settings.setRegistry(transactionManagerKey, transactionManager);
        }
        transactionManagerHolder.set(null);
    }


    protected boolean isWithinExistingTransaction(InternalCompassSession session) throws CompassException {
        return TransactionSynchronizationManager.isActualTransactionActive();
//        return ExistingSpringTxCompassHelper.isExistingTransaction(transactionManager);
    }

    protected InternalCompassTransaction doBeginTransaction(InternalCompassSession session) throws CompassException {
        SpringSyncTransaction tr = new SpringSyncTransaction(this);
        // transaction manager might be null, we rely then on the fact that the
        // transaction started before
        tr.begin(transactionManager, session, commitBeforeCompletion);
        return tr;
    }

    protected InternalCompassTransaction doContinueTransaction(InternalCompassSession session)
            throws CompassException {
        SpringSyncTransaction tr = new SpringSyncTransaction(this);
        tr.join(session);
        return tr;
    }

    public CompassSession getTransactionBoundSession() throws CompassException {
        TransactionSynchronization sync = lookupTransactionSynchronization();
        if (sync == null) {
            return null;
        }
        return currentSessionMap.get(sync);
    }

    protected void doBindSessionToTransaction(CompassTransaction tr, CompassSession session) throws CompassException {
        TransactionSynchronization sync = lookupTransactionSynchronization();
        if (sync == null) {
            throw new TransactionException("Failed to find compass registered spring synchronization");
        }
        currentSessionMap.put(sync, session);
    }

    public void unbindSessionFromTransaction(TransactionSynchronization sync, CompassSession session) {
        ((InternalCompassSession) session).unbindTransaction();
        currentSessionMap.remove(sync);
    }

    private TransactionSynchronization lookupTransactionSynchronization() throws TransactionException {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            if (transactionManager == null) {
                throw new TransactionException(
                        "Either spring trnasction synchronization is not active, or a spring transaction has not been started, "
                                + "you might want to check if transactionManager is set on LocalCompassBean configuration, so compass can start one by itself");
            } else {
                return null;
            }
        }
        List syncs = TransactionSynchronizationManager.getSynchronizations();
        for (int i = 0; i < syncs.size(); i++) {
            Object sync = syncs.get(i);
            if (sync instanceof SpringSyncTransaction.SpringTransactionSynchronization) {
                SpringSyncTransaction.SpringTransactionSynchronization springSync = (SpringSyncTransaction.SpringTransactionSynchronization) sync;
                if (springSync.getSession().getCompass() == compass) {
                    return springSync;
                }
            }
        }
        return null;
    }

    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }
}
