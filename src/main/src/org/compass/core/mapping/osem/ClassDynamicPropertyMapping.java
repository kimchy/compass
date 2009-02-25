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

    public static enum ObjectType {
        PLAIN,
        ARRAY,
        COLLECTION,
        MAP
    }

    private String namePrefix;

    private String nameProperty;

    private String valueProperty;

    private Getter nameGetter;

    private Getter valueGetter;

    private String nameConverterName;

    private String valueConverterName;

    private String nameFormat;

    private String valueFormat;

    private ResourcePropertyConverter nameConverter;

    private ResourcePropertyConverter valueConverter;

    private boolean overrideByName = true;

    private ValueType valueType;

    private ValueType mapValueType;

    private ObjectType objectType;

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
        copy.setMapValueType(getMapValueType());
        copy.setObjectType(getObjectType());
        copy.setNameFormat(getNameFormat());
        copy.setValueFormat(getValueFormat());
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

    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public ValueType getMapValueType() {
        return mapValueType;
    }

    public void setMapValueType(ValueType mapValueType) {
        this.mapValueType = mapValueType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public void setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    private static class DynamicResourcePropertyMapping extends AbstractResourcePropertyMapping {
        public Mapping copy() {
            DynamicResourcePropertyMapping copy = new DynamicResourcePropertyMapping();
            super.copy(copy);
            return copy;
        }
    }
}
