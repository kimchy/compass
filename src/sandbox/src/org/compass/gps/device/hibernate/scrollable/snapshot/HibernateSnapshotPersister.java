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
 * A jdbc snapshot persister, resposible for persisting and loading a
 * {@link org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshot} from a persistance
 * store. Examples for stores can be RAM and FS (File System).
 *
 * @author kimchy
 * @see HibernateSnapshotPersister
 * @see org.compass.gps.device.hibernate.scrollable.snapshot.RAMHibernateSnapshotPersister
 * @see org.compass.gps.device.hibernate.scrollable.snapshot.FSHibernateSnapshotPersister
 */
public interface HibernateSnapshotPersister {

    /**
     * Saves the hibernate snapshot to the store.
     *
     * @param snapshot
     * @throws HibernateGpsDeviceException
     */
    void save(HibernateSnapshot snapshot) throws HibernateGpsDeviceException;

    /**
     * Loads the hibernate snapshot from the store.
     *
     * @return The hibernate snapshot
     * @throws HibernateGpsDeviceException
     */
    HibernateSnapshot load() throws HibernateGpsDeviceException;
}