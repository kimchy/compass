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

import java.sql.Connection;
import java.util.List;

import org.compass.gps.device.jdbc.dialect.JdbcDialect;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * A create and update snapshot event, works with
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener#onCreateAndUpdate(CreateAndUpdateSnapshotEvent)}.
 * Holds the
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping} that
 * maps to the result set that initiated the event, a list of
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot} for all the
 * row snapapshots that were created and a list for all the ones that were
 * updated, and the <code>CompassTemplate</code> to use in order to reflect
 * the changes to the index.
 * 
 * @author kimchy
 */
public class CreateAndUpdateSnapshotEvent extends AbstractSnapshotEvent {

    private ResultSetToResourceMapping mapping;

    private CompassGpsInterfaceDevice compassGps;

    private List createSnapshots;

    private List updateSnapshots;

    public CreateAndUpdateSnapshotEvent(Connection connection, JdbcDialect dialect, ResultSetToResourceMapping mapping,
            List createSnapshots, List updateSnapshots, CompassGpsInterfaceDevice compassGps) {
        super(connection, dialect);
        this.mapping = mapping;
        this.createSnapshots = createSnapshots;
        this.updateSnapshots = updateSnapshots;
        this.compassGps = compassGps;
    }

    public List getCreateSnapshots() {
        return createSnapshots;
    }

    public List getUpdateSnapshots() {
        return updateSnapshots;
    }

    public ResultSetToResourceMapping getMapping() {
        return mapping;
    }

    public CompassGpsInterfaceDevice getCompassGps() {
        return compassGps;
    }

}
