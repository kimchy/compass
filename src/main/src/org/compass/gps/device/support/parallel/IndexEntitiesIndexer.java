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

import org.compass.core.CompassException;
import org.compass.core.CompassSession;

/**
 * <p>Handles indexing of the list of {@link org.compass.gps.device.support.parallel.IndexEntity}.
 *
 * <p>Usually, each {@link org.compass.gps.device.support.parallel.IndexEntity} represents a groups
 * of the indexable content (like a certain class when using ORM or a select statement). It is then
 * used to fetch the data and then index it. The process of fetching the data and indexing it in
 * Compass is done by this interface implementation.
 *
 * @author kimchy
 */
public interface IndexEntitiesIndexer {

    /**
     * Performs the actual indexing of the list of index entities. Usually, an
     * index entity represent a group of indexable content (like a certain class
     * when using ORM, or a select statement). This method perform the fetching
     * of the data and indexing it in Compass using the provided Compass session.
     *
     * @param session  The compass session to index the data with
     * @param entities A list of entities to perform the indexing by
     */
    void performIndex(CompassSession session, IndexEntity[] entities) throws CompassException;
}
