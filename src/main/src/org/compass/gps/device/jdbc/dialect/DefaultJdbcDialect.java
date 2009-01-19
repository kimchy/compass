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

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.compass.gps.device.jdbc.mapping.ColumnMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;

/**
 * @author kimchy
 */
public class DefaultJdbcDialect implements JdbcDialect {

    protected Long getIntegerAsLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        int integer;
        if (columnMapping.isUsingColumnIndex()) {
            integer = rs.getInt(columnMapping.getColumnIndex());
        } else {
            integer = rs.getInt(columnMapping.getColumnName());
        }
        return (long) integer;
    }

    protected Long getNumericAsLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        if (columnMapping.isUsingColumnIndex()) {
            return rs.getLong(columnMapping.getColumnIndex());
        }
        return rs.getLong(columnMapping.getColumnName());
    }

    protected Long getLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        if (columnMapping.isUsingColumnIndex()) {
            return (Long) rs.getObject(columnMapping.getColumnIndex());
        }
        return (Long) rs.getObject(columnMapping.getColumnName());
    }

    protected Long getDateAsLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        Date date;
        if (columnMapping.isUsingColumnIndex()) {
            date = rs.getDate(columnMapping.getColumnIndex());
        } else {
            date = rs.getDate(columnMapping.getColumnName());
        }
        return date.getTime();
    }

    protected Long getTimeAsLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        Date date;
        if (columnMapping.isUsingColumnIndex()) {
            date = rs.getTime(columnMapping.getColumnIndex());
        } else {
            date = rs.getTime(columnMapping.getColumnName());
        }
        return date.getTime();
    }

    protected Long getTimestampAsLong(ResultSet rs, ColumnMapping columnMapping) throws SQLException {
        Timestamp timestamp;
        if (columnMapping.isUsingColumnIndex()) {
            timestamp = rs.getTimestamp(columnMapping.getColumnIndex());
        } else {
            timestamp = rs.getTimestamp(columnMapping.getColumnName());
        }
        return timestamp.getTime();
    }

    protected Long getBigIntAsLong(final ResultSet rs, final ColumnMapping columnMapping) throws SQLException {
        if (columnMapping.isUsingColumnIndex()) {
            return rs.getBigDecimal(columnMapping.getColumnIndex()).longValue();
        }
        return rs.getBigDecimal(columnMapping.getColumnName()).longValue();
    }

    public Long getVersion(ResultSet rs, VersionColumnMapping versionMapping) throws SQLException {
        Long result;
        int sqlType = versionMapping.getSqlType();
        if (sqlType == Types.INTEGER) {
            result = getIntegerAsLong(rs, versionMapping);
        } else if (sqlType == Types.DATE) {
            result = getDateAsLong(rs, versionMapping);
        } else if (sqlType == Types.TIMESTAMP) {
            result = getTimestampAsLong(rs, versionMapping);
        } else if (sqlType == Types.TIME) {
            result = getTimeAsLong(rs, versionMapping);
        } else if (sqlType == Types.NUMERIC) {
            result = getNumericAsLong(rs, versionMapping);
        } else if (sqlType == Types.BIGINT) {
            result = getBigIntAsLong(rs, versionMapping);
        } else {
            result = getLong(rs, versionMapping);
        }
        return result;
    }

    public void setParameter(PreparedStatement ps, int paramIndex, String value) throws SQLException {
        ParameterMetaData metaData = ps.getParameterMetaData();
        int type = metaData.getParameterType(paramIndex);
        if (type == Types.BIGINT) {
            long lValue = Long.parseLong(value);
            ps.setLong(paramIndex, lValue);
        } else if (type == Types.INTEGER) {
            int iValue = Integer.parseInt(value);
            ps.setInt(paramIndex, iValue);
        } else if (type == Types.SMALLINT) {
            short iValue = Short.parseShort(value);
            ps.setShort(paramIndex, iValue);
        } else if (type == Types.VARCHAR || type == Types.LONGVARCHAR || type == Types.CHAR) {
            ps.setString(paramIndex, value);
        } else {
            throw new IllegalArgumentException("Failed to set parameter for type ["
                    + metaData.getParameterTypeName(paramIndex) + "], not supported");
        }
    }

    public String getStringValue(ResultSet rs, ColumnMapping mapping) throws SQLException {
        String value;
        if (mapping.isUsingColumnIndex()) {
            value = rs.getString(mapping.getColumnIndex());
        } else {
            value = rs.getString(mapping.getColumnName());
        }
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    public String getStringValue(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }
}

