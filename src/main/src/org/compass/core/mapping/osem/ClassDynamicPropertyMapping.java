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

import java.util.Collection;

import org.compass.core.accessor.Getter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourcePropertyMapping;

/**
 * @author kimchy
 */
public class ClassDynamicPropertyMapping extends AbstractAccessorMapping implements InternalOverrideByNameMapping {

    public static enum ValueType {
        PLAIN,
        ARRAY,
        COLLECTION
    }

    private String namePrefix;

    private String nameProperty;

    private String valueProperty;

    private Getter nameGetter;

    private Getter valueGetter;

    private String nameConverterName;

    private String valueConverterName;

    private ResourcePropertyConverter nameConverter;

    private ResourcePropertyConverter valueConverter;

    private boolean overrideByName = true;

    private ValueType valueType = ValueType.PLAIN;

    private InternalResourcePropertyMapping resourcePropertyMapping = new DynamicResourcePropertyMapping();

    public Mapping copy() {
        ClassDynamicPropertyMapping copy = new ClassDynamicPropertyMapping();
        super.copy(copy);
        copy.setNamePrefix(getNamePrefix());
        copy.setNameProperty(getNameProperty());
        copy.setValueProperty(getValueProperty());
        copy.setNameGetter(getNameGetter());
        copy.setValueGetter(getValueGetter());
        copy.setNameConverterName(getNameConverterName());
        copy.setValueConverterName(getValueConverterName());
        copy.setNameConverter(getNameConverter());
        copy.setValueConverter(getValueConverter());
        copy.setValueType(getValueType());
        if (getResourcePropertyMapping() != null) {
            copy.setResourcePropertyMapping((InternalResourcePropertyMapping) getResourcePropertyMapping().copy());
        }
        return copy;
    }

    public boolean canBeCollectionWrapped() {
        return true;
    }

    public String getNamePrefix() {
        return namePrefix;
    }

    public void setNamePrefix(String namePrefix) {
        this.namePrefix = namePrefix;
    }

    public String getNameProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty = nameProperty;
    }

    public String getValueProperty() {
        return valueProperty;
    }

    public void setValueProperty(String valueProperty) {
        this.valueProperty = valueProperty;
    }

    public Getter getNameGetter() {
        return nameGetter;
    }

    public void setNameGetter(Getter nameGetter) {
        this.nameGetter = nameGetter;
    }

    public Getter getValueGetter() {
        return valueGetter;
    }

    public void setValueGetter(Getter valueGetter) {
        this.valueGetter = valueGetter;
        if (valueGetter != null) {
            if (valueGetter.getReturnType().isArray()) {
                setValueType(ValueType.ARRAY);
            } else if (Collection.class.isAssignableFrom(valueGetter.getReturnType())) {
                setValueType(ValueType.COLLECTION);
            } else {
                setValueType(ValueType.PLAIN);
            }
        }
    }

    public String getNameConverterName() {
        return nameConverterName;
    }

    public void setNameConverterName(String nameConverterName) {
        this.nameConverterName = nameConverterName;
    }

    public String getValueConverterName() {
        return valueConverterName;
    }

    public void setValueConverterName(String valueConverterName) {
        this.valueConverterName = valueConverterName;
    }

    public ResourcePropertyConverter getNameConverter() {
        return nameConverter;
    }

    public void setNameConverter(ResourcePropertyConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    public ResourcePropertyConverter getValueConverter() {
        return valueConverter;
    }

    public void setValueConverter(ResourcePropertyConverter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public InternalResourcePropertyMapping getResourcePropertyMapping() {
        return resourcePropertyMapping;
    }

    public void setResourcePropertyMapping(InternalResourcePropertyMapping resourcePropertyMapping) {
        this.resourcePropertyMapping = resourcePropertyMapping;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    private static class DynamicResourcePropertyMapping extends AbstractResourcePropertyMapping {
        public Mapping copy() {
            DynamicResourcePropertyMapping copy = new DynamicResourcePropertyMapping();
            super.copy(copy);
            return copy;
        }
    }
}
