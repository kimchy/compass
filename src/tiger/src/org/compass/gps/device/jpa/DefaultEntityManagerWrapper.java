package org.compass.gps.device.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

/**
 * The default {@link EntityManagerWrapper} implementation. Works well both in
 * JTA and Resource Local JPA transactions.
 *
 * @author kimchy
 */
public class DefaultEntityManagerWrapper extends AbstractEntityManagerWrapper {

    private boolean isNew;

    @Override
    protected EntityManager doGetEntityManager() throws PersistenceException {
        EntityManager entityManager;
        try {
            entityManager = entityManagerFactory.getEntityManager();
            isNew = false;
            if (log.isDebugEnabled()) {
                log.debug("Got an existing JPA EntityManager");
            }
        } catch (IllegalStateException e) {
            try {
                entityManager = entityManagerFactory.createEntityManager();
                isNew = true;
                if (log.isDebugEnabled()) {
                    log.debug("Created a new JPA EntityManager");
                }
            } catch (PersistenceException ex) {
                throw new JpaGpsDeviceException("Failed to open JPA EntityManager", e);
            }
        }
        return entityManager;
    }

    @Override
    protected EntityTransaction doGetEntityTransaction() throws PersistenceException {
        if (!isNew) {
            return null;
        }
        return entityManager.getTransaction();
    }

    @Override
    protected boolean shouldCloseEntityManager() {
        return isNew;
    }
}
