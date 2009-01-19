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

package org.compass.core.transaction.context;

import java.util.concurrent.Callable;

import org.compass.core.CompassException;
import org.compass.core.transaction.TransactionException;

/**
 * A wrapper around a delegate callable that will execute it within a transactional context.
 *
 * @author kimchy
 */
public class TransactionalCallable<T> implements Callable<T> {

    private TransactionContext transactionContext;

    private Callable<T> delegate;

    public TransactionalCallable(TransactionContext transactionContext, Callable<T> delegate) {
        this.transactionContext = transactionContext;
        this.delegate = delegate;
    }

    public T call() throws Exception {
        return transactionContext.execute(new TransactionContextCallback<T>() {
            public T doInTransaction() throws CompassException {
                try {
                    return delegate.call();
                } catch (Exception e) {
                    throw new TransactionException("Failed to execute callable", e);
                }
            }
        });
    }
}