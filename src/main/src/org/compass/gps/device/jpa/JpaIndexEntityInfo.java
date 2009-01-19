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

package org.compass.gps.device.jpa;

import org.compass.gps.device.jpa.queryprovider.DefaultJpaQueryProvider;
import org.compass.gps.device.jpa.queryprovider.JpaQueryProvider;

/**
 * Represents an index entity info that will be used to describe how (sql
 * query or query provider) an entity will be indexed.
 *
 * @author kimchy
 */
public class JpaIndexEntityInfo {

    private String entityName;

    private JpaQueryProvider queryProvider;

    /**
     * Constructs an index entity info that describes how an entity
     * will be indexed.
     *
     * @param entityName  The entity name
     * @param selectQuery The select query string
     */
    public JpaIndexEntityInfo(String entityName, String selectQuery) {
        this.entityName = entityName;
        this.queryProvider = new DefaultJpaQueryProvider(selectQuery);
    }

    /**
     * Constructs an index entity info that describes how an entity
     * will be indexed.
     *
     * @param entityName    The entity name
     * @param queryProvider The query provider to use in order to index
     */
    public JpaIndexEntityInfo(String entityName, JpaQueryProvider queryProvider) {
        this.entityName = entityName;
        this.queryProvider = queryProvider;
    }

    /**
     * Returns the entity name.
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * Returns the query provier.
     */
    public JpaQueryProvider getQueryProvider() {
        return queryProvider;
    }
}
