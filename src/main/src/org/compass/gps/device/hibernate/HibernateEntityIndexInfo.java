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

package org.compass.gps.device.hibernate;

/**
 * An object holding the entity name and a specific sql query for indexing
 * or a direct {link HibernateQueryProvider}.
 *
 * @author kimchy
 * @see HibernateGpsDevice#setindexEntityInfo(HibernateEntityIndexInfo)
 */
public class HibernateEntityIndexInfo {

    private String entityName;

    private HibernateQueryProvider queryProvider;

    /**
     * Constructs a new index entity info.
     *
     * @param entityName The entity name
     * @param sqlQuery   The sql query
     */
    public HibernateEntityIndexInfo(String entityName, String sqlQuery) {
        this.entityName = entityName;
        this.queryProvider = new DefaultHibernateQueryProvider(sqlQuery);
    }

    /**
     * Constructs a new index entity info.
     *
     * @param entityName    The entity name
     * @param queryProvider A query provider that will construct the index query
     */
    public HibernateEntityIndexInfo(String entityName, HibernateQueryProvider queryProvider) {
        this.entityName = entityName;
        this.queryProvider = queryProvider;
    }

    /**
     * Returns the index entity name.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the index query provider.
     */
    public HibernateQueryProvider getQueryProvider() {
        return queryProvider;
    }
}
