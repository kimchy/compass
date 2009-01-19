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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.hibernate.HibernateGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.entities.EntityInformation;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;

/**
 * A Hibernate indexer uses Hibernate <code>ScrollableResults</code> to index the database
 * instead of using <code>setFirstResult</code> and <code>setMaxResults</code>. Using scrollable
 * results yields better performance especially for large result set.
 *
 * <p>First tries to call {@link org.compass.gps.device.hibernate.HibernateQueryProvider#createCriteria(org.hibernate.Session, org.compass.gps.device.hibernate.entities.EntityInformation)}
 * in order to use Hibernate <code>Criteria</code> to construct the cursor. If no criteria is returned (<code>null</code>
 * is returned), Hibernate <code>Query</code> is used by calling {@link org.compass.gps.device.hibernate.HibernateQueryProvider#createQuery(org.hibernate.Session, org.compass.gps.device.hibernate.entities.EntityInformation)}.
 *
 * <p>When using Criteria, by default, orders the results by entity id. This can be turned off either globablly using
 * {@link #setPerformOrderById(boolean)}, or per entity using {@link #setPerformOrderById(String, boolean)}.
 *
 * @author kimchy
 */
public class ScrollableHibernateIndexEntitiesIndexer implements HibernateIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(ScrollableHibernateIndexEntitiesIndexer.class);

    private HibernateGpsDevice device;

    private boolean performOrderById = true;

    private Map<String, Boolean> performOrderByPerEntity = new HashMap<String, Boolean>();

    public void setHibernateGpsDevice(HibernateGpsDevice device) {
        this.device = device;
    }

    /**
     * Should this indxer order by the ids when <code>Criteria</code> is available.
     * Defaults to <code>true</code>.
     */
    public void setPerformOrderById(boolean performOrderById) {
        this.performOrderById = performOrderById;
    }

    /**
     * Should this indxer order by the ids when <code>Criteria</code> is available for
     * the given entity. Defaults to {@link #setPerformOrderById(boolean)}.
     */
    public void setPerformOrderById(String entity, boolean performOrderById) {
        performOrderByPerEntity.put(entity, performOrderById);
    }

    public void performIndex(CompassSession session, IndexEntity[] entities) {
        for (IndexEntity entity : entities) {
            EntityInformation entityInformation = (EntityInformation) entity;
            if (device.isFilteredForIndex(entityInformation.getName())) {
                continue;
            }
            if (!device.isRunning()) {
                return;
            }
            ScrollableResults cursor = null;
            Session hibernateSession = device.getSessionFactory().openSession();
            hibernateSession.setCacheMode(CacheMode.IGNORE);
            Transaction hibernateTransaction = null;
            try {
                hibernateTransaction = hibernateSession.beginTransaction();
                if (log.isDebugEnabled()) {
                    log.debug(device.buildMessage("Indexing entities [" + entityInformation.getName() + "] using query ["
                            + entityInformation.getQueryProvider() + "]"));
                }

                Criteria criteria = entityInformation.getQueryProvider().createCriteria(hibernateSession, entityInformation);
                if (criteria != null) {
                    if (performOrderById) {
                        Boolean performOrder = performOrderByPerEntity.get(entityInformation.getName());
                        if (performOrder == null || performOrder) {
                            ClassMetadata metadata = hibernateSession.getSessionFactory().getClassMetadata(entityInformation.getName());
                            String idPropName = metadata.getIdentifierPropertyName();
                            if (idPropName != null) {
                                criteria.addOrder(Order.asc(idPropName));
                            }
                        }
                    }
                    criteria.setFetchSize(device.getFetchCount());
                    cursor = criteria.scroll(ScrollMode.FORWARD_ONLY);
                } else {
                    Query query = entityInformation.getQueryProvider().createQuery(hibernateSession, entityInformation);
                    cursor = query.scroll(ScrollMode.FORWARD_ONLY);
                }

                // store things in row buffer to allow using batch fetching in Hibernate
                RowBuffer buffer = new RowBuffer(session, hibernateSession, device.getFetchCount());
                Object prev = null;
                while (true) {
                    try {
                        if (!cursor.next()) {
                            break;
                        }
                    } catch (ObjectNotFoundException e) {
                        continue;
                    }
                    Object item = cursor.get(0);
                    if (prev != null && item != prev) {
                        buffer.put(prev);
                    }
                    prev = item;
                    if (buffer.shouldFlush()) {
                        // put also the item/prev since we are clearing the session
                        // in the flush process
                        buffer.put(prev);
                        buffer.flush();
                        prev = null;
                    }
                }
                if (prev != null) {
                    buffer.put(prev);
                }
                buffer.close();
                cursor.close();

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
                hibernateSession.close();
                session.close();
            }
        }
    }

    private class RowBuffer {
        private Object[] buffer;
        private int fetchCount;
        private int index = 0;
        private CompassSession compassSession;
        private Session hibernateSession;

        RowBuffer(CompassSession compassSession, Session hibernateSession, int fetchCount) {
            this.compassSession = compassSession;
            this.hibernateSession = hibernateSession;
            this.fetchCount = fetchCount;
            this.buffer = new Object[fetchCount + 1];
        }

        public void put(Object row) {
            buffer[index] = row;
            index++;
        }

        public boolean shouldFlush() {
            return index >= fetchCount;
        }

        public void close() {
            flush();
            buffer = null;
        }

        private void flush() {
            for (int i = 0; i < index; i++) {
                compassSession.create(buffer[i]);
            }
            // clear buffer and sessions to allow for GC
            Arrays.fill(buffer, null);
            compassSession.evictAll();
            hibernateSession.clear();
            index = 0;
        }
    }

}
