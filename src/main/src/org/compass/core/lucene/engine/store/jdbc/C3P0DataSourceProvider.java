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

import java.beans.PropertyVetoException;
import javax.sql.DataSource;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.compass.core.CompassException;
import org.compass.core.config.CompassSettings;

/**
 * A c3p0 data source provider. Creates a new C3P0 pooled data source. For configuration, use a
 * file called c3p0.properties and storing it as a top-level resource in the same
 * CLASSPATH / classloader that loads c3p0's jar file.
 * <p/>
 * Will set the url, driverClass, user and password based on compass configuration settings.
 *
 * @author kimchy
 */
public class C3P0DataSourceProvider extends AbstractDataSourceProvider {

    protected DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(driverClass);
        } catch (PropertyVetoException e) {
            throw new CompassException("Failed to set driverClass [" + driverClass + "] in c3p0", e);
        }
        dataSource.setJdbcUrl(url);
        dataSource.setUser(username);
        dataSource.setPassword(password);
        if (!externalAutoCommit) {
            dataSource.setAutoCommitOnClose(autoCommit);
        }
        return dataSource;
    }
}
