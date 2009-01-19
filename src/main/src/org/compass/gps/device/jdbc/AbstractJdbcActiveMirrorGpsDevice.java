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

import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotPersister;
import org.compass.gps.device.jdbc.snapshot.RAMJdbcSnapshotPersister;

/**
 * A helper base class for Jdbc active mirror gps device.
 * 
 * @author kimchy
 */
public abstract class AbstractJdbcActiveMirrorGpsDevice extends AbstractJdbcGpsDevice implements
        JdbcActiveMirrorGpsDevice {

    private boolean mirrorDataChanges = true;

    private JdbcSnapshotPersister snapshotPersister = new RAMJdbcSnapshotPersister();

    private JdbcSnapshotEventListener snapshotEventListener = new ResultSetSnapshotEventListener();

    private boolean saveSnapshotAfterMirror = false;

    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }

    public JdbcSnapshotEventListener getSnapshotEventListener() {
        return snapshotEventListener;
    }

    public void setSnapshotEventListener(JdbcSnapshotEventListener snapshotEventListener) {
        this.snapshotEventListener = snapshotEventListener;
    }

    public JdbcSnapshotPersister getSnapshotPersister() {
        return snapshotPersister;
    }

    public void setSnapshotPersister(JdbcSnapshotPersister snapshotPersister) {
        this.snapshotPersister = snapshotPersister;
    }

    public boolean isSaveSnapshotAfterMirror() {
        return saveSnapshotAfterMirror;
    }

    public void setSaveSnapshotAfterMirror(boolean saveSnapshotAfterMirror) {
        this.saveSnapshotAfterMirror = saveSnapshotAfterMirror;
    }
}
