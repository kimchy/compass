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

package org.compass.gps.device.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;

/**
 * An <code>EntityManager<code> wrapper allows controlling the creation and destruction of JPA
 * <code>EntityManager</code>s, as well as any transactions control (such as JPA resource local
 * <code>EntityTransaction<code>).
 * <p>
 * Used by {@link JpaGpsDevice} when performing the index operation.
 *
 * @author kimchy
 */
public interface EntityManagerWrapper {

    /**
     * Sets up the entity manager wrapper with the <code>EntityManagerFactory</code>.
     *
     * @param entityManagerFactory The <code>EntityManagerFactory</code> the wrapper will use
     */
    void setUp(EntityManagerFactory entityManagerFactory);

    /**
     * Opens the warpper for a session of reading enteties for indexing.
     */
    void open() throws JpaGpsDeviceException, PersistenceException;

    /**
     * Returns the <code>EntityManager</code> opened by the wrapper open operation.
     *
     * @return The current <code>EntityManager</code>
     * @throws IllegalStateException If not called between the open and close* operations
     */
    EntityManager getEntityManager() throws IllegalStateException;

    /**
     * Closes the current <code>EntityManager</code>, commiting the transaction if necessary.
     *
     * @throws JpaGpsDeviceException
     */
    void close() throws JpaGpsDeviceException, PersistenceException;

    /**
     * Closes the current <code>EntityManager</code>, rollback the transaction if necessary.
     *
     * @throws JpaGpsDeviceException
     */
    void closeOnError() throws JpaGpsDeviceException, PersistenceException;

    /**
     * Creates a new instance of this entity manager wrapper for multi threaded usage.
     */
    EntityManagerWrapper newInstance();
}
