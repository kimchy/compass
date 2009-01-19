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
import java.util.Arrays;
import java.util.HashSet;

import org.compass.core.CompassSession;
import org.compass.core.spi.InternalCompass;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;
import org.compass.gps.device.AbstractGpsDevice;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * <p>A base class for gps device that can parallel the index operation.
 *
 * <p>When the device starts up, the {@link #doGetIndexEntities()} callback
 * is called (should be implemented by sub classes) in order to get the
 * {@link org.compass.gps.device.support.parallel.IndexEntity}ies. The
 * {@link org.compass.gps.device.support.parallel.IndexEntitiesPartitioner} is
 * then used in order to partition the index entities into several groups
 * that can be parallel indexed. An {@link org.compass.gps.device.support.parallel.IndexEntitiesIndexer}
 * is also obtained using the {@link #doGetIndexEntitiesIndexer()} that can
 * index entities into the search engine and is provided by sub classes as well.
 *
 * <p>The {@link org.compass.gps.device.support.parallel.IndexEntitiesPartitioner}
 * defaults to the {@link org.compass.gps.device.support.parallel.SubIndexIndexEntitiesPartitioner}
 * that partition the index entities based on the sub index. This is the only meanigful
 * way to partition the index entities, as it allows for the best concurrent support
 * (locking is performed on the sub index level).
 *
 * <p>The {@link #index(org.compass.gps.IndexPlan)} operation uses the {@link org.compass.gps.device.support.parallel.ParallelIndexExecutor}
 * in order to execute the indexing process. The default implementation used is
 * {@link org.compass.gps.device.support.parallel.ConcurrentParallelIndexExecutor}.
 *
 * @author kimchy
 */
public abstract class AbstractParallelGpsDevice extends AbstractGpsDevice {

    private ParallelIndexExecutor parallelIndexExecutor = new ConcurrentParallelIndexExecutor();

    private IndexEntitiesPartitioner indexEntitiesPartitioner = new SubIndexIndexEntitiesPartitioner();

    private volatile IndexEntity[][] entities;

    private IndexEntitiesIndexer indexEntitiesIndexer;

    /**
     * Starts the device. Calls {@link #doGetIndexEntities} in order to get all the
     * indexeable entities and uses the {@link org.compass.gps.device.support.parallel.IndexEntitiesPartitioner}
     * to partition them index groups that can be parallel indexed. Also calls
     * {@link #doGetIndexEntitiesIndexer()} in order to obtain the index entities indexer.
     *
     * @throws CompassGpsException
     */
    public synchronized void start() throws CompassGpsException {
        super.start();
        indexEntitiesIndexer = doGetIndexEntitiesIndexer();
    }

    /**
     * Index the indexable entities. Calls the {@link org.compass.gps.device.support.parallel.ParallelIndexExecutor}
     * in order to index the different groups of indexed entities partitioned at startup.
     *
     * @throws CompassGpsException
     */
    public synchronized void index(IndexPlan indexPlan) throws CompassGpsException {
        if (!isRunning()) {
            throw new IllegalStateException(
                    buildMessage("must be running in order to perform the index operation"));
        }

        entities = indexEntitiesPartitioner.partition(doGetIndexEntities());
        
        // here, we go over and filter out things that are not based on the index plan
        // we first get the sub indexes based on the index plan, and then filter out
        // entities[] that do not match.
        // Note: once we find a match, we must add all the entities array, since the partitioner
        // "said" that they must be indexed 2ghter.
        String[] arrSubIndexes = ((InternalCompass) ((CompassGpsInterfaceDevice) getGps()).getIndexCompass()).getSearchEngineFactory().getIndexManager().polyCalcSubIndexes(indexPlan.getSubIndexes(), indexPlan.getAliases(), indexPlan.getTypes());
        HashSet<String> subIndexes = new HashSet<String>(Arrays.asList(arrSubIndexes));

        ArrayList<IndexEntity[]> calcEntities = new ArrayList<IndexEntity[]>();
        for (IndexEntity[] arrEleEntities : entities) {
            boolean found = false;
            for (IndexEntity entity : arrEleEntities) {
                for (String subIndex : entity.getSubIndexes()) {
                    if (subIndexes.contains(subIndex)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (found) {
                calcEntities.add(arrEleEntities);
            }
        }

        IndexEntity[][] entitiesToIndex = calcEntities.toArray(new IndexEntity[calcEntities.size()][]);
        parallelIndexExecutor.performIndex(entitiesToIndex, indexEntitiesIndexer, compassGps);
    }

    /**
     * Returns all the indexed entities for this device.
     */
    protected abstract IndexEntity[] doGetIndexEntities() throws CompassGpsException;

    /**
     * Returns an index entities indexer that knows how to index indexable entities.
     */
    protected abstract IndexEntitiesIndexer doGetIndexEntitiesIndexer();

    /**
     * Overriding this method and throws an {@link IllegalStateException} as it should
     * not be called. The {@link #index(org.compass.gps.IndexPlan)} operation is implemented here and does not
     * call this method.
     */
    protected final void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException {
        throw new IllegalStateException("This should not be called");
    }

    /**
     * Sets the parallel index executor. Defaults to
     * {@link org.compass.gps.device.support.parallel.ConcurrentParallelIndexExecutor}.
     *
     * @see #index(org.compass.gps.IndexPlan)
     */
    public void setParallelIndexExecutor(ParallelIndexExecutor parallelIndexExecutor) {
        this.parallelIndexExecutor = parallelIndexExecutor;
    }

    /**
     * Sets the index entities partitioner. Defaults to
     * {@link org.compass.gps.device.support.parallel.SubIndexIndexEntitiesPartitioner}.
     *
     * @see #start()
     */
    public void setIndexEntitiesPartitioner(IndexEntitiesPartitioner indexEntitiesPartitioner) {
        this.indexEntitiesPartitioner = indexEntitiesPartitioner;
    }
}
