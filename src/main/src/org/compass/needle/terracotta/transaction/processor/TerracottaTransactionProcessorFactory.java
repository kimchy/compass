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

package org.compass.needle.terracotta.transaction.processor;

import java.io.IOException;
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
import org.compass.core.lucene.engine.transaction.support.TransactionJob;
import org.compass.core.lucene.engine.transaction.support.TransactionJobs;
import org.compass.core.lucene.engine.transaction.support.WriterHelper;

/**
 * Experimental!.
 *
 * @author kimchy
 */
public class TerracottaTransactionProcessorFactory implements TransactionProcessorFactory, CompassConfigurable, SearchEngineFactoryAware {

    private static transient final Log logger = LogFactory.getLog(TerracottaTransactionProcessorFactory.class);

    private final TerracottaHolder holder = new TerracottaHolder();

    private transient LuceneSearchEngineFactory searchEngineFactory;

    private transient CompassSettings settings;

    private final transient Map<String, TerracottaProcessor> currentProcessors = new ConcurrentHashMap<String, TerracottaProcessor>();

    public void setSearchEngineFactory(SearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = (LuceneSearchEngineFactory) searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
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
        if (settings.getSettingAsBoolean(TerracottaTransactionProcessorEnvironment.PROCESS, false)) {
            logger.info("Terracotta processor started");
            for (String subIndex : searchEngineFactory.getIndexManager().getSubIndexes()) {
                TerracottaProcessor processor = new TerracottaProcessor(subIndex, holder.getJobsPerSubIndex().get(subIndex));
                searchEngineFactory.getExecutorManager().submit(processor);
                currentProcessors.put(subIndex, processor);
            }
        } else {
            logger.info("Terracotta transaction processor will not process transactions on this node");
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

        public void run() {
            while (running) {
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
                logger.info("Processor [" + subIndex + "] procesing jobs [" + jobs + "]");
                jobs.attach(searchEngineFactory);

                Lock processLock = holder.getProcessorLocks().get(subIndex);

                processLock.lock(); // create a shared lock for terracotta to process
                try {
                    IndexWriter writer;
                    try {
                        writer = searchEngineFactory.getLuceneIndexManager().openIndexWriter(settings, subIndex);
                    } catch (LockObtainFailedException e) {
                        // we failed to get a lock, probably another one running and getting it, which is bad!
                        logger.error("Another instance is running on the sub index, make sure it does not");
                        // TODO we need to do something here with the job
                        continue;
                    } catch (IOException e) {
                        logger.error("Failed to open index writer, dismissing jobs [" + jobs + "]", e);
                        continue;
                    }
                    try {
                        for (TransactionJob job : jobs.getJobs()) {
                            WriterHelper.processJob(writer, job);
                        }
                        writer.commit();
                    } catch (Exception e) {
                        logger.error("Failed to process jobs [" + jobs + "]", e);
                        try {
                            writer.rollback();
                        } catch (IOException e1) {
                            logger.warn("Failed to rollback transaction on jobs [" + jobs + "]", e);
                        }
                    } finally {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            logger.warn("Failed to close writer, ignoring", e);
                        }
                    }
                } finally {
                    processLock.unlock();
                }
            }
        }
    }
}
