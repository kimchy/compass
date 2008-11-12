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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.support.ResourceEnhancer;
import org.compass.core.lucene.engine.transaction.support.TransactionJob;
import org.compass.core.lucene.engine.transaction.support.TransactionJobs;
import org.compass.core.transaction.context.TransactionalCallable;
import org.compass.core.util.CollectionUtils;

/**
 * @author kimchy
 */
public class AsyncTransactionProcessorFactory implements TransactionProcessorFactory, CompassConfigurable, SearchEngineFactoryAware {

    private static Log logger = LogFactory.getLog(AsyncTransactionProcessorFactory.class);

    private LuceneSearchEngineFactory searchEngineFactory;


    private int batchJobsSize = LuceneEnvironment.Transaction.Processor.Async.DEFAULT_BATCH_JOBS_SIZE;

    private long batchJobTimeout;

    private BlockingQueue<TransactionJobs> jobsToProcess;

    private volatile boolean closed = false;

    private Future pollingProcessorFuture;

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        jobsToProcess = new ArrayBlockingQueue<TransactionJobs>(settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BACKLOG,
                LuceneEnvironment.Transaction.Processor.Async.DEFAULT_BACKLOG), true);

        batchJobsSize = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, LuceneEnvironment.Transaction.Processor.Async.DEFAULT_BATCH_JOBS_SIZE);
        batchJobTimeout = settings.getSettingAsTimeInMillis(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, LuceneEnvironment.Transaction.Processor.Async.DEFAULT_BATCH_JOBS_TIMEOUT);

        if (logger.isDebugEnabled()) {
            logger.debug("Starting Async polling transaction processor");
        }
        pollingProcessorFuture = searchEngineFactory.getExecutorManager().submit(new PollingProcessor(settings));
    }

    public void close() {
        closed = true;
        pollingProcessorFuture.cancel(true);
    }

    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new AsyncTransactionProcessor(searchEngine);
    }

    private class PollingProcessor implements Callable {

        private final CompassSettings settings;

        private final Set<String> subIndexes = new HashSet<String>();

        private final Map<String, IndexWriter> writers = new HashMap<String, IndexWriter>();

        private final List<TransactionJob>[] concurrentJobsToProcess;

        private final int concurrencyLevel = 5;

        private PollingProcessor(CompassSettings settings) {
            this.settings = settings;
            // concurrencyLevel = settings.get
            concurrentJobsToProcess = new List[concurrencyLevel];
            for (int i = 0; i < concurrentJobsToProcess.length; i++) {
                concurrentJobsToProcess[i] = new ArrayList<TransactionJob>();
            }
        }

        public Object call() throws Exception {
            while (!closed) {
                try {
                    TransactionJobs jobs = jobsToProcess.poll(10, TimeUnit.SECONDS);
                    if (jobs == null) {
                        continue;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Procesing jobs [" + System.identityHashCode(jobs) + "]");
                    }
                    clear();

                    // build the concurrent job list of lists
                    addConcurrentJobsToProcess(jobs);
                    // spin a bit to get more possible jobs, if enabled (batchJobSize is set to higher value than 0)
                    for (int i = 0; i < batchJobsSize; i++) {
                        jobs = jobsToProcess.poll(batchJobTimeout, TimeUnit.MILLISECONDS);
                        if (jobs == null) {
                            break;
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace("Batching additional Jobs [" + System.identityHashCode(jobs) + "]");
                        }
                        addConcurrentJobsToProcess(jobs);
                    }

                    boolean failure = false;

                    // open index writers
                    Map<String, IndexWriter> writers = new HashMap<String, IndexWriter>();
                    for (String subIndex : subIndexes) {
                        try {
                            IndexWriter writer = searchEngineFactory.getLuceneIndexManager().openIndexWriter(settings, subIndex, false);
                            writers.put(subIndex, writer);
                        } catch (Exception e) {
                            logger.warn("Failed to open index writer for sub index [" + subIndex + "]", e);
                            failure = true;
                            break;
                        }
                    }
                    if (failure) {
                        clear();
                        continue;
                    }

                    // process all the jobs by multiple threads
                    ArrayList<Callable<Object>> processCallables = new ArrayList<Callable<Object>>();
                    for (List<TransactionJob> list : concurrentJobsToProcess) {
                        if (list.isEmpty()) {
                            // no need to create a thread for empty list
                            continue;
                        }
                        processCallables.add(new TransactionalCallable(searchEngineFactory.getTransactionContext(), new TransactionJobProcessor(list, writers)));
                    }
                    try {
                        searchEngineFactory.getLuceneIndexManager().getExecutorManager().invokeAllWithLimitBailOnException(processCallables, 1);
                    } catch (Exception e) {
                        logger.warn("Failed to index", e);
                        failure = true;
                    }
                    if (failure) {
                        clear();
                        continue;
                    }

                    // prepare for commit

                    // commit

                    if (logger.isTraceEnabled()) {
                        logger.trace("Procesing jobs done");
                    }
                } catch (InterruptedException e) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Polling for transaction jobs interrupted", e);
                    }
                    // continue here, since when we get interrupted, the closed flag should be set to true
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Async polling transaction processor thread stopped");
            }
            return null;
        }

        private void closeWriters() {
            for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
                try {
                    entry.getValue().close();
                } catch (IOException e) {
                    logger.warn("Failed to close index writer for sub index [" + entry.getKey() + "]");
                }
            }
            writers.clear();
        }

        private void clear() {
            closeWriters();
            subIndexes.clear();
            for (List<TransactionJob> list : concurrentJobsToProcess) {
                list.clear();
            }
        }

        private void addConcurrentJobsToProcess(TransactionJobs jobs) {
            subIndexes.addAll(jobs.getSubIndexes());
            for (TransactionJob job : jobs.getJobs()) {
                concurrentJobsToProcess[hash(job) % concurrencyLevel].add(job);
            }
        }

        private int hash(TransactionJob job) {
            // TODO add the option to choose to hash by sub index as well
            return CollectionUtils.hash(job.getResourceUID());
        }
    }

    private class TransactionJobProcessor implements Callable {

        private final List<TransactionJob> jobsToProcess;

        private final Map<String, IndexWriter> writers;

        private TransactionJobProcessor(List<TransactionJob> jobsToProcess, Map<String, IndexWriter> writers) {
            this.jobsToProcess = jobsToProcess;
            this.writers = writers;
        }

        public Object call() throws Exception {
            for (TransactionJob job : jobsToProcess) {
                IndexWriter writer = writers.get(job.getResourceKey().getSubIndex());
                Analyzer analyzer;
                switch (job.getType()) {
                    case CREATE:
                        analyzer = ResourceEnhancer.enahanceResource(job.getResource(), searchEngineFactory);
                        writer.addDocument(job.getDocument(), analyzer);
                        break;
                    case UPDATE:
                        analyzer = ResourceEnhancer.enahanceResource(job.getResource(), searchEngineFactory);
                        writer.updateDocument(job.getUIDTerm(), job.getDocument(), analyzer);
                        break;
                    case DELETE:
                        writer.deleteDocuments(job.getUIDTerm());
                        break;
                }
            }
            return null;
        }
    }
}
