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

import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * Executes the indexing process on the same thread (and naturally using a single
 * thread).
 *
 * @author kimchy
 */
public class SameThreadParallelIndexExecutor implements ParallelIndexExecutor {

    /**
     * Performs the indexing process using the same thread (and naturally using a single
     * thread).
     *
     * @param entities             The index entities to index
     * @param indexEntitiesIndexer The entities indexer to use
     * @param compassGps           Compass gps interface for meta information
     */
    public void performIndex(final IndexEntity[][] entities, final IndexEntitiesIndexer indexEntitiesIndexer, CompassGpsInterfaceDevice compassGps) {
        compassGps.executeForIndex(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (int i = 0; i < entities.length; i++) {
                    indexEntitiesIndexer.performIndex(session, entities[i]);
                }
                session.flush();
            }
        });
    }
}
