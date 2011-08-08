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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Maps a Jdbc <code>ResultSet</code> to a Compass <code>Resource</code>.
 * <p>
 * The required mappings are the alias of the <code>Resource</code>, the
 * select query that generates the <code>ResultSet</code>, and at least one
 * id mapping that maps the <code>ResultSet</code> identifiers columns ({@link org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping}).
 * <p>
 * Mapping data columns ({@link org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping})
 * is optional, but provides the meta data (<code>Resource Proeprty</code>)
 * for searching. You can also enable (which is disabled by default) for
 * indexing all the un mapped columns of the <code>ResultSet</code> using the
 * {@link #setIndexUnMappedColumns(boolean)}.
 * <p>
 * For real time mirroring, the version query and at least one version column
 * mapping ({@link org.compass.gps.device.jdbc.mapping.VersionColumnMapping})
 * is required.
 * 
 * @author kimchy
 */
public class ResultSetToResourceMapping {

    private String alias;

    private String selectQuery;

    private String versionQuery;

    private List idMappings = new ArrayList();

    private List dataMappings = new ArrayList();

    private Map mappingsMap = new HashMap();

    private List versionMappings = new ArrayList();

    private boolean indexUnMappedColumns = false;

    /**
     * Creates a new <code>ResultSet</code> to <code>Resource</code>
     * mapping. Must set at least the alias, the select query, and at lease one
     * id mapping.
     * <p>
     * Indexing of unmapped columns is diasabled by default.
     */
    public ResultSetToResourceMapping() {

    }

    /**
     * Creates a new <code>ResultSet</code> to <code>Resource</code> mapping
     * using the supplied alias and select query. At least one id mapping is
     * required to be configured as well.
     * <p>
     * Indexing of unmapped columns is diasabled by default.
     * 
     * @param alias
     *            The alias of the mapped <code>Resource</code>.
     * @param selectQuery
     *            The select query that generates the <code>ResultSet</code>.
     */
    public ResultSetToResourceMapping(String alias, String selectQuery) {
        this(alias, selectQuery, null);
    }

    /**
     * Creates a new <code>ResultSet</code> to <code>Resource</code> mapping
     * using the supplied alias and select query and one id column mapping.
     * <p>
     * If additional id column mappings are required, use
     * {@link #addIdMapping(ColumnToPropertyMapping)} to add them.
     * <p>
     * Indexing of unmapped columns is diasabled by default.
     * 
     * @param alias
     *            The alias of the mapped <code>Resource</code>.
     * @param selectQuery
     *            The select query that generates the <code>ResultSet</code>.
     * @param idMapping
     *            Id mapping used to map an id column.
     */
    public ResultSetToResourceMapping(String alias, String selectQuery, ColumnToPropertyMapping idMapping) {
        this.alias = alias;
        this.selectQuery = selectQuery;
        if (idMapping != null) {
            this.idMappings.add(idMapping);
        }
    }

    /**
     * Returns the alias of the mapped <code>Resource</code>.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias of the mapped <code>Resource</code>.
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Returns the select query that generates the <code>ResultSet</code>.
     */
    public String getSelectQuery() {
        return selectQuery;
    }

    /**
     * Sets the select query that generates the <code>ResultSet</code>.
     */
    public void setSelectQuery(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    /**
     * Returns the version query that is used for real time mirror data changes.
     */
    public String getVersionQuery() {
        return versionQuery;
    }

    /**
     * Sets the version query that is used for real time mirror data changes.
     */
    public void setVersionQuery(String snapshotQuery) {
        this.versionQuery = snapshotQuery;
    }

    /**
     * Adds id column mapping.
     */
    public void addIdMapping(ColumnToPropertyMapping idMapping) {
        this.idMappings.add(idMapping);
        addMappingToMap(idMapping);
    }

    /**
     * Adds data column mapping.
     */
    public void addDataMapping(ColumnToPropertyMapping dataMapping) {
        this.dataMappings.add(dataMapping);
        addMappingToMap(dataMapping);
    }

    /**
     * Adds version column mapping for real time mirror data changes.
     */
    public void addVersionMapping(VersionColumnMapping versionColumnMapping) {
        this.versionMappings.add(versionColumnMapping);
        addMappingToMap(versionColumnMapping);
    }

    /**
     * Returns a list of all the {@link ColumnMapping}s that are mapped to the
     * column name.
     * 
     * @param columnName
     *            The column name for the mapped columns.
     * @return A list of all the {@link ColumnMapping}s.
     */
    public List getMappingsForColumn(String columnName) {
        return (List) mappingsMap.get(columnName);
    }

    /**
     * Returns a list of all the {@link ColumnMapping}s that are mapped to the
     * column index.
     * 
     * @param columnIndex
     *            The column index for the mapped columns.
     * @return A list of all the {@link ColumnMapping}s.
     */
    public List getMappingsForColumn(int columnIndex) {
        return (List) mappingsMap.get("" + columnIndex);
    }

    /**
     * Adds the array of {@link ColumnToPropertyMapping}s which acts as the id
     * column mappings.
     * 
     * @param idMappingsArr
     *            An array of {@link ColumnToPropertyMapping}s to add.
     */
    public void setIdMappings(ColumnToPropertyMapping[] idMappingsArr) {
        for (int i = 0; i < idMappingsArr.length; i++) {
            addIdMapping(idMappingsArr[i]);
        }
    }

    /**
     * Adds of array of {@link ColumnToPropertyMapping}s which acts as the data
     * column mappings.
     * 
     * @param dataMappingsArr
     *            An array of {@link ColumnToPropertyMapping}s to add.
     */
    public void setDataMappings(ColumnToPropertyMapping[] dataMappingsArr) {
        for (int i = 0; i < dataMappingsArr.length; i++) {
            addDataMapping(dataMappingsArr[i]);
        }
    }

    /**
     * Adds an array of {@link ColumnToPropertyMapping}s which acts as the
     * version column mappings.
     * 
     * @param versionColumnMappingsArr
     *            An array of {@link ColumnToPropertyMapping}s to add.
     */
    public void setVersionMappings(VersionColumnMapping[] versionColumnMappingsArr) {
        for (int i = 0; i < versionColumnMappingsArr.length; i++) {
            addVersionMapping(versionColumnMappingsArr[i]);
        }
    }

    /**
     * Returns an iterator over the id mappings.
     */
    public Iterator idMappingsIt() {
        return idMappings.iterator();
    }

    /**
     * Returns the size of the id mappings.
     */
    public int idMappingsSize() {
        return idMappings.size();
    }

    /**
     * Returns an iterator over the data mappings.
     */
    public Iterator dataMappingsIt() {
        return dataMappings.iterator();
    }

    /**
     * Returns the size of the data mappings.
     */
    public int dataMappingsSize() {
        return dataMappings.size();
    }

    /**
     * Returns an iterator of the version mappings.
     */
    public Iterator versionMappingsIt() {
        return versionMappings.iterator();
    }

    /**
     * Returns the size of the version mappings.
     */
    public int versionMappingsSize() {
        return versionMappings.size();
    }
    
    public Iterator mappingsIt() {
        return mappingsMap.values().iterator();
    }

    /**
     * Should the mapping index unmapped columns as data columns.
     */
    public boolean isIndexUnMappedColumns() {
        return indexUnMappedColumns;
    }

    /**
     * Returns if the mapping should set to index unmapped columns as data
     * columns.
     */
    public void setIndexUnMappedColumns(boolean indexUnMappedColumns) {
        this.indexUnMappedColumns = indexUnMappedColumns;
    }

    /**
     * Is the mapping support versioning. It is a derived property, returning
     * <code>false</code> if no version column mapping are set.
     */
    public boolean supportsVersioning() {
        return this.versionMappings.size() != 0;
    }

    private String getColumnKey(ColumnMapping columnMapping) {
        if (columnMapping.isUsingColumnIndex()) {
            return "" + columnMapping.getColumnIndex();
        }
        return columnMapping.getColumnName();
    }

    private void addMappingToMap(ColumnMapping columnMapping) {
        String key = getColumnKey(columnMapping);
        List list = (List) mappingsMap.get(key);
        if (list == null) {
            list = new ArrayList();
            mappingsMap.put(key, list);
        }
        list.add(columnMapping);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("alias [" + alias + "] ");
        sb.append("select query [" + selectQuery + "] ");
        sb.append("version[" + supportsVersioning() + "] ");
        if (supportsVersioning()) {
            sb.append(" version query [" + versionQuery + "] ");
        }
        sb.append("indexUnMappedColumns[" + indexUnMappedColumns + "] ");
        sb.append("Id Mappings [");
        for (Iterator it = idMappingsIt(); it.hasNext();) {
            sb.append("{" + it.next() + "}, ");
        }
        if (supportsVersioning()) {
            sb.append("] Version Mappings [");
            for (Iterator it = versionMappingsIt(); it.hasNext();) {
                sb.append("{" + it.next() + "}, ");
            }
        }
        sb.append("] Data Mappings [");
        for (Iterator it = dataMappingsIt(); it.hasNext();) {
            sb.append("{" + it.next() + "}, ");
        }
        sb.append("}");
        return sb.toString();
    }

}
