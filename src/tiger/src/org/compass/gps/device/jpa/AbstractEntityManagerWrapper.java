package org.compass.gps.device.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;

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

    protected EntityTransaction entityTransaction;

    public void setUp(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("Must be called between open and close");
        }
        return this.entityManager;
    }

    public void open() throws JpaGpsDeviceException {
        entityManager = doGetEntityManager();
        entityTransaction = doGetEntityTransaction();
        if (entityTransaction != null) {
            try {
                entityTransaction.begin();
            } catch (Exception e) {
                throw new JpaGpsDeviceException("Failed to start JPA resource local transaction");
            }
        }
    }

    public void close() throws JpaGpsDeviceException {
        if (entityTransaction != null) {
            try {
                entityTransaction.commit();
            } catch (PersistenceException e) {
                throw new JpaGpsDeviceException("Failed to commit JPA resource local transaction");
            }
            entityTransaction = null;
        }
        if (shouldCloseEntityManager()) {
            try {
                entityManager.close();
            } catch (PersistenceException e) {
                log.warn("Failed to close JPA EntityManager");
            } finally {
                entityManager = null;
            }
        }
    }

    public void closeOnError() throws JpaGpsDeviceException {
        if (entityTransaction != null) {
            try {
                entityTransaction.rollback();
            } catch (PersistenceException e) {
                log.warn("Failed to rollback JPA resource local transaction");
            }
            entityTransaction = null;
        }
        if (shouldCloseEntityManager()) {
            try {
                entityManager.close();
            } catch (PersistenceException e) {
                log.warn("Failed to close JPA EntityManager");
            } finally {
                entityManager = null;
            }
        }
    }

    protected abstract EntityManager doGetEntityManager() throws PersistenceException;

    protected abstract EntityTransaction doGetEntityTransaction() throws PersistenceException;

    protected abstract boolean shouldCloseEntityManager();
}
