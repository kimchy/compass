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

package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.compass.core.executor.ExecutorManager;
import org.compass.core.transaction.context.TransactionContext;
import org.compass.core.transaction.context.TransactionalRunnable;

/**
 * The executor merge scheduler is similar to Lucene {@link org.apache.lucene.index.ConcurrentMergeScheduler}
 * but instead of spawning threads it uses Compass {@link org.compass.core.executor.ExecutorManager} in order
 * to execute the merges.
 *
 * <p>Since the executor manager is a thread pool, there is no need to have the running threads continue to
 * ask the index writer for more merges. Instead, it simple reexecute another possible merge with the executor manager.
 *
 * @author kimchy
 */
// LUCENE MONITOR
public class ExecutorMergeScheduler extends MergeScheduler {

    private ExecutorManager executorManager;

    private TransactionContext transactionContext;

    private volatile int currentConcurrentMerges = 0;
    private volatile int maxConcurrentMerges = 3;

    private Directory dir;

    private boolean closed;

    private IndexWriter writer;

    public ExecutorMergeScheduler(ExecutorManager executorManager, TransactionContext transactionContext) {
        this.executorManager = executorManager;
        this.transactionContext = transactionContext;
    }

    public int getMaxConcurrentMerges() {
        return maxConcurrentMerges;
    }

    public void setMaxConcurrentMerges(int maxConcurrentMerges) {
        this.maxConcurrentMerges = maxConcurrentMerges;
    }

    private void message(String message) {
        if (writer != null)
            writer.message("EMS: " + message);
    }

    public void close() {
        closed = true;
    }

    public void merge(IndexWriter writer)
            throws CorruptIndexException, IOException {

        this.writer = writer;

        dir = writer.getDirectory();

        // First, quickly run through the newly proposed merges
        // and add any orthogonal merges (ie a merge not
        // involving segments already pending to be merged) to
        // the queue.  If we are way behind on merging, many of
        // these newly proposed merges will likely already be
        // registered.

        message("now merge");
        message("  index: " + writer.segString());

        // Iterate, pulling from the IndexWriter's queue of
        // pending merges, until its empty:
        while (true) {

            // TODO: we could be careful about which merges to do in
            // the BG (eg maybe the "biggest" ones) vs FG, which
            // merges to do first (the easiest ones?), etc.

            MergePolicy.OneMerge merge = writer.getNextMerge();
            if (merge == null) {
                message("  no more merges pending; now return");
                return;
            }

            // We do this w/ the primary thread to keep
            // deterministic assignment of segment names
            writer.mergeInit(merge);

            message("  consider merge " + merge.segString(dir));

            if (merge.isExternal) {
                message("    merge involves segments from an external directory; now run in foreground");
            } else {
                synchronized (this) {
                    if (currentConcurrentMerges < maxConcurrentMerges) {
                        // OK to spawn a new merge thread to handle this
                        // merge:
                        currentConcurrentMerges++;
                        MergeThread merger = new MergeThread(writer, merge);
                        executorManager.submit(new TransactionalRunnable(transactionContext, merger));
                        message("    executed merge in executor manager");
                        continue;
                    } else
                        message("    too many merge threads running; run merge in foreground");
                }
            }

            // Too many merge threads already running, so we do
            // this in the foreground of the calling thread
            writer.merge(merge);
        }
    }

    private class MergeThread implements Runnable {

        IndexWriter writer;
        MergePolicy.OneMerge startMerge;
        MergePolicy.OneMerge runningMerge;

        public MergeThread(IndexWriter writer, MergePolicy.OneMerge startMerge) throws IOException {
            this.writer = writer;
            this.startMerge = startMerge;
        }

        public synchronized void setRunningMerge(MergePolicy.OneMerge merge) {
            runningMerge = merge;
        }

        public synchronized MergePolicy.OneMerge getRunningMerge() {
            return runningMerge;
        }

        public void run() {
            // First time through the while loop we do the merge
            // that we were started with:
            MergePolicy.OneMerge merge = this.startMerge;

            // COMPASS: If we get into this because of another reschecdule, we set just before we run it the
            // running merge, so, if it is not null, we use that one instead of the startMerge
            if (runningMerge != null) {
                merge = runningMerge;
            }

            try {

                message("  merge thread: start");

// Compass: No need to execute continous merges, we simply reschedule another merge, if there is any, using executor manager                
//                while (true) {
                setRunningMerge(merge);
                writer.merge(merge);

                // Subsequent times through the loop we do any new
                // merge that writer says is necessary:
                merge = writer.getNextMerge();
                if (merge != null) {
                    writer.mergeInit(merge);
                    message("  merge thread: do another merge " + merge.segString(dir));
                    // COMPASS: Set the running merge so it will be picked up in the next run
                    setRunningMerge(merge);
                    executorManager.submit(new TransactionalRunnable(transactionContext, this));
                } else {
                    currentConcurrentMerges--;
                }
//                }

                message("  merge thread: done");

            } catch (Throwable exc) {

                if (merge != null) {
                    merge.setException(exc);
                    writer.addMergeException(merge);
                }

                // Ignore the exception if it was due to abort:
                if (!(exc instanceof MergePolicy.MergeAbortedException)) {
                    if (!suppressExceptions) {
                        // suppressExceptions is normally only set during
                        // testing.
                        anyExceptions = true;
                        throw new MergePolicy.MergeException(exc, dir);
                    }
                }
            } finally {
                if (merge == null) { // only decrease if we have no more merges and we actually exit
                    synchronized (ExecutorMergeScheduler.this) {
//                    ExecutorMergeScheduler.this.notifyAll();
                    }
                }
            }
        }

        public String toString() {
            MergePolicy.OneMerge merge = getRunningMerge();
            if (merge == null)
                merge = startMerge;
            return "merge thread: " + merge.segString(dir);
        }
    }

    static boolean anyExceptions = false;

    private boolean suppressExceptions;

    /**
     * Used for testing
     */
    void setSuppressExceptions() {
        suppressExceptions = true;
    }

    /**
     * Used for testing
     */
    void clearSuppressExceptions() {
        suppressExceptions = false;
    }
}
