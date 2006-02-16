/*
 * Copyright 2004-2006 the original author or authors.
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

package org.compass.core.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author kimchy
 */
public abstract class AbstractMultipleMapping extends AbstractMapping implements MultipleMapping {

    protected ArrayList mappings = new ArrayList();

    protected HashMap mappingsMap = new HashMap();

    public void removeExistingByPath(Mapping mapping) {
        if (mappingsMap.get(mapping.getPath()) != null) {
            for (int i = 0; i < mappings.size(); i++) {
                Mapping tempMapping = (Mapping) mappings.get(i);
                if (tempMapping.getPath() != null && tempMapping.getPath().equals(mapping.getPath())) {
                    mappings.remove(i);
                    break;
                }
            }
            mappingsMap.remove(mapping.getPath());
        }
    }

    public void removeExistingByName(Mapping mapping) {
        if (mappingsMap.get(mapping.getName()) != null) {
            for (int i = 0; i < mappings.size(); i++) {
                Mapping tempMapping = (Mapping) mappings.get(i);
                if (tempMapping.getName() != null && tempMapping.getName().equals(mapping.getName())) {
                    mappings.remove(i);
                    break;
                }
            }
            mappingsMap.remove(mapping.getName());
        }
    }

    public int addMapping(Mapping mapping) {
        removeExistingByPath(mapping);
        if (mapping instanceof OverrideByNameMapping) {
            if (((OverrideByNameMapping) mapping).isOverrideByName()) {
                removeExistingByName(mapping);
            }
        }
        mappingsMap.put(mapping.getName(), mapping);
        mappings.add(mapping);
        return mappings.size() - 1;
    }

    public Iterator mappingsIt() {
        return mappings.iterator();
    }

    public int mappingsSize() {
        return mappings.size();
    }

    public Mapping getMapping(String name) {
        return (Mapping) mappingsMap.get(name);
    }

    public Mapping getMapping(int index) {
        return (Mapping) mappings.get(index);
    }

    public void clearMappings() {
        mappings.clear();
        mappingsMap.clear();
    }

    protected void copy(MultipleMapping mapping) {
        super.copy(mapping);
        for (Iterator it = mappingsIt(); it.hasNext();) {
            mapping.addMapping(((Mapping) it.next()).copy());
        }
    }

    protected void shallowCopy(MultipleMapping mapping) {
        super.copy(mapping);
    }
}
