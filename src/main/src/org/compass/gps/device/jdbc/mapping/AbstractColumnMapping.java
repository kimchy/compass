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

/**
 * 
 * A helper base class for columns base mappings.
 * 
 * @author kimchy
 */
public class AbstractColumnMapping implements ColumnMapping {

    private String columnName;

    private int columnIndex;

    private boolean usingColumnIndex;

    public AbstractColumnMapping() {

    }

    public AbstractColumnMapping(String columnName) {
        setColumnName(columnName);
    }

    public AbstractColumnMapping(int columnIndex) {
        setColumnIndex(columnIndex);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String column) {
        this.columnName = column;
        this.usingColumnIndex = false;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
        this.usingColumnIndex = true;
    }

    public boolean isUsingColumnIndex() {
        return usingColumnIndex;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("column[");
        if (isUsingColumnIndex()) {
            sb.append(columnIndex);
        } else {
            sb.append(columnName);
        }
        sb.append("]");
        return sb.toString();
    }
}
