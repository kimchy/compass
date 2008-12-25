/*
 * Copyright 2004-2008 the original author or authors.
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

package org.compass.core.mapping.osem.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.ExcludeFromAllType;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ManagedId;

/**
 * @author kimchy
 */
public class ClassPropertyMappingBuilder {

    final ClassPropertyMapping mapping;

    public ClassPropertyMappingBuilder(String name) {
        mapping = new ClassPropertyMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(true);
    }

    public ClassPropertyMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    public ClassPropertyMappingBuilder excludeFromAll(ExcludeFromAllType excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    public ClassPropertyMappingBuilder managedId(ManagedId managedId) {
        mapping.setManagedId(managedId);
        return this;
    }

    public ClassPropertyMappingBuilder managedIdIndex(Property.Index index) {
        mapping.setManagedIdIndex(index);
        return this;
    }

    public ClassPropertyMappingBuilder managedIdConverter(String converterName) {
        mapping.setManagedIdConverterName(converterName);
        return this;
    }

    public ClassPropertyMappingBuilder managedIdConverter(Converter managedIdConverter) {
        mapping.setManagedIdConverter(managedIdConverter);
        return this;
    }

    public ClassPropertyMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    public ClassPropertyMappingBuilder mappingConverter(String mappingConverter) {
        mapping.setConverterName(mappingConverter);
        return this;
    }

    public ClassPropertyMappingBuilder mappingConverter(Converter mappingConverter) {
        mapping.setConverter(mappingConverter);
        return this;
    }

    public ClassPropertyMappingBuilder add(ClassMetaDataMappingBuilder builder) {
        builder.mapping.setPropertyName(mapping.getPropertyName());
        builder.mapping.setAccessor(mapping.getAccessor());
        mapping.addMapping(builder.mapping);
        return this;
    }
}