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

package org.compass.gps.device.jdbc.snapshot;

import org.compass.gps.device.jdbc.JdbcGpsDeviceException;

/**
 * An interface for listening for real time data changes in the databases. The
 * events are fired by a
 * {@link org.compass.gps.device.jdbc.JdbcActiveMirrorGpsDevice}.
 * 
 * @author kimchy
 */
public interface JdbcSnapshotEventListener {

    /**
     * A configure event fired when the systems starts.
     * 
     * @param configureSnapshotEvent
     * @throws JdbcGpsDeviceException
     */
    void configure(ConfigureSnapshotEvent configureSnapshotEvent) throws JdbcGpsDeviceException;

    /**
     * A create and update event. Fired with all the created rows and updated
     * rows for a given result set when the <code>performMirroring</code> is
     * called.
     * 
     * @param createAndUpdateSnapshotEvent
     * @throws JdbcGpsDeviceException
     */
    void onCreateAndUpdate(CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent) throws JdbcGpsDeviceException;

    /**
     * A delete event. Fired with all the deleted rows for a given result set
     * when the <code>performMirroring</code> is called.
     * 
     * @param deleteSnapshotEvent
     * @throws JdbcGpsDeviceException
     */
    void onDelete(DeleteSnapshotEvent deleteSnapshotEvent) throws JdbcGpsDeviceException;
}
