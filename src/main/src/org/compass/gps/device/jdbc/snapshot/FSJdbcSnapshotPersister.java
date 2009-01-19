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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.gps.device.jdbc.JdbcGpsDeviceException;

/**
 * A FS (File System) based snapshot persister. The persister will store and
 * load the snapshot to the file system, using the given file path (using
 * {@link #setPath(String)}). The data will be saved using Java
 * <code>ObjectOutputStream</code>, and loaded using
 * <code>ObjectInputStream</code>.
 * <p>
 * Note that the path set is the path to the actual file that will be created.
 * 
 * @author kimchy
 */
public class FSJdbcSnapshotPersister implements JdbcSnapshotPersister {

    private static final Log log = LogFactory.getLog(FSJdbcSnapshotPersister.class);

    private String path;

    public FSJdbcSnapshotPersister() {

    }

    public FSJdbcSnapshotPersister(String path) {
        this.path = path;
    }

    public JdbcSnapshot load() throws JdbcGpsDeviceException {
        File file = new File(path);
        if (!file.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("No snapshot data found at [" + path + "], creating a new one");
            }
            return new JdbcSnapshot();
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Snapshot data found at [" + path + "], loading [" + file.length() + "bytes]");
            }
            ObjectInputStream objStream = new ObjectInputStream(new FileInputStream(path));
            return (JdbcSnapshot) objStream.readObject();
        } catch (Exception e) {
            throw new JdbcGpsDeviceException("Failed to load jdbc snapshot", e);
        }
    }

    public void save(JdbcSnapshot snapshot) throws JdbcGpsDeviceException {
        try {
            ObjectOutputStream objStream = new ObjectOutputStream(new FileOutputStream(path));
            objStream.writeObject(snapshot);
            objStream.flush();
            objStream.close();
            if (log.isDebugEnabled()) {
                File file = new File(path);
                log.debug("Saved snapshot data to [" + path + "] size [" + file.length() + "bytes]");
            }
        } catch (IOException e) {
            throw new JdbcGpsDeviceException("Failed to save jdbc snapshot", e);
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
