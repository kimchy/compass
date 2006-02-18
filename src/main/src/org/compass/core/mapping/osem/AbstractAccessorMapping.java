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

import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.mapping.AbstractMapping;

/**
 * 
 * @author kimchy
 *
 */
public abstract class AbstractAccessorMapping extends AbstractMapping implements ObjectMapping {

    private String accessor;

    private Class objClass;

    private String propertyName;

    private Getter getter;

    private Setter setter;

    protected void copy(AbstractAccessorMapping copy) {
        super.copy(copy);
        copy.setGetter(getGetter());
        copy.setSetter(getSetter());
        copy.setAccessor(getAccessor());
        copy.setObjClass(getObjClass());
        copy.setPropertyName(getPropertyName());
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

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public Class getObjClass() {
        return objClass;
    }

    public void setObjClass(Class objClass) {
        this.objClass = objClass;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
