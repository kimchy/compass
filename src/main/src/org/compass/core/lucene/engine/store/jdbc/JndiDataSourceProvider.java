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
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.lucene.store.jdbc.datasource.AbstractDataSource;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.jndi.NamingHelper;

/**
 * A JNDI based data source provider. Uses any additional jndi settings to get the JNDI
 * initial context. The data source jndi name is the url set (without the jdbc:// prefix).
 *
 * @author kimchy
 */
public class JndiDataSourceProvider extends AbstractDataSourceProvider {

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

    protected DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException {
        try {
            DataSource dataSource = (DataSource) NamingHelper.getInitialContext(settings).lookup(url);
            return new UsernamePasswordDataSourceWrapper(dataSource, username, password, autoCommit, externalAutoCommit);
        } catch (NamingException e) {
            throw new CompassException("Failed to lookup data source from jndi under [" + url + "]", e);
        }
    }
}
