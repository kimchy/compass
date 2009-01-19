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
 * During indexing time provides the Hibernate Query to extract the data
 * to be indexed per entity.
 *
 * <p>Can return either return <code>Query</code> or <code>Criteria</code>.
 * Indexers ({@link org.compass.gps.device.hibernate.indexer.HibernateIndexEntitiesIndexer}
 * are encouraged to first check the criteria, and if that returns <code>null</code>
 * use the query.
 *
 * @author kimchy
 */
public interface HibernateQueryProvider {

    /**
     * Create a HIbnerate Query based on the Hibernate <code>Session</code> and
     * the {@link org.compass.gps.device.hibernate.entities.EntityInformation}.
     *
     * @param session           The Hibernate session to create the query with
     * @param entityInformation The enity information to create the query with
     * @return the Hibernate query
     */
    Query createQuery(Session session, EntityInformation entityInformation);

    /**
     * Create a Criteria query based on the <code>EntityManager</code>
     * and the {@link org.compass.gps.device.hibernate.entities.EntityInformation}.
     * Criteria queries respect customizations about eager fetching of
     * collections.
     *
     * @param session           The Hibernate session to create the query with
     * @param entityInformation The enity information to create the query with
     * @return the Hibernate query
     */
    Criteria createCriteria(Session session, EntityInformation entityInformation);
}
