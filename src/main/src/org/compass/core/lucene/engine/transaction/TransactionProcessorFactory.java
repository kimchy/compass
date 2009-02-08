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

package org.compass.core.lucene.engine.transaction;

import org.compass.core.lucene.engine.LuceneSearchEngine;

/**
 * A transaction processor factory resposible for creating {@link org.compass.core.lucene.engine.transaction.TransactionProcessor}
 * instances.
 *
 * <p>Possibly, the factory can hold state (such as connection) that the processor can use (processors are
 * created per transaction).
 *
 * <p>Optionally, the transaction processor can implments {@link org.compass.core.config.CompassConfigurable} to
 * be injected with Compass settings. It can also implement {@link org.compass.core.config.CompassMappingAware}
 * to be injected with {@link org.compass.core.mapping.CompassMapping}.
 *
 * @author kimchy
 */
public interface TransactionProcessorFactory {

    /**
     * Creates a new transaction processor to handle a transaction.
     */
    TransactionProcessor create(LuceneSearchEngine searchEngine);

    /**
     * Closes the transaction factory.
     */
    void close();

    /**
     * Retruns <code>true</code> if the transaction processor created by the factory is thread safe.
     */
    boolean isThreadSafe();
}
