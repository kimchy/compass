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

import org.compass.gps.CompassGpsException;

/**
 * Partition a list of {@link org.compass.gps.device.support.parallel.IndexEntity} into a
 * several groups of list of {@link org.compass.gps.device.support.parallel.IndexEntity} that
 * can be indexed in parallel.
 *
 * @author kimchy
 */
public interface IndexEntitiesPartitioner {

    /**
     * Partitions a list of index entities into several groups of lists of index entities
     * that can be indexed in parallel.
     *
     * @param entities The index entities to partition
     * @return Groups of list of entities that can be indexed in
     * @throws CompassGpsException
     */
    IndexEntity[][] partition(IndexEntity[] entities) throws CompassGpsException;
}
