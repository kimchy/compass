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

package org.compass.gps.device.hibernate.scrollable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.compass.core.CompassException;
import org.compass.core.CompassSession;
import org.compass.core.Resource;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.gps.ActiveMirrorGpsDevice;
import org.compass.gps.CompassGpsException;
import org.compass.gps.device.AbstractGpsDevice;
import org.compass.gps.device.hibernate.HibernateGpsDeviceException;
import org.compass.gps.device.hibernate.scrollable.snapshot.ConfigureSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.CreateAndUpdateSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.DeleteSnapshotEvent;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateAliasRowSnapshot;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateAliasSnapshot;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshot;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshotEventListener;
import org.compass.gps.device.hibernate.scrollable.snapshot.HibernateSnapshotPersister;
import org.compass.gps.device.hibernate.scrollable.snapshot.RAMHibernateSnapshotPersister;
import org.compass.gps.device.jdbc.AbstractJdbcGpsDevice.IndexExecution;
import org.compass.gps.device.jdbc.mapping.ColumnMapping;
import org.compass.gps.device.jdbc.mapping.ColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.hibernate.CacheMode;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;


public class Hibernate3ScrollableResultsGpsDevice extends AbstractGpsDevice
        implements ActiveMirrorGpsDevice {

    protected List mappings = new ArrayList();

    private boolean autoDetectVersionColumnSqlType = true;

    private SessionFactory sessionFactory;

    private boolean mirrorDataChanges;

    private HibernateSnapshot snapshot;

    private HibernateSnapshotPersister snapshotPersister = new RAMHibernateSnapshotPersister();

    private HibernateSnapshotEventListener snapshotEventListener = new ScrollableResultsSnapshotEventListener();

    private boolean saveSnapshotAfterMirror = false;


    protected static interface HibernateSessionWrapper {
        void open() throws HibernateGpsDeviceException;

        void close();

        void closeOnError();
    }

    protected int fetchCount = 200;

    public Hibernate3ScrollableResultsGpsDevice() {
        super();
    }


    public Hibernate3ScrollableResultsGpsDevice(String name,
                                                SessionFactory sessionFactory) {
        setName(name);
        this.sessionFactory = sessionFactory;
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setFetchCount(int fetchCount) {
        this.fetchCount = fetchCount;
    }

    public boolean isMirrorDataChanges() {
        return mirrorDataChanges;
    }

    public void setMirrorDataChanges(boolean mirrorDataChanges) {
        this.mirrorDataChanges = mirrorDataChanges;
    }


    protected void doStart() throws CompassGpsException {
        super.doStart();
        // support for meta data lookup
        CommonMetaDataLookup commonMetaDataLookup = new CommonMetaDataLookup(
                ((InternalCompass) compassGps.getIndexCompass()).getMetaData());
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping rsMapping = (ResultSetToResourceMapping) it.next();
            rsMapping.setAlias(commonMetaDataLookup.lookupAliasName(rsMapping.getAlias()));
            for (Iterator it1 = rsMapping.mappingsIt(); it1.hasNext();) {
                List columns = (List) it1.next();
                for (Iterator it2 = columns.iterator(); it2.hasNext();) {
                    ColumnMapping columnMapping = (ColumnMapping) it2.next();
                    if (columnMapping instanceof ColumnToPropertyMapping) {
                        ColumnToPropertyMapping columnToPropertyMapping = (ColumnToPropertyMapping) columnMapping;

                        columnToPropertyMapping.setPropertyName(
                                commonMetaDataLookup.lookupMetaDataName(columnToPropertyMapping.getPropertyName()));
                    }
                }
            }
        }
        // double check that all the result set mapping have Compass::Core
        // resource mapping
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping rsMapping = (ResultSetToResourceMapping) it
                    .next();
            if (!compassGps.hasMappingForEntityForMirror(rsMapping.getAlias(), CascadeMapping.Cascade.ALL)) {
                throw new IllegalStateException(
                        buildMessage("No resource mapping defined in gps mirror compass for alias ["
                                + rsMapping.getAlias()
                                + "]. Did you defined a jdbc mapping builder?"));
            }
            if (!compassGps.hasMappingForEntityForIndex(rsMapping.getAlias())) {
                throw new IllegalStateException(
                        buildMessage("No resource mapping defined in gps index compass for alias ["
                                + rsMapping.getAlias()
                                + "]. Did you defined a jdbc mapping builder?"));
            }
        }

        if (isMirrorDataChanges()) {
            if (log.isInfoEnabled()) {
                log.info(buildMessage("Using mirroring, loading snapshot data"));
            }
            // set up the snapshot
            snapshot = getSnapshotPersister().load();
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                ResultSetToResourceMapping mapping = (ResultSetToResourceMapping)
                        it.next();
                if (mapping.supportsVersioning() &&
                        snapshot.getAliasSnapshot(mapping.getAlias()) == null) {
                    if (log.isDebugEnabled()) {
                        log.debug(buildMessage("Alias [" + mapping.getAlias() + "] not found in snapshot data, creating..."));
                    }
                    HibernateAliasSnapshot aliasSnapshot = new HibernateAliasSnapshot(mapping.getAlias());
                    snapshot.putAliasSnapshot(aliasSnapshot);
                }
            }
            // configure the snapshot event listener
            //TODO: hibernate session is not needed for ConfigureSnapshotEvent
            getSnapshotEventListener().configure(new ConfigureSnapshotEvent(null, mappings));

        }

        if (log.isDebugEnabled()) {
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                log.debug(buildMessage("Using DB Mapping " + it.next()));
            }
        }
    }

    protected void doStop() throws CompassGpsException {
        getSnapshotPersister().save(snapshot);
        super.doStop();
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
    public void setAutoDetectVersionColumnSqlType(
            boolean autoDetectVersionColumnSqlType) {
        this.autoDetectVersionColumnSqlType = autoDetectVersionColumnSqlType;
    }

    /**
     * Returns the array of index execution with a size of the number of
     * mappings.
     */
    protected IndexExecution[] doGetIndexExecutions() {
        IndexExecution[] indexExecutions = new IndexExecution[mappings.size()];
        for (int i = 0; i < indexExecutions.length; i++) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) mappings
                    .get(i);
            indexExecutions[i] = new IndexExecution(mapping, mapping
                    .getSelectQuery());
        }
        return indexExecutions;
    }

    /**
     * Indexes the data
     */
    protected void doIndex(CompassSession session) throws CompassException {
        // reset the snapshot data before we perform the index operation
        snapshot = new HibernateSnapshot();
        for (Iterator it = mappings.iterator(); it.hasNext();) {
            ResultSetToResourceMapping mapping = (ResultSetToResourceMapping)
                    it.next();
            if (mapping.supportsVersioning()) {
                HibernateAliasSnapshot aliasSnapshot = new
                        HibernateAliasSnapshot(mapping.getAlias());
                snapshot.putAliasSnapshot(aliasSnapshot);
            }
        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Indexing the database with fetch count ["
                    + fetchCount + "]"));
        }

        IndexExecution[] indexExecutions = doGetIndexExecutions();
        for (int i = 0; i != indexExecutions.length; i++) {
            IndexExecution indexExecution = indexExecutions[i];

            HibernateSessionWrapper sessionWrapper = doGetHibernateSessionWrapper();

            try {
                sessionWrapper.open();

                Session hibernateSession = ((Hibernate3SessionWrapper) sessionWrapper).getSession();

                String queryString = indexExecution.getStatementQuery();

                if (log.isDebugEnabled()) {
                    log.debug("queryString: " + queryString);
                }

                Query query = hibernateSession.createQuery(queryString)
                        .setCacheMode(CacheMode.IGNORE);
                String[] returnAliases = query.getReturnAliases();

                ScrollableResults rs = query.scroll(ScrollMode.FORWARD_ONLY);
                int count = 0;
                while (rs.next()) {
                    processRow(indexExecution.getDescription(), rs, returnAliases, session);
                    if (++count % fetchCount == 0) {
                        // release memory
                        hibernateSession.flush();
                        hibernateSession.clear();
                    }
                }
                rs.close();

            } catch (Exception e) {
                log.error(buildMessage("Failed to index the database"), e);
                sessionWrapper.closeOnError();
                if (!(e instanceof HibernateGpsDeviceException)) {
                    throw new HibernateGpsDeviceException(
                            buildMessage("Failed to index the database"), e);
                }
                throw (HibernateGpsDeviceException) e;
            }

        }

        if (log.isInfoEnabled()) {
            log.info(buildMessage("Finished indexing the database"));
        }

        // save the sanpshot data
        getSnapshotPersister().save(snapshot);
    }


    protected void processRow(Object description, ScrollableResults rs,
                              String[] returnAliases, CompassSession session)
            throws CompassException {
        Object value = processRowValue(description, rs, returnAliases, session);
        if (value != null) {
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object value1 = Array.get(value, i);
                    session.create(value1);
                }
            } else {
                session.create(value);
            }
        }
    }

    protected HibernateSessionWrapper doGetHibernateSessionWrapper() {
        return new Hibernate3SessionWrapper(sessionFactory);
    }

    /**
     * A helper method that returns the actual session factory for event
     * registration. Can be used by subclasses if the
     * <code>SessionFactory</code> is proxied.
     */
    protected SessionFactory doGetActualSessionFactory() {
        return this.sessionFactory;
    }


    /**
     * Index the given <code>ResultSet</code> row into a Compass
     * <code>Resource</code>.
     */
    protected Object processRowValue(Object description, ScrollableResults rs,
                                     String[] returnAliases, CompassSession session)
            throws CompassException {

        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer();
            sb.append(buildMessage("Indexing data row with values "));
            for (int i = 0; i != returnAliases.length; i++) {
                sb.append("[").append(returnAliases[i]).append(":");
                Object value = rs.get(i);
                sb.append(value);
                sb.append("] ");
            }
            log.debug(sb.toString());
        }

        ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) description;

        HibernateAliasRowSnapshot rowSnapshot = null;
        if (shouldMirrorDataChanges() && mapping.supportsVersioning()) {
            rowSnapshot = new HibernateAliasRowSnapshot();
        }
        Resource resource = ((InternalCompassSession) session).getCompass().getResourceFactory().createResource(mapping.getAlias());
        Hibernate3ScrollableResultsRowMarshallHelper marshallHelper = new Hibernate3ScrollableResultsRowMarshallHelper(
                mapping, session, resource, rowSnapshot);
        marshallHelper.marshallResultSet(rs, returnAliases);

        if (shouldMirrorDataChanges() && mapping.supportsVersioning()) {
            snapshot.getAliasSnapshot(mapping.getAlias()).putRow(rowSnapshot);
        }

        return resource;
    }

    /**
     * Performs the data change mirroring operation.
     */
    public synchronized void performMirroring() throws HibernateGpsDeviceException {
        if (!shouldMirrorDataChanges() || isPerformingIndexOperation()) {
            return;
        }
        if (snapshot == null) {
            throw new IllegalStateException(
                    buildMessage("Versioning data was not properly initialized, did you index the device or loaded the data?"));
        }

        HibernateSessionWrapper sessionWrapper = doGetHibernateSessionWrapper();

        try {
            sessionWrapper.open();
            for (Iterator it = mappings.iterator(); it.hasNext();) {
                ResultSetToResourceMapping mapping = (ResultSetToResourceMapping) it.next();
                if (!mapping.supportsVersioning()) {
                    continue;
                }
                HibernateAliasSnapshot oldAliasSnapshot = snapshot.getAliasSnapshot(mapping.getAlias());
                if (oldAliasSnapshot == null) {
                    log.warn(buildMessage("No snapshot for alias [" + mapping.getAlias()
                            + "] even though there should be support for versioning ignoring the alias"));
                    continue;
                }
                HibernateAliasSnapshot newAliasSnapshot = new HibernateAliasSnapshot(mapping.getAlias());
                ArrayList createdRows = new ArrayList();
                ArrayList updatedRows = new ArrayList();
                ArrayList deletedRows = new ArrayList();
                if (log.isDebugEnabled()) {
                    log.debug(buildMessage("Executing version query [" + mapping.getVersionQuery() + "]"));
                }

                String[] returnAliases = null;

                Session hibernateSession = ((Hibernate3SessionWrapper) sessionWrapper)
                        .getSession();


                String queryString = mapping.getVersionQuery();

                if (log.isDebugEnabled()) {
                    log.debug("queryString: " + queryString);
                }

                Query query = hibernateSession.createQuery(queryString)
                        .setCacheMode(CacheMode.IGNORE);
                returnAliases = query.getReturnAliases();

                ScrollableResults rs = query.scroll(ScrollMode.FORWARD_ONLY);
                int count = 0;
                while (rs.next()) {
                    if (log.isDebugEnabled()) {
                        StringBuffer sb = new StringBuffer();
                        sb.append(buildMessage("Version row with values "));
                        for (int i = 0; i != returnAliases.length; i++) {
                            sb.append("[").append(returnAliases[i]).append(":");
                            Object value = rs.get(i);
                            sb.append(value);
                            sb.append("] ");
                        }
                        log.debug(sb.toString());
                    }

                    HibernateAliasRowSnapshot newRowSnapshot = new HibernateAliasRowSnapshot();
                    Hibernate3ScrollableResultsRowMarshallHelper marshallHelper = new Hibernate3ScrollableResultsRowMarshallHelper(mapping,
                            newRowSnapshot, compassGps.getMirrorCompass());
                    marshallHelper.marshallResultSet(rs, returnAliases);

                    // new and old have the same ids
                    HibernateAliasRowSnapshot oldRowSnapshot = oldAliasSnapshot.getRow(newRowSnapshot);

                    // new row or updated row
                    if (oldRowSnapshot == null) {
                        createdRows.add(newRowSnapshot);
                    } else if (oldRowSnapshot.isOlderThan(newRowSnapshot)) {
                        updatedRows.add(newRowSnapshot);
                    }

                    newAliasSnapshot.putRow(newRowSnapshot);

                    if (++count % fetchCount == 0) {
                        // release memory
                        hibernateSession.flush();
                        hibernateSession.clear();
                    }
                }
                rs.close();


                for (Iterator oldRowIt = oldAliasSnapshot.rowSnapshotIt(); oldRowIt.hasNext();) {
                    HibernateAliasRowSnapshot tmpRow = (HibernateAliasRowSnapshot) oldRowIt.next();
                    // deleted row
                    if (newAliasSnapshot.getRow(tmpRow) == null) {
                        deletedRows.add(tmpRow);
                    }
                }
                if (!createdRows.isEmpty() || !updatedRows.isEmpty()) {
                    getSnapshotEventListener().onCreateAndUpdate(
                            new CreateAndUpdateSnapshotEvent(hibernateSession, mapping, createdRows, updatedRows,
                                    compassGps));
                }
                if (!deletedRows.isEmpty()) {
                    getSnapshotEventListener().onDelete(
                            new DeleteSnapshotEvent(hibernateSession, mapping, deletedRows, compassGps));
                }
                snapshot.putAliasSnapshot(newAliasSnapshot);
            }
        } catch (Exception e) {
            throw new HibernateGpsDeviceException(buildMessage("Failed while mirroring data changes"), e);
        } finally {
            sessionWrapper.close();
        }
        if (isSaveSnapshotAfterMirror()) {
            getSnapshotPersister().save(snapshot);
        }
    }

    public HibernateSnapshotEventListener getSnapshotEventListener() {
        return snapshotEventListener;
    }

    public void setSnapshotEventListener(HibernateSnapshotEventListener snapshotEventListener) {
        this.snapshotEventListener = snapshotEventListener;
    }

    public HibernateSnapshotPersister getSnapshotPersister() {
        return snapshotPersister;
    }

    public void setSnapshotPersister(HibernateSnapshotPersister snapshotPersister) {
        this.snapshotPersister = snapshotPersister;
    }

    public boolean isSaveSnapshotAfterMirror() {
        return saveSnapshotAfterMirror;
    }

    public void setSaveSnapshotAfterMirror(boolean saveSnapshotAfterMirror) {
        this.saveSnapshotAfterMirror = saveSnapshotAfterMirror;
    }

    private class Hibernate3SessionWrapper implements HibernateSessionWrapper {

        private SessionFactory sessionFactory;

        private Session session;

        private Transaction tr;

        public Hibernate3SessionWrapper(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public Session getSession() {
            return session;
        }

        public void open() throws HibernateGpsDeviceException {
            try {
                session = sessionFactory.openSession();
            } catch (HibernateException e) {
                throw new HibernateGpsDeviceException(buildMessage("Failed to open session to fetch data"), e);
            }
            try {
                tr = session.beginTransaction();
            } catch (HibernateException e) {
                throw new HibernateGpsDeviceException(buildMessage("Failed to begin transaction to fetch data"), e);
            }
        }

        public void close() {
            if (tr != null) {
                try {
                    tr.commit();
                } catch (HibernateException e) {
                    throw new HibernateGpsDeviceException("Failed to commit hibernate transaction");
                }
            }
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (HibernateException e) {
                log.error("Failed to close Hibernate session", e);
            }
        }

        public void closeOnError() {
            if (tr != null) {
                try {
                    tr.rollback();
                } catch (HibernateException e1) {
                    log.error(buildMessage("Failed to rollback hibernate transaction"), e1);
                }
            }
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (HibernateException e) {
                log.error(buildMessage("Failed to close Hibernate session"), e);
            }
        }
    }

}

