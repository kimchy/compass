package org.compass.gps.device.jpa;

import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

/**
 * A JPA Resource Local only {@link EntityManagerWrapper} implementation. Creates the
 * <code>EntityManager</code>, and an <code>EntityTransaction</code>.
 *
 * @author kimchy
 */
public class ResourceLocalEntityManagerWrapper extends AbstractEntityManagerWrapper {

    private EntityTransaction transaction;

    @Override
    protected void beginTransaction() throws PersistenceException {
        transaction = entityManager.getTransaction();
        transaction.begin();
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
        return true;
    }
}
