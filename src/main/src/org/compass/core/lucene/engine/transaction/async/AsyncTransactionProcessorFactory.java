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
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.support.CommitCallable;
import org.compass.core.lucene.engine.transaction.support.PrepareCommitCallable;
import org.compass.core.lucene.engine.transaction.support.ResourceHashing;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJobs;
import org.compass.core.transaction.context.TransactionalCallable;

/**
 * A transaction processor that created {@link org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessor}
 * instances. Supports async execution of transactions against the index. A transaction (which includes several dirty
 * operations) is packaged into a single operation which is then applied to the index asynchronously.
 *
 * <p>Note, when several instances of Compass are running using this transaction processor, order of transactions is
 * not maintained, which might result in out of order transaction being applied to the index.
 *
 * <p>The number of transactions that have not been processed (backlog) are bounded and default to <code>10</code>.
 * If the processor is falling behind in processing transactions, commit operations will block until the backlog
 * lowers below its threshold. The backlog can be set using the {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#BACKLOG}.
 * Commit operations will block by default for 10 seconds in order for the backlog to lower below its threshold. It
 * can be changed using the {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#ADD_TIMEOUT}
 * setting.
 *
 * <p>Processing of transactions is done by a background thread that waits for transactions. Once there is a transaction
 * to process, it will first try to wait for additional transactions. It will block for 100 milliseconds (configurable
 * using {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#BATCH_JOBS_TIMEOUT}), and if one
 * was added, will wait again up to 5 times (configurable using {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#BATCH_JOBS_SIZE}).
 * Once batch jobs based on timeout is done, the processor will try to get up to 5 more transactions in a non blocking
 * manner (configurable using {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#NON_BLOCKING_BATCH_JOBS_SIZE}).
 *
 * <p>When all transaction jobs are accumulated, the processor starts up to 5 threads
 * (configurable using {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#CONCURRENCY_LEVEL})
 * in order to process all the transaction jobs against the index. Hashing of actual operation (create/update/delete)
 * can either be done based on uid (of the resource) or sub index. By default, hashing is done based on <code>uid</code>
 * and can be configured using {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#HASHING}.
 *
 * <p>When the transaction processor closes, by default it will wait for all the transactions to finish. In order to
 * disable it, the {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#PROCESS_BEFORE_CLOSE}
 * setting should be set to <code>false</code>.
 *
 * @author kimchy
 */
public class AsyncTransactionProcessorFactory implements TransactionProcessorFactory, CompassConfigurable, SearchEngineFactoryAware {

    private static Log logger = LogFactory.getLog(AsyncTransactionProcessorFactory.class);

    private CompassSettings settings;

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineIndexManager indexManager;

    private boolean processBeforeClose;

    private int concurrencyLevel;

    private long addTimeout;

    private int batchJobsSize;

    private long batchJobTimeout;

    private int nonBlockingBatchSize;

    private ResourceHashing hashing;

    private BlockingQueue<TransactionJobs> jobsToProcess;

    private Future pollingProcessorFuture;

    private volatile PollingProcessor pollingProcessor;

