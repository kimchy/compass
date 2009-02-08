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

package org.compass.core.lucene.engine.transaction.lucene;

import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;

/**
 * Lucene based transaction, allows to perfom dirty operations directly over the index
 * using Lucene support for transactions. Reads and search will be performed on the
 * index itself without taking into account any transactional operations.
 *
 * @author kimchy
 * @see org.compass.core.lucene.engine.transaction.lucene.LuceneTransactionProcessor
 */
public class LuceneTransactionProcessorFactory implements TransactionProcessorFactory {

    /**
     * Creates a new {@link org.compass.core.lucene.engine.transaction.lucene.LuceneTransactionProcessor}.
     */
    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new LuceneTransactionProcessor(searchEngine);
    }

    public void close() {
    }

    /**
     * Lucene transaction processor is not thread safe.
     */
    public boolean isThreadSafe() {
        return false;
    }
}
