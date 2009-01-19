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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import javax.sql.DataSource;

import org.compass.gps.device.jdbc.JdbcGpsDeviceException;
import org.compass.gps.device.jdbc.JdbcUtils;

/**
 * A specialized form of
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping},
 * mapping a specfic database table to a resource.
 * <p>
 * Since the mapping works against a table, most of the parameters can be
 * automatically generated. The required mappings are the alias name and the
 * table name. The settings that can be generated are the id mappings
 * (based on the table primary keys), the select query (based on the table
 * name), and the version query (based on the table name and the version column
 * mappings).
 * <p>
 * If no data column mappings are provided, the
 * <code>indexUnMappedColumns</code> from
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping} can be
 * user to auto map all the table columns as data columns.
 * <p>
 * For real time mirroring, at least one version column mapping ({@link org.compass.gps.device.jdbc.mapping.VersionColumnMapping})
 * is required. And the version query can be auto generated.
 * <p>
 * Note that the mapping will auto generate only settings that have not been
 * set. If, for example, the select query was set, it will not be generated.
 * 
 * @author kimchy
 */
public class TableToResourceMapping extends ResultSetToResourceMapping implements AutoGenerateMapping {

    private String tableName;

    /**
     * Creates a new table to <code>Resource</code> mapping. Must set the
     * alias, and the table name.
     * <p>
     * Indexing of unmapped columns is diasabled by default.
     * 
     */
    public TableToResourceMapping() {

    }

    /**
     * Creates a new table to {@link org.compass.core.Resource} mapping with the given
     * table name and alias.
     * <p>
     * Indexing of unmapped columns is disabled by default.
     * 
     * @param tableName The table name
     * @param alias The {@link org.compass.core.Resource} alias
     */
    public TableToResourceMapping(String tableName, String alias) {
        setAlias(alias);
        this.tableName = tableName;
    }

    /**
     * Generates the unset mappings.
     * <p>
     * Generates the id mappings based on the table primary keys if no id column
     * mappings are set.
     * <p>
     * Generates the select query based on the table name if no select query is
     * set.
     * <p>
     * Generates the version query based on the table name and the version
     * column mappings if no version query is set and at least one version
     * column mapping is set.
     */
    public void generateMappings(DataSource dataSource) throws JdbcGpsDeviceException {
        if (idMappingsSize() == 0) {
            generateIdMappings(dataSource);
        }
        if (getSelectQuery() == null) {
            generateSelectQuery(dataSource);
        }
        if (getVersionQuery() == null && supportsVersioning()) {
            generateVersionQuery(dataSource);
        }
    }

    private void generateIdMappings(DataSource dataSource) throws JdbcGpsDeviceException {
        Connection con = JdbcUtils.getConnection(dataSource);
        ResultSet pks = null;
        try {
            DatabaseMetaData metaData = con.getMetaData();
            pks = metaData.getPrimaryKeys(null, null, getTableName());
            while (pks.next()) {
                String pkColumnName = pks.getString("COLUMN_NAME");
                addIdMapping(new IdColumnToPropertyMapping(pkColumnName, pkColumnName));
            }
        } catch (SQLException e) {
            throw new JdbcGpsDeviceException("Failed to fetch primary keys for table [" + getTableName() + "]", e);
        } finally {
            JdbcUtils.closeResultSet(pks);
            JdbcUtils.closeConnection(con);
        }
    }

    private void generateSelectQuery(DataSource dataSource) {
        if (isIndexUnMappedColumns()) {
            setSelectQuery("select * from " + getTableName());
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("select ");
            boolean first = true;
            for (Iterator idIt = idMappingsIt(); idIt.hasNext();) {
                ColumnToPropertyMapping idMapping = (ColumnToPropertyMapping) idIt.next();
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                if (idMapping.isUsingColumnIndex()) {
                    throw new IllegalArgumentException(
                            "When mapping and not using the indexUnMappedColumns, must specify id column name and not column index");
                }
                sb.append(idMapping.getColumnName());
            }
            for (Iterator dataIt = dataMappingsIt(); dataIt.hasNext();) {
                ColumnToPropertyMapping dataMapping = (ColumnToPropertyMapping) dataIt.next();
                sb.append(", ");
                if (dataMapping.isUsingColumnIndex()) {
                    throw new IllegalArgumentException(
                            "When mapping and not using the indexUnMappedColumns, must specify id column name and not column index");
                }
                sb.append(dataMapping.getColumnName());
            }
            for (Iterator verIt = versionMappingsIt(); verIt.hasNext();) {
                VersionColumnMapping verMapping = (VersionColumnMapping) verIt.next();
                sb.append(", ");
                if (verMapping.isUsingColumnIndex()) {
                    throw new IllegalArgumentException(
                            "When mapping version column to a table, must specify version column name and not column index");
                }
                sb.append(verMapping.getColumnName());
            }
            sb.append(" from ");
            sb.append(getTableName());
            setSelectQuery(sb.toString());
        }
    }

    private void generateVersionQuery(DataSource dataSource) {
        StringBuffer sb = new StringBuffer();
        sb.append("select ");
        boolean first = true;
        for (Iterator idIt = idMappingsIt(); idIt.hasNext();) {
            IdColumnToPropertyMapping idMapping = (IdColumnToPropertyMapping) idIt.next();
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(idMapping.getColumnNameForVersion());
        }
        for (Iterator verIt = versionMappingsIt(); verIt.hasNext();) {
            VersionColumnMapping verMapping = (VersionColumnMapping) verIt.next();
            sb.append(", ");
            if (verMapping.isUsingColumnIndex()) {
                throw new IllegalArgumentException(
                        "When mapping version column to a table, must specify version column name and not column index");
            }
            sb.append(verMapping.getColumnName());
        }
        sb.append(" from ");
        sb.append(getTableName());
        setVersionQuery(sb.toString());
    }

    /**
     * Returns the table name that the mappings maps to.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the table name that the mappings maps to.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String toString() {
        return "Table[" + tableName + "] " + super.toString();
    }
}