    private volatile boolean closed = false;

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
        this.indexManager = this.searchEngineFactory.getLuceneIndexManager();
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        jobsToProcess = new ArrayBlockingQueue<TransactionJobs>(settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BACKLOG, 10), true);

        addTimeout = settings.getSettingAsTimeInMillis(LuceneEnvironment.Transaction.Processor.Async.ADD_TIMEOUT, 10 * 1000);
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor will wait for [" + addTimeout + "ms] if backlog is full");
        }

        batchJobsSize = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, 5);
        batchJobTimeout = settings.getSettingAsTimeInMillis(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, 100);
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor blocking batch size is [" + batchJobsSize + "] with timeout of [" + batchJobTimeout + "ms]");
        }

        nonBlockingBatchSize = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.NON_BLOCKING_BATCH_JOBS_SIZE, 5);
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor non blocking batch size is [" + nonBlockingBatchSize + "]");
        }

        processBeforeClose = settings.getSettingAsBoolean(LuceneEnvironment.Transaction.Processor.Async.PROCESS_BEFORE_CLOSE, true);
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor process before close is set to [" + processBeforeClose + "]");
        }

        this.concurrencyLevel = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.CONCURRENCY_LEVEL, 5);
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor will use [" + concurrencyLevel + "] concrrent threads to process transactions");
        }

        hashing = ResourceHashing.fromName(settings.getSetting(LuceneEnvironment.Transaction.Processor.Async.HASHING, "uid"));
        if (logger.isDebugEnabled()) {
            logger.debug("Async Transaction Processor uses [" + hashing + "] based hashing for concurrent processing");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Starting Async polling transaction processor");
        }
    }

    /**
     * Closes the transaction processor. Will wait for ongoing transactions if the
     * {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#PROCESS_BEFORE_CLOSE} is set to
     * <code>true</code> (the default).
     */
    public synchronized void close() {
        closed = true;
        if (processBeforeClose && pollingProcessor != null) {
            // TODO don't sleep forever (can be implemented nicely with singal)
            while (!jobsToProcess.isEmpty()) {
                try {
                    this.wait(100);
                } catch (InterruptedException e) {
                    // break out
                    break;
                }
            }
        }
        if (pollingProcessor != null) {
            try {
                pollingProcessor.close();
                pollingProcessorFuture.cancel(true);
                while (!pollingProcessor.isDone()) {
                    try {
                        this.wait(100);
                    } catch (InterruptedException e) {
                        // break out
                        break;
                    }
                }
            } finally {
                pollingProcessor = null;
                pollingProcessorFuture = null;
            }
        }
    }

    /**
     * Creates a new {@link org.compass.core.lucene.engine.transaction.async.AsyncTransactionProcessor}.
     */
    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new AsyncTransactionProcessor(searchEngine, this);
    }

    /**
     * Async transaction processor is not thread safe.
     */
    public boolean isThreadSafe() {
        return false;
    }

    /**
     * Removed (if still pending) the given {@link org.compass.core.lucene.engine.transaction.support.job.TransactionJobs}
     * from being processed.
     */
    public boolean remove(TransactionJobs jobs) throws SearchEngineException {
        return jobsToProcess.remove(jobs);
    }

    /**
     * Adds the {@link org.compass.core.lucene.engine.transaction.support.job.TransactionJobs} to be processed
     * asynchronously. If a procesing threads has not started, will start it (it is started lazily so if the
     * async transaction processor is not used, it won't incur any overhead).
     *
     * <p>The addition of {@link org.compass.core.lucene.engine.transaction.support.job.TransactionJobs} is "offered"
     * to a blocking queue, waiting until the queue if cleared in case it is full. This will cause a transaction
     * commit to block if the backlog is full. The time to wait can be controlled using
     * {@link org.compass.core.lucene.LuceneEnvironment.Transaction.Processor.Async#ADD_TIMEOUT} and defaults to
     * 10 seconds.
     */
    public void add(TransactionJobs jobs) throws SearchEngineException {
        if (pollingProcessor == null) {
            synchronized (this) {
                if (pollingProcessor == null) {
                    this.pollingProcessor = new PollingProcessor();
                    pollingProcessorFuture = searchEngineFactory.getExecutorManager().submit(pollingProcessor);
                }
            }
        }
        try {
            boolean offered = jobsToProcess.offer(jobs, addTimeout, TimeUnit.MILLISECONDS);
            if (!offered) {
                throw new SearchEngineException("Failed to add jobs [" + System.identityHashCode(jobs) + "], queue is full and nothing empties it");
            }
        } catch (InterruptedException e) {
            throw new SearchEngineException("Failed to add jobs [" + System.identityHashCode(jobs) + "], interrupted", e);
        }
    }

    private void process(TransactionJobs jobs) throws InterruptedException {
        Set<String> subIndexes = new HashSet<String>();
        List<TransactionJob>[] concurrentJobsToProcess = new List[concurrencyLevel];
        for (int i = 0; i < concurrentJobsToProcess.length; i++) {
            concurrentJobsToProcess[i] = new ArrayList<TransactionJob>();
        }

        // build the concurrent job list of lists
        addConcurrentJobsToProcess(concurrentJobsToProcess, subIndexes, jobs);
        // spin a bit to get more possible jobs, if enabled (batchJobSize is set to higher value than 0)
        for (int i = 0; i < batchJobsSize; i++) {
            jobs = jobsToProcess.poll(batchJobTimeout, TimeUnit.MILLISECONDS);
            if (jobs == null) {
                break;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Batching additional Jobs [" + System.identityHashCode(jobs) + "]");
            }
            addConcurrentJobsToProcess(concurrentJobsToProcess, subIndexes, jobs);
        }
        // now spin non blocking
        List<TransactionJobs> nonBlockingDrainToList = new ArrayList<TransactionJobs>();
        if (jobsToProcess.drainTo(nonBlockingDrainToList, nonBlockingBatchSize) > 0) {
            for (TransactionJobs transactionJobs : nonBlockingDrainToList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Batching additional Jobs [" + System.identityHashCode(transactionJobs) + "]");
                }
                addConcurrentJobsToProcess(concurrentJobsToProcess, subIndexes, transactionJobs);
            }
        }

        boolean failure = false;

        Map<String, IndexWriter> writers = new HashMap<String, IndexWriter>();
        // open index writers
        for (String subIndex : subIndexes) {
            try {
                IndexWriter writer = indexManager.getIndexWritersManager().openIndexWriter(settings, subIndex);
                indexManager.getIndexWritersManager().trackOpenIndexWriter(subIndex, writer);
                writers.put(subIndex, writer);
            } catch (Exception e) {
                logger.warn("Failed to open index writer for sub index [" + subIndex + "]", e);
                failure = true;
                break;
            }
        }
        if (failure) {
            closeWriters(writers);
            return;
        }

        // process all the jobs by multiple threads
        ArrayList<Callable<Object>> processCallables = new ArrayList<Callable<Object>>();
        for (List<TransactionJob> list : concurrentJobsToProcess) {
            if (list.isEmpty()) {
                // no need to create a thread for empty list
                continue;
            }
            processCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new TransactionJobProcessor(list, writers)));
        }
        try {
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(processCallables, 1);
        } catch (Exception e) {
            logger.warn("Failed to index", e);
            failure = true;
        }
        if (failure) {
            rollbackWriters(writers);
            return;
        }

        // prepare for commit
        ArrayList<Callable<Object>> prepareCallables = new ArrayList<Callable<Object>>();
        for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
            prepareCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new PrepareCommitCallable(entry.getKey(), entry.getValue())));
        }
        try {
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(prepareCallables, 1);
        } catch (Exception e) {
            logger.warn("Faield to prepare commit", e);
            failure = true;
        }
        if (failure) {
            rollbackWriters(writers);
            return;
        }

        // commit
        ArrayList<Callable<Object>> commitCallables = new ArrayList<Callable<Object>>();
        for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
            commitCallables.add(new TransactionalCallable(indexManager.getTransactionContext(), new CommitCallable(indexManager, entry.getKey(), entry.getValue(), isClearCacheOnCommit())));
        }
        try {
            indexManager.getExecutorManager().invokeAllWithLimitBailOnException(commitCallables, 1);
        } catch (Exception e) {
            logger.warn("Failed to commit", e);
        }
    }

    /**
     * Closes all the list of writers passed as part of the writers map.
     */
    private void closeWriters(Map<String, IndexWriter> writers) {
        for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().close();
            } catch (AlreadyClosedException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to close transaction for sub index [" + entry.getKey() + "] since it is alreayd closed");
                }
            } catch (IOException e) {
                Directory dir = searchEngineFactory.getLuceneIndexManager().getStore().openDirectory(entry.getKey());
                try {
                    if (IndexWriter.isLocked(dir)) {
                        IndexWriter.unlock(dir);
                    }
                } catch (Exception e1) {
                    logger.warn("Failed to check for locks or unlock failed commit for sub index [" + entry.getKey() + "]", e);
                }
                logger.warn("Failed to close index writer for sub index [" + entry.getKey() + "]", e);
            } finally {
                searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackCloseIndexWriter(entry.getKey(), entry.getValue());
            }
        }
        writers.clear();
    }

    /**
     * Rolls back all the list of writers passed as part of the writers map.
     */
    private void rollbackWriters(Map<String, IndexWriter> writers) {
        SearchEngineException exception = null;
        for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().rollback();
            } catch (AlreadyClosedException e) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Failed to abort transaction for sub index [" + entry.getKey() + "] since it is alreayd closed");
                }
            } catch (IOException e) {
                Directory dir = searchEngineFactory.getLuceneIndexManager().getStore().openDirectory(entry.getKey());
                try {
                    if (IndexWriter.isLocked(dir)) {
                        IndexWriter.unlock(dir);
                    }
                } catch (Exception e1) {
                    logger.warn("Failed to check for locks or unlock failed commit for sub index [" + entry.getKey() + "]", e);
                }
                exception = new SearchEngineException("Failed to rollback transaction for sub index [" + entry.getKey() + "]", e);
            } finally {
                searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackCloseIndexWriter(entry.getKey(), entry.getValue());
            }
        }
        writers.clear();
    }

    private void addConcurrentJobsToProcess(List<TransactionJob>[] concurrentJobsToProcess, Set<String> subIndexes, TransactionJobs jobs) {
        subIndexes.addAll(jobs.getSubIndexes());
        for (TransactionJob job : jobs.getJobs()) {
            concurrentJobsToProcess[hashing.hash(job) % concurrencyLevel].add(job);
        }
    }

    protected boolean isClearCacheOnCommit() {
        return settings.getSettingAsBoolean(LuceneEnvironment.Transaction.CLEAR_CACHE_ON_COMMIT, true);
    }

    private class PollingProcessor implements Callable<Object> {

        private volatile boolean closed = false;

        private volatile boolean done = false;

        public void close() {
            this.closed = true;
        }

        public boolean isDone() {
            return this.done;
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

                    process(jobs);

                    if (logger.isTraceEnabled()) {
                        logger.trace("Procesing jobs done");
                    }
                } catch (InterruptedException e) {
                    if (closed) {
                        break;
                    }
                    if (logger.isTraceEnabled()) {
                        logger.trace("Polling for transaction jobs interrupted", e);
                    }
                    // continue here, since when we get interrupted, the closed flag should be set to true
                } catch (Exception e) {
                    // non handled exception within process, log it
                    if (logger.isWarnEnabled()) {
                        logger.warn("Exception while processing job", e);
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Async polling transaction processor thread stopped");
            }
            this.done = true;
            return null;
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
                IndexWriter writer = writers.get(job.getSubIndex());
                job.execute(writer, searchEngineFactory);
            }
            return null;
        }
    }

}
