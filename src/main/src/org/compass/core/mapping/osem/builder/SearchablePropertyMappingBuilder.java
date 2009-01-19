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
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ManagedId;

/**
 * Specifies a searchable property on property or field of {@link SearchableMappingBuilder}.
 *
 * <p>In order to make the property searchable, at least one {@link SearchableMetaDataMappingBuilder}
 * must be added to it using {@link #add(SearchableMetaDataMappingBuilder)}. If no meta data are
 * added to the searchable property, a managed id will be created for it (automatically, by default,
 * can be controlled using {@link #managedId(org.compass.core.mapping.osem.ManagedId)}).
 *
 * <p>The searchable property/meta-data is meant to handle basic types (which usually translate to
 * a String saved in the search engine). The conversion is done using converters which can be
 * set on the {@link SearchableMetaDataMappingBuilder}. The managed id converter can also be
 * controlled using one of the <code>managedIdConverter</code> method.
 *
 * <p>Note, that most of the time, a specialized converter for user classes will not be needed,
 * since the {@link SearchableComponentMappingBuilder} usually makes more sense to use.
 *
 * <p>The searchable property can annotate an array/collections as well, with the array element type used for
 * Converter lookups.
 *
 * @author kimchy
 * @see OSEM#property(String)
 * @see SearchableMappingBuilder#add(SearchablePropertyMappingBuilder)
 */
public class SearchablePropertyMappingBuilder {

    final ClassPropertyMapping mapping;

    /**
     * Constructs a new searchable property mapping builder based on the property/field name.
     */
    public SearchablePropertyMappingBuilder(String name) {
        mapping = new ClassPropertyMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(true);
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchablePropertyMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchablePropertyMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * Sets the managed id for the mapping. Managed id controls Compass (optionally) created managed
     * id (mainly used when unmarshalling, when it is set to true). Defaults to the global setting
     * {@link org.compass.core.config.CompassEnvironment.Osem#MANAGED_ID_DEFAULT} which in turn
     * defaults to {@link org.compass.core.mapping.osem.ManagedId#NO_STORE}.
     */
    public SearchablePropertyMappingBuilder managedId(ManagedId managedId) {
        mapping.setManagedId(managedId);
        return this;
    }

    /**
     * Sets the index of the managed id (if it is created). Defaults to the global setting
     * {@link org.compass.core.config.CompassEnvironment.Osem#MANAGED_ID_INDEX}
     */
    public SearchablePropertyMappingBuilder managedIdIndex(Property.Index index) {
        mapping.setManagedIdIndex(index);
        return this;
    }

    /**
     * Sets the converter lookup name that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchablePropertyMappingBuilder managedIdConverter(String converterName) {
        mapping.setManagedIdConverterName(converterName);
        return this;
    }

    /**
     * Sets the converter that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchablePropertyMappingBuilder managedIdConverter(Converter managedIdConverter) {
        mapping.setManagedIdConverter(managedIdConverter);
        return this;
    }

    /**
     * Sets the converter that will be used when converting the managed id. Note,
     * if there is a single meta-data mapping, and it has a special converter set for it, then the
     * managed id converter will be automatically set to it.
     */
    public SearchablePropertyMappingBuilder managedIdConverter(ResourcePropertyConverter converter) {
        mapping.setManagedIdConverter(converter);
        return this;
    }

    /**
     * Sets if this mapping will override another mapping with the same name. Defaults to
     * <code>true</code>.
     */
    public SearchablePropertyMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    /**
     * Sets the mapping converter lookup name. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter}.
     */
    public SearchablePropertyMappingBuilder mappingConverter(String mappingConverter) {
        mapping.setConverterName(mappingConverter);
        return this;
    }

    /**
     * Sets the mapping converter. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassPropertyMappingConverter}.
     */
    public SearchablePropertyMappingBuilder mappingConverter(Converter mappingConverter) {
        mapping.setConverter(mappingConverter);
        return this;
    }

    /**
     * The class type of the property. Mainly used for <code>Collection</code> properties, without
     * specific Generic type parameter.
     */
    public SearchablePropertyMappingBuilder type(Class type) {
        mapping.setClassName(type.getName());
        return this;
    }

    /**
     * Adds an optional meta data mapping.
     */
    public SearchablePropertyMappingBuilder add(SearchableMetaDataMappingBuilder builder) {
        builder.mapping.setPropertyName(mapping.getPropertyName());
        builder.mapping.setAccessor(mapping.getAccessor());
        mapping.addMapping(builder.mapping);
        return this;
    }
}