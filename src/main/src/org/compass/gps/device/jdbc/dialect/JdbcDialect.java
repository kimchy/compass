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

package org.compass.gps.device.jdbc.dialect;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.compass.gps.device.jdbc.mapping.ColumnMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;

/**
 * A dialect specific operations that might be different accross different
 * databases.
 * 
 * @author kimchy
 * 
 */
public interface JdbcDialect {

    /**
     * Returns the String value for the given column index. Will return
     * <code>null</code> if the column value is <code>null</code>.
     */
    String getStringValue(ResultSet rs, int columnIndex) throws SQLException;

    /**
     * Returns the String value for the given column mapping. Will return
     * <code>null</code> if the column value is <code>null</code>.
     */
    String getStringValue(ResultSet rs, ColumnMapping mapping) throws SQLException;

    /**
     * Returns the version value of the given version mapping. It is always a
     * long value.
     */
    Long getVersion(ResultSet rs, VersionColumnMapping versionMapping) throws SQLException;

    /**
     * Sets the paremeter value for the given parameter index and the
     * <code>PreparedStatement</code>.
     */
    void setParameter(PreparedStatement ps, int paramIndex, String value) throws SQLException;
}
