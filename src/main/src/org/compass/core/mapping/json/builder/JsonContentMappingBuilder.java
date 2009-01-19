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
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.json.JsonContentMapping;

/**
 * A builder allowing to constrcut json content mapping definition allowing to
 * store the actual JSON content withn the index to and be able to rebuild the
 * {@link org.compass.core.json.JsonObject} back from the index.
 *
 * @author kimchy
 * @see JSEM#content(String)
 */
public class JsonContentMappingBuilder {

    final JsonContentMapping mapping;

    /**
     * Constructs a new JSON content mapping with the given name. The JSON
     * string will be stored as a property within the Resource in the index
     * under the name.
     */
    public JsonContentMappingBuilder(String name) {
        this.mapping = new JsonContentMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
        mapping.setInternal(true);
    }

    /**
     * Specifies whether and how a property will be stored. Deftauls to
     * {@link org.compass.core.Property.Store#YES}. Note, {@link org.compass.core.Property.Store#NO}
     * is not valid here.
     */
    public JsonContentMappingBuilder store(Property.Store store) {
        if (store == Property.Store.NO) {
            throw new IllegalArgumentException("Content must be stored");
        }
        mapping.setStore(store);
        return this;
    }

    /**
     * Sets the lookup converter name (registered with Compass) that will be used to convert the JSON
     * content. Defaults to {@link org.compass.core.converter.mapping.json.JsonContentMappingConverter}.
     */
    public JsonContentMappingBuilder converter(String converterName) {
        mapping.setConverterName(converterName);
        return this;
    }

    /**
     * Sets the actual converter that will be used to convert the JSON
     * content. Defaults to {@link org.compass.core.converter.mapping.json.JsonContentMappingConverter}.
     */
    public JsonContentMappingBuilder converter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }
}