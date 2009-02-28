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

package org.compass.gps.device.support.parallel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.util.concurrent.NamedThreadFactory;
import org.compass.gps.CompassGpsException;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * <p>Executes the indexing process using sevearl threads based on the partitioned
 * list of index entities.
 *
 * <p>By default (with <code>maxThreads</code> set to <code>-1</code>) creates N threads
 * during the indexing process where N is the number of partitioned index entities
 * groups (one therad per group). If <code>maxThreads<code> is set to a positive integer
 * number, the index executor will use it as the number of threads to create, regardless
 * of the number of partitioned entities groups.
 *
 * @author kimchy
 */
public class ConcurrentParallelIndexExecutor implements ParallelIndexExecutor {

    private static final Log log = LogFactory.getLog(ConcurrentParallelIndexExecutor.class);

    private int maxThreads = -1;

    /**
     * Constructs a new concurrent index executor with <code>maxThreads</code>
     * defaults to -1.
     */
    public ConcurrentParallelIndexExecutor() {

    }

    /**
     * Constructs a new concurrent index executor with the given max threads.
     *
     * @param maxThreads The number of threads to use or -1 for dynamic threads
     */
    public ConcurrentParallelIndexExecutor(int maxThreads) {
        if (maxThreads < -1 || maxThreads == 0) {
            throw new IllegalArgumentException("maxThreads must either be -1 or a value greater than 0");
        }
        this.maxThreads = maxThreads;
    }

    /**
     * Performs the indexing process using the provided index entities indexer. Creates a pool of N
     * threads (if <code>maxThreads</code> is set to -1, N is the numer of entities groups, otherwise
     * N is the number of <code>maxThreads</code>).
     *
     * @param entities             The partitioned index entities groups and index entities to index
     * @param indexEntitiesIndexer The entities indexer to use
     * @param compassGps           Compass gps interface for meta information
     */
    public void performIndex(final IndexEntity[][] entities, final IndexEntitiesIndexer indexEntitiesIndexer,
                             final CompassGpsInterfaceDevice compassGps) {

        if (entities.length <= 0) {
            throw new IllegalArgumentException("No entities listed to be indexed, have you defined your entities correctly?");
        }
        int maxThreads = this.maxThreads;
        if (maxThreads == -1) {
            maxThreads = entities.length;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads,
                new NamedThreadFactory("Compass Gps Index", false));
        try {
            ArrayList tasks = new ArrayList();
            for (int i = 0; i < entities.length; i++) {
                final IndexEntity[] indexEntities = entities[i];
                tasks.add(new Callable() {
                    public Object call() throws Exception {
                        compassGps.executeForIndex(new CompassCallbackWithoutResult() {
                            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                                indexEntitiesIndexer.performIndex(session, indexEntities);
                                session.flush();
                            }
                        });
                        return null;
                    }
                });
            }
            List futures;
            try {
                futures = executorService.invokeAll(tasks);
            } catch (InterruptedException e) {
                throw new CompassGpsException("Failed to index, interrupted", e);
            }

            for (Iterator it = futures.iterator(); it.hasNext();) {
                Future future = (Future) it.next();
                try {
                    future.get();
                } catch (InterruptedException e) {
                    throw new CompassGpsException("Failed to index, interrupted", e);
                } catch (ExecutionException e) {
                    throw new CompassGpsException("Failed to index, execution exception", e);
                }
            }
        } finally {
            executorService.shutdownNow();
        }
    }

}
