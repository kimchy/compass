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

package org.compass.gps.device.jdbc.mapping;

import org.compass.core.mapping.ExcludeFromAll;

/**
 * A helper base class for mappings from a jdbc column to a Compass
 * <code>Property</code>. Holds the property name that the column maps to.
 *
 * @author kimchy
 */
public abstract class AbstractColumnToPropertyMapping extends AbstractColumnMapping implements ColumnToPropertyMapping {

    private String propertyName;

    private ExcludeFromAll excludeFromAll = ExcludeFromAll.NO;

    private String analyzer;

    private String converter;

    public AbstractColumnToPropertyMapping() {

    }

    public AbstractColumnToPropertyMapping(String columnName, String propertyName) {
        super(columnName);
        this.propertyName = propertyName;
    }

    public AbstractColumnToPropertyMapping(int columnIndex, String propertyName) {
        super(columnIndex);
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String property) {
        this.propertyName = property;
    }

    public ExcludeFromAll getExcludeFromAll() {
        return excludeFromAll;
    }

    public void setExcludeFromAll(String excludeFromAll) {
        this.excludeFromAll = ExcludeFromAll.fromString(excludeFromAll);
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public String getConverter() {
        return converter;
    }

    public void setConverter(String converter) {
        this.converter = converter;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(super.toString());
        sb.append(" property [");
        sb.append(propertyName);
        sb.append("] store [");
        sb.append(getPropertyStore());
        sb.append("] index [");
        sb.append(getPropertyIndex());
        sb.append("] termVector [");
        sb.append(getPropertyTermVector());
        sb.append("] excludeFromAll [");
        sb.append(getExcludeFromAll());
        sb.append("] analyzer [");
        sb.append(getAnalyzer());
        sb.append("] converter [");
        sb.append(getConverter());
        sb.append("]");
        return sb.toString();
    }
}
