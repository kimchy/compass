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

import org.compass.core.converter.Converter;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.osem.IdComponentMapping;

/**
 * Specifies a searchable id component on property or field of the {@link SearchableMappingBuilder} class.
 *
 * <p>A searchable id component is a class field/property that reference another class, which
 * content need to be embedded into the content of its searchable class and
 * represents one of its ids.
 *
 * @author kimchy
 */
public class SearchableIdComponentMappingBuilder {

    final IdComponentMapping mapping;

    /**
     * Construct a new id component mapping buidler for the specified searchable class property name.
     */
    public SearchableIdComponentMappingBuilder(String name) {
        mapping = new IdComponentMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    /**
     * The reference alias that points to the searchable class (either defined using
     * annotations or xml). Not required since most of the times it can be automatically
     * detected.
     */
    public SearchableIdComponentMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    /**
     * The depth of cyclic component references allowed. Defaults to 1.
     */
    public SearchableIdComponentMappingBuilder maxDepth(int maxDepth) {
        mapping.setMaxDepth(maxDepth);
        return this;
    }

    /**
     * An optional prefix that will be appended to all the component referenced class mappings.
     */
    public SearchableIdComponentMappingBuilder prefix(String prefix) {
        mapping.setPrefix(prefix);
        return this;
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableIdComponentMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableIdComponentMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * Sets if this mapping will override another mapping with the same name. Defaults to
     * <code>true</code>.
     */
    public SearchableIdComponentMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    /**
     * Controls which operations will cascade from the parent searchable class to the referenced component
     * based class. Defaults to no cascading.
     */
    public SearchableIdComponentMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }

    /**
     * Sets the mapping converter lookup name. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ComponentMappingConverter}.
     */
    public SearchableIdComponentMappingBuilder mappingConverter(String mappingConverter) {
        mapping.setConverterName(mappingConverter);
        return this;
    }

    /**
     * Sets the mapping converter. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ComponentMappingConverter}.
     */
    public SearchableIdComponentMappingBuilder mappingConverter(Converter mappingConverter) {
        mapping.setConverter(mappingConverter);
        return this;
    }
}