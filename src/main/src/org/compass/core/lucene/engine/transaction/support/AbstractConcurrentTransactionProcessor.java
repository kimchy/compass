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

package org.compass.core.lucene.engine.transaction.support;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public abstract class AbstractConcurrentTransactionProcessor extends AbstractSearchTransactionProcessor {

    private final boolean waitForSearchOperations;

    private final boolean concurrentOperations;

    private final int concurrencyLevel;

    private Processor[] processors;

    private final ResourceHashing hashing;

    private final int backlog;

    private final long addTimeout;

    protected AbstractConcurrentTransactionProcessor(Log logger, LuceneSearchEngine searchEngine,
                                                     boolean waitForSearchOperations, boolean concurrentOperations) {
        super(logger, searchEngine);
        this.waitForSearchOperations = waitForSearchOperations;
        this.concurrentOperations = concurrentOperations && searchEngine.getSettings().getSettingAsBoolean(getSettingName("concurrentOperations"), true);
        this.concurrencyLevel = searchEngine.getSettings().getSettingAsInt(getSettingName("concurrencyLevel"), 5);
        this.hashing = ResourceHashing.fromName(searchEngine.getSettings().getSetting(getSettingName("hashing"), "uid"));
        this.backlog = searchEngine.getSettings().getSettingAsInt(getSettingName("backlog"), 100);
        this.addTimeout = searchEngine.getSettings().getSettingAsTimeInMillis(getSettingName("addTimeout"), 10000);
    }

    public boolean isConcurrentOperations() {
        return concurrentOperations;
    }

    public void begin() throws SearchEngineException {
        // nothing to do here
    }

    public void prepare() throws SearchEngineException {
        if (concurrentOperations) {
            waitForJobs();
        }
        doPrepare();
    }

    protected abstract void doPrepare() throws SearchEngineException;

    public void commit(boolean onePhase) throws SearchEngineException {
        if (concurrentOperations) {
            waitForJobs();
        }
        doCommit(onePhase);
    }

    protected abstract void doCommit(boolean onePhase) throws SearchEngineException;

    public void rollback() throws SearchEngineException {
        clearJobs();
        doRollback();
    }

    protected abstract void doRollback() throws SearchEngineException;

    public void flush() throws SearchEngineException {
        waitForJobs();
        doFlush();
    }

    protected void doFlush() throws SearchEngineException {

    }

    public void create(InternalResource resource) throws SearchEngineException {
        if (concurrentOperations) {
            TransactionJob job = new TransactionJob(TransactionJob.Type.CREATE, resource);
            prepareBeforeAsyncDirtyOperation(job);
            getProcessor(job).addJob(job);
        } else {
            doCreate(resource);
        }
    }

    protected abstract void doCreate(InternalResource resource) throws SearchEngineException;

    public void update(InternalResource resource) throws SearchEngineException {
        if (concurrentOperations) {
            TransactionJob job = new TransactionJob(TransactionJob.Type.UPDATE, resource);
            prepareBeforeAsyncDirtyOperation(job);
            getProcessor(job).addJob(job);
        } else {
            doUpdate(resource);
        }
    }

    protected abstract void doUpdate(InternalResource resource) throws SearchEngineException;

    public void delete(ResourceKey resourceKey) throws SearchEngineException {
        if (concurrentOperations) {
            TransactionJob job = new TransactionJob(TransactionJob.Type.DELETE, resourceKey);
            prepareBeforeAsyncDirtyOperation(job);
            getProcessor(job).addJob(job);
        } else {
            doDelete(resourceKey);
        }
    }

    protected abstract void doDelete(ResourceKey resourceKey) throws SearchEngineException;

    /**
     * Called by a single thread (the calling thread) before a dirty transaction job is added to the
     * queue to be executed in an async manner.
     */
    protected abstract void prepareBeforeAsyncDirtyOperation(TransactionJob job) throws SearchEngineException;

    public LuceneSearchEngineHits find(LuceneSearchEngineQuery query) throws SearchEngineException {
        if (waitForSearchOperations && concurrentOperations) {
            waitForJobs();
        }
        return doFind(query);
    }

    protected abstract LuceneSearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException;

    public LuceneSearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        if (waitForSearchOperations && concurrentOperations) {
            waitForJobs();
        }
        return doInternalSearch(subIndexes, aliases);
    }

    protected abstract LuceneSearchEngineInternalSearch doInternalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException;

    public Resource[] get(ResourceKey resourceKey) throws SearchEngineException {
        if (waitForSearchOperations && concurrentOperations) {
            waitForJobs();
        }
        return doGet(resourceKey);
    }

    protected abstract Resource[] doGet(ResourceKey resourceKey) throws SearchEngineException;

    private void clearJobs() {
        if (!concurrentOperations || processors == null) {
            return;
        }
        InterruptedException ie = null;
        int lastId = -1;
        for (Processor processor : processors) {
            if (processor != null) {
                // we clean before stop so we won't even process any remaining jobs
                processor.clear();
                try {
                    processor.stop();
                } catch (InterruptedException e) {
                    lastId = processor.getId();
                    ie = e;
                }
            }
        }
        if (ie != null) {
            logger.warn("Failed to wait for processor [" + lastId + "] to stop, interrupted", ie);
        }
        SearchEngineException exception = null;
        for (Processor processor : processors) {
            if (processor != null) {
                try {
                    processor.waitTillStopped();
                } catch (InterruptedException e) {
                    throw new SearchEngineException("Failed to wait for processor [" + processor.getId() + "] to be stopped / process all jobs", e);
                }
                exception = processor.getException();
            }
        }
        if (exception != null) {
            logger.trace("EXception while waiting to clear jobs for rollback", exception);
        }
    }

    private void waitForJobs() {
        if (!concurrentOperations || processors == null) {
            return;
        }
        InterruptedException ie = null;
        int lastId = -1;
        for (Processor processor : processors) {
            if (processor != null) {
                try {
                    processor.stop();
                } catch (InterruptedException e) {
                    lastId = processor.getId();
                    ie = e;
                }
            }
        }
        if (ie != null) {
            logger.warn("Failed to wait for processor [" + lastId + "] to stop, interrupted", ie);
        }
        SearchEngineException exception = null;
        for (Processor processor : processors) {
            if (processor != null) {
                try {
                    processor.waitTillStopped();
                } catch (InterruptedException e) {
                    throw new SearchEngineException("Failed to wait for processor [" + processor.getId() + "] to be stopped / process all jobs", e);
                }
                exception = processor.getException();
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    private Processor getProcessor(TransactionJob job) {
        if (processors == null) {
            processors = new Processor[concurrencyLevel];
        }
        int processorIndex = hashing.hash(job) % concurrencyLevel;
        Processor processor = processors[processorIndex];
        if (processor == null) {
            processor = new Processor(processorIndex);
            processors[processorIndex] = processor;
        }
        try {
            if (processor.needsReschedule()) {
                processor.start();
                indexManager.getExecutorManager().submit(processor);
            }
        } catch (InterruptedException e) {
            throw new SearchEngineException("Failed to wait for processor [" + processor.getId() + "] to check if stopped", e);
        }

        return processor;
    }

    private class Processor implements Runnable {

        private final BlockingQueue<TransactionJob> jobs = new ArrayBlockingQueue<TransactionJob>(backlog);

        private final int id;

        private volatile boolean stopped = true;

        private volatile CountDownLatch stopLatch;

        private volatile CountDownLatch startLatch;

        private volatile SearchEngineException exception;

        private Processor(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        /**
         * Returns an exception that happened during an execution of the processor.
         *
         * <p>Note, should be called after {@link #waitTillStopped()}.
         */
        public SearchEngineException getException() {
            return exception;
        }

        public boolean needsReschedule() throws InterruptedException {
            if (stopped) {
                waitTillStopped();
            }
            return stopped;
        }

        public void start() {
            if (logger.isTraceEnabled()) {
                logger.trace("Processor [" + id + "]: Starting");
            }
            startLatch = new CountDownLatch(1);
            stopped = false;
        }

        public void stop() throws InterruptedException {
            if (stopped) {
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("Processor [" + id + "]: Stopping");
            }
            stopped = true;
        }

        public void clear() {
            jobs.clear();
        }

        /**
         * Wait till stop. Note, should be called only after {@link #stop()}  was called.
         */
        public void waitTillStopped() throws InterruptedException {
            if (startLatch != null) {
                startLatch.await();
            }
            if (stopLatch != null) {
                stopLatch.await();
            }
        }

        public void addJob(TransactionJob job) throws SearchEngineException {
            if (exception != null) {
                throw exception;
            }
            try {
                if (logger.isTraceEnabled()) {
                    logger.trace("Processor [" + id + "]: Adding Job [" + job + "]");
                }
                boolean offered = jobs.offer(job, addTimeout, TimeUnit.MILLISECONDS);
                if (!offered) {
                    throw new SearchEngineException("Processor [" + id + "]: Failed to add job [" + job + "] after [" + addTimeout + "ms] and backlog size [" + backlog + "]");
                }
            } catch (InterruptedException e) {
                throw new SearchEngineException("Processor [" + id + "]: Failed to add job [" + job + "], interrupted while adding to queue", e);
            }
        }

        public void run() {
            try {
                // clear the exception, since this is a new run
                // note, calling getException is only done after we waitTillStopped (which is only done after stop)
                exception = null;

                stopLatch = new CountDownLatch(1);
                startLatch.countDown();
                if (logger.isTraceEnabled()) {
                    logger.trace("Processor [" + id + "]: Started");
                }
                while (!stopped) {
                    TransactionJob job;
                    try {
                        job = jobs.poll(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        if (!stopped) {
                            logger.warn("Processor [" + id + "]: Interrupted without being stopped", e);
                        }
                        break;
                    }
                    if (job != null) {
                        try {
                            processJob(job);
                        } catch (SearchEngineException e) {
                            exception = e;
                            break;
                        }
                    }
                }
                if (exception != null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Processor [" + id + "]: Stopping because of an exception", exception);
                    }
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Processor [" + id + "]: Received stop, processing remaining jobs");
                    }
                    try {
                        processRemainingJobs();
                    } catch (SearchEngineException e) {
                        if (logger.isTraceEnabled()) {
                            logger.trace("Processor [" + id + "]: Failed to processes remaining jobs", e);
                        }
                        exception = e;
                    }
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Processor [" + id + "]: Stopped");
                }
            } catch (Exception e) {
                logger.warn("Processor [" + id + "]: Recevied an unexpected exception", e);
            } finally {
                stopLatch.countDown();
            }
        }

        private void processRemainingJobs() throws SearchEngineException {
            ArrayList<TransactionJob> remainingJobs = new ArrayList<TransactionJob>();
            jobs.drainTo(remainingJobs);
            for (TransactionJob job : remainingJobs) {
                processJob(job);
            }
        }

        private void processJob(TransactionJob job) throws SearchEngineException {
            if (logger.isTraceEnabled()) {
                logger.trace("Processor [" + id + "]: Processing Job  [" + job + "]");
            }
            switch (job.getType()) {
                case CREATE:
                    doCreate(job.getResource());
                    break;
                case UPDATE:
                    doUpdate(job.getResource());
                    break;
                case DELETE:
                    doDelete(job.getResourceKey());
                    break;
            }
        }
    }
}
