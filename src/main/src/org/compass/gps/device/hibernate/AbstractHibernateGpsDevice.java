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

    protected static class HibernateClassInfo {
        public String entityname;

        public int count;
    }

    protected static interface HibernateSessionWrapper {
        void open() throws HibernateGpsDeviceException;

        void close();

        void closeOnError();
    }

    protected int fetchCount = 200;

    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    protected void doStart() throws CompassGpsException {
    }

    /**
     * Indexes all the data that has hibernate mapping and compass mapping
     * associated with it.
     */
    protected void doIndex(CompassSession session) throws CompassException {
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database with fetch count [" + fetchCount + "]"));
        }

        final HibernateClassInfo[] infos = doGetHibernateClassesInfo();

        for (int i = 0; i < infos.length; i++) {
            int current = 0;
            while (current < infos[i].count) {
                HibernateSessionWrapper sessionWrapper = doGetHibernateSessionWrapper();
                try {
                    sessionWrapper.open();
                    final List values = doGetObjects(infos[i], current, fetchCount, sessionWrapper);
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("Indexing entities [" + infos[i].entityname + "] range ["
                                + current + "-" + (current + fetchCount) + "] out of total [" + infos[i].count
                                + "]"));
                    }
                    current += fetchCount;
                    for (Iterator it = values.iterator(); it.hasNext();) {
                        session.create(it.next());
                    }
                    session.evictAll();
                    sessionWrapper.close();
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
     * Returns all the hibernate class info (the hibernate mapped classes and
     * their count).
     *
     * @return
     * @throws HibernateGpsDeviceException
     */
    protected abstract HibernateClassInfo[] doGetHibernateClassesInfo() throws HibernateGpsDeviceException;

    /**
     * Returns the data that maps to the given class info, paginated with from
     * and count.
     *
     * @param info
     * @param from
     * @param count
     * @return
     * @throws HibernateGpsDeviceException
     */
    protected abstract List doGetObjects(HibernateClassInfo info, int from, int count,
                                         HibernateSessionWrapper sessionWrapper) throws HibernateGpsDeviceException;

    protected abstract HibernateSessionWrapper doGetHibernateSessionWrapper();
}
