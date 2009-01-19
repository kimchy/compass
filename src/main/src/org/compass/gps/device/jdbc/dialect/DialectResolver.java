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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.JdbcStoreException;
import org.compass.gps.device.jdbc.JdbcUtils;

/**
 * @author kimchy
 */
public class DialectResolver {

    public static interface DatabaseMetaDataToDialectMapper {

        Class<? extends JdbcDialect> getDialect(DatabaseMetaData metaData) throws SQLException;
    }

    public static class DatabaseNameToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class<? extends JdbcDialect> dialect;

        public DatabaseNameToDialectMapper(String databaseName, Class<? extends JdbcDialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        public Class<? extends JdbcDialect> getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameStartsWithToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class<? extends JdbcDialect> dialect;

        public DatabaseNameStartsWithToDialectMapper(String databaseName, Class<? extends JdbcDialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
        }

        public Class<? extends JdbcDialect> getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().startsWith(databaseName)) {
                return dialect;
            }
            return null;
        }
    }

    public static class DatabaseNameAndVersionToDialectMapper implements DatabaseMetaDataToDialectMapper {

        private String databaseName;

        private Class<? extends JdbcDialect> dialect;

        private int version;

        public DatabaseNameAndVersionToDialectMapper(String databaseName, int version, Class<? extends JdbcDialect> dialect) {
            this.databaseName = databaseName;
            this.dialect = dialect;
            this.version = version;
        }

        public Class<? extends JdbcDialect> getDialect(DatabaseMetaData metaData) throws SQLException {
            if (metaData.getDatabaseProductName().equals(databaseName) && metaData.getDatabaseMajorVersion() == version) {
                return dialect;
            }
            return null;
        }
    }

    private LinkedList<DatabaseMetaDataToDialectMapper> mappers = new LinkedList<DatabaseMetaDataToDialectMapper>();

    public DialectResolver() {
        this(true);
    }

    public DialectResolver(boolean useDefaultMappers) {
        if (!useDefaultMappers) {
            return;
        }
        mappers.add(new DatabaseNameToDialectMapper("HSQL Database Engine", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("DB2/NT", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("MySQL", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("PostgreSQL", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameStartsWithToDialectMapper("Microsoft SQL Server", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Sybase SQL Server", DefaultJdbcDialect.class));
        mappers.add(new DatabaseNameAndVersionToDialectMapper("Oracle", 8, OracleJdbcDialect.class));
        mappers.add(new DatabaseNameAndVersionToDialectMapper("Oracle", 9, OracleJdbcDialect.class));
        mappers.add(new DatabaseNameToDialectMapper("Oracle", OracleJdbcDialect.class));
    }

    public void addFirstMapper(DatabaseMetaDataToDialectMapper mapper) {
        mappers.addFirst(mapper);
    }

    public void addLastMapper(DatabaseMetaDataToDialectMapper mapper) {
        mappers.addLast(mapper);
    }

    public JdbcDialect getDialect(DataSource dataSource) throws JdbcStoreException {
        Connection conn = JdbcUtils.getConnection(dataSource);
        String databaseName;
        int databaseMajorVersion;
        int databaseMinorVersion;
        String driverName;
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            databaseName = metaData.getDatabaseProductName();
            databaseMajorVersion = metaData.getDatabaseMajorVersion();
            databaseMinorVersion = metaData.getDatabaseMinorVersion();
            driverName = metaData.getDriverName();
            for (DatabaseMetaDataToDialectMapper mapper : mappers) {
                Class<? extends JdbcDialect> dialectClass = mapper.getDialect(metaData);
                if (dialectClass == null) {
                    continue;
                }
                return dialectClass.newInstance();
            }
        } catch (Exception e) {
            throw new JdbcStoreException("Failed to auto detect dialect", e);
        } finally {
            JdbcUtils.closeConnection(conn);
        }
        throw new JdbcStoreException("Failed to auto detect dialect, no match found for database [" + databaseName +
                "] version [" + databaseMajorVersion + "/" + databaseMinorVersion + "] driver [" + driverName + "]");
    }
}