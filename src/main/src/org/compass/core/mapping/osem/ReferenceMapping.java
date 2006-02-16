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
public class ReferenceMapping extends AbstractAccessorMapping implements OverrideByNameMapping {

    private String refAlias;

    private ClassMapping refClassMapping;

    private String refCompAlias;

    private ClassMapping refCompMapping;

    protected void copy(ReferenceMapping copy) {
        super.copy(copy);
        copy.setRefAlias(getRefAlias());
        copy.setRefClassMapping(getRefClassMapping());
        copy.setRefCompAlias(getRefCompAlias());
        copy.setRefCompMapping(getRefCompMapping());
    }

    public Mapping copy() {
        ReferenceMapping copy = new ReferenceMapping();
        copy(copy);
        return copy;
    }

    public boolean canBeCollectionWrapped() {
        return true;
    }

    public String getRefAlias() {
        return refAlias;
    }

    public void setRefAlias(String refAlias) {
        this.refAlias = refAlias;
    }

    public ClassMapping getRefClassMapping() {
        return refClassMapping;
    }

    public void setRefClassMapping(ClassMapping refClassMapping) {
        this.refClassMapping = refClassMapping;
    }

    public boolean isOverrideByName() {
        return true;
    }

    public void setOverrideByName(boolean overrideByName) {
        throw new IllegalArgumentException("The reference mapping always overrides");
    }

    public String getRefCompAlias() {
        return refCompAlias;
    }

    public void setRefCompAlias(String refCompAlias) {
        this.refCompAlias = refCompAlias;
    }

    public ClassMapping getRefCompMapping() {
        return refCompMapping;
    }

    public void setRefCompMapping(ClassMapping refCompMapping) {
        this.refCompMapping = refCompMapping;
    }
}
