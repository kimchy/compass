/*
 * Copyright 2004-2006 the original author or authors.
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
 * A data source provider that can use an externally configured data source. In order to set the external
 * DataSource to be used, the {@link #setDataSource(javax.sql.DataSource)} static method needs to be called
 * before the Compass instance is created.
 *
 * @author kimchy
 */
public class ExternalDataSourceProvider extends AbstractDataSourceProvider {

    private static ThreadLocal dataSourceHolder = new ThreadLocal();

    /**
     * Sets the external data source to be used. Must be set before creating the compass instance.
     */
    public static void setDataSource(DataSource dataSource) {
        dataSourceHolder.set(dataSource);
    }

    protected DataSource doCreateDataSource(String url, CompassSettings settings) throws CompassException {
        return (DataSource) dataSourceHolder.get();
    }

    public void closeDataSource() {
        dataSourceHolder.set(null);
    }
}
