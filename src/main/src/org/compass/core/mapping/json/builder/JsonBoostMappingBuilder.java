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
import org.compass.core.mapping.json.JsonBoostPropertyMapping;

/**
 * @author kimchy
 */
public class JsonBoostMappingBuilder {

    final JsonBoostPropertyMapping mapping;

    public JsonBoostMappingBuilder(JsonBoostPropertyMapping mapping) {
        this.mapping = mapping;
    }

    public JsonBoostMappingBuilder defaultBoost(float defaultBoost) {
        mapping.setDefaultBoost(defaultBoost);
        return this;
    }

    public JsonBoostMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    public JsonBoostMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    public JsonBoostMappingBuilder converter(ResourcePropertyConverter converter) {
        mapping.setConverter(converter);
        return this;
    }
}