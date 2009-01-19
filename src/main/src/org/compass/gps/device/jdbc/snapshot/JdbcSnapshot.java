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

/**
 * A system level (collection of aliases - <code>ResultSet</code>s)
 * snapshots. Holds a collection of
 * {@link org.compass.gps.device.jdbc.snapshot.JdbcAliasSnapshot}s.
 * 
 * @author kimchy
 */
public class JdbcSnapshot implements Serializable {

    private static final long serialVersionUID = 5118761723679749546L;

    private HashMap<String, JdbcAliasSnapshot> aliasSnapshots = new HashMap<String, JdbcAliasSnapshot>();

    public void putAliasSnapshot(JdbcAliasSnapshot aliasSnapshot) {
        aliasSnapshots.put(aliasSnapshot.getAlias(), aliasSnapshot);
    }

    public JdbcAliasSnapshot getAliasSnapshot(String alias) {
        return aliasSnapshots.get(alias);
    }

    public JdbcAliasSnapshot removeAliasSnapshot(String alias) {
        return aliasSnapshots.remove(alias);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Snapshot ");
        for (JdbcAliasSnapshot jdbcAliasSnapshot : aliasSnapshots.values()) {
            sb.append(jdbcAliasSnapshot);
        }
        return sb.toString();
    }
}
