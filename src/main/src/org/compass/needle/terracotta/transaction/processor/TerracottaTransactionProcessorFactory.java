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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.SearchEngineFactoryAware;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJob;
import org.compass.core.lucene.engine.transaction.support.job.TransactionJobs;
import org.compass.core.transaction.context.TransactionContextCallback;
import org.compass.core.util.StringUtils;

/**
 * The terracotta transaction processor factory allows to add {@link org.compass.core.lucene.engine.transaction.support.job.TransactionJobs TransactionJobs}
 * to a shared work queue (partitioned by sub index) to be processed later by worker processors.
 *
 * <p>By default, the transaction processor factory acts as a worker processor as well. In order to disable it (and make
 * it only a producer node) set the {@link TerracottaTransactionProcessorEnvironment#PROCESS} to <code>false</code>.
 *
 * <p>By default, each worker processor node will try and processes jobs from all sub indexes (you can start as many
 * as you like). In order to pin down the worker processor to work only on specific sub indexes, set then using
 * {@link TerracottaTransactionProcessorEnvironment#SUB_INDEXES} setting.
 *
 * <p>The processor itself, once it identifies that there is a transactional job to be processed, will try and get
 * more transactional jobs in a non blocking manner for better utilization of an already opened IndexWriter. By default
 * it will try and get 5 more, and it can be controlled using {@link TerracottaTransactionProcessorEnvironment#NON_BLOCKING_BATCH_JOBS_SIZE}.
 *
 * <p>Transactions visibility (once a transaction commits, how long till the other nodes, including the one that committed
 * will see the result) can be controlld using {@link org.compass.core.lucene.LuceneEnvironment.SearchEngineIndex#CACHE_INTERVAL_INVALIDATION}.
 * Note, by default, refreshing to a new index happens in the background and does not affect the search nodes.
 *
 * <p>When working with several machines, the index should probably be shared between all nodes. The terracotta based
 * directory store can be used to share the index as well.
 *
 * @author kimchy
 */
public class TerracottaTransactionProcessorFactory implements TransactionProcessorFactory, CompassConfigurable, SearchEngineFactoryAware {

    private static transient final Log logger = LogFactory.getLog(TerracottaTransactionProcessorFactory.class);

    private final TerracottaHolder holder = new TerracottaHolder();

    private transient LuceneSearchEngineFactory searchEngineFactory;

    private transient CompassSettings settings;

    private final transient Map<String, TerracottaProcessor> currentProcessors = new ConcurrentHashMap<String, TerracottaProcessor>();

    private int batchJobsSize;

    private long batchJobTimeout;

