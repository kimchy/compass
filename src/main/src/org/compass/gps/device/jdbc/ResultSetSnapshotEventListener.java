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

package org.compass.gps.device.jdbc;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.compass.gps.device.jdbc.dialect.JdbcDialect;
import org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.device.jdbc.snapshot.ConfigureSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.CreateAndUpdateSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.DeleteSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener;
import org.compass.gps.spi.CompassGpsInterfaceDevice;

/**
 * A
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcSnapshotEventListener}
 * that works with
 * {@link org.compass.gps.device.jdbc.ResultSetJdbcGpsDevice} and
 * performs the changes to the compass index after the change snapshots have
 * been detected by the device.
 *
 * @author kimchy
 */
public class ResultSetSnapshotEventListener implements JdbcSnapshotEventListener {

    private static Log log = LogFactory.getLog(ResultSetSnapshotEventListener.class);

    private HashMap<String, String> createAndUpdateQueries;

    public void configure(ConfigureSnapshotEvent configureSnapshotEvent) throws JdbcGpsDeviceException {
        createAndUpdateQueries = new HashMap<String, String>();
        for (Iterator it = configureSnapshotEvent.getMappings().iterator(); it.hasNext();) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
            if (!mapping.supportsVersioning()) {
                continue;
            }
            // TODO If there is only one id, need to check if select ID ... IN
            // () is faster, need also to find how to do it in JDBC
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

    public void onDelete(final DeleteSnapshotEvent deleteSnapshotEvent) throws JdbcGpsDeviceException {
        final ResultSetToResourceMapping mapping = deleteSnapshotEvent.getMapping();
        CompassGpsInterfaceDevice compassGps = deleteSnapshotEvent.getCompassGps();
        compassGps.executeForMirror(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                for (Iterator it = deleteSnapshotEvent.getDeleteSnapshots().iterator(); it.hasNext();) {
                    JdbcAliasRowSnapshot rowSnapshot = (JdbcAliasRowSnapshot) it.next();
                    List ids = rowSnapshot.getIds();
                    if (ids.size() == 1) {
                        session.delete(mapping.getAlias(), ids.get(0));
                    } else {
                        String[] idsArr = (String[]) ids.toArray(new String[ids.size()]);
                        session.delete(mapping.getAlias(), (Object) idsArr);
                    }
                }
            }
        });
    }

    public void onCreateAndUpdate(final CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent)
            throws JdbcGpsDeviceException {
        doCreateAndUpdateFor(createAndUpdateSnapshotEvent.getCreateSnapshots(), createAndUpdateSnapshotEvent, true);
        doCreateAndUpdateFor(createAndUpdateSnapshotEvent.getUpdateSnapshots(), createAndUpdateSnapshotEvent, false);
    }

    private void doCreateAndUpdateFor(final List snapshots,
                                      final CreateAndUpdateSnapshotEvent createAndUpdateSnapshotEvent, final boolean useCreate)
            throws JdbcGpsDeviceException {
        final ResultSetToResourceMapping mapping = createAndUpdateSnapshotEvent.getMapping();
        final JdbcDialect dialect = createAndUpdateSnapshotEvent.getDialect();
        CompassGpsInterfaceDevice compassGps = createAndUpdateSnapshotEvent.getCompassGps();
        compassGps.executeForMirror(new CompassCallbackWithoutResult() {
            protected void doInCompassWithoutResult(CompassSession session) throws CompassException {
                String query = createAndUpdateQueries.get(mapping.getAlias());
                PreparedStatement ps = null;
                try {
                    ps = createAndUpdateSnapshotEvent.getConnection().prepareStatement(query);
                    for (Iterator it = snapshots.iterator(); it.hasNext();) {
                        JdbcAliasRowSnapshot rowSnapshot = (JdbcAliasRowSnapshot) it.next();
                        Resource resource = ((InternalCompassSession) session).getCompass().getResourceFactory().createResource(mapping.getAlias());
                        ResultSetRowMarshallHelper marshallHelper = new ResultSetRowMarshallHelper(mapping, session,
                                dialect, resource);
                        ps.clearParameters();
                        List ids = rowSnapshot.getIds();
                        for (int i = 0; i < ids.size(); i++) {
                            String idValue = (String) ids.get(i);
                            dialect.setParameter(ps, i + 1, idValue);
                        }
                        ResultSet rs = ps.executeQuery();
                        if (!rs.next()) {
                            // it was deleted between the calls, do nothing
                            continue;
                        }
                        marshallHelper.marshallResultSet(rs);
                        if (useCreate) {
                            session.create(resource);
                        } else {
                            session.save(resource);
                        }
                        session.evictAll();
                    }
                } catch (SQLException e) {
                    throw new JdbcGpsDeviceException("Failed to execute query for create/update", e);
                } finally {
                    JdbcUtils.closeStatement(ps);
                }
            }
        });
    }
}
