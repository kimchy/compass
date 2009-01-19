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
import org.compass.core.metadata.MetaData;
import org.compass.core.metadata.MetaDataGroup;

/**
 * 
 * @author kimchy
 * 
 */

public class DefaultMetaDataGroup extends AbstractMetaDataItem implements MetaDataGroup {

    private HashMap aliasMap = new HashMap();

    private HashMap metaDataMap = new HashMap();

    public void addAlias(Alias alias) {
        aliasMap.put(alias.getId(), alias);
    }

    public Alias getAlias(String id) {
        return (Alias) aliasMap.get(id);
    }

    public Iterator aliasIterator() {
        return aliasMap.values().iterator();
    }

    public void addMetaData(MetaData metaData) {
        metaDataMap.put(metaData.getId(), metaData);
    }

    public MetaData getMetaData(String id) {
        return (MetaData) metaDataMap.get(id);
    }

    public Iterator metaDataIterator() {
        return metaDataMap.values().iterator();
    }

    public MetaDataGroup copy() {
        DefaultMetaDataGroup copyGroup = new DefaultMetaDataGroup();
        copy(copyGroup);
        for (Iterator it = aliasIterator(); it.hasNext();) {
            DefaultAlias alias = (DefaultAlias) it.next();
            alias = (DefaultAlias) alias.copy();
            alias.setGroup(copyGroup);
            copyGroup.addAlias(alias);
        }

        for (Iterator it = metaDataIterator(); it.hasNext();) {
            DefaultMetaData metaData = (DefaultMetaData) it.next();
            metaData = (DefaultMetaData) metaData.copy();
            metaData.setGroup(copyGroup);
            copyGroup.addMetaData(metaData);
        }

        return copyGroup;
    }
}
