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
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
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

    private CompassSettings settings;

    private LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSearchEngineIndexManager indexManager;

    private boolean processBeforeClose;

    private int concurrencyLevel;

    private int batchJobsSize;

    private long batchJobTimeout;

    private Hashing hashing;

    private BlockingQueue<TransactionJobs> jobsToProcess;

    private Future pollingProcessorFuture;

    private PollingProcessor pollingProcessor;

    private volatile boolean closed = false;

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
        this.indexManager = this.searchEngineFactory.getLuceneIndexManager();
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        jobsToProcess = new ArrayBlockingQueue<TransactionJobs>(settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BACKLOG, 10), true);

        batchJobsSize = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, 5);
        batchJobTimeout = settings.getSettingAsTimeInMillis(LuceneEnvironment.Transaction.Processor.Async.BATCH_JOBS_SIZE, 100);

        processBeforeClose = settings.getSettingAsBoolean(LuceneEnvironment.Transaction.Processor.Async.PROCESS_BEFORE_CLOSE, true);

        this.concurrencyLevel = settings.getSettingAsInt(LuceneEnvironment.Transaction.Processor.Async.CONCURRENCY_LEVEL, 5);

        String hashingSetting = settings.getSetting(LuceneEnvironment.Transaction.Processor.Async.HASHING, "uid");
        if ("uid".equalsIgnoreCase(hashingSetting)) {
            hashing = Hashing.UID;
        } else if ("subindex".equalsIgnoreCase(hashingSetting)) {
            hashing = Hashing.SUBINDEX;
        } else {
            throw new ConfigurationException("No hashing support for [" + hashingSetting + "]. Either use 'uid' (defualt) or 'subindex'");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Starting Async polling transaction processor");
        }
    }

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

    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new AsyncTransactionProcessor(searchEngine, this);
    }

    public boolean remove(TransactionJobs jobs) throws SearchEngineException {
        return jobsToProcess.remove(jobs);
    }

    public void add(TransactionJobs jobs) throws SearchEngineException {
        synchronized (this) { // though called from single thread each time, not that  big an overhead to make it thread safe
            if (pollingProcessor == null) {
                this.pollingProcessor = new PollingProcessor();
                pollingProcessorFuture = searchEngineFactory.getExecutorManager().submit(pollingProcessor);
            }
        }
        try {
            boolean offered = jobsToProcess.offer(jobs, 10, TimeUnit.SECONDS);
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

        boolean failure = false;

        Map<String, IndexWriter> writers = new HashMap<String, IndexWriter>();
        // open index writers
        for (String subIndex : subIndexes) {
            try {
                IndexWriter writer = indexManager.openIndexWriter(settings, subIndex, false);
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

    private void closeWriters(Map<String, IndexWriter> writers) {
        for (Map.Entry<String, IndexWriter> entry : writers.entrySet()) {
            try {
                entry.getValue().rollback();
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
            }
        }
        writers.clear();
    }

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
            }
        }
        writers.clear();
    }

    private void addConcurrentJobsToProcess(List<TransactionJob>[] concurrentJobsToProcess, Set<String> subIndexes, TransactionJobs jobs) {
        subIndexes.addAll(jobs.getSubIndexes());
        for (TransactionJob job : jobs.getJobs()) {
            concurrentJobsToProcess[hash(job) % concurrencyLevel].add(job);
        }
    }

    private int hash(TransactionJob job) {
        if (hashing == Hashing.UID) {
            return CollectionUtils.absHash(job.getResourceUID());
        }
        return CollectionUtils.absHash(job.getSubIndex());
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

    private enum Hashing {
        UID,
        SUBINDEX
    }
}
