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
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;

/**
 * A meta data mapping builder that can be used to define resource properties that will be
 * added to the index. Defined on both property and id based searchable mappings.
 *
 * <p>The searchable meta-data is meant to handle basic types (which usually translate to
 * a String saved in the search engine). The conversion is done using converters, with
 * Compass providing converters for most basic types. A specialized Converter can be
 * associated with the meta-data using {@link #converter(String)} or {@link #converter(org.compass.core.converter.mapping.ResourcePropertyConverter)}.
 * The specialized converter will implement the {@link org.compass.core.converter.Converter}
 * interface, usually extending the {@link org.compass.core.converter.basic.AbstractBasicConverter}.
 *
 * <p>Note, that most of the time, a specialized converter for user classes will not be needed,
 * since the {@link SearchableComponentMappingBuilder} usually makes more sense to use.
 *
 * <p>The searchalbe property can annotate a {@link java.util.Collection} type field/property,
 * supporting either {@link java.util.List} or {@link java.util.Set}. The searchable property
 * will try and automatically identify the element type using generics, but if the collection
 * is not defined with generics, {@link SearchablePropertyMappingBuilder#type(Class)} should be used to hint for
 * the collection element type.
 *
 * <p>The searchable property can annotate an array as well, with the array element type used for
 * Converter lookups.
 *
 * @author kimchy
 * @see OSEM#metadata(String)
 * @see SearchablePropertyMappingBuilder#add(SearchableMetaDataMappingBuilder)
 * @see SearchableIdMappingBuilder#add(SearchableMetaDataMappingBuilder)
 */
public class SearchableMetaDataMappingBuilder {

    final ClassPropertyMetaDataMapping mapping;

    public SearchableMetaDataMappingBuilder(String name) {
        mapping = new ClassPropertyMetaDataMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}.
     */
    public SearchableMetaDataMappingBuilder store(Property.Store store) {
        mapping.setStore(store);
        return this;
    }

    /**
     * Specifies whether and how a property should be indexed. Defaults to
     * {@link org.compass.core.Property.Index#ANALYZED}.
     */
    public SearchableMetaDataMappingBuilder index(Property.Index index) {
        mapping.setIndex(index);
        return this;
    }

    /**
     * Specifies whether and how a property should have term vectors. Defaults to
     * {@link org.compass.core.Property.TermVector#NO}.
     */
    public SearchableMetaDataMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. Defaults
     * to <code>false</code>.
     */
    public SearchableMetaDataMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. Defaults to <code>false</code>.
     */
    public SearchableMetaDataMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the property mapping. Defaults to <code>1.0f</code>.
     */
    public SearchableMetaDataMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the format that will be used for formattable capable converters (such as numbers and dates).
     */
    public SearchableMetaDataMappingBuilder format(String format) {
        mapping.setConverter(new FormatDelegateConverter(format));
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the value
     * of the property.
     */
    public SearchableMetaDataMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets an actual converter that will be used to convert this property value.
     */
    public SearchableMetaDataMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets an actual converter that will be used to convert this property value.
     */
    public SearchableMetaDataMappingBuilder converter(ResourcePropertyConverter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public SearchableMetaDataMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Controls if the property will be excluded from all or not.
     */
    public SearchableMetaDataMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets the null value of the property. If the property value is null, will store in the
     * index the provided value.
     */
    public SearchableMetaDataMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public SearchableMetaDataMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}
