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
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import org.compass.gps.device.jdbc.datasource.SingleConnectionDataSource;

public abstract class AbstractJdbcGpsDeviceTests extends TestCase {

    private static final String DB_SETUP = ""
            + "CREATE TABLE parent (id INTEGER NOT NULL IDENTITY PRIMARY KEY,  first_name VARCHAR(30), last_name VARCHAR(30), version BIGINT NOT NULL );"
            + "CREATE TABLE child (id INTEGER NOT NULL IDENTITY PRIMARY KEY, parent_id INTEGER NOT NULL, first_name VARCHAR(30), last_name VARCHAR(30), version BIGINT NOT NULL );"
            + "alter table child add constraint fk_child_parent foreign key (parent_id) references parent(id);";

    private static final String[] DB_DATA = { "INSERT INTO parent VALUES (1, 'parent first 1', 'last 1', 1);",
            "INSERT INTO parent VALUES (2, 'parent first 2', 'last 2', 1);",
            "INSERT INTO parent VALUES (3, 'parent first 3', 'last 3', 1);",
            "INSERT INTO parent VALUES (4, 'parent first 4', 'last 4', 1);",
            "INSERT INTO child VALUES (1, 1, 'child first 1 1', 'last 1 1', 1);",
            "INSERT INTO child VALUES (2, 1, 'child first 1 2', 'last 1 2', 1);",
            "INSERT INTO child VALUES (3, 1, 'child first 1 3', 'last 1 3', 1);",
            "INSERT INTO child VALUES (4, 2, 'child first 2 1', 'last 2 1', 1);",
            "INSERT INTO child VALUES (5, 3, 'child first 3 1', 'last 3 1', 1);",
            "INSERT INTO child VALUES (6, 4, 'child first 3 2', 'last 3 2', 1);" };

    private static final String DB_TEARDOWN = "DROP TABLE child; DROP TABLE parent;";

    protected SingleConnectionDataSource dataSource;

    protected void setUp() throws Exception {
        dataSource = new SingleConnectionDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:test", "sa", "", true);
        setUpDB();
        setUpDBData();
    }

    protected void tearDown() throws Exception {
        tearDownDB();
        dataSource.destroy();
    }

    protected void setUpDB() throws SQLException {
        try {
            tearDownDB();
        } catch (Exception e) {
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

}
