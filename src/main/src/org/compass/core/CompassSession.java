/*
 * Copyright 2004-2006 the original author or authors.
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

import org.compass.core.CompassTransaction.TransactionIsolation;

/**
 * The main interface between a Java application and Compass.
 * <p>
 * Provides the basic operations with semantic mapped objects (save, delete, and
 * load/get). The session provides operations on both the objects levels and
 * Resource levels (indexed object model). The CompassSession operations are
 * delegated to the underlying SearchEngine, so no direct access to the
 * SearchEngine is needed.
 * </p>
 *
 * <p>
 * Implementations will not be thread safe, Instead each thread/transaction
 * should obtain its own instance from a Compass.
 * </p>
 *
 * <p>
 * If the CompassSession throws an exception, the transaction must be rolled
 * back and the session discarded. The internal state of the CompassSession
 * might not be consistent with the search engine if discarded.
 * </p>
 *
 * <p>
 * Please see the CompassTemplate class for easier programmatic control using
 * the template design pattern.
 *
 * @author kimchy
 * @see org.compass.core.Resource
 * @see org.compass.core.Compass
 */

public interface CompassSession extends CompassOperations {

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
     * Begin a unit of work and return the associated CompassTranscation object.
     * If a new underlying transaction is required, begin the transaction.
     * Otherwise continue the new work in the context of the existing underlying
     * transaction. The class of the returned CompassTransaction object is
     * determined by the property <code>compass.transaction.factory</code>.
     * <p>
     * Also accepts the transcation isolation of the transaction.
     *
     * @param transactionIsolation
     * @return a CompassTransaction instance
     * @throws CompassException
     * @see CompassTransaction
     */
    CompassTransaction beginTransaction(TransactionIsolation transactionIsolation) throws CompassException;

    /**
     * Creats a new query builder, used to build queries programmatically.
     *
     * @return The query builder.
     * @deprecated please use queryBuilder
     */
    CompassQueryBuilder createQueryBuilder() throws CompassException;

    /**
     * Creats a new query filter builder, used to build filters of queries
     * programmatically.
     *
     * @return The query filter builder.
     * @deprecated please use queryFilterBuilder
     */
    CompassQueryFilterBuilder createQueryFilterBuilder() throws CompassException;

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
     * @param names The property names
     * @return A term freqs builder
     * @throws CompassException
     */
    CompassTermFreqsBuilder termFreqsBuilder(String[] names) throws CompassException;

    /**
     * Returns an Analyzer helper. Can be used to help analyze given texts.
     *
     * @return the analyzer helper
     * @throws CompassException
     */
    CompassAnalyzerHelper analyzerHelper() throws CompassException;

    /**
     * Closes the CompassSession.
     *
     * @throws CompassException
     */
    void close() throws CompassException;
}