    private int nonBlockingBatchSize;

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
        batchJobsSize = settings.getSettingAsInt(TerracottaTransactionProcessorEnvironment.BATCH_JOBS_SIZE, 5);
        batchJobTimeout = settings.getSettingAsTimeInMillis(TerracottaTransactionProcessorEnvironment.BATCH_JOBS_SIZE, 100);
        if (logger.isDebugEnabled()) {
            logger.debug("Terracotta Transaction Processor blocking batch size is [" + batchJobsSize + "] with timeout of [" + batchJobTimeout + "ms]");
        }
        nonBlockingBatchSize = settings.getSettingAsInt(TerracottaTransactionProcessorEnvironment.NON_BLOCKING_BATCH_JOBS_SIZE, 5);
        if (logger.isDebugEnabled()) {
            logger.debug("Terracotta Transaction Processor non blocking batch size is [" + nonBlockingBatchSize + "]");
        }
        holder.getInitializationLock().lock();
        try {
            for (String subIndex : searchEngineFactory.getIndexManager().getSubIndexes()) {
                BlockingQueue<TransactionJobs> subIndexJobs = holder.getJobsPerSubIndex().get(subIndex);
                if (subIndexJobs == null) {
                    subIndexJobs = new LinkedBlockingQueue<TransactionJobs>();
                    holder.getJobsPerSubIndex().put(subIndex, subIndexJobs);
                }
                Lock processorLock = holder.getProcessorLocks().get(subIndex);
                if (processorLock == null) {
                    processorLock = new ReentrantLock();
                    holder.getProcessorLocks().put(subIndex, processorLock);
                }
            }
        } finally {
            holder.getInitializationLock().unlock();
        }
        if (settings.getSettingAsBoolean(TerracottaTransactionProcessorEnvironment.PROCESS, true)) {
            String[] subIndexesSetting = StringUtils.commaDelimitedListToStringArray(settings.getSetting(TerracottaTransactionProcessorEnvironment.SUB_INDEXES));
            if (subIndexesSetting.length == 0) {
                subIndexesSetting = null;
            }
            String[] aliasesSetting = StringUtils.commaDelimitedListToStringArray(settings.getSetting(TerracottaTransactionProcessorEnvironment.ALIASES));
            if (aliasesSetting.length == 0) {
                aliasesSetting = null;
            }
            String[] subIndexes = searchEngineFactory.getIndexManager().calcSubIndexes(subIndexesSetting, aliasesSetting, null);
            logger.info("Terracotta Transaction Processor Worker started. Sub indexes to process: " + Arrays.toString(subIndexes));
            for (String subIndex : subIndexes) {
                TerracottaProcessor processor = new TerracottaProcessor(subIndex, holder.getJobsPerSubIndex().get(subIndex));
                searchEngineFactory.getExecutorManager().submit(processor);
                currentProcessors.put(subIndex, processor);
            }
        } else {
            logger.info("Terracotta transaction processor will only submit transactions to be processed (none worker mode)");
        }
    }

    public TransactionProcessor create(LuceneSearchEngine searchEngine) {
        return new TerracottaTransactionProcessor(searchEngine, this);
    }

    public void close() {
        for (TerracottaProcessor processor : currentProcessors.values()) {
            processor.stop();
        }
    }

    /**
     * The terracotta transaction processor is not thread safe.
     */
    public boolean isThreadSafe() {
        return false;
    }

    public Map<String, TransactionJobs> add(TransactionJobs jobs) {
        Map<String, TransactionJobs> subIndexesJobs = jobs.buildJobsPerSubIndex();
        for (Map.Entry<String, TransactionJobs> entry : subIndexesJobs.entrySet()) {
            holder.getJobsPerSubIndex().get(entry.getKey()).add(entry.getValue());
        }
        return subIndexesJobs;
    }

    public void remove(Map<String, TransactionJobs> subIndexesJobs) {
        for (Map.Entry<String, TransactionJobs> entry : subIndexesJobs.entrySet()) {
            holder.getJobsPerSubIndex().get(entry.getKey()).remove(entry.getValue());
        }
    }

    private class TerracottaProcessor implements Runnable {

        private final BlockingQueue<TransactionJobs> jobsToProcess;

        private final String subIndex;

        private volatile boolean running = true;

        private TerracottaProcessor(String subIndex, BlockingQueue<TransactionJobs> jobsToProcess) {
            this.subIndex = subIndex;
            this.jobsToProcess = jobsToProcess;
        }

        public String getSubIndex() {
            return subIndex;
        }

        public void stop() {
            running = false;
        }

        private String message(String message) {
            return "Processor [" + subIndex + "]: " + message;
        }

        public void run() {
            while (running) {
                // each node locks and waits for jobs. This means that there are never
                // two processors for the same sub index waiting for (and potentially taking) jobs.
                // This also allows for several JVMs to run and be able to share the load for a specific
                // sub index (if they are handling more than one sub index)
                Lock processLock = holder.getProcessorLocks().get(subIndex);
                boolean locked = false; // create a shared lock for terracotta to process
                try {
                    locked = processLock.tryLock(1000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // we geto interupted, bail out
                    running = false;                    
                }
                if (!locked) {
                    continue;
                }

                try {
                    TransactionJobs jobs = null;
                    try {
                        jobs = jobsToProcess.poll(1000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        // we geto interupted, bail out
                        running = false;
                    }
                    if (jobs == null) {
                        continue;
                    }
                    final List<TransactionJobs> jobsList = new ArrayList<TransactionJobs>();
                    jobsList.add(jobs);

                    for (int i = 0; i < batchJobsSize; i++) {
                        try {
                            jobs = jobsToProcess.poll(batchJobTimeout, TimeUnit.MILLISECONDS);
                        } catch (InterruptedException e) {
                            // we geto interupted, bail out in the next run
                            running = false;
                        }
                        if (jobs == null) {
                            break;
                        }
                        if (logger.isTraceEnabled()) {
                            logger.trace("Batching additional Jobs [" + System.identityHashCode(jobs) + "]");
                        }
                        jobsList.add(jobs);
                    }

                    jobsToProcess.drainTo(jobsList, nonBlockingBatchSize);
                    if (logger.isDebugEnabled()) {
                        int totalJobs = 0;
                        for (TransactionJobs x : jobsList) {
                            totalJobs += x.getJobs().size();
                        }
                        logger.debug(message("procesing [" + jobsList.size() + "] transactions with [" + totalJobs + "] jobs"));
                    }

                    final TransactionJobs finalJobs = jobs;
                    searchEngineFactory.getTransactionContext().execute(new TransactionContextCallback<Boolean>() {
                        public Boolean doInTransaction() throws CompassException {
                            IndexWriter writer;
                            try {
                                writer = searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().openIndexWriter(settings, subIndex);
                                searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackOpenIndexWriter(subIndex, writer);
                            } catch (LockObtainFailedException e) {
                                // we failed to get a lock, probably another one running and getting it, which is bad!
                                logger.error(message("Another instance is running on the sub index, make sure it does not. Should not happen really..."));
                                return false;
                            } catch (IOException e) {
                                logger.error(message("Failed to open index writer, dismissing jobs [" + finalJobs + "]. Should not happen really..."), e);
                                return false;
                            }
                            try {
                                for (TransactionJobs xJobs : jobsList) {
                                    for (TransactionJob job : xJobs.getJobs()) {
                                        job.execute(writer, searchEngineFactory);
                                    }
                                }
                                writer.commit();
                            } catch (Exception e) {
                                logger.error(message("Failed to process jobs [" + finalJobs + "]"), e);
                                try {
                                    writer.rollback();
                                } catch (IOException e1) {
                                    logger.warn(message("Failed to rollback transaction on jobs [" + finalJobs + "]"), e);
                                }
                            } finally {
                                try {
                                    writer.close();
                                } catch (IOException e) {
                                    logger.warn(message("Failed to close writer, ignoring"), e);
                                } finally {
                                    searchEngineFactory.getLuceneIndexManager().getIndexWritersManager().trackCloseIndexWriter(subIndex, writer);
                                }
                            }
                            return null;
                        }
                    });
                } finally {
                    processLock.unlock();
                }
            }
        }
    }
}
