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

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.internal.InternalOverrideByNameMapping;

/**
 * @author kimchy
 */
public class ClassPropertyMapping extends AbstractAccessorMultipleMapping implements InternalOverrideByNameMapping {

    private static final int ID_NOT_SET_VALUE = -1;

    private String className;

    private float boost = 1.0f;

    private int idPropertyIndex = ID_NOT_SET_VALUE;

    private ManagedId managedId;

    private Converter managedIdConverter;

    private String managedIdConverterName;

    private ExcludeFromAll excludeFromAll = ExcludeFromAll.NO;

    private String analyzer;

    private boolean overrideByName = true;

    private Property.Index managedIdIndex;

    private String accessor;

    private String propertyName;

    private String definedInAlias;

    protected void copy(ClassPropertyMapping mapping) {
        super.copy(mapping);
        mapping.setClassName(getClassName());
        mapping.setBoost(getBoost());
        mapping.setManagedId(getManagedId());
        mapping.setIdPropertyIndex(getIdPropertyIndex());
        mapping.setExcludeFromAll(getExcludeFromAll());
        mapping.setAnalyzer(getAnalyzer());
        mapping.setOverrideByName(isOverrideByName());
        mapping.setManagedIdIndex(getManagedIdIndex());
        mapping.setAccessor(getAccessor());
        mapping.setPropertyName(getPropertyName());
        mapping.setManagedIdConverter(getManagedIdConverter());
        mapping.setManagedIdConverterName(getManagedIdConverterName());
        mapping.setDefinedInAlias(getDefinedInAlias());
    }

    public Mapping copy() {
        ClassPropertyMapping copy = new ClassPropertyMapping();
        copy(copy);
        return copy;
    }

    public boolean canBeCollectionWrapped() {
        return true;
    }

    public ClassPropertyMetaDataMapping getIdMapping() {
        if (!isIdPropertySet()) {
            return null;
        }
        return (ClassPropertyMetaDataMapping) getMapping(idPropertyIndex);
    }

    public float getBoost() {
        return boost;
    }

    public void setBoost(float boost) {
        this.boost = boost;
    }

    public ManagedId getManagedId() {
        return managedId;
    }

    public void setManagedId(ManagedId managedId) {
        this.managedId = managedId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getIdPropertyIndex() {
        return idPropertyIndex;
    }

    public void setIdPropertyIndex(int idPropertyIndex) {
        this.idPropertyIndex = idPropertyIndex;
    }

    public boolean isIdPropertySet() {
        return idPropertyIndex != ID_NOT_SET_VALUE;
    }

    public ExcludeFromAll getExcludeFromAll() {
        return excludeFromAll;
    }

    public void setExcludeFromAll(ExcludeFromAll excludeFromAll) {
        this.excludeFromAll = excludeFromAll;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public void setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
    }

    public boolean isOverrideByName() {
        return overrideByName;
    }

    public void setOverrideByName(boolean overrideByName) {
        this.overrideByName = overrideByName;
    }

    public Property.Index getManagedIdIndex() {
        return managedIdIndex;
    }

    public void setManagedIdIndex(Property.Index managedIdIndex) {
        this.managedIdIndex = managedIdIndex;
    }

    public String getAccessor() {
        return accessor;
    }

    public void setAccessor(String accessor) {
        this.accessor = accessor;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Converter getManagedIdConverter() {
        return managedIdConverter;
    }

    public void setManagedIdConverter(Converter managedIdConverter) {
        this.managedIdConverter = managedIdConverter;
    }

    public String getManagedIdConverterName() {
        return managedIdConverterName;
    }

    public void setManagedIdConverterName(String managedIdConverterName) {
        this.managedIdConverterName = managedIdConverterName;
    }

    public String getDefinedInAlias() {
        return definedInAlias;
    }

    public void setDefinedInAlias(String definedInAlias) {
        this.definedInAlias = definedInAlias;
    }

    /**
     * Returns if the mapping must have id processing performed on it (even, for example, when support
     * unmarshall is set to false).
     */
    public boolean requiresIdProcessing() {
        return false;
    }
}
