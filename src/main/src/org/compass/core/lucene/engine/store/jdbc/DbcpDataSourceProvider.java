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

import java.sql.SQLException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * A Jakarta Commons Data Source Pool (DBCP) provider.
 *
 * @author kimchy
 */
public class DbcpDataSourceProvider extends AbstractDataSourceProvider {

    protected DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException {
        BasicDataSource dataSource = new BasicDataSource();
        if (!externalAutoCommit) {
            dataSource.setDefaultAutoCommit(autoCommit);
        }
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.DEFAULT_TRANSACTION_ISOLATION) != null) {
            dataSource.setDefaultTransactionIsolation(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.DEFAULT_TRANSACTION_ISOLATION, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.INITIAL_SIZE) != null) {
            dataSource.setInitialSize(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.INITIAL_SIZE, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_ACTIVE) != null) {
            dataSource.setMaxActive(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_ACTIVE, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_IDLE) != null) {
            dataSource.setMaxIdle(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_IDLE, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MIN_IDLE) != null) {
            dataSource.setMinIdle(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MIN_IDLE, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_WAIT) != null) {
            dataSource.setMaxWait(settings.getSettingAsLong(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_WAIT, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_OPEN_PREPARED_STATEMENTS) != null) {
            dataSource.setMaxOpenPreparedStatements(settings.getSettingAsInt(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.MAX_OPEN_PREPARED_STATEMENTS, 0));
        }

        if (settings.getSetting(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.POOL_PREPARED_STATEMENTS) != null) {
            dataSource.setPoolPreparedStatements(settings.getSettingAsBoolean(LuceneEnvironment.JdbcStore.DataSourceProvider.Dbcp.POOL_PREPARED_STATEMENTS, false));
        }

        return dataSource;
    }

    public void closeDataSource() {
        try {
            ((BasicDataSource) getDataSource()).close();
        } catch (SQLException e) {
            // do nothing
        }
    }
}
