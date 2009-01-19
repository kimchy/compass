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

package org.compass.core.metadata.impl;

import java.util.HashMap;
import java.util.Iterator;

import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.metadata.MetaData;
import org.compass.core.metadata.MetaDataGroup;

/**
 * 
 * @author kimchy
 * 
 */

public class DefaultCompassMetaData implements CompassMetaData {

    private HashMap aliasMap = new HashMap();

    private HashMap metaDataMap = new HashMap();

    private HashMap groupMap = new HashMap();

    public Alias getAlias(String name) {
        return (Alias) aliasMap.get(name);
    }

    public MetaData getMetaData(String name) {
        return (MetaData) metaDataMap.get(name);
    }

    public void addGroup(MetaDataGroup group) {
        groupMap.put(group.getId(), group);
        for (Iterator it = ((DefaultMetaDataGroup) group).aliasIterator(); it.hasNext();) {
            Alias alias = (Alias) it.next();
            aliasMap.put(alias.getId(), alias);
        }

        for (Iterator it = ((DefaultMetaDataGroup) group).metaDataIterator(); it.hasNext();) {
            MetaData metaData = (MetaData) it.next();
            metaDataMap.put(metaData.getId(), metaData);
        }
    }

    public MetaDataGroup getGroup(String name) {
        return (MetaDataGroup) groupMap.get(name);
    }

    public Iterator groupsIterator() {
        return groupMap.values().iterator();
    }

    public CompassMetaData copy() {
        DefaultCompassMetaData copyMetaData = new DefaultCompassMetaData();
        for (Iterator it = groupMap.values().iterator(); it.hasNext();) {
            DefaultMetaDataGroup group = (DefaultMetaDataGroup) it.next();
            copyMetaData.addGroup(group.copy());
        }
        return copyMetaData;
    }
}
