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

/**
 * Resposible for providing a Jdbc {@link DataSource} for
 * {@link org.compass.core.lucene.engine.store.JdbcDirectoryStore}.
 *
 * <p>The {@link DataSource} is a shared data source that should be used throughout
 * the lifecycle of this class.
 *
 * @author kimchy
 */
public interface DataSourceProvider {

    /**
     * Configures the data source provider with the give settings and url,
     * the configuration will control the {@link DataSource} that will be
     * instansiated.
     *
     * @param url      The jdbc url connection string
     * @param settings The settings for the given data source provider (and the {@link DataSource}).
     * @throws CompassException
     */
    void configure(String url, CompassSettings settings) throws CompassException;

    /**
     * Retuns an instance of the data source, as per the configuration set for it.
     * <p/>
     * Note, that the instance should be created (either in the configure method,
     * or lazily in this method), and shared for all repeating calls. This will
     * also allow {@link #closeDataSource()} to close the actual data source.
     */
    DataSource getDataSource();

    /**
     * Closes the created data source.
     */
    void closeDataSource();
}
