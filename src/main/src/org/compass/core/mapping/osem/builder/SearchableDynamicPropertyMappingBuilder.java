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
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;

/**
 * Specifies a searchable dynamic property on property or field of the Searchable class.
 *
 * <p>Dynamic property is a proeprty where the Resource Property name is dynamic in addition to the value itself.
 *
 * <p>The annotation can be placed on a custom type (or an array / collection of it). When used on a custom type,
 * the field/property of the custom type for the dynamic name, and the field/property of the custom type for the dynamic value
 * should be explciitly set using {@link #nameProperty(String)} and {@link #valueProperty(String)}. Another option, instead of setting
 * them, is by using {@link org.compass.annotations.SearchableDynamicName} on the custom type field/property and
 * {@link org.compass.annotations.SearchableDynamicValue} on the custom type field/property.
 *
 * <p>The annotation can also be placed on a {@link java.util.Map} type, where its key will act as the Resource property
 * name and its value will act as the Resource property value. The Map value can also be an array or collection. It is
 * best to use generics when defining the Map.
 *
 * <p>When annotating a Map, the {@link #nameProperty(String)} or {@link #valueConverter(String)} (or their respective annotation)
 * can also be used in case the Map key and/or Map value are custom types.
 *
 * <p>The format for the name can be set using {@link #nameFormat(String)} and the format for the value can be set using
 * {@link #valueFormat(String)}.
 *
 * @author kimchy
 * @see org.compass.core.mapping.osem.builder.OSEM#metadata(String)
 * @see org.compass.core.mapping.osem.builder.SearchableMappingBuilder#add(SearchableDynamicPropertyMappingBuilder)
 */
public class SearchableDynamicPropertyMappingBuilder {

    final ClassDynamicPropertyMapping mapping;

    public SearchableDynamicPropertyMappingBuilder(String name) {
        mapping = new ClassDynamicPropertyMapping();
        mapping.setName(name);
        mapping.setPropertyName(name);
        mapping.setOverrideByName(true);
    }

    /**
     * A prefix that will be prepended to the dynamic proeprty names that will be created.
     */
    public SearchableDynamicPropertyMappingBuilder namePrefix(String namePrefix) {
        mapping.setNamePrefix(namePrefix);
        return this;
    }

    /**
     * When the name is a custom object, the name property can get field/property from it and use it
     * as the resource property name. Note, the field/property can be marked using {@link org.compass.annotations.SearchableDynamicName}
     * annotation as well.
     */
    public SearchableDynamicPropertyMappingBuilder nameProperty(String nameProperty) {
        mapping.setNameProperty(nameProperty);
        return this;
    }

    /**
     * When the name is a custom object, the value property can get field/property from it and use it
     * as the resource property value. Note, the field/property can be marked using {@link org.compass.annotations.SearchableDynamicValue}
     * annotation as well.
     */
    public SearchableDynamicPropertyMappingBuilder valueProperty(String valueProperty) {
        mapping.setValueProperty(valueProperty);
        return this;
    }

    /**
     * The format to apply to the name. Only applies to format-able converters
     * (like dates and numbers).
     */
    public SearchableDynamicPropertyMappingBuilder nameFormat(String nameFormat) {
        mapping.setNameFormat(nameFormat);
        return this;
    }

    /**
     * The format to apply to the value. Only applies to format-able converters
     * (like dates and numbers).
     */
    public SearchableDynamicPropertyMappingBuilder valueFormat(String valueFormat) {
        mapping.setValueFormat(valueFormat);
        return this;
    }

    public SearchableDynamicPropertyMappingBuilder nameConverter(String nameConverter) {
        mapping.setNameConverterName(nameConverter);
        return this;
    }

    public SearchableDynamicPropertyMappingBuilder nameConverter(ResourcePropertyConverter nameConverter) {
        mapping.setNameConverter(nameConverter);
        return this;
    }

    public SearchableDynamicPropertyMappingBuilder valueConverter(String valueConverter) {
        mapping.setValueConverterName(valueConverter);
        return this;
    }

    public SearchableDynamicPropertyMappingBuilder valueConverter(ResourcePropertyConverter valueConverter) {
        mapping.setValueConverter(valueConverter);
        return this;
    }

    /**
     * Sets if this mapping will override another mapping with the same name. Defaults to
     * <code>true</code>.
     */
    public SearchableDynamicPropertyMappingBuilder overrideByName(boolean override) {
        mapping.setOverrideByName(override);
        return this;
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter).
     */
    public SearchableDynamicPropertyMappingBuilder accessor(Accessor accessor) {
        return accessor(accessor.toString());
    }

    /**
     * Sets the acessor the will be used for the class property. Defaults to property (getter
     * and optionally setter). Note, this is the lookup name of a {@link org.compass.core.accessor.PropertyAccessor}
     * registered with Compass, with two default ones (custom ones can be easily added) named <code>field</code>
     * and <code>property</code>.
     */
    public SearchableDynamicPropertyMappingBuilder accessor(String accessor) {
        mapping.setAccessor(accessor);
        return this;
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}.
     */
    public SearchableDynamicPropertyMappingBuilder store(Property.Store store) {
        mapping.getResourcePropertyMapping().setStore(store);
        return this;
    }

    /**
     * Specifies whether and how a property should be indexed. Defaults to
     * {@link org.compass.core.Property.Index#ANALYZED}.
     */
    public SearchableDynamicPropertyMappingBuilder index(Property.Index index) {
        mapping.getResourcePropertyMapping().setIndex(index);
        return this;
    }

    /**
     * Specifies whether and how a property should have term vectors. Defaults to
     * {@link org.compass.core.Property.TermVector#NO}.
     */
    public SearchableDynamicPropertyMappingBuilder termVector(Property.TermVector termVector) {
        mapping.getResourcePropertyMapping().setTermVector(termVector);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. Defaults
     * to <code>false</code>.
     */
    public SearchableDynamicPropertyMappingBuilder omitNorms(boolean omitNorms) {
        mapping.getResourcePropertyMapping().setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. Defaults to <code>false</code>.
     */
    public SearchableDynamicPropertyMappingBuilder omitTf(boolean omitTf) {
        mapping.getResourcePropertyMapping().setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the property mapping. Defaults to <code>1.0f</code>.
     */
    public SearchableDynamicPropertyMappingBuilder boost(float boost) {
        mapping.getResourcePropertyMapping().setBoost(boost);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the value
     * of the property.
     */
    public SearchableDynamicPropertyMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets an actual converter that will be used to convert this property value.
     */
    public SearchableDynamicPropertyMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets the null value of the property. If the property value is null, will store in the
     * index the provided value.
     */
    public SearchableDynamicPropertyMappingBuilder nullValue(String nullValue) {
        mapping.getResourcePropertyMapping().setNullValue(nullValue);
        return this;
    }
}