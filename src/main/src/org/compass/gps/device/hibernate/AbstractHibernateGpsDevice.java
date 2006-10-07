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

package org.compass.gps.device.hibernate;

import java.util.Iterator;
import java.util.List;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.device.AbstractGpsDevice;

/**
 * An abstract hibernate device support. Aimed to provide the base operations
 * required by both Hiberante 2 and Hibernate 3.
 *
 * @author kimchy
 */
public abstract class AbstractHibernateGpsDevice extends AbstractGpsDevice implements HibernateGpsDevice {

    /**
     * A data holder used to index a specific Hibernate entity.
     */
    protected static class HibernateEntityInfo {
        public String entityname;

        public String selectQuery;
    }

    protected static interface HibernateSessionWrapper {
        void open() throws HibernateGpsDeviceException;

        void close();

        void closeOnError();
    }

    protected int fetchCount = 200;

    private HibernateEntityInfo[] entetiesInfo;

    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    protected void doStart() throws CompassGpsException {
        entetiesInfo = doGetHibernateEntitiesInfo();
    }

    /**
     * Indexes all the data that has hibernate mapping and compass mapping
     * associated with it.
     */
    protected void doIndex(CompassSession session) throws CompassException {
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database with fetch count [" + fetchCount + "]"));
        }

        for (int i = 0; i < entetiesInfo.length; i++) {
            int current = 0;
            while (true) {
                HibernateSessionWrapper sessionWrapper = doGetHibernateSessionWrapper();
                try {
                    sessionWrapper.open();
                    final List values = doGetObjects(entetiesInfo[i], current, fetchCount, sessionWrapper);
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("Indexing entity [" + entetiesInfo[i].entityname + "] range ["
                                + current + "-" + (current + fetchCount) + "]"));
                    }
                    current += fetchCount;
                    for (Iterator it = values.iterator(); it.hasNext();) {
                        session.create(it.next());
                    }
                    session.evictAll();
                    sessionWrapper.close();
                    if (values.size() < fetchCount) {
                        break;
                    }
                } catch (Exception e) {
                    log.error(buildMessage("Failed to index the database"), e);
                    sessionWrapper.closeOnError();
                    if (!(e instanceof HibernateGpsDeviceException)) {
                        throw new HibernateGpsDeviceException(buildMessage("Failed to index the database"), e);
                    }
                    throw (HibernateGpsDeviceException) e;
                }
            }
        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Finished indexing the database"));
        }
    }

    /**
     * Returns all the hibernate entity info. Called when the device starts up.
     *
     * @return Hibernate class informtion
     * @throws HibernateGpsDeviceException
     */
    protected abstract HibernateEntityInfo[] doGetHibernateEntitiesInfo() throws HibernateGpsDeviceException;

    /**
     * Returns the data that maps to the given class info, paginated with from
     * and count.
     */
    protected abstract List doGetObjects(HibernateEntityInfo info, int from, int count,
                                         HibernateSessionWrapper sessionWrapper) throws HibernateGpsDeviceException;

    protected abstract HibernateSessionWrapper doGetHibernateSessionWrapper();
}
