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

package org.compass.gps.device.hibernate.entities;

import org.compass.gps.device.hibernate.DefaultHibernateQueryProvider;
import org.compass.gps.device.hibernate.HibernateQueryProvider;
import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * A general Entity information to be used by the {@link org.compass.gps.device.hibernate.HibernateGpsDevice}
 * during the indexing process.
 *
 * @author kimchy
 */
public class EntityInformation implements IndexEntity {

    private Class clazz;

    private String name;

    private HibernateQueryProvider queryProvider;

    private String[] subIndexes;

    public EntityInformation(Class clazz, String name, String[] subIndexes) {
        this(clazz, name, new DefaultHibernateQueryProvider(clazz, name), subIndexes);
    }

    public EntityInformation(Class clazz, String name, String selectQuery, String[] subIndexes) {
        this(clazz, name, new DefaultHibernateQueryProvider(selectQuery), subIndexes);
    }

    public EntityInformation(Class clazz, String name, HibernateQueryProvider queryProvider, String[] subIndexes) {
        this.clazz = clazz;
        this.name = name;
        this.queryProvider = queryProvider;
        this.subIndexes = subIndexes;
    }

    /**
     * Returns the entity class associated with the entity
     */
    public Class getEntityClass() {
        return clazz;
    }

    /**
     * Returns the entity name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a string based select query. Uses {@link org.compass.gps.device.hibernate.DefaultHibernateQueryProvider}
     * based on the string query.
     */
    public void setSelectQuery(String selectQuery) {
        this.queryProvider = new DefaultHibernateQueryProvider(selectQuery);
    }

    /**
     * Sets a query provider. Responsible during indexing time to create
     * a query for the given entity that will be used to query the database
     * for indexing.
     */
    public void setQueryProvider(HibernateQueryProvider queryProvider) {
        this.queryProvider = queryProvider;
    }

    /**
     * Gets a query provider. Responsible during indexing time to create
     * a query for the given entity that will be used to query the database
     * for indexing.
     */
    public HibernateQueryProvider getQueryProvider() {
        return this.queryProvider;
    }

    /**
     * Returns a list of the sub indexes this indexable
     * content the index entity represents is going to
     * be indexed into. Used for parallel indexing.
     */
    public String[] getSubIndexes() {
        return subIndexes;
    }
}