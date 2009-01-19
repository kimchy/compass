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

package org.compass.spring.test.device.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import junit.framework.TestCase;
import org.compass.core.Compass;
import org.compass.core.CompassDetachedHits;
import org.compass.core.CompassTemplate;
import org.compass.core.Resource;
import org.compass.gps.ActiveMirrorGpsDevice;
import org.compass.gps.CompassGps;
import org.compass.gps.device.jdbc.JdbcUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringJdbcGpsDeviceTests extends TestCase {

    private static final String DB_SETUP = ""
            + "CREATE TABLE parent (id INTEGER NOT NULL IDENTITY PRIMARY KEY,  first_name VARCHAR(30), last_name VARCHAR(30), version BIGINT NOT NULL );"
            + "CREATE TABLE child (id INTEGER NOT NULL IDENTITY PRIMARY KEY, parent_id INTEGER NOT NULL, first_name VARCHAR(30), last_name VARCHAR(30), version BIGINT NOT NULL );"
            + "alter table child add constraint fk_child_parent foreign key (parent_id) references parent(id);";

    private static final String[] DB_DATA = {"INSERT INTO parent VALUES (1, 'parent first 1', 'last 1', 1);",
            "INSERT INTO parent VALUES (2, 'parent first 2', 'last 2', 1);",
            "INSERT INTO parent VALUES (3, 'parent first 3', 'last 3', 1);",
            "INSERT INTO parent VALUES (4, 'parent first 4', 'last 4', 1);",
            "INSERT INTO child VALUES (1, 1, 'child first 1 1', 'last 1 1', 1);",
            "INSERT INTO child VALUES (2, 1, 'child first 1 2', 'last 1 2', 1);",
            "INSERT INTO child VALUES (3, 1, 'child first 1 3', 'last 1 3', 1);",
            "INSERT INTO child VALUES (4, 2, 'child first 2 1', 'last 2 1', 1);",
            "INSERT INTO child VALUES (5, 3, 'child first 3 1', 'last 3 1', 1);",
            "INSERT INTO child VALUES (6, 4, 'child first 3 2', 'last 3 2', 1);"};

    private static final String DB_TEARDOWN = "DROP TABLE child; DROP TABLE parent;";

    protected DataSource dataSource;

    private ApplicationContext dataSourceApplicationContext;

    protected void setUp() throws Exception {
        dataSourceApplicationContext = new ClassPathXmlApplicationContext(
                "org/compass/spring/test/device/jdbc/datasource-applicationContext.xml");
        dataSource = (DataSource) dataSourceApplicationContext.getBean("dataSource");
        setUpDB();
        setUpDBData();
    }

    protected void tearDown() throws Exception {
        tearDownDB();
        ((DisposableBean) dataSourceApplicationContext).destroy();
    }

    protected void setUpDB() throws SQLException {
        try {
            tearDownDB();
        } catch (SQLException e) {
            // do nothing
        }
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(DB_SETUP);
        ps.execute();
        ps.close();
        con.close();
    }

    protected void tearDownDB() throws SQLException {
        Connection con = dataSource.getConnection();
        PreparedStatement ps = con.prepareStatement(DB_TEARDOWN);
        try {
            ps.execute();
            ps.close();
        } finally {
            con.close();
        }
    }

    protected void setUpDBData() throws SQLException {
        Connection con = dataSource.getConnection();
        Statement stmt = con.createStatement();
        for (int i = 0; i < DB_DATA.length; i++) {
            stmt.addBatch(DB_DATA[i]);
        }
        stmt.executeBatch();
        stmt.close();
        con.close();
    }

    public void testResultSetJdbcDevice() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[]{"org/compass/spring/test/device/jdbc/resultset-applicationContext.xml"},
                dataSourceApplicationContext);

        CompassGps gps = (CompassGps) applicationContext.getBean("gps");
        Compass compass = (Compass) applicationContext.getBean("compass");
        CompassTemplate compassTemplate = new CompassTemplate(compass);
        ActiveMirrorGpsDevice gpsDevice = (ActiveMirrorGpsDevice) applicationContext.getBean("jdbcGpsDevice");

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

        applicationContext.close();
    }

    public void testTableJdbcDevice() throws Exception {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(
                new String[]{"org/compass/spring/test/device/jdbc/table-applicationContext.xml"},
                dataSourceApplicationContext);

        CompassGps gps = (CompassGps) applicationContext.getBean("gps");
        Compass compass = (Compass) applicationContext.getBean("compass");
        CompassTemplate compassTemplate = new CompassTemplate(compass);
        ActiveMirrorGpsDevice gpsDevice = (ActiveMirrorGpsDevice) applicationContext.getBean("jdbcGpsDevice");

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

        applicationContext.close();
    }
}
