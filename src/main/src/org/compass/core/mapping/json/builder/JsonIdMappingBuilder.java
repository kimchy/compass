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

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.json.JsonIdMapping;

/**
 * A builder allowing to constrcut json id mapping definition.
 *
 * @author kimchy
 * @see JSEM#id(String)
 */
public class JsonIdMappingBuilder {

    final JsonIdMapping mapping;

    /**
     * Constructs a new json id mapping builder for the specified name.
     */
    public JsonIdMappingBuilder(String name) {
        this.mapping = new JsonIdMapping();
        mapping.setName(name);
        mapping.setOmitNorms(true);
        mapping.setOmitTf(true);
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public JsonIdMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. By
     * default, it is set for id mapping.
     */
    public JsonIdMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. By default, it is set for
     * id mapping.
     */
    public JsonIdMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the id property mapping. Defaults to <code>1.0f</code>.
     */
    public JsonIdMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the format that will be used for formattable capable converters (such as numbers and dates).
     */
    public JsonIdMappingBuilder format(String format) {
        mapping.setValueConverter(new FormatDelegateConverter(format));
        mapping.setFormat(format);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the JSON
     * property. Defaults to {@link org.compass.core.converter.mapping.json.JsonIdMappingConverter}.
     */
    public JsonIdMappingBuilder mappingConverter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the JSON
     * property. Defaults to {@link org.compass.core.converter.mapping.json.JsonIdMappingConverter}.
     */
    public JsonIdMappingBuilder mappingConverter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonIdMappingBuilder valueConverter(String converterName) {
        mapping.setValueConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonIdMappingBuilder valueConverter(Converter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the actual
     * value of the json property. Detaults to {@link org.compass.core.converter.json.SimpleJsonValueConverter}.
     */
    public JsonIdMappingBuilder valueConverter(ResourcePropertyConverter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    /**
     * Controls if the id property will be excluded from all or not.
     */
    public JsonIdMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public JsonIdMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}