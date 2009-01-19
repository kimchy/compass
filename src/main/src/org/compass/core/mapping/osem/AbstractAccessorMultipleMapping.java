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

import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.mapping.osem.internal.InternalObjectMapping;
import org.compass.core.mapping.support.AbstractMultipleMapping;

/**
 * @author kimchy
 */
public abstract class AbstractAccessorMultipleMapping extends AbstractMultipleMapping implements InternalObjectMapping {

    private Getter getter;

    private Setter setter;

    protected void copy(AbstractAccessorMultipleMapping copy) {
        super.copy(copy);
        copy.setGetter(getGetter());
        copy.setSetter(getSetter());
    }

    public boolean hasAccessors() {
        return true;
    }

    public Getter getGetter() {
        return getter;
    }

    public void setGetter(Getter getter) {
        this.getter = getter;
    }

    public Setter getSetter() {
        return setter;
    }

    public void setSetter(Setter setter) {
        this.setter = setter;
    }

}
