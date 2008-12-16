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

package org.compass.core.mapping.json.builder;

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAllType;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.json.JsonIdMapping;
import org.compass.core.mapping.json.NamingType;

/**
 * @author kimchy
 */
public class JsonIdMappingBuilder {

    final JsonIdMapping mapping;

    JsonIdMappingBuilder(JsonIdMapping mapping) {
        this.mapping = mapping;
    }

    public JsonIdMappingBuilder indexName(String indexName) {
        mapping.setPath(new StaticPropertyPath(indexName));
        return this;
    }

    public JsonIdMappingBuilder namingType(NamingType namingType) {
        mapping.setNamingType(namingType);
        return this;
    }

    public JsonIdMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    public JsonIdMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    public JsonIdMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public JsonIdMappingBuilder format(String format) {
        mapping.setValueConverter(new FormatDelegateConverter(format));
        mapping.setFormat(format);
        return this;
    }

    public JsonIdMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    public JsonIdMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public JsonIdMappingBuilder valueConverter(String converterName) {
        mapping.setValueConverterName(converterName);
        return this;
    }

    public JsonIdMappingBuilder valueConverter(Converter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    public JsonIdMappingBuilder valueConverter(ResourcePropertyConverter converter) {
        mapping.setValueConverter(converter);
        return this;
    }

    public JsonIdMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public JsonIdMappingBuilder excludeFromAll(ExcludeFromAllType excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    public JsonIdMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    public JsonIdMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}