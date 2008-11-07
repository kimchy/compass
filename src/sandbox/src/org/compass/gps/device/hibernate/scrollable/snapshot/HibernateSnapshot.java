/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.gps.device.hibernate.scrollable.snapshot;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A system level (collection of aliases - <code>ScrollableResult</code>s)
 * snapshots. Holds a collection of
 * {@link org.compass.gps.device.hibernate.scrollable.snapshot.HibernateAliasSnapshot}s.
 *
 * @author kimchy
 */
public class HibernateSnapshot implements Serializable {

    private static final long serialVersionUID = 5118761723679749546L;

    private HashMap aliasSnapshots = new HashMap();

    public void putAliasSnapshot(HibernateAliasSnapshot aliasSnapshot) {
        aliasSnapshots.put(aliasSnapshot.getAlias(), aliasSnapshot);
    }

    public HibernateAliasSnapshot getAliasSnapshot(String alias) {
        return (HibernateAliasSnapshot) aliasSnapshots.get(alias);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Snapshot ");
        for (Iterator it = aliasSnapshots.values().iterator(); it.hasNext();) {
            sb.append(it.next());
        }
        return sb.toString();
    }
}
