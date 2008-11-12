/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.lucene.engine.transaction.async;

import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.support.AbstractJobBasedTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.TransactionJobs;

/**
 * @author kimchy
 */
public class AsyncTransactionProcessor extends AbstractJobBasedTransactionProcessor {

    public AsyncTransactionProcessor(LuceneSearchEngine searchEngine) {
        super(searchEngine);
    }

    protected void doPrepare(TransactionJobs jobs) throws SearchEngineException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void doCommit(boolean onePhase, TransactionJobs jobs) throws SearchEngineException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    protected void doRollback(TransactionJobs jobs) throws SearchEngineException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
