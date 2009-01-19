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

package org.compass.core.mapping.osem.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ManagedId;

/**
 * Specifies a searchable id on property or field of {@link SearchableMappingBuilder}.
 *
 * <p>A root searchable class must have at least one id (or id component) mapping defined for it.
 *
 * <p>The searchable id can optionally have a {@link SearchableMetaDataMappingBuilder} added to it
 * using {@link #add(SearchableMetaDataMappingBuilder)}. If no meta-data is added to it, or Compass
 * identifies that there is another meta-data with the same name, a managed id (internal) will be
 * created for it so it can be correctly unmarshall it from the index. Note, when support unmarshall
 * is set to <code>false</code> for the searchable class, a managed id will still be created for
 * id mappings (and only for id mappings).
 *
 * @author kimchy
 * @see OSEM#id(String)
 * @see SearchableMappingBuilder#add(SearchableIdMappingBuilder)
 */
public class SearchableIdMappingBuilder {

    final ClassIdPropertyMapping mapping;

    /**
     * Constructs a new searchable id mapping builder based on the property/field name.
     */
    public SearchableIdMappingBuilder(String name) {
        mapping = new ClassIdPropertyMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(true);
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableIdMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableIdMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * Sets the managed id for the mapping. Managed id controls Compass (optionally) created managed
     * id (mainly used when unmarshalling, when it is set to true). Defaults to the global setting
     * {@link org.compass.core.config.CompassEnvironment.Osem#MANAGED_ID_DEFAULT} which in turn
     * defaults to {@link org.compass.core.mapping.osem.ManagedId#NO_STORE}.
     */
    public SearchableIdMappingBuilder managedId(ManagedId managedId) {
        mapping.setManagedId(managedId);
        return this;
    }

    /**
     * Sets the index of the managed id (if it is created). Defaults to the global setting
     * {@link org.compass.core.config.CompassEnvironment.Osem#MANAGED_ID_INDEX}
     */
    public SearchableIdMappingBuilder managedIdIndex(Property.Index index) {
        mapping.setManagedIdIndex(index);
        return this;
    }

    /**
     * Sets the converter lookup name that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchableIdMappingBuilder managedIdConverter(String converter) {
        mapping.setManagedIdConverterName(converter);
        return this;
    }

    /**
     * Sets the converter that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchableIdMappingBuilder managedIdConverter(Converter converter) {
        mapping.setManagedIdConverter(converter);
        return this;
    }

    /**
     * Sets the converter that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchableIdMappingBuilder managedIdConverter(ResourcePropertyConverter converter) {
        mapping.setManagedIdConverter(converter);
        return this;
    }

    /**
     * Sets if this mapping will override another mapping with the same name. Defaults to
     * <code>true</code>.
     */
    public SearchableIdMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    /**
     * Sets the mapping converter lookup name. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter}.
     */
    public SearchableIdMappingBuilder mappingConverter(String mappingConverter) {
        mapping.setConverterName(mappingConverter);
        return this;
    }

    /**
     * Sets the mapping converter. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter}.
     */
    public SearchableIdMappingBuilder mappingConverter(Converter mappingConverter) {
        mapping.setConverter(mappingConverter);
        return this;
    }

    /**
     * Adds an optional meta data mapping.
     */
    public SearchableIdMappingBuilder add(SearchableMetaDataMappingBuilder builder) {
        builder.mapping.setPropertyName(mapping.getPropertyName());
        builder.mapping.setAccessor(mapping.getAccessor());
        mapping.addMapping(builder.mapping);
        return this;
    }
}
