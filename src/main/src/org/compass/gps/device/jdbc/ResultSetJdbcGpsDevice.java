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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.mapping.Cascade;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.gps.CompassGpsException;
import org.compass.gps.IndexPlan;
import org.compass.gps.device.jdbc.mapping.AutoGenerateMapping;
import org.compass.gps.device.jdbc.mapping.ColumnMapping;
import org.compass.gps.device.jdbc.mapping.ColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.compass.gps.device.jdbc.snapshot.ConfigureSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.CreateAndUpdateSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.DeleteSnapshotEvent;
import org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot;
import org.compass.gps.device.jdbc.snapshot.JdbcAliasSnapshot;
import org.compass.gps.device.jdbc.snapshot.JdbcSnapshot;

/**
 * A gps device that index a jdbc <code>ResultSet</code> to a set of Compass
 * <code>Resource</code>s. Each <code>Resource</code> maps to a
 * <code>ResultSet</code> row. The device can handle multiple
 * <code>ResultSet</code>s.
 * <p>
 * The device holds a list of
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping}s
 * (or derived classes like
 * {@link org.compass.gps.device.jdbc.mapping.TableToResourceMapping}).
 * Each one has all the required mappings setting to map the
 * <code>ResultSet</code> with all it's rows to the set of corresponding
 * <code>Resource</code>s.
 * <p>
 * The device can perform active data base mirroring. The mirror operation is
 * enabled only if the mirror flag is enabled, and will execute against each
 * mapping that
 * {@link org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping#supportsVersioning()}.
 * <p>
 * The <code>autoDetectVersionColumnSqlType</code> setting (which defauls to
 * <code>true</code>) will automatically set the version column jdbc type for
 * mappings that support versioning.
 *
 * @author kimchy
 * @see org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping
 * @see org.compass.gps.device.jdbc.mapping.TableToResourceMapping
 */
public class ResultSetJdbcGpsDevice extends AbstractJdbcActiveMirrorGpsDevice {

    protected List mappings = new ArrayList();

    private JdbcSnapshot snapshot;

    private boolean autoDetectVersionColumnSqlType = true;

    protected JdbcSnapshot getJdbcSnapshot() {
        return this.snapshot;
    }

