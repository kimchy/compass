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

import javax.sql.DataSource;

import org.compass.gps.CompassGpsDevice;
import org.compass.gps.device.jdbc.dialect.JdbcDialect;

/**
 * A general contract for a Jdbc Gps device. The Jdbc Gps Device must be able to
 * reindex a jdbc enabled database.
 * 
 * @author kimchy
 */
public interface JdbcGpsDevice extends CompassGpsDevice {

    /**
     * Returns the Jdbc data source that will be used to connect to the
     * database.
     * 
     * @return The data source used with the device
     */
    DataSource getDataSource();

    /**
     * Sets the Jdbc data source that will be used to connect to the database.
     * Note that it must be set before calling the <code>start</code> method.
     * 
     * @param dataSource
     */
    void setDataSource(DataSource dataSource);

    /**
     * Returns the fetch size that will be used when executing select queries
     * against the database. See <code>PreparedStatement#setFetchSize</code>.
     * 
     * @return The fetch size for indexing and mirroring
     */
    int getFetchSize();

    /**
     * Sets the fetch size that will be used when executing select queries
     * against the database. See <code>PreparedStatement#setFetchSize</code>.
     * 
     * @param fetchSize
     */
    void setFetchSize(int fetchSize);

    /**
     * Returns the {@link JdbcDialect} that will be used when executing
     * operations that might have different implementations based on the target
     * database.
     * 
     * @return The dialect used by the device
     */
    JdbcDialect getDialect();

    /**
     * Sets the {@link JdbcDialect} that will be used when executing operations
     * that might have different implementations based on the target database.
     * 
     * @param dialect
     */
    void setDialect(JdbcDialect dialect);
}
