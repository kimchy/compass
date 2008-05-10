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

package org.compass.gps.device.hibernate.scrollable.snapshot;

import org.compass.gps.device.hibernate.HibernateGpsDeviceException;

/**
 * A RAM (memory) based snapshot persister. The persister will store the data in
 * memory for the whole device lifecycle (start to stop).
 *
 * @author kimchy
 */
public class RAMHibernateSnapshotPersister implements HibernateSnapshotPersister {

    private HibernateSnapshot hibernateSnapshot = new HibernateSnapshot();

    /**
     * Loads the snapshot from memory.
     */
    public HibernateSnapshot load() throws HibernateGpsDeviceException {
        return hibernateSnapshot;
    }

    /**
     * Saves the snapshot to memory.
     */
    public void save(HibernateSnapshot snapshot) throws HibernateGpsDeviceException {
        hibernateSnapshot = snapshot;
    }
}
