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
import org.apache.lucene.store.jdbc.handler.MarkDeleteFileEntryHandler;
import org.apache.lucene.store.jdbc.index.FetchOnBufferReadJdbcIndexInput;
import org.apache.lucene.store.jdbc.index.RAMAndFileJdbcIndexOutput;

/**
 * @author kimchy
 */
public class JdbcFileEntrySettingsTests extends TestCase {

    public void testDefaultSettings() throws Exception {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();

        assertEquals(3, settings.getProperties().size());
        assertEquals(MarkDeleteFileEntryHandler.class, settings.getSettingAsClass(JdbcFileEntrySettings.FILE_ENTRY_HANDLER_TYPE, null));
        assertEquals(FetchOnBufferReadJdbcIndexInput.class, settings.getSettingAsClass(JdbcFileEntrySettings.INDEX_INPUT_TYPE_SETTING, null));
        assertEquals(RAMAndFileJdbcIndexOutput.class, settings.getSettingAsClass(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, null));
    }

    public void testSetting() {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        String value1 = settings.getSetting("value1");
        assertNull(value1);

        value1 = settings.getSetting("value1", "default");
        assertEquals("default", value1);

        settings.setSetting("value1", "val");

        value1 = settings.getSetting("value1");
        assertEquals("val", value1);

        value1 = settings.getSetting("value1", "default");
        assertEquals("val", value1);
    }

    public void testSettingFloat() {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        float value1 = settings.getSettingAsFloat("value1", 0);
        assertEquals(0f, value1, 0.01);

        settings.setFloatSetting("value1", 1.1f);

        value1 = settings.getSettingAsFloat("value1", 0.0f);
        assertEquals(1.1f, value1, 0.01);
    }

    public void testSettingLong() {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        long value1 = settings.getSettingAsLong("value1", 0);
        assertEquals(0, value1);

        settings.setLongSetting("value1", 1);

        value1 = settings.getSettingAsLong("value1", 0);
        assertEquals(1, value1);
    }

    public void testSettingInt() {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        int value1 = settings.getSettingAsInt("value1", 0);
        assertEquals(0, value1);

        settings.setIntSetting("value1", 1);

        value1 = settings.getSettingAsInt("value1", 0);
        assertEquals(1, value1);
    }

    public void testSettingBoolean() {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        boolean value1 = settings.getSettingAsBoolean("value1", false);
        assertFalse(value1);

        settings.setBooleanSetting("value1", true);

        value1 = settings.getSettingAsBoolean("value1", false);
        assertTrue(value1);
    }

    public void testSettingClass() throws Exception {
        JdbcFileEntrySettings settings = new JdbcFileEntrySettings();
        Class value1 = settings.getSettingAsClass("value1", Class.class);
        assertEquals(Class.class, value1);

        settings.setClassSetting("value1", Object.class);

        value1 = settings.getSettingAsClass("value1", Class.class);
        assertEquals(Object.class, value1);
    }
}
