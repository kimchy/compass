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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;

import org.compass.core.Compass;
import org.compass.core.CompassSession;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.gps.device.jdbc.dialect.JdbcDialect;
import org.compass.gps.device.jdbc.mapping.ColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot;

/**
 * A helper marshaller from a <code>ResultSet</code> current row to a
 * <code>Resource</code> and/or to a
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot}.
 *
 * @author kimchy
 */
public class ResultSetRowMarshallHelper {

    private ResultSetToResourceMapping mapping;

    private InternalCompassSession session;

    private JdbcDialect dialect;

    private JdbcAliasRowSnapshot rowSnapshot;

    private Resource resource;

    private boolean marshallResource = false;

    private boolean marshallVersioning = false;

    private ResourceMapping resourceMapping;

    /**
     * Creates a new marshaller helper that will marhsall the
     * <code>ResultSet</code> to the given <code>Resource</code>.
     */
    public ResultSetRowMarshallHelper(ResultSetToResourceMapping mapping, CompassSession session, JdbcDialect dialect,
                                      Resource resource) {
        this(mapping, session, dialect, resource, null);
    }

    /**
     * Creates a new marshaller helper that will marshall the
     * <code>ResultSet</code> to the given {@link JdbcAliasRowSnapshot}.
     */
    public ResultSetRowMarshallHelper(ResultSetToResourceMapping mapping, JdbcDialect dialect,
                                      JdbcAliasRowSnapshot rowSnapshot, Compass compass) {
        this(mapping, null, dialect, null, rowSnapshot, compass);
    }

    public ResultSetRowMarshallHelper(ResultSetToResourceMapping mapping, CompassSession session, JdbcDialect dialect,
                                      Resource resource, JdbcAliasRowSnapshot rowSnapshot) {
        this(mapping, session, dialect, resource, rowSnapshot, ((InternalCompassSession) session).getCompass());
    }

    /**
     * Creates a new marshaller helper that will marhsall that
     * <code>ResultSet</code> to both the given <code>Resource</code> and
     * {@link JdbcAliasRowSnapshot}.
     */
    public ResultSetRowMarshallHelper(ResultSetToResourceMapping mapping, CompassSession session, JdbcDialect dialect,
                                      Resource resource, JdbcAliasRowSnapshot rowSnapshot, Compass compass) {
        this.mapping = mapping;
        this.session = (InternalCompassSession) session;
        this.dialect = dialect;
        this.rowSnapshot = rowSnapshot;
        resourceMapping = ((InternalCompass) compass).getMapping().getMappingByAlias(mapping.getAlias());
        if (rowSnapshot == null || !mapping.supportsVersioning()) {
            marshallVersioning = false;
        } else {
            marshallVersioning = true;
        }
        this.resource = resource;
        if (resource == null) {
            marshallResource = false;
        } else {
            marshallResource = true;
        }
    }

    /**
     * Marshalls the <code>ResultSet</code>.
     */
    public void marshallResultSet(ResultSet rs) throws SQLException {
        marshallIds(rs);
        marshallVersionsIfNeeded(rs);
        marshallMappedData(rs);
        marshallUnMappedIfNeeded(rs);
    }

    public void marshallIds(ResultSet rs) throws SQLException {
        for (Iterator it = mapping.idMappingsIt(); it.hasNext();) {
            ColumnToPropertyMapping ctpMapping = (ColumnToPropertyMapping) it.next();
            String value = dialect.getStringValue(rs, ctpMapping);
            if (value == null) {
                throw new JdbcGpsDeviceException("Id [" + ctpMapping + "] for alias [" + mapping.getAlias()
                        + "] can not be null");
            }
            if (marshallResource) {
                marshallProperty(ctpMapping, value);
            }
            if (marshallVersioning) {
                rowSnapshot.addIdValue(value);
            }
        }
    }

    public void marshallMappedData(ResultSet rs) throws SQLException {
        if (!marshallResource) {
            return;
        }
        for (Iterator it = mapping.dataMappingsIt(); it.hasNext();) {
            ColumnToPropertyMapping ctpMapping = (ColumnToPropertyMapping) it.next();
            String value = dialect.getStringValue(rs, ctpMapping);
            if (value == null) {
                continue;
            }
            marshallProperty(ctpMapping, value);
        }
    }

    public void marshallVersionsIfNeeded(ResultSet rs) throws SQLException {
        if (!marshallVersioning) {
            return;
        }
        for (Iterator it = mapping.versionMappingsIt(); it.hasNext();) {
            VersionColumnMapping versionMapping = (VersionColumnMapping) it.next();
            Long version = dialect.getVersion(rs, versionMapping);
            rowSnapshot.addVersionValue(version);
        }
    }

    public void marshallUnMappedIfNeeded(ResultSet rs) throws SQLException {
        if (!marshallResource || !mapping.isIndexUnMappedColumns()) {
            return;
        }
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        for (int i = 1; i <= count; i++) {
            String columnName = metaData.getColumnName(i);
            if (mapping.getMappingsForColumn(columnName) == null && mapping.getMappingsForColumn(i) == null) {
                String value = dialect.getStringValue(rs, i);
                if (value == null) {
                    continue;
                }
                Property p = session.getCompass().getResourceFactory().createProperty(columnName, value, Property.Store.YES, Property.Index.ANALYZED);
                resource.addProperty(p);
            }
        }
    }

    public void marshallProperty(ColumnToPropertyMapping ctpMapping, String value) {
        ResourcePropertyMapping propertyMapping = resourceMapping.getResourcePropertyMapping(ctpMapping.getPropertyName());
        if (propertyMapping == null) {
            Property p = session.getCompass().getResourceFactory().createProperty(ctpMapping.getPropertyName(), value, ctpMapping.getPropertyStore(),
                    ctpMapping.getPropertyIndex(), ctpMapping.getPropertyTermVector());
            p.setBoost(ctpMapping.getBoost());
            resource.addProperty(p);
        } else {
            // has explicit mappings (not auto generated), use additional settings (like analyzer and such).
            resource.addProperty(ctpMapping.getPropertyName(), value);
        }
    }

}
