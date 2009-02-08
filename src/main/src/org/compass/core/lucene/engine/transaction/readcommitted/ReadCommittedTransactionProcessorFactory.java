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

package org.compass.core.lucene.engine.transaction.readcommitted;

import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;

/**
 * A transaction processor factory that creates {@link ReadCommittedTransactionProcessor}
 * instances.
 *
 * <p>Read committed transaction processor allows to isolate changes done during a transaction from other
 * transactions until commit. It also allows for load/get/find operations to take into account changes
 * done during the current transaction. This means that a delete that occurs during a transaction will
 * be filtered out if a search is executed within the same transaction just after the delete.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessor
 */
public class ReadCommittedTransactionProcessorFactory implements TransactionProcessorFactory {

    /**
     * Constructs a new {@link org.compass.core.lucene.engine.transaction.readcommitted.ReadCommittedTransactionProcessor}.
     */
    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new ReadCommittedTransactionProcessor(searchEngine);
    }

    public void close() {
    }

    /**
     * Read committed transaction processor is not threads safe.
     */
    public boolean isThreadSafe() {
        return false;
    }
}