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

package org.apache.lucene.store.jdbc.dialect;

import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.store.jdbc.JdbcDirectorySettings;
import org.apache.lucene.store.jdbc.JdbcFileEntrySettings;
import org.apache.lucene.store.jdbc.index.FileJdbcIndexOutput;
import org.apache.lucene.store.jdbc.index.RAMAndFileJdbcIndexOutput;
import org.apache.lucene.store.jdbc.index.RAMJdbcIndexOutput;
import org.apache.lucene.store.jdbc.index.oracle.OracleFileJdbcIndexOutput;
import org.apache.lucene.store.jdbc.index.oracle.OracleRAMAndFileJdbcIndexOutput;
import org.apache.lucene.store.jdbc.index.oracle.OracleRAMJdbcIndexOutput;

/**
 * An Oralce 9i dialect, changes all to work with Oracle related
 * index output.
 *
 * @author kimchy
 */
public class Oracle9Dialect extends OracleDialect {

    public void processSettings(JdbcDirectorySettings settings) {
        Map filesEntrySettings = settings.getFileEntrySettings();
        for (Iterator it = filesEntrySettings.values().iterator(); it.hasNext();) {
            JdbcFileEntrySettings fileEntrySettings = (JdbcFileEntrySettings) it.next();
            try {
                Class indexOutputClass = fileEntrySettings.getSettingAsClass(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING,
                        RAMAndFileJdbcIndexOutput.class);
                if (indexOutputClass.equals(RAMAndFileJdbcIndexOutput.class)) {
                    indexOutputClass = OracleRAMAndFileJdbcIndexOutput.class;
                } else if (indexOutputClass.equals(RAMJdbcIndexOutput.class)) {
                    indexOutputClass = OracleRAMJdbcIndexOutput.class;
                } else if (indexOutputClass.equals(FileJdbcIndexOutput.class)) {
                    indexOutputClass = OracleFileJdbcIndexOutput.class;
                }
                fileEntrySettings.setClassSetting(JdbcFileEntrySettings.INDEX_OUTPUT_TYPE_SETTING, indexOutputClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Failed to find class", e);
            }
        }
    }
}
