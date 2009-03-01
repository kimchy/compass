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
import org.compass.core.util.FileHandlerMonitor;
import org.compass.gps.device.jdbc.mapping.DataColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.IdColumnToPropertyMapping;
import org.compass.gps.device.jdbc.mapping.ResultSetToResourceMapping;
import org.compass.gps.device.jdbc.mapping.VersionColumnMapping;
import org.compass.gps.impl.SingleCompassGps;

/**
 * @author kimchy
 */
public class ResultSetJdbcGpsDeviceTests extends AbstractJdbcGpsDeviceTests {

    protected Compass compass;

    private FileHandlerMonitor fileHandlerMonitor;

    protected CompassTemplate compassTemplate;

    private ResultSetJdbcGpsDevice gpsDevice;

    private SingleCompassGps gps;

    protected void tearDown() throws Exception {
        if (gps != null) {
            gps.stop();
        }
        if (compass != null) {
            compass.close();
        }

        fileHandlerMonitor.verifyNoHandlers();

        super.tearDown();
    }

    protected void setUpExactMappingNoMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping();
        mapping.setAlias("result-set");
        mapping.setSelectQuery("select "
                + "p.id as parent_id, p.first_name as parent_first_name, p.last_name as parent_last_name, "
                + "c.id as child_id, c.first_name as child_first_name, c.last_name child_last_name "
                + "from parent p left join child c on p.id = c.parent_id");
        mapping.addIdMapping(new IdColumnToPropertyMapping("parent_id", "parent_id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping("child_id", "child_id"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "child_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "first_name"));

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, dataSource));
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new ResultSetJdbcGpsDevice();
        gpsDevice.setDataSource(dataSource);
        gpsDevice.setName("resultSetJdbcDevice");
        // setting up no mirroring, even though it should not mirror since we
        // mapped no version columns
        gpsDevice.setMirrorDataChanges(false);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testExactMappingNoMirror() throws Exception {
        setUpExactMappingNoMirror();
        gps.index();
        Resource r = compassTemplate.getResource("result-set", "1", "1");
        assertNotNull(r.getProperty("parent_id"));
        assertNotNull(r.getProperty("parent_first_name"));
        assertNotNull(r.getProperty("child_id"));
        assertNotNull(r.getProperty("child_first_name"));
        assertNotNull(r.getProperty("first_name"));
        assertNull(r.getProperty("ID"));
        assertNull(r.getProperty("FIRST_NAME"));
        assertNull(r.getProperty("LAST_NAME"));
        assertNotNull(r);
        r = compassTemplate.getResource("result-set", "4", "6");
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());
    }

    protected void setUpUnmappedMappingNoMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping("result-set",
                "select * from parent p left join child c on p.id = c.parent_id");
        mapping.setIndexUnMappedColumns(true);
        mapping.addIdMapping(new IdColumnToPropertyMapping(1, "parent_id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping(5, "child_id"));
        mapping.addDataMapping(new DataColumnToPropertyMapping(2, "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping(7, "child_first_name"));

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/test-index");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, this.dataSource));
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new ResultSetJdbcGpsDevice();
        gpsDevice.setDataSource(dataSource);
        gpsDevice.setName("resultSetJdbcDevice");
        // it should not mirror the data since we did not mapped any version
        // columns
        gpsDevice.setMirrorDataChanges(true);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testUnmappedMappingNoMirror() throws Exception {
        setUpUnmappedMappingNoMirror();
        gps.index();
        Resource r = compassTemplate.getResource("result-set", "1", "1");
        assertNotNull(r);
        assertNotNull(r.getProperty("parent_id"));
        assertNotNull(r.getProperty("parent_first_name"));
        assertNotNull(r.getProperty("child_id"));
        assertNotNull(r.getProperty("child_first_name"));
        assertNotNull(r.getProperty("LAST_NAME"));
        assertNull(r.getProperty("FIRST_NAME"));
        r = compassTemplate.getResource("result-set", "4", "6");
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());
    }

    protected void setUpExactMappingWithMirror() throws Exception {
        // set up the database mappings, since they are used both to generate
        // the resource mappings and configure the jdbc gps device
        ResultSetToResourceMapping mapping = new ResultSetToResourceMapping();
        mapping.setAlias("result-set");
        mapping
                .setSelectQuery("select "
                        + "p.id as parent_id, p.first_name as parent_first_name, p.last_name as parent_last_name, p.version as parent_version, "
                        + "COALESCE(c.id, 0) as child_id, c.first_name as child_first_name, c.last_name child_last_name, COALESCE(c.version, 0) as child_version "
                        + "from parent p left join child c on p.id = c.parent_id");
        mapping
                .setVersionQuery("select p.id as parent_id, COALESCE(c.id, 0) as child_id, p.version as parent_version, COALESCE(c.version, 0) as child_version from parent p left join child c on p.id = c.parent_id");
        mapping.addIdMapping(new IdColumnToPropertyMapping("parent_id", "parent_id", "p.id"));
        mapping.addIdMapping(new IdColumnToPropertyMapping("child_id", "child_id", "COALESCE(c.id, 0)"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("parent_first_name", "parent_first_name"));
        mapping.addDataMapping(new DataColumnToPropertyMapping("child_first_name", "child_first_name"));
        mapping.addVersionMapping(new VersionColumnMapping("parent_version"));
        mapping.addVersionMapping(new VersionColumnMapping("child_version"));

        CompassConfiguration conf = new CompassConfiguration().setSetting(CompassEnvironment.CONNECTION,
                "target/testindex");
        conf.addMappingResolver(new ResultSetResourceMappingResolver(mapping, this.dataSource));
        conf.getSettings().setBooleanSetting(CompassEnvironment.DEBUG, true);
        compass = conf.buildCompass();

        fileHandlerMonitor = FileHandlerMonitor.getFileHandlerMonitor(compass);
        fileHandlerMonitor.verifyNoHandlers();

        compass.getSearchEngineIndexManager().deleteIndex();
        compass.getSearchEngineIndexManager().verifyIndex();

        compassTemplate = new CompassTemplate(compass);

        gpsDevice = new ResultSetJdbcGpsDevice();
        gpsDevice.setDataSource(dataSource);
        gpsDevice.setName("resultSetJdbcDevice");
        gpsDevice.setMirrorDataChanges(true);
        gpsDevice.addMapping(mapping);

        gps = new SingleCompassGps(compass);
        gps.addGpsDevice(gpsDevice);
        gps.start();
    }

    public void testExactMappingWithMirrorMockEvent() throws Exception {
        setUpExactMappingWithMirror();
        MockSnapshotEventListener eventListener = new MockSnapshotEventListener();
        gpsDevice.setSnapshotEventListener(eventListener);
        gps.index();
        compassTemplate.loadResource("result-set", "1", "1");
        Resource r = compassTemplate.getResource("result-set", "4", "6");
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());

        eventListener.clear();
        gpsDevice.performMirroring();
        assertFalse(eventListener.isCreateAndUpdateCalled());
        assertFalse(eventListener.isDeleteCalled());

        // test that create works
        Connection con = JdbcUtils.getConnection(dataSource);
        PreparedStatement ps = con
                .prepareStatement("INSERT INTO parent VALUES (999, 'parent first 999', 'last 999', 1);");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        eventListener.clear();
        gpsDevice.performMirroring();
        assertTrue(eventListener.isCreateAndUpdateCalled());
        assertTrue(eventListener.isCreateHappen());
        assertFalse(eventListener.isUpdateHappen());
        assertFalse(eventListener.isDeleteCalled());

        // test that update parent works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("update parent set first_name = 'new first name', version = 2 where id = 1");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        eventListener.clear();
        gpsDevice.performMirroring();
        assertTrue(eventListener.isCreateAndUpdateCalled());
        assertTrue(eventListener.isUpdateHappen());
        assertFalse(eventListener.isCreateHappen());
        assertFalse(eventListener.isDeleteCalled());

        // test that update child works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("update child set first_name = 'new first name', version = 2 where id = 1");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        eventListener.clear();
        gpsDevice.performMirroring();
        assertTrue(eventListener.isCreateAndUpdateCalled());
        assertTrue(eventListener.isUpdateHappen());
        assertFalse(eventListener.isCreateHappen());
        assertFalse(eventListener.isDeleteCalled());

        // test that delete works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("delete from parent where id = 999");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        eventListener.clear();
        gpsDevice.performMirroring();
        assertTrue(eventListener.isDeleteCalled());
        assertTrue(eventListener.isDeleteHappen());
        assertFalse(eventListener.isCreateAndUpdateCalled());

        eventListener.clear();
        gpsDevice.performMirroring();
        assertFalse(eventListener.isCreateAndUpdateCalled());
        assertFalse(eventListener.isDeleteCalled());
    }

    public void testExactMappingWithMirror() throws Exception {
        setUpExactMappingWithMirror();
        gps.index();
        compassTemplate.loadResource("result-set", "1", "1");
        Resource r = compassTemplate.getResource("result-set", "4", "6");
        assertNotNull(r);
        CompassDetachedHits hits = compassTemplate.findWithDetach("parent");
        assertEquals(6, hits.getLength());

        // test that create works
        Connection con = JdbcUtils.getConnection(dataSource);
        PreparedStatement ps = con
                .prepareStatement("INSERT INTO parent VALUES (999, 'parent first 999', 'last 999', 1);");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        r = compassTemplate.getResource("result-set", "999", "0");
        assertNull(r);
        gpsDevice.performMirroring();
        compassTemplate.loadResource("result-set", "999", "0");

        // test that update works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("update parent set first_name = 'new first name', version = 2 where id = 1");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        gpsDevice.performMirroring();
        r = compassTemplate.loadResource("result-set", "1", "1");
        assertEquals("new first name", r.getValue("parent_first_name"));

        // test that delete works
        con = JdbcUtils.getConnection(dataSource);
        ps = con.prepareStatement("delete from parent where id = 999");
        ps.execute();
        ps.close();
        con.commit();
        con.close();

        gpsDevice.performMirroring();
        r = compassTemplate.getResource("result-set", "999", "0");
        assertNull(r);
    }
}
