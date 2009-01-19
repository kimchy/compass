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

package org.compass.gps.device.jpa.indexer;

import java.util.List;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.openjpa.persistence.OpenJPAEntityManager;
import org.apache.openjpa.persistence.OpenJPAPersistence;
import org.apache.openjpa.persistence.OpenJPAQuery;
import org.apache.openjpa.persistence.jdbc.FetchDirection;
import org.apache.openjpa.persistence.jdbc.JDBCFetchPlan;
import org.apache.openjpa.persistence.jdbc.LRSSizeAlgorithm;
import org.apache.openjpa.persistence.jdbc.ResultSetType;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.EntityManagerWrapper;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.embedded.openjpa.CompassProductDerivation;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * OpenJPA indexer that uses fetch plan to scroll the result set and index it. Performs
 * better than JPA <code>setFirstResult</code> and <code>setMaxResults</code> especially
 * for large result sets.
 *
 * @author kimchy
 */
public class OpenJPAJpaIndexEntitiesIndexer implements JpaIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(OpenJPAJpaIndexEntitiesIndexer.class);

    private JpaGpsDevice jpaGpsDevice;

    public void setJpaGpsDevice(JpaGpsDevice jpaGpsDevice) {
        this.jpaGpsDevice = jpaGpsDevice;
    }

    public void performIndex(CompassSession session, IndexEntity[] entities) {
        for (IndexEntity indexEntity : entities) {
            EntityInformation entityInformation = (EntityInformation) indexEntity;
            if (jpaGpsDevice.isFilteredForIndex(entityInformation.getName())) {
                continue;
            }
            int fetchCount = jpaGpsDevice.getFetchCount();
            if (!jpaGpsDevice.isRunning()) {
                return;
            }
            EntityManagerWrapper wrapper = jpaGpsDevice.getEntityManagerWrapper().newInstance();
            try {
                wrapper.open();
                OpenJPAEntityManager entityManager = OpenJPAPersistence.cast(wrapper.getEntityManager());
                entityManager.setPopulateStoreCache(false);
                if (log.isDebugEnabled()) {
                    log.debug(jpaGpsDevice.buildMessage("Indexing entities [" + entityInformation.getName() + "] using query ["
                            + entityInformation.getQueryProvider() + "]"));
                }
                Query query = entityInformation.getQueryProvider().createQuery(entityManager, entityInformation);
                OpenJPAQuery openJPAQuery = OpenJPAPersistence.cast(query);
                JDBCFetchPlan fetch = (JDBCFetchPlan) openJPAQuery.getFetchPlan();
                if (CompassProductDerivation.isReleasedVersion()) {
                    doSetFetchPlan(fetchCount, fetch);
                }
                List results = openJPAQuery.getResultList();
                for (Object item : results) {
                    session.create(item);
                    entityManager.evict(item);
                    session.evictAll();
                }
                entityManager.clear();
                wrapper.close();
            } catch (Exception e) {
                log.error(jpaGpsDevice.buildMessage("Failed to index the database"), e);
                wrapper.closeOnError();
                if (!(e instanceof JpaGpsDeviceException)) {
                    throw new JpaGpsDeviceException(jpaGpsDevice.buildMessage("Failed to index the database"), e);
                }
                throw (JpaGpsDeviceException) e;
            }
        }
    }

    private void doSetFetchPlan(int fetchCount, JDBCFetchPlan fetch) {
        fetch.setFetchBatchSize(fetchCount);
        fetch.setResultSetType(ResultSetType.SCROLL_INSENSITIVE);
        fetch.setFetchDirection(FetchDirection.FORWARD);
        fetch.setLRSSizeAlgorithm(LRSSizeAlgorithm.UNKNOWN);
    }
}