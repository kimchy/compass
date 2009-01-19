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

package org.compass.core.mapping.json.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.Naming;

/**
 * A builder allowing to constrcut json property mapping definition.
 *
 * @author kimchy
 * @see JSEM#property(String)
 */
public class JsonPropertyMappingBuilder {

    final JsonPropertyMapping mapping;

    /**
     * Constructs a new JSON property mapping with the given name. The name can be
     * <code>null</code> when used with array mapping.
     */
    public JsonPropertyMappingBuilder(String name) {
        this.mapping = new JsonPropertyMapping();
        mapping.setName(name);
        if (name != null) {
            mapping.setPath(new StaticPropertyPath(name));
        }
    }

    /**
     * The name of the resource property that will be stored in the index. Defaults to the element name.
     */
    public JsonPropertyMappingBuilder indexName(String indexName) {
        mapping.setPath(new StaticPropertyPath(indexName));
        return this;
    }

    /**
     * Controls the resource property name that will be used. Default to {@link org.compass.core.mapping.json.Naming#PLAIN} which means
     * that only the element name / index name will be stored. If {@link org.compass.core.mapping.json.Naming#FULL} is set, will use the full
     * path (element1.element2.) to be stored in as the property name.
     */
    public JsonPropertyMappingBuilder namingType(Naming namingType) {
        mapping.setNamingType(namingType);
        return this;
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}.
     */
    public JsonPropertyMappingBuilder store(Property.Store store) {
        mapping.setStore(store);
        return this;
    }

    /**
     * Specifies whether and how a property should be indexed. Defaults to
     * {@link org.compass.core.Property.Index#ANALYZED}.
     */
    public JsonPropertyMappingBuilder index(Property.Index index) {
        mapping.setIndex(index);
        return this;
    }

    /**
     * Specifies whether and how a property should have term vectors. Defaults to
     * {@link org.compass.core.Property.TermVector#NO}.
     */
    public JsonPropertyMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. Defaults
     * to <code>false</code>.
     */
    public JsonPropertyMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. Defaults to <code>false</code>.
     */
    public JsonPropertyMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the property mapping. Defaults to <code>1.0f</code>.
     */
    public JsonPropertyMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the format that will be used for formattable capable converters (such as numbers and dates).
     */
    public JsonPropertyMappingBuilder format(String format) {
        mapping.setValueConverter(new FormatDelegateConverter(format));
        mapping.setFormat(format);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the JSON
     * property. Defaults to {@link org.compass.core.converter.mapping.json.JsonPropertyMappingConverter}.
     */
    public JsonPropertyMappingBuilder mappingConverter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the JSON
     * property. Defaults to {@link org.compass.core.converter.mapping.json.JsonPropertyMappingConverter}.
     */
    public JsonPropertyMappingBuilder mappingConverter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonPropertyMappingBuilder valueConverter(String converterName) {
        mapping.setValueConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonPropertyMappingBuilder valueConverter(Converter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonPropertyMappingBuilder valueConverter(ResourcePropertyConverter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public JsonPropertyMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Controls if the property will be excluded from all or not.
     */
    public JsonPropertyMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets the null value of the property. If the property value is null, will store in the
     * index the provided value.
     */
    public JsonPropertyMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public JsonPropertyMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}