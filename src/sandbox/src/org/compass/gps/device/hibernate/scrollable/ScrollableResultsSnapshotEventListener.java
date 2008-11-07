/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.gps.device.hibernate.scrollable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassCallbackWithoutResult;
import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.spi.InternalCompassSession;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.scrollable.snapshot.ConfigureSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.CreateAndUpdateSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.DeleteSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateAliasRowSnapshot;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshotEventListener;
import org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.spi.CompassGpsInterfaceDevice;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

/**
 * A
 * {@link org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshotEventListener}
 * that works with
 * {@link org.compass.gps.device.hibernate.scrollable.Hibernate3ScrollableResultsGpsDevice} and
 * performs the changes to the compass index after the change snapshots have
 * been detected by the device.
 *
 * @author kimchy
 */
public class ScrollableResultsSnapshotEventListener implements HibernateSnapshotEventListener {

    private static Log log = LogFactory.getLog(ScrollableResultsSnapshotEventListener.class);

    private HashMap createAndUpdateQueries;

    public void configure(ConfigureSnapshotEvent configureSnapshotEvent) throws HibernateGpsDeviceException {
        createAndUpdateQueries = new HashMap();
        for (Iterator it = configureSnapshotEvent.getMappings().iterator(); it.hasNext();) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
            if (!mapping.supportsVersioning()) {
                continue;
            }
            // TODO If there is only one id, need to check if select ID ... IN
            // () is faster, need also to find how to do it in HQL
            StringBuffer sb = new StringBuffer();
            String selectQuery = mapping.getSelectQuery();
            sb.append(selectQuery);
            if (selectQuery.indexOf(" where") != -1) {
                sb.append(" and (");
            } else {
                sb.append(" where (");
            }
            boolean first = true;
            for (Iterator idIt = mapping.idMappingsIt(); idIt.hasNext();) {
                IdColumnToPropertyMapping idMapping = (IdColumnToPropertyMapping) idIt.next();
                if (idMapping.getColumnNameForVersion() == null) {
                    throw new IllegalArgumentException("Id Mapping " + idMapping
                            + " must have column name for versioning."
                            + " If you set the column index, you must set the version as well");
                }
                if (first) {
                    first = false;
                } else {
                    sb.append(" and ");
                }
                //Note that columnNameForVersion should contain a 'property' expression, not column name!
                sb.append(idMapping.getColumnNameForVersion());
                sb.append(" = ?");
            }
            sb.append(")");
            String query = sb.toString();
            if (log.isDebugEnabled()) {
                log.debug("Using create/update query [" + query + "] for alias [" + mapping.getAlias() + "]");
            }
            createAndUpdateQueries.put(mapping.getAlias(), query);
        }
    }

    public void onDelete(final DeleteSnapshotEvent deleteSnapshotEvent) throws HibernateGpsDeviceException {
        final ResultSetToResourceMapping mapping = deleteSnapshotEvent.getMapping();
        CompassGpsInterfaceDevice compassGps = deleteSnapshotEvent.getCompassGps();
        compassGps.executeForMirror(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Iterator it = deleteSnapshotEvent.getDeleteSnapshots().iterator(); it.hasNext();) {
                    HibernateAliasRowSnapshot rowSnapshot = (HibernateAliasRowSnapshot) it.next();
                    List ids = rowSnapshot.getIds();
                    if (ids.size() == 1) {
                        session.delete(mapping.getAlias(), ids.get(0));
                    } else {
                        Object[] idsArr = ids.toArray();
                        session.delete(mapping.getAlias(), idsArr);
                    }
                }
            }
        });
    }

    public void onCreateAndUpdate(final CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent)
            throws HibernateGpsDeviceException {
        doCreateAndUpdateFor(createAndUpdateSnapshotEvent.getCreateSnapshots(), createAndUpdateSnapshotEvent, true);
        doCreateAndUpdateFor(createAndUpdateSnapshotEvent.getUpdateSnapshots(), createAndUpdateSnapshotEvent, false);
    }

    private void doCreateAndUpdateFor(final List snapshots,
                                      final CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent, final boolean useCreate)
            throws HibernateGpsDeviceException {
        final ResultSetToResourceMapping mapping = createAndUpdateSnapshotEvent.getMapping();
        CompassGpsInterfaceDevice compassGps = createAndUpdateSnapshotEvent.getCompassGps();
        compassGps.executeForMirror(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                String query = (String) createAndUpdateQueries.get(mapping.getAlias());

                try {
                    Session s = createAndUpdateSnapshotEvent.getSession();
                    Query hibernateQuery = s.createQuery(query);
                    for (Iterator it = snapshots.iterator(); it.hasNext();) {
                        HibernateAliasRowSnapshot rowSnapshot = (HibernateAliasRowSnapshot) it.next();
                        Resource resource = ((InternalCompassSession) session).getCompass().getResourceFactory().createResource(mapping.getAlias());
                        Hibernate3ScrollableResultsRowMarshallHelper marshallHelper = new Hibernate3ScrollableResultsRowMarshallHelper(mapping, session,
                                resource);
                        //XXX: clearParameters of hibernateQuery?
                        List ids = rowSnapshot.getIds();
                        for (int i = 0; i < ids.size(); i++) {
                            Object idValue = ids.get(i);
                            hibernateQuery.setParameter(i, idValue);
                        }

                        String[] returnAliases = hibernateQuery.getReturnAliases();
                        ScrollableResults rs = hibernateQuery.scroll(ScrollMode.FORWARD_ONLY);
                        if (!rs.next()) {
                            // it was deleted between the calls, do nothing
                            continue;
                        }
                        rs.close();

                        marshallHelper.marshallResultSet(rs, returnAliases);
                        if (useCreate) {
                            session.create(resource);
                        } else {
                            session.save(resource);
                        }
                        session.evictAll();
                    }
                } catch (Exception e) {
                    throw new HibernateGpsDeviceException("Failed to execute query for create/update", e);
                } finally {
                    //TODO: close Session?
                    // maybe keeping Session in the events is not a good idea
                    // -> keep the SessionWrapper in event?
                    // or close session somewhere else
                    //(session will also be closed on committing the transaction)
                }
            }
        });
    }
}
