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

package org.compass.core.mapping.json;

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.OverrideByNameMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.support.AbstractResourcePropertyMapping;
import org.compass.core.util.Parameter;

/**
 * @author kimchy
 */
public class JsonPropertyMapping extends AbstractResourcePropertyMapping implements JsonMapping, ResourcePropertyMapping,
        OverrideByNameMapping {

    public static final class NamingType extends Parameter {

        private NamingType(String name) {
            super(name);
        }

        public static final NamingType PLAIN = new NamingType("PLAIN");

        public static final NamingType FULL = new NamingType("FULL");

        public static String toString(NamingType namingType) {
            if (namingType == NamingType.PLAIN) {
                return "plain";
            } else if (namingType == NamingType.FULL) {
                return "full";
            }
            throw new IllegalArgumentException("Can't find naming type for [" + namingType + "]");
        }

        public static NamingType fromString(String namingType) {
            if ("plain".equalsIgnoreCase(namingType)) {
                return NamingType.PLAIN;
            } else if ("full".equalsIgnoreCase(namingType)) {
                return NamingType.FULL;
            }
            throw new IllegalArgumentException("Can't find naming type for [" + namingType + "]");
        }

    }

    private boolean overrideByName;

    private Converter valueConverter;

    private String valueConverterName;

    private String format;

    private String fullPath;

    private NamingType namingType;

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

    public NamingType getNamingType() {
        return namingType;
    }

    public void setNamingType(NamingType namingType) {
        this.namingType = namingType;
    }

    public ResourcePropertyConverter getResourcePropertyConverter() {
        if (valueConverter instanceof ResourcePropertyConverter) {
            return (ResourcePropertyConverter) valueConverter;
        }
        return null;
    }
}