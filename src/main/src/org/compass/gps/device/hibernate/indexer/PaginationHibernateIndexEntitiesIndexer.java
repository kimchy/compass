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

package org.compass.gps.device.hibernate.indexer;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

/**
 * A Hibernate indexer uses Hibernate pagination using <code>setFirstResult</code>
 * and <code>setMaxResults</code>.
 *
 * @author kimchy
 */
public class PaginationHibernateIndexEntitiesIndexer implements HibernateIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(PaginationHibernateIndexEntitiesIndexer.class);

    private HibernateGpsDevice device;

    public void setHibernateGpsDevice(HibernateGpsDevice device) {
        this.device = device;
    }

    public void performIndex(CompassSession session, IndexEntity[] entities) {
        for (IndexEntity entity : entities) {
            EntityInformation entityInfo = (EntityInformation) entity;
            int fetchCount = device.getFetchCount();
            int current = 0;
            while (true) {
                if (!device.isRunning()) {
                    return;
                }
                Session hibernateSession = device.getSessionFactory().openSession();
                hibernateSession.setCacheMode(CacheMode.IGNORE);
                Transaction hibernateTransaction = null;
                try {
                    hibernateTransaction = hibernateSession.beginTransaction();
                    if (log.isDebugEnabled()) {
                        log.debug(device.buildMessage("Indexing entity [" + entityInfo.getName() + "] range ["
                                + current + "-" + (current + fetchCount) + "]"));
                    }
                    List values;
                    Criteria criteria = entityInfo.getQueryProvider().createCriteria(hibernateSession, entityInfo);
                    if (criteria != null) {
                        criteria.setFetchSize(device.getFetchCount());
                        criteria.setFirstResult(current);
                        criteria.setMaxResults(fetchCount);
                        values = criteria.list();
                    } else {
                        Query query = entityInfo.getQueryProvider().createQuery(hibernateSession, entityInfo).setFirstResult(current).setMaxResults(fetchCount);
                        values = query.list();
                    }
                    for (Object value : values) {
                        session.create(value);
                    }
                    session.evictAll();
                    hibernateTransaction.commit();
                    session.close();
                    current += fetchCount;
                    if (values.size() < fetchCount) {
                        break;
                    }
                } catch (Exception e) {
                    log.error(device.buildMessage("Failed to index the database"), e);
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
                    hibernateSession.close();
                }
            }
        }
    }
}