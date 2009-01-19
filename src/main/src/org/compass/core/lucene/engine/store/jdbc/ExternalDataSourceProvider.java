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

package org.compass.core.lucene.engine.store.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.datasource.AbstractDataSource;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;

/**
 * A data source provider that can use an externally configured data source. In order to set the external
 * DataSource to be used, the {@link #setDataSource(javax.sql.DataSource)} static method needs to be called
 * before the Compass instance is created.
 *
 * @author kimchy
 */
public class ExternalDataSourceProvider extends AbstractDataSourceProvider {

    private static class UsernamePasswordDataSourceWrapper extends AbstractDataSource {

        private DataSource dataSource;

        private String username;

        private String password;

        private boolean autoCommit;

        private boolean externalAutoCommit;

        public UsernamePasswordDataSourceWrapper(DataSource dataSource, String username, String password,
                                                 boolean autoCommit, boolean externalAutoCommit) {
            this.dataSource = dataSource;
            this.username = username;
            this.password = password;
            this.autoCommit = autoCommit;
            this.externalAutoCommit = externalAutoCommit;
        }

        public Connection getConnection() throws SQLException {
            return getConnection(username, password);
        }

        public Connection getConnection(String username, String password) throws SQLException {
            Connection conn;
            if (username == null) {
                conn = dataSource.getConnection();
            } else {
                conn = dataSource.getConnection(username, password);
            }
            if (!externalAutoCommit) {
                if (conn.getAutoCommit() != autoCommit) {
                    conn.setAutoCommit(autoCommit);
                }
            }
            return conn;
        }
    }

    private static ThreadLocal dataSourceHolder = new ThreadLocal();

    private static String dataSourceKey = ExternalDataSourceProvider.class.getName();

    private CompassSettings settings;

    /**
     * Sets the external data source to be used. Must be set before creating the compass instance.
     */
    public static void setDataSource(DataSource dataSource) {
        dataSourceHolder.set(dataSource);
    }

    protected DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException {
        this.settings = settings;
        DataSource dataSource = (DataSource) dataSourceHolder.get();
        if (dataSource == null) {
            dataSource = (DataSource) settings.getRegistry(dataSourceKey);
        }
        if (dataSource == null) {
            throw new CompassException("Failed to find data source, have you set the static set data source?");
        } else {
            settings.setRegistry(dataSourceKey, dataSource);
            dataSourceHolder.set(null);
        }
        return new UsernamePasswordDataSourceWrapper(dataSource, username, password, autoCommit, externalAutoCommit);
    }

    public void closeDataSource() {
        dataSourceHolder.set(null);
        settings.removeRegistry(dataSourceKey);
    }
}
