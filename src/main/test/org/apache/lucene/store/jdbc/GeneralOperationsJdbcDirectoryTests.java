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

package org.apache.lucene.store.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.handler.ActualDeleteFileEntryHandler;
import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.apache.lucene.store.jdbc.support.JdbcTemplate;

/**
 * @author kimchy
 */
public class GeneralOperationsJdbcDirectoryTests extends AbstractJdbcDirectoryTests {

    private JdbcDirectory jdbcDirectory;

    protected void setUp() throws Exception {
        super.setUp();
        JdbcDirectorySettings settings = new JdbcDirectorySettings();
        settings.getDefaultFileEntrySettings().setClassSetting(
                JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, ActualDeleteFileEntryHandler.class);

        jdbcDirectory = new JdbcDirectory(dataSource, new JdbcTable(settings, createDialect(), "TEST"));
    }

    protected void tearDown() throws Exception {
        jdbcDirectory.close();
        super.tearDown();
    }

    public void testCreateDelteExists() throws IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        if (jdbcDirectory.getDialect().supportsTableExists()) {
            con = DataSourceUtils.getConnection(dataSource);
            assertTrue(jdbcDirectory.tableExists());
            DataSourceUtils.commitConnectionIfPossible(con);
            DataSourceUtils.releaseConnection(con);
        }

        con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.delete();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        if (jdbcDirectory.getDialect().supportsTableExists()) {
            con = DataSourceUtils.getConnection(dataSource);
            assertFalse(jdbcDirectory.tableExists());
            DataSourceUtils.commitConnectionIfPossible(con);
            DataSourceUtils.releaseConnection(con);
        }
    }

    public void testCreateDelteExistsWitinTransaction() throws IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();

        if (jdbcDirectory.getDialect().supportsTableExists()) {
            assertTrue(jdbcDirectory.tableExists());
        }

        jdbcTemplate.executeSelect("select * from test", new JdbcTemplate.ExecuteSelectCallback() {

            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
            }

            public Object execute(ResultSet rs) throws Exception {
                assertFalse(rs.next());
                return null;
            }
        });

        jdbcDirectory.delete();

        if (jdbcDirectory.getDialect().supportsTableExists()) {
            assertFalse(jdbcDirectory.tableExists());
        }

        try {
            jdbcTemplate.executeSelect("select * from test", new JdbcTemplate.ExecuteSelectCallback() {

                public void fillPrepareStatement(PreparedStatement ps) throws Exception {
                }

                public Object execute(ResultSet rs) throws Exception {
                    assertFalse(rs.next());
                    return null;
                }
            });
            fail();
        } catch (Exception e) {

        }
        DataSourceUtils.rollbackConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
    }

    public void testList() throws IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        String[] list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(0, list.length);

        con = DataSourceUtils.getConnection(dataSource);
        IndexOutput indexOutput = jdbcDirectory.createOutput("test1");
        indexOutput.writeString("TEST STRING");
        indexOutput.close();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        jdbcTemplate.executeSelect("select * from test", new JdbcTemplate.ExecuteSelectCallback() {

            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
            }

            public Object execute(ResultSet rs) throws Exception {
                assertTrue(rs.next());
                return null;
            }
        });

        con = DataSourceUtils.getConnection(dataSource);
        list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(1, list.length);

        con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.deleteFile("test1");
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(0, list.length);
    }

    public void testListWithinTransaction() throws IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);

        jdbcDirectory.create();

        String[] list = jdbcDirectory.list();
        assertEquals(0, list.length);

        IndexOutput indexOutput = jdbcDirectory.createOutput("test1");
        indexOutput.writeString("TEST STRING");
        indexOutput.close();

        jdbcTemplate.executeSelect("select * from test", new JdbcTemplate.ExecuteSelectCallback() {

            public void fillPrepareStatement(PreparedStatement ps) throws Exception {
            }

            public Object execute(ResultSet rs) throws Exception {
                assertTrue(rs.next());
                return null;
            }
        });

        list = jdbcDirectory.list();
        assertEquals(1, list.length);

        jdbcDirectory.deleteFile("test1");
        list = jdbcDirectory.list();
        assertEquals(0, list.length);

        DataSourceUtils.rollbackConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
    }


    public void testDeleteContent() throws IOException {
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        String[] list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(0, list.length);

        con = DataSourceUtils.getConnection(dataSource);
        IndexOutput indexOutput = jdbcDirectory.createOutput("test1");
        indexOutput.writeString("TEST STRING");
        indexOutput.close();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(1, list.length);

        con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.deleteContent();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        list = jdbcDirectory.list();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
        assertEquals(0, list.length);
    }
}
