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

package org.compass.core.mapping.rsem.builder;

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.mapping.ExcludeFromAllType;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;

/**
 * @author kimchy
 */
public class ResourcePropertyMappingBuilder {

    final RawResourcePropertyMapping mapping;

    ResourcePropertyMappingBuilder(RawResourcePropertyMapping mapping) {
        this.mapping = mapping;
    }

    public ResourcePropertyMappingBuilder store(Property.Store store) {
        mapping.setStore(store);
        return this;
    }

    public ResourcePropertyMappingBuilder index(Property.Index index) {
        mapping.setIndex(index);
        return this;
    }

    public ResourcePropertyMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    public ResourcePropertyMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    public ResourcePropertyMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    public ResourcePropertyMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public ResourcePropertyMappingBuilder format(String format) {
        mapping.setConverter(new FormatDelegateConverter(format));
        return this;
    }

    public ResourcePropertyMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    public ResourcePropertyMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public ResourcePropertyMappingBuilder converter(ResourcePropertyConverter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public ResourcePropertyMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public ResourcePropertyMappingBuilder excludeFromAll(ExcludeFromAllType excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    public ResourcePropertyMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    public ResourcePropertyMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}
