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

import org.compass.gps.device.jdbc.snapshot.ConfigureSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.CreateAndUpdateSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.DeleteSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener;

/**
 * 
 * @author kimchy
 * 
 */
public class MockSnapshotEventListener implements JdbcSnapshotEventListener {

    private boolean createAndUpdateCalled;

    private boolean createHappen;

    private boolean updateHappen;

    private boolean deleteCalled;

    private boolean deleteHappen;

    public void configure(ConfigureSnapshotEvent configureSnapshotEvent) throws JdbcGpsDeviceException {
    }

    public void onCreateAndUpdate(CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent)
            throws JdbcGpsDeviceException {
        createAndUpdateCalled = true;
        createHappen = !createAndUpdateSnapshotEvent.getCreateSnapshots().isEmpty();
        updateHappen = !createAndUpdateSnapshotEvent.getUpdateSnapshots().isEmpty();
    }

    public void onDelete(DeleteSnapshotEvent deleteSnapshotEvent) throws JdbcGpsDeviceException {
        deleteCalled = true;
        deleteHappen = !deleteSnapshotEvent.getDeleteSnapshots().isEmpty();
    }

    public void clear() {
        createAndUpdateCalled = false;
        createHappen = false;
        updateHappen = false;
        deleteCalled = false;
        deleteHappen = false;
    }

    public boolean isCreateAndUpdateCalled() {
        return createAndUpdateCalled;
    }

    public boolean isDeleteCalled() {
        return deleteCalled;
    }

    public boolean isCreateHappen() {
        return createHappen;
    }

    public boolean isUpdateHappen() {
        return updateHappen;
    }

    public boolean isDeleteHappen() {
        return deleteHappen;
    }

}
