/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.gps.device.hibernate.scrollable;

import java.sql.Types;
import java.util.Date;

import org.compass.gps.device.jdbc.mapping.ColumnMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.hibernate.ScrollableResults;
import org.hibernate.type.DateType;
import org.hibernate.type.IntegerType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;


public class Hibernate3Dialect {

    private int getIndex(String[] returnAliases, ColumnMapping mapping) {
        int index = -1;
        String key = mapping.getColumnName();
        for (int i = 0; i != returnAliases.length; i++) {
            if (returnAliases[i].equals(key)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new RuntimeException("unknown columnName: " + key + ", returnAliases: " + returnAliases);
        }
        return index;
    }


    protected Long getIntegerAsLong(ScrollableResults rs, String[] returnAliases, ColumnMapping columnMapping) {
        Integer integer;
        if (columnMapping.isUsingColumnIndex()) {
            integer = rs.getInteger(columnMapping.getColumnIndex());
        } else {
            integer = rs.getInteger(getIndex(returnAliases, columnMapping));
        }
        return new Long(integer.longValue());
    }

    protected Long getLong(ScrollableResults rs, String[] returnAliases, ColumnMapping columnMapping) {
        int index = -1;
        if (columnMapping.isUsingColumnIndex()) {
            index = columnMapping.getColumnIndex();
        } else {
            index = getIndex(returnAliases, columnMapping);
        }
        Type type = rs.getType(index);
        Long result;
        if (type instanceof IntegerType) {
            result = new Long(rs.getInteger(index).longValue());
        } else if (type instanceof DateType) {
            result = new Long(rs.getDate(index).getTime());
        } else {
            result = rs.getLong(index);
        }
        return result;
    }

    protected Long getDateAsLong(ScrollableResults rs, String[] returnAliases, ColumnMapping columnMapping) {
        Date date = null;
        if (columnMapping.isUsingColumnIndex()) {
            date = rs.getDate(columnMapping.getColumnIndex());
        } else {
            date = rs.getDate(getIndex(returnAliases, columnMapping));
        }
        return new Long(date.getTime());
    }

    protected Long getTimeAsLong(ScrollableResults rs, String[] returnAliases, ColumnMapping columnMapping) {
        Date date = null;
        if (columnMapping.isUsingColumnIndex()) {
            date = rs.getDate(columnMapping.getColumnIndex());
        } else {
            date = rs.getDate(getIndex(returnAliases, columnMapping));
        }
        return new Long(date.getTime());
    }

    protected Long getTimestampAsLong(ScrollableResults rs, String[] returnAliases, ColumnMapping columnMapping) {
        Date timestamp = null;
        if (columnMapping.isUsingColumnIndex()) {
            timestamp = rs.getDate(columnMapping.getColumnIndex());
        } else {
            timestamp = rs.getDate(getIndex(returnAliases, columnMapping));
        }
        return new Long(timestamp.getTime());
    }

    public Long getVersion(ScrollableResults rs, String[] returnAliases, VersionColumnMapping versionMapping) {
        Long result = null;
        int sqlType = versionMapping.getSqlType();
        if (sqlType == Types.INTEGER) {
            result = getIntegerAsLong(rs, returnAliases, versionMapping);
        } else if (sqlType == Types.DATE) {
            result = getDateAsLong(rs, returnAliases, versionMapping);
        } else if (sqlType == Types.TIMESTAMP) {
            result = getTimestampAsLong(rs, returnAliases, versionMapping);
        } else if (sqlType == Types.TIME) {
            result = getTimeAsLong(rs, returnAliases, versionMapping);
        } else {
            result = getLong(rs, returnAliases, versionMapping);
        }
        return result;
    }


    public String getStringValue(ScrollableResults rs, String[] returnAliases, ColumnMapping mapping) {
        int index = -1;
        if (mapping.isUsingColumnIndex()) {
            index = mapping.getColumnIndex();
        } else {
            index = getIndex(returnAliases, mapping);
        }
        Type type = rs.getType(index);
        String result;
        if (type instanceof StringType) {
            result = rs.getString(index);
        } else {
            result = rs.get(index).toString();
        }
        return result;
    }

    public Object getValue(ScrollableResults rs, String[] returnAliases, ColumnMapping mapping) {
        int index = -1;
        if (mapping.isUsingColumnIndex()) {
            index = mapping.getColumnIndex();
        } else {
            index = getIndex(returnAliases, mapping);
        }
        return rs.get(index);
    }


    public String getStringValue(ScrollableResults rs, int columnIndex) {
        Type type = rs.getType(columnIndex);
        String result;
        if (type instanceof StringType) {
            result = rs.getString(columnIndex);
        } else {
            result = rs.get(columnIndex).toString();
        }
        return result;
    }
}
