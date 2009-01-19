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

package org.compass.core.mapping.rsem;

import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;
import org.compass.core.mapping.support.AbstractResourcePropertyMapping;


/**
 * @author kimchy
 */
public class RawResourcePropertyMapping extends AbstractResourcePropertyMapping implements ResourcePropertyMapping, InternalOverrideByNameMapping {

    private boolean overrideByName = true;

    protected void copy(RawResourcePropertyMapping rawResourcePropertyMapping) {
        super.copy(rawResourcePropertyMapping);
        rawResourcePropertyMapping.setOverrideByName(isOverrideByName());
    }

    public Mapping copy() {
        RawResourcePropertyMapping copy = new RawResourcePropertyMapping();
        copy(copy);
        return copy;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        return (ResourcePropertyConverter) getConverter();
    }
}
