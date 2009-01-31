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

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;
import org.compass.core.mapping.support.AbstractResourcePropertyMapping;

/**
 * @author kimchy
 */
public class JsonPropertyMapping extends AbstractResourcePropertyMapping implements JsonMapping, ResourcePropertyMapping,
        InternalOverrideByNameMapping {

    private boolean overrideByName = false;

    private Converter valueConverter;

    private String valueConverterName;

    private String format;

    private String fullPath;

    private Naming namingType; // null value means nothing is set, need to be taken into account

    private boolean dynamic = false;

    public JsonPropertyMapping() {
    }

    public Mapping copy() {
        JsonPropertyMapping xmlPropertyMapping = new JsonPropertyMapping();
        copy(xmlPropertyMapping);
        return xmlPropertyMapping;
    }

    protected void copy(JsonPropertyMapping copy) {
        super.copy(copy);
        copy.setOverrideByName(isOverrideByName());
        copy.setValueConverter(getValueConverter());
        copy.setValueConverterName(getValueConverterName());
        copy.setFormat(getFormat());
        copy.setFullPath(getFullPath());
        copy.setNamingType(getNamingType());
        copy.setDynamic(isDynamic());
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    public boolean isOverrideByName() {
        return this.overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public Converter getValueConverter() {
        return valueConverter;
    }

    public void setValueConverter(Converter valueConverter) {
        this.valueConverter = valueConverter;
    }

    public String getValueConverterName() {
        return valueConverterName;
    }

    public void setValueConverterName(String valueConverterName) {
        this.valueConverterName = valueConverterName;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Naming getNamingType() {
        return namingType;
    }

    public void setNamingType(Naming namingType) {
        this.namingType = namingType;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        if (valueConverter instanceof ResourcePropertyConverter) {
            return (ResourcePropertyConverter) valueConverter;
        }
        return null;
    }
}