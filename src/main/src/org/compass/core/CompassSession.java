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
 * The main interface between a Java application and Compass.
 *
 * <p>Provides the basic operations with semantic mapped objects (save, delete, and
 * load/get). The session provides operations on both the objects levels and
 * Resource levels (indexed object model). The CompassSession operations are
 * delegated to the underlying SearchEngine, so no direct access to the
 * SearchEngine is needed.
 *
 * <p>Implementations will not be thread safe, Instead each thread/transaction
 * should obtain its own instance from a Compass.
 *
 * <p>If the CompassSession throws an exception, the transaction must be rolled
 * back and the session discarded. The internal state of the CompassSession
 * might not be consistent with the search engine if discarded.
 *
 * <p>Using the session depends on how transaction managemnet should be done (also see
 * {@link org.compass.core.Compass#openSession()}. The simplest form looks like this:
 *
 * <pre>
 * CompassSession session = compass.openSession();
 * try {
 *      // do operations with the session
 *      session.commit(); // same as session.close()
 * } catch (Exception e) {
 *      session.rollback();
 * }
 * </pre>
 *
 * <p>A more complex form includes explicit control using {@link org.compass.core.CompassTransaction}:
 *
 * <pre>
 * CompassSession session = compass.openSession();
 * CompassTransaction tx = null;
 * try {
 * 	  tx = session.beginTransaction();
 * 	  Object result = compassCallback.doInCompass(session);
 * 	  tx.commit();
 * 	  return result;
 * } catch (RuntimeException e) {
 * 	  if (tx != null) {
 * 		  tx.rollback();
 * 	  }
 * 	  throw e;
 * } finally {
 * 	  session.close();
 * }
 * </pre>
 *
 * @author kimchy
 * @see org.compass.core.Resource
 * @see org.compass.core.Compass
 */

public interface CompassSession extends CompassOperations, CompassSearchSession, CompassIndexSession {

    /**
     * Indicates that the session will be used for read only operations. Allowing to optimize
     * search and read.
     */
    void setReadOnly();

    /**
     * Returns <code>true</code> if the session is read only.
     *
     * @see #setReadOnly()
     */
    boolean isReadOnly();

    /**
     * When not using the {@link org.compass.core.CompassTransaction} interface, will begin a local transaction
     * instead of the configured transaction.
     */
    CompassSession useLocalTransaction();

    /**
     * Returns a resource factory allowing to create resources and properties.
     */
    ResourceFactory resourceFactory();

    /**
     * Runtimes settings that apply on the session level.
     *
     * @return Runtime settings applies on the session level
     */
    CompassSettings getSettings();

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
     * Begin a unit of work and return the associated CompassTranscation object.
     * If a new underlying transaction is required, begin the transaction.
     * Otherwise continue the new work in the context of the existing underlying
     * transaction. The class of the returned CompassTransaction object is
     * determined by the property <code>compass.transaction.factory</code>.
     *
     * @return a CompassTransaction instance
     * @throws CompassException
     * @see CompassTransaction
     */
    CompassTransaction beginTransaction() throws CompassException;

    /**
     * Begins a unit of work using a Compass local transaction. Very handy when using
     * transaction strategy other than local transaction factory but still wish to use
     * a local one for example to perform search (which will be faster as it won't start
     * and external transaction).
     */
    CompassTransaction beginLocalTransaction() throws CompassException;

    /**
     * Creats a new query builder, used to build queries programmatically.
     *
     * @return The query builder.
     */
    CompassQueryBuilder queryBuilder() throws CompassException;

    /**
     * Creats a new query filter builder, used to build filters of queries
     * programmatically.
     *
     * @return The query filter builder.
     */
    CompassQueryFilterBuilder queryFilterBuilder() throws CompassException;

    /**
     * Creates a new terms frequencies builder used to get terms names and
     * freqs for a list of property names.
     *
     * <p>Note, term frequencies are updated to reflect latest changes to the index
     * only after an optimization has taken place (note, calling optimize might not
     * cause optimization).
     *
     * @param names The property names
     * @return A term freqs builder
     * @throws CompassException
     */
    CompassTermFreqsBuilder termFreqsBuilder(String... names) throws CompassException;

    /**
     * Returns an Analyzer helper. Can be used to help analyze given texts.
     *
     * @return the analyzer helper
     * @throws CompassException
     */
    CompassAnalyzerHelper analyzerHelper() throws CompassException;

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
