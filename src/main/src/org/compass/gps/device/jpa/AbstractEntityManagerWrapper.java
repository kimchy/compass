package org.compass.gps.device.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple base class for {@link EntityManagerWrapper} implementations. Calls the subclasses
 * for <code>EntityManager</code>, and an optioan <code>EntityTransaction</code>. Takes care of
 * all the rest.
 *
 * @author kimchy
 */
public abstract class AbstractEntityManagerWrapper implements EntityManagerWrapper {

    protected Log log = LogFactory.getLog(getClass());

    protected EntityManagerFactory entityManagerFactory;

    protected EntityManager entityManager;

    public void setUp(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("Must be called between open and close");
        }
        return this.entityManager;
    }

    public void open() throws JpaGpsDeviceException, PersistenceException {
        doCreateEntityManager();
        beginTransaction();
    }

    public void close() throws JpaGpsDeviceException, PersistenceException {
        try {
            commitTransaction();
        } finally {
            if (shouldCloseEntityManager()) {
                try {
                    entityManager.close();
                } catch (PersistenceException e) {
                    log.warn("Failed to close JPA EntityManager", e);
                } finally {
                    entityManager = null;
                }
            }
        }
    }

    public void closeOnError() throws JpaGpsDeviceException {
        try {
            rollbackTransaction();
        } catch (PersistenceException e) {
            log.warn("Failed to rollback JPA transaction, ignoring", e);
        }
        if (shouldCloseEntityManager()) {
            try {
                if (entityManager != null) {
                    entityManager.close();
                }
            } catch (PersistenceException e) {
                log.warn("Failed to close JPA EntityManager, ignoring", e);
            } finally {
                entityManager = null;
            }
        }
    }

    public EntityManagerWrapper newInstance() {
        try {
            AbstractEntityManagerWrapper copy = getClass().newInstance();
            copy.entityManagerFactory = entityManagerFactory;
            return copy;
        } catch (Exception e) {
           throw new JpaGpsDeviceException("Failed to create new wrapper", e);
        }
    }

    protected void doCreateEntityManager() throws PersistenceException {
        entityManager = entityManagerFactory.createEntityManager();
    }

    protected abstract void beginTransaction() throws PersistenceException;

    protected abstract void commitTransaction() throws PersistenceException;

    protected abstract void rollbackTransaction() throws PersistenceException;

    protected abstract boolean shouldCloseEntityManager();
}
