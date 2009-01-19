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

package org.apache.lucene.store.jdbc.index.oracle;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;

import org.apache.lucene.store.jdbc.support.JdbcTable;
import org.compass.core.util.MethodInvoker;

/**
 * @author kimchy
 */
public abstract class OracleIndexOutputHelper {

    public static String sqlInsert(JdbcTable table) {
        return new StringBuffer().append("insert into ").append(table.getQualifiedName())
                .append(" (").append(table.getNameColumn().getQuotedName()).append(", ")
                .append(table.getValueColumn().getQuotedName()).append(", ")
                .append(table.getSizeColumn().getQuotedName()).append(", ")
                .append(table.getLastModifiedColumn().getQuotedName()).append(", ")
                .append(table.getDeletedColumn().getQuotedName())
                .append(") values ( ?, EMPTY_BLOB(), ?, ").append(table.getDialect().getCurrentTimestampFunction()).append(", ?").append(" )").toString();
    }

    public static String sqlUpdate(JdbcTable table) {
        return new StringBuffer().append("select ").append(table.getValueColumn().getQuotedName())
                .append(" as x from ").append(table.getQualifiedName())
                .append(" where ").append(table.getNameColumn().getQuotedName())
                .append(" = ? for update").toString();
    }

    public static OutputStream getBlobOutputStream(ResultSet rs) throws IOException {
        MethodInvoker getBlobMethod = new MethodInvoker();
        getBlobMethod.setTargetMethod("getBLOB");
        getBlobMethod.setTargetObject(rs);
        getBlobMethod.setArguments(new Object[]{"x"});
        Object BLOB;
        try {
            BLOB = getBlobMethod.prepare().invoke();
        } catch (Exception e) {
            throw new RuntimeException("Failed to getBLOB on [" + rs + "]", e);
        }
        MethodInvoker getBinaryOutputStreamMethod = new MethodInvoker();
        getBinaryOutputStreamMethod.setTargetMethod("getBinaryOutputStream");
        getBinaryOutputStreamMethod.setTargetObject(BLOB);
        try {
            return (OutputStream) getBinaryOutputStreamMethod.prepare().invoke();
        } catch (Exception e) {
            throw new RuntimeException("Failed to getBinaryOutputStream on [" + BLOB + "]", e);
        }
    }


}
