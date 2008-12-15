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

import org.compass.core.Property;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.json.JsonContentMapping;

/**
 * @author kimchy
 */
public class JsonContentMappingBuilder {

    final JsonContentMapping mapping;

    public JsonContentMappingBuilder(JsonContentMapping mapping) {
        this.mapping = mapping;
    }

    public JsonContentMappingBuilder store(Property.Store store) {
        if (store == Property.Store.NO) {
            throw new IllegalArgumentException("Content must be stored");
        }
        mapping.setStore(store);
        return this;
    }

    public JsonContentMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    public JsonContentMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }
}