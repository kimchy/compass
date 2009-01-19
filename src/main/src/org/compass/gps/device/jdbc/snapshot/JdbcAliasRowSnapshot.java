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
import java.util.ArrayList;
import java.util.List;

/**
 * A row level snapshot holder. Holds a list of id values (as Strings) and a
 * list of version values (as Long).
 * <p>
 * <code>hashCode</code> and <code>equals</code> uses the ids alone to
 * perform the operations.
 * <p>
 * A utility method {@link #isOlderThan(JdbcAliasRowSnapshot)} is provides to
 * compare it with other row snapshots.
 * 
 * @author kimchy
 */
public class JdbcAliasRowSnapshot implements Serializable {

    private static final long serialVersionUID = 4300559727598252558L;

    private ArrayList<String> ids = new ArrayList<String>();

    private ArrayList<Long> versions = new ArrayList<Long>();

    public void addIdValue(String idValue) {
        ids.add(idValue);
    }

    public List<String> getIds() {
        return ids;
    }

    public void addVersionValue(Long versionValue) {
        versions.add(versionValue);
    }

    public boolean isOlderThan(JdbcAliasRowSnapshot rowSnapshot) {
        for (int i = 0; i < versions.size(); i++) {
            if ((versions.get(i)).compareTo(rowSnapshot.versions.get(i)) < 0) {
                return true;
            }
        }
        return false;
    }

    public boolean equals(Object other) {
        JdbcAliasRowSnapshot otherRow = (JdbcAliasRowSnapshot) other;
        if (otherRow.ids.size() == 1) {
            return ids.get(0).equals(otherRow.ids.get(0));
        } else {
            if (ids.size() != otherRow.ids.size()) {
                return false;
            }
            for (int i = 0; i < ids.size(); i++) {
                if (!ids.get(i).equals(otherRow.ids.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int result;
        result = ids.get(0).hashCode();
        if (ids.size() > 1) {
            for (int i = 1; i < ids.size(); i++) {
                result = 7 * result + ids.get(i).hashCode();
            }
        }
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ids [");
        for (String id : ids) {
            sb.append(id);
            sb.append(",");
        }
        sb.append("] Versions [");
        for (Long version : versions) {
            sb.append(version);
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
