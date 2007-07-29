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

package org.compass.gps.device.hibernate.indexer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * A Hibernate indexer uses Hibernate <code>ScrollableResults</code> to index the database
 * instead of using <code>setFirstResult</code> and <code>setMaxResults</code>. Using scrollable
 * results yields better performance especially for large result set.
 *
 * @author kimchy
 */
public class ScrollableHibernateIndexEntitiesIndexer implements HibernateIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(ScrollableHibernateIndexEntitiesIndexer.class);

    private HibernateGpsDevice device;

    public void setHibernateGpsDevice(HibernateGpsDevice device) {
        this.device = device;
    }

    public void performIndex(CompassSession session, IndexEntity[] entities) {
        for (int i = 0; i < entities.length; i++) {
            EntityInformation entityInformation = (EntityInformation) entities[i];
            if (device.isFilteredForIndex(entityInformation.getName())) {
                continue;
            }
            if (!device.isRunning()) {
                return;
            }
            ScrollableResults cursor = null;
            Session hibernateSession = device.getSessionFactory().openSession();
            Transaction hibernateTransaction = null;
            try {
                hibernateTransaction = hibernateSession.beginTransaction();
                if (log.isDebugEnabled()) {
                    log.debug(device.buildMessage("Indexing entities [" + entityInformation.getName() + "] using query ["
                            + entityInformation.getQueryProvider() + "]"));
                }
                Query query = entityInformation.getQueryProvider().createQuery(hibernateSession, entityInformation);
                // TODO how can we set the fetchCount?
                cursor = query.scroll(ScrollMode.FORWARD_ONLY);
                while (cursor.next()) {
                    Object item = cursor.get(0);
                    session.create(item);
                    hibernateSession.evict(item);
                    session.evictAll();
                }
                cursor.close();
                session.evictAll();
                hibernateTransaction.commit();
            } catch (Exception e) {
                log.error(device.buildMessage("Failed to index the database"), e);
                if (cursor != null) {
                    try {
                        cursor.close();
                    } catch (Exception e1) {
                        log.warn(device.buildMessage("Failed to close cursor on error, ignoring"), e1);
                    }
                }
                if (hibernateTransaction != null) {
                    try {
                        hibernateTransaction.rollback();
                    } catch (Exception e1) {
                        log.warn("Failed to rollback Hibernate", e1);
                    }
                }
                if (!(e instanceof HibernateGpsDeviceException)) {
                    throw new HibernateGpsDeviceException(device.buildMessage("Failed to index the database"), e);
                }
                throw (HibernateGpsDeviceException) e;
            } finally {
                session.close();
            }
        }
    }
}