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

package org.compass.gps.device.jdbc.snapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An alias (usually <code>ResultSet</code>) level snapshot. Holds a
 * collection of
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcAliasRowSnapshot}s.
 * 
 * @author kimchy
 */
public class JdbcAliasSnapshot implements Serializable {

    private static final long serialVersionUID = 8610812620723482815L;

    private String alias;

    private HashMap<JdbcAliasRowSnapshot, JdbcAliasRowSnapshot> rowEntries = new HashMap<JdbcAliasRowSnapshot, JdbcAliasRowSnapshot>();

    public JdbcAliasSnapshot(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void putRow(JdbcAliasRowSnapshot rowSnapshot) {
        this.rowEntries.put(rowSnapshot, rowSnapshot);
    }

    public JdbcAliasRowSnapshot getRow(JdbcAliasRowSnapshot rowSnapshot) {
        return this.rowEntries.get(rowSnapshot);
    }

    public Iterator<JdbcAliasRowSnapshot> rowSnapshotIt() {
        return this.rowEntries.values().iterator();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("alias [").append(alias).append("]");
        for (JdbcAliasRowSnapshot jdbcAliasRowSnapshot : rowEntries.values()) {
            sb.append(jdbcAliasRowSnapshot);
            sb.append(", ");
        }
        return sb.toString();
    }
}
