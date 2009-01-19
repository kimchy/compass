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
 * A general interface for a jdbc column mapping. Holds the column index OR the
 * column name.
 * 
 * @author kimchy
 */
public interface ColumnMapping {

    /**
     * Returns the column name.
     * 
     * @return The column name.
     */
    String getColumnName();

    /**
     * Returns the column index.
     * 
     * @return The column index.
     */
    int getColumnIndex();

    /**
     * Returns <code>true</code> if using the column index to map the column,
     * <code>false</code> if using the column name.
     * 
     * @return If using colum index (otherwise using column name).
     * 
     */
    boolean isUsingColumnIndex();

}
