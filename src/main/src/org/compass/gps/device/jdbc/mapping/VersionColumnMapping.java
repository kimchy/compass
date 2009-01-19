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

package org.compass.gps.device.jdbc.mapping;

import java.sql.Types;

/**
 * Maps a version column (no property mapping required - if you wish to map the
 * version colum to a <code>Resource Property</code> use the
 * {@link org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping}).
 * <p>
 * In order to map a version column, either the column index or the column name
 * must be set, and the version column jdbc type (one of
 * <code>java.sql.Types</code>). The version column jdbc type defaults to
 * <code>java.sql.Types.BIGINT</code>.
 * <p>
 * Note that {@link org.compass.gps.device.jdbc.ResultSetJdbcGpsDevice} defaults
 * to automatically detect and assign the version column sql type.
 * 
 * @author kimchy
 */
public class VersionColumnMapping extends AbstractColumnMapping {

    private int sqlType = Types.BIGINT;

    /**
     * Creates an empty version column mapping. Must set at least the colum
     * index or colum name.
     * <p>
     * The <code>sqlType</code> defaults to <code>java.sql.Types.BIGINT</code>.
     */
    public VersionColumnMapping() {

    }

    /**
     * Creates a new version column mapping given the column name.
     * <p>
     * The <code>sqlType</code> defaults to <code>java.sql.Types.BIGINT</code>.
     * 
     * @param columnName
     *            The version column name that will be used to look up the
     *            column value.
     */
    public VersionColumnMapping(String columnName) {
        super(columnName);
    }

    /**
     * Creates a new version column mapping given the column name.
     * <p>
     * The <code>sqlType</code> defaults to <code>java.sql.Types.BIGINT</code>.
     * 
     * @param columnIndex
     *            The version column name that will be used to look up the
     *            column value.
     */
    public VersionColumnMapping(int columnIndex) {
        super(columnIndex);
    }

    /**
     * Creates a new version column mapping given the column name and the column
     * sql type.
     * 
     * @param columnName
     *            The version column name that will be used to look up the
     *            column value.
     * @param sqlType
     *            The sql type (<code>java.sql.Types</code>) of the version
     *            column.
     */
    public VersionColumnMapping(String columnName, int sqlType) {
        super(columnName);
        this.sqlType = sqlType;
    }

    /**
     * Creates a new version column mapping given the column index and the
     * column sql type.
     * 
     * @param columnIndex
     *            The version column name that will be used to look up the
     *            column value.
     * @param sqlType
     *            The sql type (<code>java.sql.Types</code>) of the version
     *            column.
     */
    public VersionColumnMapping(int columnIndex, int sqlType) {
        super(columnIndex);
        this.sqlType = sqlType;
    }

    /**
     * Returns the jdbc sql type of the version column. Should be one of
     * <code>java.sql.Types</code>.
     * 
     * @return The jdbc sql type of the version column.
     */
    public int getSqlType() {
        return sqlType;
    }

    /**
     * Sets the jdbc sql type of the version column. Should be one of
     * <code>java.sql.Types</code>.
     * 
     * @param sqlType
     *            The jdbc sql type of the version column.
     */
    public void setSqlType(int sqlType) {
        this.sqlType = sqlType;
    }
}
