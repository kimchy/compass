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

package org.compass.core.mapping.rsem.builder;

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;

/**
 * A builder allowing to constrcut resource id mapping definition.
 *
 * @author kimchy
 * @see RSEM#id(String)
 */
public class ResourceIdMappingBuilder {

    final RawResourcePropertyIdMapping mapping;

    /**
     * Constructs a new resource id mapping builder for the specified name.
     */
    public ResourceIdMappingBuilder(String name) {
        this.mapping = new RawResourcePropertyIdMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setOmitNorms(true);
        mapping.setOmitTf(true);
    }

    /**
     * Sets the analyzer logical name that will be used to analyzer the property value. The name
     * is a lookup name for an Analyzer that is registered with Compass.
     */
    public ResourceIdMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * If set, omit normalization factors associated with this indexed field.
     * This effectively disables indexing boosts and length normalization for this field. By
     * default, it is set for id mapping.
     */
    public ResourceIdMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * If set, omit tf from postings of this indexed property. By default, it is set for
     * id mapping.
     */
    public ResourceIdMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * Sets the boost value for the id property mapping. Defaults to <code>1.0f</code>.
     */
    public ResourceIdMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the format that will be used for formattable capable converters (such as numbers and dates).
     */
    public ResourceIdMappingBuilder format(String format) {
        mapping.setConverter(new FormatDelegateConverter(format));
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the value
     * of the property.
     */
    public ResourceIdMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets an actual converter that will be used to convert this property value.
     */
    public ResourceIdMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Sets an actual converter that will be used to convert this property value.
     */
    public ResourceIdMappingBuilder converter(ResourcePropertyConverter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Controls if the id property will be excluded from all or not.
     */
    public ResourceIdMappingBuilder excludeFromAll(ExcludeFromAll excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    /**
     * Sets the spell check specific setting for the mapping.
     */
    public ResourceIdMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}