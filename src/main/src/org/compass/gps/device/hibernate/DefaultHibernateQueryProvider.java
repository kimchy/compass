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

import org.compass.gps.device.hibernate.entities.EntityInformation;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;

/**
 * A simple Hibernate query provider based on a select statement. The select
 * statement can be automatically generated based on the entity name
 * as well.
 *
 * @author kimchy
 */
public class DefaultHibernateQueryProvider implements HibernateQueryProvider {

    private String selectQuery;

    private boolean isUsingDefaultSelectQuery;

    /**
     * Creates a new query provider based on the entity name. The select
     * statement is <code>from entityName</code>.
     *
     * @param entityClass The entity class
     * @param entityName  The entity name
     */
    public DefaultHibernateQueryProvider(Class entityClass, String entityName) {
        this.selectQuery = "from " + entityName;
        this.isUsingDefaultSelectQuery = true;
    }

    /**
     * Creates a new query provider based on the provided select statement.
     *
     * @param selectQuery The select query
     */
    public DefaultHibernateQueryProvider(String selectQuery) {
        this.selectQuery = selectQuery;
    }

    /**
     * Creates a query based on the select statement initlaized in the query provider
     * construction.
     */
    public Query createQuery(Session session, EntityInformation entityInformation) {
        if (selectQuery != null) {
            return session.createQuery(selectQuery);
        }
        return session.createQuery("from " + entityInformation.getName());
    }

    /**
     * Creates the Hibernate criteria for the given entity information. Returns
     * <code>null</code> if the select query is set.
     */
    public Criteria createCriteria(Session session, EntityInformation entityInformation) {
        if (!isUsingDefaultSelectQuery) {
            return null;
        }
    	return session.createCriteria(entityInformation.getName());
    }

    public String toString() {
        return "QueryProvider[" + selectQuery + "]";
    }
}
