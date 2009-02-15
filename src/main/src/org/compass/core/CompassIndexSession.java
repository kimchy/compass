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

package org.compass.core;

import org.compass.core.config.CompassSettings;

/**
 * A specialized interface that provides only index capabilities.
 *
 * <p>Using the session depends on how transaction managemnet should be done (also see
 * {@link org.compass.core.Compass#openSession()}. The simplest form looks like this:
 *
 * <pre>
 * CompassIndexSession session = compass.openIndexSession();
 * try {
 *      // do operations with the session
 *      session.commit(); // same as session.close()
 * } catch (Exception e) {
 *      session.rollback();
 * } finally {
 *      session.close();
 * }
 * </pre>
 * 
 * @author kimchy
 */
public interface CompassIndexSession {

    /**
     * Runtimes settings that apply on the session level.
     *
     * @return Runtime settings applies on the session level
     */
    CompassSettings getSettings();

    /**
     * Returns a resource factory allowing to create resources and properties.
     */
    ResourceFactory resourceFactory();

    /**
     * Flush the current transaction.
     */
    void flush() throws CompassException;

    /**
     * Flush commit all the provided aliases (or all of them, if none is provided). Flush commit
     * means that all operations up to this point will be made available in the index, and other
     * sessions will be able to see it. It also means that the operations up to this point will
     * not be rolledback.
     */
    void flushCommit(String ... aliases) throws CompassException;
    
    /**
     * Deletes a resource with the specified alias. Note that the resource must
     * have the defined ids in the mapping files set and an alias set.
     *
     * @param resource The resource to be deleted.
     * @throws CompassException
     */
    void delete(Resource resource) throws CompassException;

    /**
     * Deletes an object from Compass. The object must have been either loaded
     * by Compass or it's ids must be set if already known.
     *
     * @param obj The object to delete
     * @throws CompassException
     */
    void delete(Object obj) throws CompassException;

    /**
     * Deletes an object from Compass with multiple alias's. The object can
     * either be the id (or an array of ids), or the actual data object with
     * it's property ids set.
     *
     * @param alias The alias that the objects maps under
     * @param obj   The object to delete
     * @throws CompassException
     */
    void delete(String alias, Object obj) throws CompassException;

    /**
     * Deletes an object from Compass with multiple alias's based on
     * its ids.
     *
     * @param alias The alias that the objects maps under
     * @param ids   The ids of the object to delete
     * @throws CompassException
     */
    void delete(String alias, Object... ids) throws CompassException;

    /**
     * Deletes an object from Compass that match the mapping specified for the defined class.
     * The object can either be the id (or an array of ids), or the actual data object with
     * it's property ids set.
     *
     * @param clazz The class that represtents the required mapping
     * @param obj   The object to delete
     * @throws CompassException
     */
    void delete(Class clazz, Object obj) throws CompassException;

    /**
     * Deletes an object from Compass that match the mapping specified for the defined class
     * based on its ids.
     *
     * @param clazz The class that represtents the required mapping
     * @param ids   The object ids to delete
     * @throws CompassException
     */
    void delete(Class clazz, Object... ids) throws CompassException;

    /**
     * Deletes all entries in the index that match the given query.
     *
     * @param query The query to delete by
     * @throws CompassException
     */
    void delete(CompassQuery query) throws CompassException;

    /**
     * Creates a NEW object in Compass. All the meta data defined in the Compass
     * mapping files will be indexed and saved for later searching. Note that if
     * the same object (same alias and same id's) already exists in the index, it
     * won't be deleted.
     *
     * @param obj The object to save.
     * @throws CompassException
     */
    void create(Object obj) throws CompassException;

    /**
     * Creates a NEW object in Compass that shares mapping alais with multiple
     * objects. All the meta data defined in Compass mapping files will be
     * indexed and saved for later searching. Note that if
     * the same object (same alias and same id's) already exists in the index, it
     * won't be deleted.
     *
     * @param alias The alias that match the object mappings
     * @param obj   The object to save
     * @throws CompassException
     */
    void create(String alias, Object obj) throws CompassException;

    /**
     * Saves an object in Compass. All the meta data defined in the Compass
     * mapping files will be indexed and saved for later searching.
     *
     * @param obj The object to save.
     * @throws CompassException
     */
    void save(Object obj) throws CompassException;

    /**
     * Saves an object in Compass that shares mapping alais with multiple
     * objects. All the meta data defined in Compass mapping files will be
     * indexed and saved for later searching.
     *
     * @param alias The alias that match the object mappings
     * @param obj   The object to save
     * @throws CompassException
     */
    void save(String alias, Object obj) throws CompassException;

    /**
     * When not using explicit {@link org.compass.core.CompassTransaction} in order to manage transactions, can be called
     * to rollback the current running transaction. Effectively also closes the session.
     */
    void rollback() throws CompassException;

    /**
     * Same as {@link CompassSession#close()}.
     */
    void commit() throws CompassException;

    /**
     * Closes the CompassSession. Note, if this session is "contained" within another session,
     * it won't actually be closed, and defer closing the session to the other session.
     *
     * <p>If there is an on going transaction associated with the session that has not been committed
     * / rolledback yet, will commit the transaction (and in case of failure, will roll it back). Failed
     * commits will throw an exception from the close method.
     *
     * @throws CompassException
     * @see org.compass.core.Compass#openSession()
     */
    void close() throws CompassException;

    /**
     * Returns <code>true</code> if the session is closed. Note, if this session
     * "joined" another session, it won't actually be closed, and defer closing
     * the session to the outer session.
     */
    boolean isClosed();
}
