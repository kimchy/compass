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

package org.compass.gps.device.hibernate;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.spi.InternalCompass;
import org.compass.core.transaction.TransactionFactory;
import org.compass.core.util.FieldInvoker;
import org.hibernate.CallbackException;
import org.hibernate.EntityMode;
import org.hibernate.Interceptor;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

/**
 * <p>A Compass Hibernate interceptor to manage Compass transactions based on Hibernate
 * Interceptor transaction lifecycle callbacks. Useful when working with Compass Local
 * transactions and Hibernate JDBC transaction manager.
 *
 * <p>In order to use this interceptor call {@link #injectInterceptor(org.hibernate.SessionFactory, CompassTransactionInterceptor)}
 * using the <code>SessionFactory</code> and an instance of this class.
 *
 * <p>For another option of integrating Compass transactions and Hibernate, please see
 * {@link org.compass.core.transaction.JTASyncTransactionFactory}, {@link org.compass.core.transaction.XATransactionFactory}
 * or {@link org.compass.gps.device.hibernate.HibernateSyncTransactionFactory}.
 *
 * @author kimchy
 */
public class CompassTransactionInterceptor implements Interceptor {

    public static void injectInterceptor(SessionFactory sessionFactory, CompassTransactionInterceptor interceptor) throws Exception {
        FieldInvoker interceptorField = new FieldInvoker(sessionFactory.getClass(), "interceptor").prepare();
        Interceptor origInterceptor = (Interceptor) interceptorField.get(sessionFactory);
        interceptor.setInterceptor(origInterceptor);
        interceptorField.set(sessionFactory, interceptor);
    }

    private Interceptor interceptor;

    private InternalCompass compass;

    private TransactionFactory transactionFactory;

    private boolean commitBeforeTransactionCompletion = true;

    private ConcurrentHashMap activeTransactions = new ConcurrentHashMap();

    public CompassTransactionInterceptor(Compass compass) {
        this(compass, true, null);
    }

    public CompassTransactionInterceptor(Compass compass, boolean commitBeforeTransactionCompletion, Interceptor interceptor) {
        this.commitBeforeTransactionCompletion = commitBeforeTransactionCompletion;
        this.compass = (InternalCompass) compass;
        this.interceptor = interceptor;
        this.transactionFactory = this.compass.getTransactionFactory();
    }

    void setInterceptor(Interceptor interceptor) {
        this.interceptor = interceptor;
    }

    public void afterTransactionBegin(Transaction transaction) {
        if (interceptor != null) {
            interceptor.afterTransactionBegin(transaction);
        }
        CompassSession session = transactionFactory.getTransactionBoundSession();
        // there is already a running transaction, do nothing
        if (session != null) {
            return;
        }
        if (activeTransactions.get(transaction) != null) {
            return;
        }
        session = compass.openSession();
        CompassTransaction tr = session.beginTransaction();
        activeTransactions.put(transaction, tr);
    }

    public void beforeTransactionCompletion(Transaction transaction) {
        if (interceptor != null) {
            interceptor.beforeTransactionCompletion(transaction);
        }
        if (!commitBeforeTransactionCompletion) {
            return;
        }
        CompassTransaction tr = (CompassTransaction) activeTransactions.remove(transaction);
        if (tr == null) {
            return;
        }
        CompassSession session = transactionFactory.getTransactionBoundSession();
        tr.commit();
        session.close();
    }


    public void afterTransactionCompletion(Transaction transaction) {
        if (interceptor != null) {
            interceptor.afterTransactionCompletion(transaction);
        }
        if (commitBeforeTransactionCompletion) {
            return;
        }
        CompassTransaction tr = (CompassTransaction) activeTransactions.remove(transaction);
        if (tr == null) {
            return;
        }
        CompassSession session = transactionFactory.getTransactionBoundSession();
        tr.commit();
        session.close();
    }

    public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (interceptor != null) {
            interceptor.onDelete(entity, id, state, propertyNames, types);
        }
    }

    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (interceptor != null) {
            return interceptor.onFlushDirty(entity, id, currentState, previousState, propertyNames, types);
        }
        return false;
    }

    public boolean onLoad(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (interceptor != null) {
            return interceptor.onLoad(entity, id, state, propertyNames, types);
        }
        return false;
    }

    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (interceptor != null) {
            return interceptor.onSave(entity, id, state, propertyNames, types);
        }
        return false;
    }

    public void postFlush(Iterator entities) {
        if (interceptor != null) {
            interceptor.postFlush(entities);
        }
    }

    public void preFlush(Iterator entities) {
        if (interceptor != null) {
            interceptor.preFlush(entities);
        }
    }

    public Boolean isTransient(Object entity) {
        if (interceptor != null) {
            return interceptor.isTransient(entity);
        }
        return null;
    }

    public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
        if (interceptor != null) {
            return interceptor.instantiate(entityName, entityMode, id);
        }
        return null;
    }

    public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        if (interceptor != null) {
            return interceptor.findDirty(entity, id, currentState, previousState, propertyNames, types);
        }
        return null;
    }

    public String getEntityName(Object object) {
        if (interceptor != null) {
            return interceptor.getEntityName(object);
        }
        return null;
    }

    public Object getEntity(String entityName, Serializable id) {
        if (interceptor != null) {
            return interceptor.getEntity(entityName, id);
        }
        return null;
    }

    public String onPrepareStatement(String sql) {
        if (interceptor != null) {
            return interceptor.onPrepareStatement(sql);
        }
        return sql;
    }

    public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {
        if (interceptor != null) {
            interceptor.onCollectionRemove(collection, key);
        }
    }

    public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {
        if (interceptor != null) {
            interceptor.onCollectionRecreate(collection, key);
        }
    }

    public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {
        if (interceptor != null) {
            interceptor.onCollectionUpdate(collection, key);
        }
    }
}
