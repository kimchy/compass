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

package org.compass.gps.device.jdo;

import java.util.Collection;
import java.util.Iterator;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.compass.core.CompassSession;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;
import org.compass.gps.device.AbstractGpsDevice;

/**
 * A JDO device, provides support for using jdo and jdo mapping files to index a
 * database. The path can be views as: Database <-> JDO <-> Objects <->
 * Compass::Gps <-> Compass::Core (Search Engine). What it means is that for
 * every object that has both jdo and compass mappings, you will be able to
 * index it's data, as well as real time mirroring of data changes.
 * <p/>
 * The <code>persistenceManagerFactory</code> must be set in order to perform
 * the index operation.
 * <p/>
 * Note: Real time monitoring is only supported with JDO 2, please see
 * {@link Jdo2GpsDevice} for more details.
 *
 * @author kimchy
 */
public class JdoGpsDevice extends AbstractGpsDevice {

    protected PersistenceManagerFactory persistenceManagerFactory;

    public JdoGpsDevice() {

    }

    public JdoGpsDevice(String name, PersistenceManagerFactory persistenceManagerFactory) {
        setName(name);
        this.persistenceManagerFactory = persistenceManagerFactory;
    }

    protected void doStart() throws CompassGpsException {
        if (persistenceManagerFactory == null) {
            throw new IllegalArgumentException(buildMessage("persistenceManagerFactory must be set"));
        }
    }

    protected void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException {
        // TODO take into account the index plan
        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database"));
        }
        // TODO is there a meta data option in jdo so we can get the persistent
        // classes (similar to hibernate) instead of exception level handling
        // based on the OSEM mappings?
        final ResourceMapping[] resourceMappings = ((InternalCompass) compassGps.getIndexCompass()).getMapping().getRootMappings();

        for (int i = 0; i < resourceMappings.length; i++) {
            if (!(resourceMappings[i] instanceof ClassMapping)) {
                continue;
            }

            final ClassMapping classMapping = (ClassMapping) resourceMappings[i];
            final Class clazz = classMapping.getClazz();
            if (isFilteredForIndex(clazz.getName())) {
                continue;
            }
            PersistenceManager pm = persistenceManagerFactory.getPersistenceManager();
            Transaction tr = pm.currentTransaction();
            try {
                tr.begin();
                Collection datas = null;
                try {
                    Query query = pm.newQuery(clazz);
                    datas = (Collection) query.execute();
                } catch (Exception e) {
                    // no mapping for the class in jdo
                }
                if (datas == null || datas.size() == 0) {
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Indexing alias [" + classMapping.getAlias()
                            + "] with object count [" + datas.size() + "]"));
                }
                for (Iterator it = datas.iterator(); it.hasNext();) {
                    session.create(it.next());
                }
                tr.commit();
            } catch (Exception e) {
                log.error(buildMessage("Failed to index the database"), e);
                throw new JdoGpsDeviceException(buildMessage("Failed to index the database"), e);
            } finally {
                if (tr.isActive()) {
                    tr.rollback();
                }
                pm.close();
            }
        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Finished indexing the database"));
        }
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return persistenceManagerFactory;
    }

    public void setPersistenceManagerFactory(PersistenceManagerFactory persistenceManagerFactory) {
        this.persistenceManagerFactory = persistenceManagerFactory;
    }
}
