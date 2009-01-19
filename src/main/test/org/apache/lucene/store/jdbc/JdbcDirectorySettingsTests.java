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

import junit.framework.TestCase;

/**
 * @author kimchy
 */
public class JdbcDirectorySettingsTests extends TestCase {

    public void testDefaultSettings() {
        JdbcDirectorySettings settings = new JdbcDirectorySettings();

        assertEquals("name_", settings.getNameColumnName());
        assertEquals("size_", settings.getSizeColumnName());
        assertEquals("value_", settings.getValueColumnName());
        assertEquals("deleted_", settings.getDeletedColumnName());
        assertEquals("lf_", settings.getLastModifiedColumnName());

        assertEquals(50, settings.getNameColumnLength());
        assertEquals(500 * 1000, settings.getValueColumnLengthInK());

        assertEquals(10, settings.getQueryTimeout());

        assertEquals(60 * 60 * 1000, settings.getDeleteMarkDeletedDelta());
    }

    public void testFileEntrySettings() {
        JdbcDirectorySettings settings = new JdbcDirectorySettings();
        JdbcFileEntrySettings feSettings = new JdbcFileEntrySettings();
        settings.registerFileEntrySettings("tst", feSettings);
        assertEquals(feSettings, settings.getFileEntrySettings("tst"));
        assertEquals(feSettings, settings.getFileEntrySettings("1.tst"));
        assertEquals(settings.getDefaultFileEntrySettings(), settings.getFileEntrySettings("test"));
        assertEquals(settings.getDefaultFileEntrySettings(), settings.getFileEntrySettings("1.test"));
    }
}
