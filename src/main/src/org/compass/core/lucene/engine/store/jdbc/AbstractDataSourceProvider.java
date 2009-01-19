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

import javax.sql.DataSource;

import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;

/**
 * @author kimchy
 */
public abstract class AbstractDataSourceProvider implements DataSourceProvider {

    private DataSource dataSource;

    protected String driverClass;

    protected String username;

    protected String password;

    protected boolean autoCommit;

    protected boolean externalAutoCommit;

    public void configure(String url, CompassSettings settings) throws CompassException {
        driverClass = settings.getSetting(LuceneEnvironment.JdbcStore.Connection.DRIVER_CLASS);
        username = settings.getSetting(LuceneEnvironment.JdbcStore.Connection.USERNAME);
        password = settings.getSetting(LuceneEnvironment.JdbcStore.Connection.PASSWORD);
        String autoCommitSetting = settings.getSetting(LuceneEnvironment.JdbcStore.Connection.AUTO_COMMIT, "false");
        if ("external".equalsIgnoreCase(autoCommitSetting)) {
            externalAutoCommit = true;
        } else {
            externalAutoCommit = false;
            autoCommit = "true".equalsIgnoreCase(autoCommitSetting);
        }
        this.dataSource = doCreateDataSource(url, settings);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public void closeDataSource() {
        // do nothing
    }

    protected abstract DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException;
}
