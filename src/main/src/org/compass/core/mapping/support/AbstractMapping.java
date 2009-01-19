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

package org.compass.core.mapping.support;

import org.compass.core.converter.Converter;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.internal.InternalMapping;

/**
 * A based implementation for basic mapping in Compass.
 *
 * @author kimchy
 */
public abstract class AbstractMapping implements InternalMapping {

    private String name;

    private PropertyPath path;

    private String converterName;

    private Converter converter;

    protected void copy(InternalMapping copy) {
        copy.setName(getName());
        copy.setPath(getPath());
        copy.setConverterName(getConverterName());
        copy.setConverter(getConverter());
    }

    public boolean controlsObjectNullability() {
        return true;
    }

    public Converter getConverter() {
        return converter;
    }

    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PropertyPath getPath() {
        return path;
    }

    public void setPath(PropertyPath path) {
        this.path = path;
    }

    public String getConverterName() {
        return converterName;
    }

    public void setConverterName(String converterName) {
        this.converterName = converterName;
    }

}
