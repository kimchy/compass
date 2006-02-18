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

/**
 * @author kimchy
 */
public abstract class AbstractRefAliasMapping extends AbstractAccessorMapping implements HasRefAliasMapping {

    private String refAlias;

    private ClassMapping refClassMapping;

    private Class refClass;

    protected void copy(AbstractRefAliasMapping mapping) {
        super.copy(mapping);
        mapping.setRefAlias(getRefAlias());
        mapping.setRefClass(getRefClass());
        mapping.setRefClassMapping(getRefClassMapping());
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

    public Class getRefClass() {
        return refClass;
    }

    public void setRefClass(Class refClass) {
        this.refClass = refClass;
    }
}
