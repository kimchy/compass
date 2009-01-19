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
 * Maps an id column to <code>Resource Property</code>. The id column is the column that identifies
 * or is part of columns that identifies the <code>ResultSet</code>. In a
 * table, it is the table primary keys.
 * <p>
 * The <code>PropertyIndex</code> is <code>Property.Index.UN_TOKENIZED</code>.
 * the <code>PropertyStore</code> is <code>Property.Store.YES</code>, the
 * <code>PropertyTermVector</code> is <code>Property.TermVector.NO</code>
 * and the <code>Boost</code> is <code>1.0f</code>.
 * <p>
 * The id mapping also holds an additional mapping, the
 * <code>columnNameForVersion</code> mapping. When performing the mirror
 * operation (if enabled), it is the id column name that is added to the select
 * query in order to filter by the specified ids. For example, if the select
 * query is (for a PARENT and CHILD table relationship):
 * <code>select p.id as parent_id, p.first_name as parent_first_name, p.last_name as parent_last_name, p.version as parent_version, COALESCE(c.id, 0) as child_id, c.first_name as child_first_name, c.last_name child_last_name, COALESCE(c.version, 0) as child_version from parent p left join child c on p.id = c.parent_id</code>
 * than when performing the query to get the values for certain values of ids
 * (the parent and child ids), the column names that will be used are:
 * <code>p.id</code> and <code>COALESCE(c.id, 0)</code>. And the actual
 * where clause will be: <code>where p.id = ? and COALESCE(c.id, 0) = ?</code>.
 * <p>
 * Note, that the id column name is <code>parent_id</code>, and
 * <code>child_id</code>. They are used for reading the id values, not
 * constructing queries.
 * 
 * @author kimchy
 */
public class IdColumnToPropertyMapping extends AbstractConstantColumnToPropertyMapping {

    private String columnNameForVersion;

    /**
     * Creates an empty id column to property mapping. Must set at least the
     * colum index or colum name, and the property name. If using mirroring,
     * must set the column name for version as well.
     */
    public IdColumnToPropertyMapping() {

    }

    /**
     * Creates a new Id column to propery mapping given the column name and the
     * property name. The column name will also be used as the
     * <code>columnNameForVersion</code> value.
     * 
     * @param columnName
     *            The id column name that will be used to look up the id value
     *            (also acts as the columnNameForVersion).
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     */
    public IdColumnToPropertyMapping(String columnName, String propertyName) {
        this(columnName, propertyName, columnName);
    }

    /**
     * Creates a new Id column to property mapping given the column name, the
     * property name, and the column name for the versioning (the mirror
     * operation).
     * 
     * @param columnName
     *            The id column name that will be used to look up the id value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param columnNameForVersion
     *            The id column name that will be used in the dynamically
     *            created where clause with the original select query.
     */
    public IdColumnToPropertyMapping(String columnName, String propertyName, String columnNameForVersion) {
        super(columnName, propertyName);
        this.columnNameForVersion = columnNameForVersion;
    }

    /**
     * Creates a new Id Column to property mapping given the column index and
     * the property name. Note that the column name for versioning will be null,
     * so if mirroring will be enabled, it must be set.
     * 
     * @param columnIndex
     *            The id column index that will be used to look up the id value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     */
    public IdColumnToPropertyMapping(int columnIndex, String propertyName) {
        this(columnIndex, propertyName, null);
    }

    /**
     * Creates a new Id Column to property mapping given the column index, the
     * property name, and the column name for the versioning (the mirror
     * operation).
     * 
     * @param columnIndex
     *            The id column index that will be used to look up the id value.
     * @param propertyName
     *            The Compass <code>Resource Property</code> name.
     * @param columnNameForVersion
     *            The id column name that will be used in the dynamically
     *            created where clause with the original select query.
     */
    public IdColumnToPropertyMapping(int columnIndex, String propertyName, String columnNameForVersion) {
        super(columnIndex, propertyName);
        this.columnNameForVersion = columnNameForVersion;
    }

    /**
     * Returns the id column name that will be used in the dynamically created
     * where clause with the original select query.
     */
    public String getColumnNameForVersion() {
        return columnNameForVersion;
    }

    /**
     * Sets the id column name that will be used in the dynamically created
     * where clause with the original select query.
     * 
     * @param columnNameForVersion
     *            The id column name that will be used in the dynamically
     *            created where clause with the original select query.
     */
    public void setColumnNameForVersion(String columnNameForVersion) {
        this.columnNameForVersion = columnNameForVersion;
    }

}
