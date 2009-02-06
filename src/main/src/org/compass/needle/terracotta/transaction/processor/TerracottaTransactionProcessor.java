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

package org.compass.needle.terracotta.transaction.processor;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.transaction.support.AbstractJobBasedTransactionProcessor;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJobs;

/**
 * The actual terracotta transaction processor that simply delegates the commit point to
 * {@link org.compass.needle.terracotta.transaction.processor.TerracottaTransactionProcessorFactory#add(org.compass.core.lucene.engine.transaction.support.job.TransactionJobs)}.
 *
 * @author kimchy
 * @see TerracottaTransactionProcessorFactory
 */
public class TerracottaTransactionProcessor extends AbstractJobBasedTransactionProcessor {

    private final static Log logger = LogFactory.getLog(TerracottaTransactionProcessor.class);

    private final TerracottaTransactionProcessorFactory processorFactory;

    private transient Map<String, TransactionJobs> committedJobs;

    /**
     */
    public TerracottaTransactionProcessor(LuceneSearchEngine searchEngine, TerracottaTransactionProcessorFactory processorFactory) {
        super(logger, searchEngine, true);
        this.processorFactory = processorFactory;
    }

    public String getName() {
        return TerracottaTransactionProcessorEnvironment.NAME;
    }

    protected void doPrepare(TransactionJobs jobs) throws SearchEngineException {
        // nothing to do here, we only add on commit
    }

    protected void doFlushCommit(TransactionJobs jobs) {
        processorFactory.add(jobs);
    }

    protected void doCommit(boolean onePhase, TransactionJobs jobs) throws SearchEngineException {
        committedJobs = processorFactory.add(jobs);
    }

    protected void doRollback(TransactionJobs jobs) throws SearchEngineException {
        if (committedJobs != null) {
            processorFactory.remove(committedJobs);
        }
    }
}