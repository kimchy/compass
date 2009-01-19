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
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.EntityManagerWrapper;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;

/**
 * The default JPA indexer. Uses plain JPA API to do pagination (<code>setFirstResult</code>
 * and <code>setMaxResults</code>).
 *
 * @author kimchy
 */
public class DefaultJpaIndexEntitiesIndexer implements JpaIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(DefaultJpaIndexEntitiesIndexer.class);

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
            int current = 0;
            while (true) {
                if (!jpaGpsDevice.isRunning()) {
                    return;
                }
                EntityManagerWrapper wrapper = jpaGpsDevice.getEntityManagerWrapper().newInstance();
                try {
                    wrapper.open();
                    EntityManager entityManager = wrapper.getEntityManager();
                    if (log.isDebugEnabled()) {
                        log.debug(jpaGpsDevice.buildMessage("Indexing entities [" + entityInformation.getName() + "] range ["
                                + current + "-" + (current + fetchCount) + "] using query ["
                                + entityInformation.getQueryProvider() + "]"));
                    }
                    Query query = entityInformation.getQueryProvider().createQuery(entityManager, entityInformation);
                    query.setFirstResult(current);
                    query.setMaxResults(fetchCount);
                    List results = query.getResultList();
                    for (Object result : results) {
                        session.create(result);
                    }
                    session.evictAll();
                    entityManager.clear();
                    wrapper.close();
                    if (results.size() < fetchCount) {
                        break;
                    }
                    current += fetchCount;
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
    }
}
