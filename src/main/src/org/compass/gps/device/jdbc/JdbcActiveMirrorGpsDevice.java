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

package org.compass.gps.device.jdbc;

import org.compass.gps.ActiveMirrorGpsDevice;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotPersister;

/**
 * An extension of the {@link org.compass.gps.device.jdbc.JdbcGpsDevice} that
 * can also detect real time data changes made to the database and reflect them
 * to the index.
 * <p>
 * The device will use the
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener} to
 * handle database change events (which should be reflected to the index).
 * <p>
 * The device will use the provided
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcSnapshotPersister} to persist
 * the snapshot information and load it.
 * 
 * @author kimchy
 */
public interface JdbcActiveMirrorGpsDevice extends JdbcGpsDevice, ActiveMirrorGpsDevice {

    /**
     * Returns the jdbc snapshot event listener that will handle database change
     * events.
     * 
     * @return The jdbc snapshot event listener.
     */
    JdbcSnapshotEventListener getSnapshotEventListener();

    /**
     * Sets the jdbc snapshot event listener that will handle database change
     * events.
     * 
     * @param snapshotEventListener
     */
    void setSnapshotEventListener(JdbcSnapshotEventListener snapshotEventListener);

    /**
     * Returns the snapshot persister that will persist and load the snapshot
     * information.
     * 
     * @return The Jdbc snapshot persister.
     */
    JdbcSnapshotPersister getSnapshotPersister();

    /**
     * Sets the snapshot persister that will persist and load the snapshot
     * information.
     * 
     * @param snapshotPersister
     */
    void setSnapshotPersister(JdbcSnapshotPersister snapshotPersister);

    /**
     * Should the snapshot be saved/persisted after each mirroring operation.
     * <p>
     * Note that it is persisted when the gps device stops.
     * 
     * @return <code>true</code> if the snapshot should be persisted after each mirroring operation.
     */
    boolean isSaveSnapshotAfterMirror();

    /**
     * Sets if the snapshot be saved/persisted after each mirroring operation.
     * <p>
     * Note that it is persisted when the gps device stops.
     * 
     * @param saveSnapshotAfterMirror
     */
    void setSaveSnapshotAfterMirror(boolean saveSnapshotAfterMirror);
}
