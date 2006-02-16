/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.hibernate;

import org.compass.gps.CompassGpsDevice;

/**
 * A general Hiberante device interface, can work with both Hibernate 2 and
 * Hibernate 3.
 * <p>
 * The hibernate device provides support for using hibernate and hibernate
 * mapping files to index a database. The path can be views as: Database <->
 * Hibernate <-> Objects <-> Compass::Gps <-> Compass::Core (Search Engine).
 * What it means is that for every object that has both hibernate and compass
 * mappings, you will be able to index it's data, as well as real time mirroring
 * of data changes (in case of Hibernate 3).
 * 
 * @author kimchy
 */
public interface HibernateGpsDevice extends CompassGpsDevice {

    /**
     * The batch fetch count the should be used when fetching data from a
     * hiberante mapped class. Defaults to 200.
     * 
     * @param fetchCount
     */
    void setFetchCount(int fetchCount);

    /**
     * Sets a list of filtered enteties (enteties that are mapped to the database)
     * that will not be indexed.
     */
    void setFilteredEntitiesForIndex(String[] filteredEntitiesForIndex);
}
