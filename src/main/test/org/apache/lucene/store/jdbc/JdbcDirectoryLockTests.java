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

import java.sql.Connection;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.jdbc.datasource.DataSourceUtils;
import org.apache.lucene.store.jdbc.support.JdbcTable;

/**
 * @author kimchy
 */
public class JdbcDirectoryLockTests extends AbstractJdbcDirectoryTests {

    private boolean DISABLE = true;

    public void testLocks() throws Exception {
        if (DISABLE) {
            return;
        }
        JdbcDirectorySettings settings = new JdbcDirectorySettings();
        settings.setQueryTimeout(1);

        JdbcDirectory dir1 = new JdbcDirectory(dataSource, new JdbcTable(settings, createDialect(), "TEST"));
        Connection con1 = DataSourceUtils.getConnection(dataSource);
        dir1.create();
        DataSourceUtils.commitConnectionIfPossible(con1);
        DataSourceUtils.releaseConnection(con1);

        JdbcDirectory dir2 = new JdbcDirectory(dataSource, new JdbcTable(settings, createDialect(), "TEST"));

        try {
            // shoudl work
            con1 = DataSourceUtils.getConnection(dataSource);
            Lock lock1 = dir1.makeLock(IndexWriter.WRITE_LOCK_NAME);

            boolean obtained = lock1.obtain();
            assertTrue(obtained);

            Connection con2 = DataSourceUtils.getConnection(dataSource);
            Lock lock2 = dir2.makeLock(IndexWriter.WRITE_LOCK_NAME);

            obtained = lock2.obtain();
            assertFalse(obtained);

            obtained = lock2.obtain();
            assertFalse(obtained);

            lock1.release();

            DataSourceUtils.commitConnectionIfPossible(con1);
            DataSourceUtils.releaseConnection(con1);

            obtained = lock2.obtain();
            assertTrue(obtained);

            DataSourceUtils.commitConnectionIfPossible(con2);
            DataSourceUtils.releaseConnection(con2);
        } finally {
            try {
                con1 = DataSourceUtils.getConnection(dataSource);
                dir1.delete();
                DataSourceUtils.commitConnectionIfPossible(con1);
                DataSourceUtils.releaseConnection(con1);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        }
    }
}
