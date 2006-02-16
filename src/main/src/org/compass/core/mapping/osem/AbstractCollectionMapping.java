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

package org.compass.core.mapping.osem;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.OverrideByNameMapping;

/**
 * @author kimchy
 */
public abstract class AbstractCollectionMapping extends AbstractAccessorMapping implements OverrideByNameMapping {

    private String colClassPath;

    private String colSizePath;

    private Mapping elementMapping;

    private boolean overrideByName;

    private Class colClass;

    public void copy(AbstractCollectionMapping copy) {
        super.copy(copy);
        copy.setElementMapping(getElementMapping());
        copy.setColClassPath(getColClassPath());
        copy.setColSizePath(getColSizePath());
        copy.setOverrideByName(isOverrideByName());
        copy.setColClass(getColClass());
    }

    public boolean canBeCollectionWrapped() {
        return false;
    }

    public Mapping getElementMapping() {
        return elementMapping;
    }

    public void setElementMapping(Mapping elementMapping) {
        this.elementMapping = elementMapping;
    }

    public String getColClassPath() {
        return colClassPath;
    }

    public void setColClassPath(String colClassPath) {
        this.colClassPath = colClassPath;
    }

    public String getColSizePath() {
        return colSizePath;
    }

    public void setColSizePath(String colSizePath) {
        this.colSizePath = colSizePath;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public Class getColClass() {
        return colClass;
    }

    public void setColClass(Class colClass) {
        this.colClass = colClass;
    }
}
