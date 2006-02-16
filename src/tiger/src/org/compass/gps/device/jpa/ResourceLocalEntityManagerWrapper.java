package org.compass.gps.device.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

/**
 * A JPA Resource Local only {@link EntityManagerWrapper} implementation. Creates the
 * <code>EntityManager</code>, and a <code>EntityTransaction</code>.
 *
 * @author kimchy
 */
public class ResourceLocalEntityManagerWrapper extends AbstractEntityManagerWrapper {

    @Override
    protected EntityManager doGetEntityManager() throws PersistenceException {
        // TODO what happens if we are within an existing one?
        return entityManagerFactory.createEntityManager();
    }

    @Override
    protected EntityTransaction doGetEntityTransaction() throws PersistenceException {
        return entityManager.getTransaction();
    }

    @Override
    protected boolean shouldCloseEntityManager() {
        return true;
    }
}
