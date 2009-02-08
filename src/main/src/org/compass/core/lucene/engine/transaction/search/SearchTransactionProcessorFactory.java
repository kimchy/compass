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

package org.compass.core.lucene.engine.transaction.search;

import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;

/**
 * A lightweight search only transaction processor factory.
 *
 * @author kimchy
 * @see SearchTransactionProcessor
 */
public class SearchTransactionProcessorFactory implements TransactionProcessorFactory {

    /**
     * Creates a new {@link SearchTransactionProcessor}.
     */
    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new SearchTransactionProcessor(searchEngine);
    }

    public void close() {
    }

    /**
     * The search transaction processor is thread safe.
     */
    public boolean isThreadSafe() {
        return true;
    }
}