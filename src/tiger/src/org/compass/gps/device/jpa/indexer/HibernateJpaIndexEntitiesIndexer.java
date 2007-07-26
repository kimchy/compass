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

package org.compass.gps.device.jpa.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.EntityManagerWrapper;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernateQuery;

/**
 * A Hibernate indexer uses Hibernate <code>ScrollableResults</code> to index the database
 * instead of using <code>setFirstResult</code> and <code>setMaxResults</code>. Using scrollable
 * results yields better performance especially for large result set.
 *
 * @author kimchy
 */
public class HibernateJpaIndexEntitiesIndexer implements JpaIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(HibernateJpaIndexEntitiesIndexer.class);

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
            ScrollableResults cursor = null;
            try {
                wrapper.open();
                HibernateEntityManager entityManager = (HibernateEntityManager) wrapper.getEntityManager();
                if (log.isDebugEnabled()) {
                    log.debug(jpaGpsDevice.buildMessage("Indexing entities [" + entityInformation.getName() + "] using query ["
                            + entityInformation.getQueryProvider() + "]"));
                }
                HibernateQuery query = (HibernateQuery) entityInformation.getQueryProvider().createQuery(entityManager, entityInformation);
                // TODO how can we set the fetchCount?
                cursor = query.getHibernateQuery().scroll(ScrollMode.FORWARD_ONLY);
                while (cursor.next()) {
                    Object item = cursor.get(0);
                    session.create(item);
                    entityManager.getSession().evict(item);
                    session.evictAll();
                }
                cursor.close();
                entityManager.clear();
                wrapper.close();
            } catch (Exception e) {
                log.error(jpaGpsDevice.buildMessage("Failed to index the database"), e);
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e1) {
                        log.warn(jpaGpsDevice.buildMessage("Failed to close cursor on error, ignoring"), e1);
                    }
                }
                wrapper.closeOnError();
                if (!(e instanceof JpaGpsDeviceException)) {
                    throw new JpaGpsDeviceException(jpaGpsDevice.buildMessage("Failed to index the database"), e);
                }
                throw (JpaGpsDeviceException) e;
            }
        }
    }
}