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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassSession;
import org.compass.gps.device.jpa.EntityManagerWrapper;
import org.compass.gps.device.jpa.JpaGpsDevice;
import org.compass.gps.device.jpa.JpaGpsDeviceException;
import org.compass.gps.device.jpa.entities.EntityInformation;
import org.compass.gps.device.jpa.queryprovider.HibernateJpaQueryProvider;
import org.compass.gps.device.support.parallel.IndexEntity;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.ejb.HibernateEntityManager;
import org.hibernate.ejb.HibernateQuery;
import org.hibernate.metadata.ClassMetadata;

/**
 * A Hibernate indexer uses Hibernate <code>ScrollableResults</code> to index the database
 * instead of using <code>setFirstResult</code> and <code>setMaxResults</code>. Using scrollable
 * results yields better performance especially for large result set.
 *
 * <p>Also takes into accont if using {@link HibernateJpaQueryProvider} by called its <code>createCriteria</code>
 * instead of the default <code>createQuery</code>. The criteria better handles outer joins, allows to set the
 * fetch size, and automatically supports ordering by the ids of the entities.
 *
 * <p>Note, if using {@link org.compass.gps.device.jpa.JpaGpsDevice#setIndexSelectQuery(Class, String)} will cause
 * not to be able to use <code>Criteria</code>. Instead, make sure to use {@link org.compass.gps.device.jpa.JpaGpsDevice#setIndexQueryProvider(Class, org.compass.gps.device.jpa.queryprovider.JpaQueryProvider)}
 * and provider your own extension on top of {@link org.compass.gps.device.jpa.queryprovider.HibernateJpaQueryProvider}
 * that returns your own <code>Criteria</code>.
 *
 * @author kimchy
 */
public class HibernateJpaIndexEntitiesIndexer implements JpaIndexEntitiesIndexer {

    private static final Log log = LogFactory.getLog(HibernateJpaIndexEntitiesIndexer.class);

    private JpaGpsDevice jpaGpsDevice;

    private boolean performOrderById = true;

    private Map<String, Boolean> performOrderByPerEntity = new HashMap<String, Boolean>();

    public void setJpaGpsDevice(JpaGpsDevice jpaGpsDevice) {
        this.jpaGpsDevice = jpaGpsDevice;
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
                entityManager.getSession().setCacheMode(CacheMode.IGNORE);
                if (log.isDebugEnabled()) {
                    log.debug(jpaGpsDevice.buildMessage("Indexing entities [" + entityInformation.getName() + "] using query ["
                            + entityInformation.getQueryProvider() + "]"));
                }

                if (entityInformation.getQueryProvider() instanceof HibernateJpaQueryProvider) {
                    Criteria criteria = ((HibernateJpaQueryProvider) entityInformation.getQueryProvider()).createCriteria(entityManager, entityInformation);
                    if (criteria != null) {
                        if (performOrderById) {
                            Boolean performOrder = performOrderByPerEntity.get(entityInformation.getName());
                            if (performOrder == null || performOrder) {
                                ClassMetadata metadata = entityManager.getSession().getSessionFactory().getClassMetadata(entityInformation.getName());
                                String idPropName = metadata.getIdentifierPropertyName();
                                if (idPropName != null) {
                                    criteria.addOrder(Order.asc(idPropName));
                                }
                            }
                        }
                        criteria.setFetchSize(fetchCount);
                        cursor = criteria.scroll(ScrollMode.FORWARD_ONLY);
                    }
                }
                if (cursor == null) {
                    HibernateQuery query = (HibernateQuery) entityInformation.getQueryProvider().createQuery(entityManager, entityInformation);
                    cursor = query.getHibernateQuery().scroll(ScrollMode.FORWARD_ONLY);
                }

                // store things in row buffer to allow using batch fetching in Hibernate
                RowBuffer buffer = new RowBuffer(session, entityManager.getSession(), fetchCount);
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
                    if (item != prev && prev != null) {
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