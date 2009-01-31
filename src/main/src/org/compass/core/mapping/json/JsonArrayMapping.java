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

package org.compass.core.mapping.json;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.support.AbstractMapping;

/**
 * @author kimchy
 */
public class JsonArrayMapping extends AbstractMapping implements JsonMapping {

    private Mapping elementMapping;

    private String fullPath;

    private boolean dynamic;

    private Naming dynamicNaming; // can be null, which means its not set

    public Mapping copy() {
        JsonArrayMapping copy = new JsonArrayMapping();
        super.copy(copy);
        if (getElementMapping() != null) {
            copy.setElementMapping(getElementMapping().copy());
        }
        copy.setFullPath(getFullPath());
        copy.setDynamic(isDynamic());
        copy.setDynamicNaming(getDynamicNaming());
        return copy;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public Mapping getElementMapping() {
        return elementMapping;
    }

    public void setElementMapping(Mapping elementMapping) {
        this.elementMapping = elementMapping;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public Naming getDynamicNaming() {
        return dynamicNaming;
    }

    public void setDynamicNaming(Naming dynamicNaming) {
        this.dynamicNaming = dynamicNaming;
    }
}
