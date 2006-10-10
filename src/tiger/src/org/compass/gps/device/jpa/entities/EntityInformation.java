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

package org.compass.gps.device.jpa.entities;

import org.compass.gps.device.jpa.DefaultJpaQueryProvider;
import org.compass.gps.device.jpa.JpaQueryProvider;

/**
 * A general Entity information to be used by the {@link org.compass.gps.device.jpa.JpaGpsDevice}
 * during the indexing process.
 *
 * @author kimchy
 * @see JpaEntitiesLocator
 */
public class EntityInformation {

    private Class<?> clazz;

    private String name;

    private JpaQueryProvider queryProvider;

    public EntityInformation(Class<?> clazz, String name) {
        this(clazz, name, new DefaultJpaQueryProvider(clazz, name));
    }

    public EntityInformation(Class<?> clazz, String name, String selectQuery) {
        this(clazz, name, new DefaultJpaQueryProvider(selectQuery));
    }

    public EntityInformation(Class<?> clazz, String name, JpaQueryProvider queryProvider) {
        this.clazz = clazz;
        this.name = name;
        this.queryProvider = queryProvider;
    }

    /**
     * Returns the entity class associated with the entity
     */
    public Class<?> getEntityClass() {
        return clazz;
    }

    /**
     * Returns the entity name
     */
    public String getEntityName() {
        return name;
    }

    /**
     * Sets a string based select query. Uses {@link org.compass.gps.device.jpa.DefaultJpaQueryProvider}
     * based on the string query.
     */
    public void setSelectQuery(String selectQuery) {
        this.queryProvider = new DefaultJpaQueryProvider(selectQuery);
    }

    /**
     * Sets a query provider. Responsible during indexing time to create
     * a query for the given entity that will be used to query the database
     * for indexing.
     */
    public void setQueryProvider(JpaQueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    /**
     * Gets a query provider. Responsible during indexing time to create
     * a query for the given entity that will be used to query the database
     * for indexing.
     */
    public JpaQueryProvider getQueryProvider() {
        return this.queryProvider;
    }
}
