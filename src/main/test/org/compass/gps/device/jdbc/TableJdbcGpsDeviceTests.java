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

import org.compass.core.Compass;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassTemplate;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.config.CompassEnvironment;
import org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.TableToResourceMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.compass.gps.device.jdbc.snapshot.FSJdbcSnapshotPersister;
import org.compass.gps.impl.SingleCompassGps;

/**
 * 
 * @author kimchy
 * 
 */
public class TableJdbcGpsDeviceTests extends AbstractJdbcGpsDeviceTests {

    protected Compass compass;

    protected CompassTemplate compassTemplate;

    private ResultSetJdbcGpsDevice gpsDevice;

    private SingleCompassGps gps;

    protected void tearDown() throws Exception {
        if (gps != null) gps.stop();
        if (compass != null) {
            compass.close();
        }
        super.tearDown();
    }

    protected void setUpDefinedExactMapping() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        TableToResourceMapping parentMapping = new TableToResourceMapping("PARENT", "parent");
        parentMapping.setIndexUnMappedColumns(false);
        parentMapping.addDataMapping(new DataColumnToPropertyMapping("first_name", "first_name"));
        TableToResourceMapping childMapping = new TableToResourceMapping("CHILD", "child");
        childMapping.addDataMapping(new DataColumnToPropertyMapping("first_name", "first_name"));
        childMapping.setIndexUnMappedColumns(false);

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(parentMapping, dataSource));
        conf.addMappingResolver(new ResultSetResourceMappingResolver(childMapping, dataSource));
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new ResultSetJdbcGpsDevice();
        gpsDevice.setDataSource(dataSource);
        gpsDevice.setName("tableJdbcDevice");
        gpsDevice.addMapping(parentMapping);
        gpsDevice.addMapping(childMapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testDefinedExactMapping() throws Exception {
        setUpDefinedExactMapping();
        gps.index();
        Resource r = compassTemplate.getResource("parent", "1");
        assertNotNull(r);
        assertNotNull(r.getProperty("ID"));
        assertNotNull(r.getProperty("first_name"));
        assertNull(r.getProperty("FIRST_NAME"));
        assertNull(r.getProperty("LAST_NAME"));
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(4, hits.getLength());
        hits = compassTemplate.findWithDetach("child");
        assertEquals(6, hits.getLength());
    }

    protected void setUpAutomaticMapping() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        TableToResourceMapping parentMapping = new TableToResourceMapping("PARENT", "parent");
        parentMapping.addVersionMapping(new VersionColumnMapping("version"));
        parentMapping.setIndexUnMappedColumns(true);

        TableToResourceMapping childMapping = new TableToResourceMapping("CHILD", "child");
        childMapping.addVersionMapping(new VersionColumnMapping("version"));
        childMapping.setIndexUnMappedColumns(true);

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/testindex");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(parentMapping, dataSource));
        conf.addMappingResolver(new ResultSetResourceMappingResolver(childMapping, dataSource));
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new ResultSetJdbcGpsDevice();
        gpsDevice.setSnapshotPersister(new FSJdbcSnapshotPersister("target/testindex/snapshot"));
        gpsDevice.setDataSource(dataSource);
        gpsDevice.setName("tableJdbcDevice");
        gpsDevice.addMapping(parentMapping);
        gpsDevice.addMapping(childMapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testAutomaticMappingAndFSPersister() throws Exception {
        setUpAutomaticMapping();
        gps.index();
        Resource r = compassTemplate.getResource("parent", "1");
        assertNotNull(r);
        assertNotNull(r.getProperty("ID"));
        assertNotNull(r.getProperty("FIRST_NAME"));
        assertNotNull(r.getProperty("LAST_NAME"));
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(4, hits.getLength());
        hits = compassTemplate.findWithDetach("child");
        assertEquals(6, hits.getLength());
    }

    public void testAutomaticMappingWithMirroringAndFSPersister() throws Exception {
        setUpAutomaticMapping();
        gpsDevice.setMirrorDataChanges(true);
        gps.index();
        Resource r = compassTemplate.getResource("parent", "1");
        assertNotNull(r);
        assertNotNull(r.getProperty("ID"));
        assertNotNull(r.getProperty("FIRST_NAME"));
        assertNotNull(r.getProperty("LAST_NAME"));
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(4, hits.getLength());
        hits = compassTemplate.findWithDetach("child");
        assertEquals(6, hits.getLength());

        // test that create works
        Connection con = JdbcUtils.getConnection(dataSource);
        PreparedStatement ps = con
                .prepareStatement("INSERT INTO parent VALUES (999, 'parent first 999', 'last 999', 1);");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        r = compassTemplate.getResource("parent", "999");
        assertNull(r);
        gpsDevice.performMirroring();
        compassTemplate.loadResource("parent", "999");

        gps.stop();
        gps.start();
        // test that update works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("update parent set first_name = 'new first name', version = 2 where id = 1");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        gpsDevice.performMirroring();
        r = compassTemplate.loadResource("parent", "1");
        assertEquals("new first name", r.getValue("FIRST_NAME"));

        // test that delete works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("delete from parent where id = 999");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        gpsDevice.performMirroring();
        r = compassTemplate.getResource("parent", "999");
        assertNull(r);
    }
}
