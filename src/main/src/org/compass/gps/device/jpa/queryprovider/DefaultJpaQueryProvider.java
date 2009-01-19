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

package org.compass.gps.device.jpa.queryprovider;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.compass.gps.device.jpa.entities.EntityInformation;

/**
 * A simple Jpa query provider based on a select statement. The select
 * statement can be automatically generated based on the entity name
 * as well.
 *
 * @author kimchy
 */
public class DefaultJpaQueryProvider implements JpaQueryProvider {

    private String selectQuery;

    private boolean isUsingDefaultSelectQuery;

    /**
     * Creates a new query provider based on the entity name. The select
     * statement is <code>select x from entityName x</code>.
     *
     * @param entityClass The entity class
     * @param entityName  The entity name
     */
    public DefaultJpaQueryProvider(Class<?> entityClass, String entityName) {
        this.selectQuery = "select x from " + entityName + " x";
        this.isUsingDefaultSelectQuery = true;
    }

    /**
     * Creates a new query provider based on the provided select statement.
     *
     * @param selectQuery The select query
     */
    public DefaultJpaQueryProvider(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    /**
     * Creates a query based on the select statement initlaized in the query provider
     * construction.
     */
    public Query createQuery(EntityManager entityManager, EntityInformation entityInformation) {
        if (selectQuery != null) {
            return entityManager.createQuery(selectQuery);
        }
        return entityManager.createQuery("select x from " + entityInformation.getName() + " x");
    }

    protected boolean isUsingDefaultSelectQuery() {
        return this.isUsingDefaultSelectQuery;
    }

    public String toString() {
        return "QueryProvider[" + selectQuery + "]";
    }
}
