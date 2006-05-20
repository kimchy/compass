package org.compass.gps.device.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import javax.persistence.TransactionRequiredException;

/**
 * The default {@link EntityManagerWrapper} implementation. Works well both in
 * JTA and Resource Local JPA transactions.
 *
 * @author kimchy
 */
public class DefaultEntityManagerWrapper extends AbstractEntityManagerWrapper {

    private boolean isJta;

    private EntityTransaction transaction;

    @Override
    protected void beginTransaction() throws PersistenceException {
        try {
            transaction = entityManager.getTransaction();
            isJta = false;
            try {
                transaction.begin();
            } finally {
                transaction = null;
            }
        } catch (IllegalStateException e) {
            // thrown when we are in a JTA transaction
            isJta = true;
            entityManager.joinTransaction();
        }
    }

    @Override
    protected void commitTransaction() throws PersistenceException {
        if (transaction == null) {
            return;
        }
        try {
            transaction.commit();
        } finally {
            transaction = null;
        }
    }

    @Override
    protected void rollbackTransaction() throws PersistenceException {
        if (transaction == null) {
            return;
        }
        try {
            transaction.rollback();
        } finally {
            transaction = null;
        }
    }


    @Override
    protected boolean shouldCloseEntityManager() {
        return !isJta;
    }
}
