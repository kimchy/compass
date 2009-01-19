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

package org.apache.lucene.store.jdbc.index;

import java.io.IOException;
import java.sql.Connection;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.jdbc.AbstractJdbcDirectoryTests;
import org.apache.lucene.store.jdbc.JdbcDirectory;
import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.support.JdbcTable;

/**
 * @author kimchy
 */
public abstract class AbstractIndexInputOutputTests extends AbstractJdbcDirectoryTests {

    protected JdbcDirectory jdbcDirectory;

    protected boolean disable;

    protected void setUp() throws Exception {
        super.setUp();
        JdbcDirectorySettings settings = new JdbcDirectorySettings();
        settings.getDefaultFileEntrySettings().setClassSetting(
                JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING, indexInputClass());
        settings.getDefaultFileEntrySettings().setClassSetting(
                JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, indexOutputClass());

        jdbcDirectory = new JdbcDirectory(dataSource, new JdbcTable(settings, createDialect(), "TEST"));
    }

    protected void tearDown() throws Exception {
        jdbcDirectory.close();
        super.tearDown();
    }

    protected abstract Class indexInputClass();

    protected abstract Class indexOutputClass();

    public void testSize5() throws IOException {
        innerTestSize(5);
    }

    public void testSize5WithinTransaction() throws IOException {
        innertTestSizeWithinTransaction(5);
    }

    public void testSize15() throws IOException {
        innerTestSize(15);
    }

    public void testSize15WithinTransaction() throws IOException {
        innertTestSizeWithinTransaction(15);
    }

    public void testSize2() throws IOException {
        innerTestSize(2);
    }

    public void testSize2WithinTransaction() throws IOException {
        innertTestSizeWithinTransaction(2);
    }

    public void testSize1() throws IOException {
        innerTestSize(1);
    }

    public void testSize1WithinTransaction() throws IOException {
        innertTestSizeWithinTransaction(1);
    }

    public void testSize50() throws IOException {
        innerTestSize(50);
    }

    public void testSize50WithinTransaction() throws IOException {
        innertTestSizeWithinTransaction(50);
    }

    protected void innerTestSize(int bufferSize) throws IOException {
        if (disable) {
            return;
        }
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        jdbcDirectory.getSettings().getDefaultFileEntrySettings()
                .setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING, bufferSize);
        jdbcDirectory.getSettings().getDefaultFileEntrySettings()
                .setIntSetting(JdbcBufferedIndexOutput.BUFFER_SIZE_SETTING, bufferSize);

        con = DataSourceUtils.getConnection(dataSource);
        insertData();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);

        con = DataSourceUtils.getConnection(dataSource);
        verifyData();
        DataSourceUtils.commitConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
    }

    protected void innertTestSizeWithinTransaction(int bufferSize) throws IOException {
        if (disable) {
            return;
        }
        Connection con = DataSourceUtils.getConnection(dataSource);
        jdbcDirectory.create();
        jdbcDirectory.getSettings().getDefaultFileEntrySettings()
                .setIntSetting(JdbcBufferedIndexInput.BUFFER_SIZE_SETTING, bufferSize);
        jdbcDirectory.getSettings().getDefaultFileEntrySettings()
                .setIntSetting(JdbcBufferedIndexOutput.BUFFER_SIZE_SETTING, bufferSize);

        insertData();
        verifyData();

        DataSourceUtils.rollbackConnectionIfPossible(con);
        DataSourceUtils.releaseConnection(con);
    }

    private void insertData() throws IOException {
        byte[] test = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        IndexOutput indexOutput = jdbcDirectory.createOutput("value1");
        indexOutput.writeInt(-1);
        indexOutput.writeLong(10);
        indexOutput.writeInt(0);
        indexOutput.writeInt(0);
        indexOutput.writeBytes(test, 8);
        indexOutput.writeBytes(test, 5);

        indexOutput.seek(28);
        indexOutput.writeByte((byte) 8);
        indexOutput.seek(30);
        indexOutput.writeBytes(new byte[]{1, 2}, 2);

        indexOutput.close();
    }

    private void verifyData() throws IOException {
        byte[] test = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertTrue(jdbcDirectory.fileExists("value1"));
        assertEquals(33, jdbcDirectory.fileLength("value1"));

        IndexInput indexInput = jdbcDirectory.openInput("value1");
        assertEquals(-1, indexInput.readInt());
        assertEquals(10, indexInput.readLong());
        assertEquals(0, indexInput.readInt());
        assertEquals(0, indexInput.readInt());
        indexInput.readBytes(test, 0, 8);
        assertEquals((byte) 1, test[0]);
        assertEquals((byte) 8, test[7]);
        indexInput.readBytes(test, 0, 5);
        assertEquals((byte) 8, test[0]);
        assertEquals((byte) 5, test[4]);

        indexInput.seek(28);
        assertEquals((byte) 8, indexInput.readByte());
        indexInput.seek(30);
        assertEquals((byte) 1, indexInput.readByte());

        indexInput.close();
    }
}
