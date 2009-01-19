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

import org.compass.gps.device.jdbc.dialect.JdbcDialect;

/**
 * A base class for all the snapshots events. Holds the connection and the
 * dialect that were used when the mirroring was performed.
 * 
 * @author kimchy
 */
public abstract class AbstractSnapshotEvent {

    private Connection connection;

    private JdbcDialect dialect;

    public AbstractSnapshotEvent(Connection connection, JdbcDialect dialect) {
        this.connection = connection;
        this.dialect = dialect;
    }

    public Connection getConnection() {
        return connection;
    }

    public JdbcDialect getDialect() {
        return dialect;
    }
}