    /**
     * performs operations on startup, such as auto generation of mappings for
     * mappings that implement the {@link AutoGenerateMapping}, auto detection
     * of version column jdbc type, and {@link JdbcSnapshot} loading (using the
     * {@link org.compass.gps.device.jdbc.snapshot.JdbcSnapshotPersister}).
     */
    protected void doStart() throws CompassGpsException {
        super.doStart();
        // call auto generate for mappings that implement the AutoGenerate
        // interface
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping rsMapping = (ResultSetToResourceMapping) it.next();
            if (rsMapping instanceof AutoGenerateMapping) {
                ((AutoGenerateMapping) rsMapping).generateMappings(dataSource);
            }
        }
        // support for meta data lookup
        CommonMetaDataLookup commonMetaDataLookup = new CommonMetaDataLookup(((InternalCompass) compassGps
                .getIndexCompass()).getMetaData());
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping rsMapping = (ResultSetToResourceMapping) it.next();
            rsMapping.setAlias(commonMetaDataLookup.lookupAliasName(rsMapping.getAlias()));
            for (Iterator it1 = rsMapping.mappingsIt(); it1.hasNext();) {
                List columns = (List) it1.next();
                for (Iterator it2 = columns.iterator(); it2.hasNext();) {
                    ColumnMapping columnMapping = (ColumnMapping) it2.next();
                    if (columnMapping instanceof ColumnToPropertyMapping) {
                        ColumnToPropertyMapping columnToPropertyMapping = (ColumnToPropertyMapping) columnMapping;
                        columnToPropertyMapping.setPropertyName(commonMetaDataLookup
                                .lookupMetaDataName(columnToPropertyMapping.getPropertyName()));
                    }
                }
            }
        }
        // double check that all the result set mapping have Compass::Core
        // resource mapping
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping rsMapping = (ResultSetToResourceMapping) it.next();
            if (!compassGps.hasMappingForEntityForMirror(rsMapping.getAlias(), Cascade.ALL)) {
                throw new IllegalStateException(
                        buildMessage("No resource mapping defined in gps mirror compass for alias ["
                                + rsMapping.getAlias() + "]. Did you defined a jdbc mapping builder?"));
            }
            if (!compassGps.hasMappingForEntityForIndex(rsMapping.getAlias())) {
                throw new IllegalStateException(
                        buildMessage("No resource mapping defined in gps index compass for alias ["
                                + rsMapping.getAlias() + "]. Did you defined a jdbc mapping builder?"));
            }
        }
        if (isAutoDetectVersionColumnSqlType()) {
            if (log.isInfoEnabled()) {
                log.info(buildMessage("Auto detecting version column sql types"));
            }
            // set the version databse type
            Connection connection = JdbcUtils.getConnection(dataSource);
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                for (Iterator it = mappings.iterator(); it.hasNext();) {
                    ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
                    if (!mapping.supportsVersioning()) {
                        continue;
                    }
                    ps = connection.prepareStatement(mapping.getVersionQuery());
                    ps.setFetchSize(1);
                    rs = ps.executeQuery();
                    ResultSetMetaData metaData = rs.getMetaData();
                    for (Iterator verIt = mapping.versionMappingsIt(); verIt.hasNext();) {
                        VersionColumnMapping versionMapping = (VersionColumnMapping) verIt.next();
                        int columnIndex;
                        if (versionMapping.isUsingColumnIndex()) {
                            columnIndex = versionMapping.getColumnIndex();
                        } else {
                            columnIndex = JdbcUtils.getColumnIndexFromColumnName(metaData, versionMapping
                                    .getColumnName());
                        }
                        versionMapping.setSqlType(metaData.getColumnType(columnIndex));
                    }
                }
            } catch (SQLException e) {
                throw new JdbcGpsDeviceException(buildMessage("Failed to find version column type"), e);
            } finally {
                JdbcUtils.closeResultSet(rs);
                JdbcUtils.closeStatement(ps);
                JdbcUtils.closeConnection(connection);
            }
        }
        if (isMirrorDataChanges()) {
            if (log.isInfoEnabled()) {
                log.info(buildMessage("Using mirroring, loading snapshot data"));
            }
            // set up the snapshot
            snapshot = getSnapshotPersister().load();
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
                if (mapping.supportsVersioning() && snapshot.getAliasSnapshot(mapping.getAlias()) == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("Alias [" + mapping.getAlias() + "] not found in snapshot data, creating..."));
                    }
                    JdbcAliasSnapshot aliasSnapshot = new JdbcAliasSnapshot(mapping.getAlias());
                    snapshot.putAliasSnapshot(aliasSnapshot);
                }
            }
            // configure the snapshot event listener
            Connection connection = JdbcUtils.getConnection(dataSource);
            try {
                getSnapshotEventListener().configure(new ConfigureSnapshotEvent(connection, dialect, mappings));
            } finally {
                JdbcUtils.closeConnection(connection);
            }
        }
        if (log.isDebugEnabled()) {
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                log.debug(buildMessage("Using DB Mapping " + it.next()));
            }
        }
    }

    /**
     * Saves the {@link JdbcSnapshot}.
     */
    protected void doStop() throws CompassGpsException {
        getSnapshotPersister().save(snapshot);
        super.doStop();
    }

    protected void doIndex(CompassSession session, IndexPlan indexPlan) throws CompassGpsException {
        // TODO take into account the index plan
        // reset the snapshot data before we perform the index operation
        snapshot = new JdbcSnapshot();
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
            if (mapping.supportsVersioning()) {
                JdbcAliasSnapshot aliasSnapshot = new JdbcAliasSnapshot(mapping.getAlias());
                snapshot.putAliasSnapshot(aliasSnapshot);
            }
        }
        super.doIndex(session);
        // save the sanpshot data
        getSnapshotPersister().save(snapshot);
    }

    /**
     * Returns the array of index execution with a size of the number of
     * mappings.
     */
    protected IndexExecution[] doGetIndexExecutions(Connection connection) throws SQLException, JdbcGpsDeviceException {
        IndexExecution[] indexExecutions = new IndexExecution[mappings.size()];
        for (int i = 0; i < indexExecutions.length; i++) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) mappings.get(i);
            indexExecutions[i] = new IndexExecution(mapping, mapping.getSelectQuery());
        }
        return indexExecutions;
    }

    /**
     * Index the given <code>ResultSet</code> row into a Compass
     * <code>Resource</code>.
     */
    protected Object processRowValue(Object description, ResultSet rs, CompassSession session) throws SQLException,
            CompassException {

        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append(buildMessage("Indexing data row with values "));
            ResultSetMetaData metaData = rs.getMetaData();
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                sb.append("[").append(metaData.getColumnName(i)).append(":");
                String value = rs.getString(i);
                if (rs.wasNull()) {
                    value = "(null)";
                }
                sb.append(value);
                sb.append("] ");
            }
            log.debug(sb.toString());
        }

        ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) description;

        JdbcAliasRowSnapshot rowSnapshot = null;
        if (shouldMirrorDataChanges() && mapping.supportsVersioning()) {
            rowSnapshot = new JdbcAliasRowSnapshot();
        }
        Resource resource = ((InternalCompassSession) session).getCompass().getResourceFactory().createResource(mapping.getAlias());
        ResultSetRowMarshallHelper marshallHelper = new ResultSetRowMarshallHelper(mapping, session, dialect, resource,
                rowSnapshot);
        marshallHelper.marshallResultSet(rs);

        if (shouldMirrorDataChanges() && mapping.supportsVersioning()) {
            snapshot.getAliasSnapshot(mapping.getAlias()).putRow(rowSnapshot);
        }

        return resource;
    }

    /**
     * Performs the data change mirroring operation.
     */
    public synchronized void performMirroring() throws JdbcGpsDeviceException {
        if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
            return;
        }
        if (snapshot == null) {
            throw new IllegalStateException(
                    buildMessage("Versioning data was not properly initialized, did you index the device or loaded the data?"));
        }
        Connection connection = JdbcUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean dirtySnapshot = false;
        try {
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
                if (!mapping.supportsVersioning()) {
                    continue;
                }
                JdbcAliasSnapshot oldAliasSnapshot = snapshot.getAliasSnapshot(mapping.getAlias());
                if (oldAliasSnapshot == null) {
                    log.warn(buildMessage("No snapshot for alias [" + mapping.getAlias()
                            + "] even though there should be support for versioning ignoring the alias"));
                    continue;
                }
                JdbcAliasSnapshot newAliasSnapshot = new JdbcAliasSnapshot(mapping.getAlias());
                ArrayList createdRows = new ArrayList();
                ArrayList updatedRows = new ArrayList();
                ArrayList deletedRows = new ArrayList();
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Executing version query [" + mapping.getVersionQuery() + "]"));
                }
                ps = connection.prepareStatement(mapping.getVersionQuery());
                if (getFetchSize() > 0) {
                    ps.setFetchSize(getFetchSize());
                }
                rs = ps.executeQuery();
                while (rs.next()) {

                    if (log.isDebugEnabled()) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(buildMessage("Version row with values "));
                        ResultSetMetaData metaData = rs.getMetaData();
                        for (int i = 1; i <= metaData.getColumnCount(); i++) {
                            sb.append("[").append(metaData.getColumnName(i)).append(":");
                            String value = rs.getString(i);
                            if (rs.wasNull()) {
                                value = "(null)";
                            }
                            sb.append(value);
                            sb.append("] ");
                        }
                        log.debug(sb.toString());
                    }

                    JdbcAliasRowSnapshot newRowSnapshot = new JdbcAliasRowSnapshot();
                    ResultSetRowMarshallHelper marshallHelper = new ResultSetRowMarshallHelper(mapping, dialect,
                            newRowSnapshot, compassGps.getMirrorCompass());
                    marshallHelper.marshallResultSet(rs);

                    // new and old have the same ids
                    JdbcAliasRowSnapshot oldRowSnapshot = oldAliasSnapshot.getRow(newRowSnapshot);

                    // new row or updated row
                    if (oldRowSnapshot == null) {
                        createdRows.add(newRowSnapshot);
                    } else if (oldRowSnapshot.isOlderThan(newRowSnapshot)) {
                        updatedRows.add(newRowSnapshot);
                    }

                    newAliasSnapshot.putRow(newRowSnapshot);
                }
                for (Iterator oldRowIt = oldAliasSnapshot.rowSnapshotIt(); oldRowIt.hasNext();) {
                    JdbcAliasRowSnapshot tmpRow = (JdbcAliasRowSnapshot) oldRowIt.next();
                    // deleted row
                    if (newAliasSnapshot.getRow(tmpRow) == null) {
                        deletedRows.add(tmpRow);
                    }
                }
                if (!createdRows.isEmpty() || !updatedRows.isEmpty()) {
                    dirtySnapshot = true;
                    getSnapshotEventListener().onCreateAndUpdate(
                            new CreateAndUpdateSnapshotEvent(connection, dialect, mapping, createdRows, updatedRows,
                                    compassGps));
                }
                if (!deletedRows.isEmpty()) {
                    dirtySnapshot = true;
                    getSnapshotEventListener().onDelete(
                            new DeleteSnapshotEvent(connection, dialect, mapping, deletedRows, compassGps));
                }
                snapshot.putAliasSnapshot(newAliasSnapshot);
            }
        } catch (SQLException e) {
            throw new JdbcGpsDeviceException(buildMessage("Failed while mirroring data changes"), e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(connection);
        }
        if (isSaveSnapshotAfterMirror() && dirtySnapshot) {
            getSnapshotPersister().save(snapshot);
        }
    }

    /**
     * Adds a mapping to be indexed and mirrored.
     */
    public void addMapping(ResultSetToResourceMapping mapping) {
        this.mappings.add(mapping);
    }

    /**
     * Adds an array of mappings to be indexed and mirrored.
     */
    public void setMappings(ResultSetToResourceMapping[] mappingsArr) {
        for (int i = 0; i < mappingsArr.length; i++) {
            addMapping(mappingsArr[i]);
        }
    }

    /**
     * Should the device auto detect the version columns jdbc type.
     */
    public boolean isAutoDetectVersionColumnSqlType() {
        return autoDetectVersionColumnSqlType;
    }

    /**
     * Sets if the device auto detect the version columns jdbc type.
     */
    public void setAutoDetectVersionColumnSqlType(boolean autoDetectVersionColumnSqlType) {
        this.autoDetectVersionColumnSqlType = autoDetectVersionColumnSqlType;
    }
}
