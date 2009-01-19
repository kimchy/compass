package org.compass.needle.terracotta.transaction.processor;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.compass.core.lucene.engine.transaction.support.job.TransactionJobs;

/**
 * @author kimchy
 */
public class TerracottaHolder {

    private final Map<String, BlockingQueue<TransactionJobs>> jobsPerSubIndex = new ConcurrentHashMap<String, BlockingQueue<TransactionJobs>>();

    private final Lock initializationLock = new ReentrantLock();

    private final Map<String, Lock> processorLocks = new ConcurrentHashMap<String, Lock>();

    public Map<String, BlockingQueue<TransactionJobs>> getJobsPerSubIndex() {
        return jobsPerSubIndex;
    }

    public Map<String, Lock> getProcessorLocks() {
        return processorLocks;
    }

    public Lock getInitializationLock() {
        return initializationLock;
    }
}
