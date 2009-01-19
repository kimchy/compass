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

package org.compass.core.mapping.osem;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;


/**
 * @author kimchy
 */
public class ComponentMapping extends AbstractRefAliasMapping implements InternalOverrideByNameMapping, RefAliasObjectMapping {

    private boolean overrideByName = true;

    // the depth of cyclic component mappings allowed
    // set by configuration
    private int maxDepth = 5;

    private String prefix;

    public Mapping copy() {
        ComponentMapping copy = new ComponentMapping();
        copy(copy);
        return copy;
    }

    protected void copy(ComponentMapping componentMapping) {
        super.copy(componentMapping);
        componentMapping.setOverrideByName(isOverrideByName());
        componentMapping.setMaxDepth(getMaxDepth());
        componentMapping.setPrefix(getPrefix());
    }

    public boolean canBeCollectionWrapped() {
        return true;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
