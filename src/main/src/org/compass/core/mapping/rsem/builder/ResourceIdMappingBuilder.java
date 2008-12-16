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

import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.ExcludeFromAllType;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.rsem.RawResourcePropertyIdMapping;

/**
 * A builder allowing to constrcut resource id mapping definition.
 * 
 * @author kimchy
 * @see RSEM#id(String) 
 */
public class ResourceIdMappingBuilder {

    final RawResourcePropertyIdMapping mapping;

    public ResourceIdMappingBuilder(String name) {
        this.mapping = new RawResourcePropertyIdMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setOmitNorms(true);
        mapping.setOmitTf(true);
    }

    public ResourceIdMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    public ResourceIdMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    public ResourceIdMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public ResourceIdMappingBuilder format(String format) {
        mapping.setConverter(new FormatDelegateConverter(format));
        return this;
    }

    public ResourceIdMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    public ResourceIdMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public ResourceIdMappingBuilder converter(ResourcePropertyConverter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public ResourceIdMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public ResourceIdMappingBuilder excludeFromAll(ExcludeFromAllType excludeFromAll) {
        mapping.setExcludeFromAll(excludeFromAll);
        return this;
    }

    public ResourceIdMappingBuilder nullValue(String nullValue) {
        mapping.setNullValue(nullValue);
        return this;
    }

    public ResourceIdMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}