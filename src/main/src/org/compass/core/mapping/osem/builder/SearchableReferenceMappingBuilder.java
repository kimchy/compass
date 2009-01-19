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
import org.compass.core.mapping.osem.ReferenceMapping;

/**
 * Specifies a searchable reference on property or field of the {@link SearchableMappingBuilder} class.
 *
 * <p>A searchable reference is a class field/property that reference another class, and the
 * relationship need to be stored by Compass so it can be traversed when getting the class
 * from the index.
 *
 * <p>Compass will end up saving only the ids of the referenced class in the search engine index.
 *
 * <p>The searchalbe reference can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable refrence
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link #refAlias(String[])} should be used to reference the referenced
 * searchable class mapping definitions.
 *
 * <p>The searchable compoent can annotate an array as well, with the array element type used for
 * refernced searchable class mapping definitions.
 *
 * <p>The refence mapping can have a "shadow" component mapping associated with it, if specifing
 * the {@link #refComponentAlias(String)}.
 *
 * @author kimchy
 * @see OSEM#reference(String)
 * @see SearchableMappingBuilder#add(SearchableReferenceMappingBuilder)
 */
public class SearchableReferenceMappingBuilder {

    final ReferenceMapping mapping;

    public SearchableReferenceMappingBuilder(String name) {
        mapping = new ReferenceMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
    }

    /**
     * The reference alias that points to the searchable class (either defined using
     * annotations or xml). Not required since most of the times it can be automatically
     * detected.
     */
    public SearchableReferenceMappingBuilder refAlias(String... refAlias) {
        mapping.setRefAliases(refAlias);
        return this;
    }

    /**
     * Specifies a reference to a searchable component that will be used
     * to embed some of the referenced class searchable content into the
     * field/property searchable class.
     */
    public SearchableReferenceMappingBuilder refComponentAlias(String alias) {
        mapping.setRefCompAlias(alias);
        return this;
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableReferenceMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableReferenceMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * Controls which operations will cascade from the parent searchable class to the referenced component
     * based class. Defaults to no cascading.
     */
    public SearchableReferenceMappingBuilder cascade(Cascade... cascade) {
        mapping.setCascades(cascade);
        return this;
    }

    /**
     * This reference mapping (only in case of collection) will be lazy  or not. By default
     * will be set by the global setting {@link org.compass.core.config.CompassEnvironment.Osem#LAZY_REFERNCE}.
     */
    public SearchableReferenceMappingBuilder lazy(boolean lazy) {
        mapping.setLazy(lazy);
        return this;
    }

    /**
     * Sets the mapping converter lookup name. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ReferenceMappingConverter}.
     */
    public SearchableReferenceMappingBuilder mappingConverter(String mappingConverter) {
        mapping.setConverterName(mappingConverter);
        return this;
    }

    /**
     * Sets the mapping converter. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ReferenceMappingConverter}.
     */
    public SearchableReferenceMappingBuilder mappingConverter(Converter mappingConverter) {
        mapping.setConverter(mappingConverter);
        return this;
    }
}
