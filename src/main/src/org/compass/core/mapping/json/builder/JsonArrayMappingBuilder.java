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

import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.Naming;

/**
 * A builder allowing to constrcut json array mapping definition.
 *
 * @author kimchy
 * @see JSEM#array(String)
 */
public class JsonArrayMappingBuilder {

    final JsonArrayMapping mapping;

    /**
     * Constructs a new JSON array mapping with the given name. The name can be
     * <code>null</code> when used with array mapping.
     */
    public JsonArrayMappingBuilder(String name) {
        this.mapping = new JsonArrayMapping();
        mapping.setName(name);
        mapping.setPath(new StaticPropertyPath(name));
    }

    /**
     * The name of the resource property that will be stored in the index. Defaults to the element name.
     */
    public JsonArrayMappingBuilder indexName(String indexName) {
        mapping.setPath(new StaticPropertyPath(indexName));
        return this;
    }

    /**
     * Should unmapped json elements be added to the search engine automatically (and recursively). Defaults
     * to <code>false</code>.
     */
    public JsonArrayMappingBuilder dynamic(boolean dynamic) {
        mapping.setDynamic(dynamic);
        return this;
    }

    /**
     * Sets how dynamic objects, arrays and properties added through this object will have their respective
     * property names named.
     */
    public JsonArrayMappingBuilder dynamicNaming(Naming dynamicNaming) {
        mapping.setDynamicNaming(dynamicNaming);
        return this;
    }

    /**
     * Sets the array json element to a json property mapping definition.
     *
     * @see JSEM#property()
     */
    public JsonArrayMappingBuilder element(JsonPropertyMappingBuilder builder) {
        if (builder.mapping.getName() == null) {
            builder.mapping.setName(mapping.getName());
        }
        if (builder.mapping.getPath() == null) {
            builder.mapping.setPath(mapping.getPath());
        }
        mapping.setElementMapping(builder.mapping);
        return this;
    }

    /**
     * Sets the array json element to a json object mapping definition.
     *
     * @see JSEM#object()
     */
    public JsonArrayMappingBuilder element(JsonObjectMappingBuilder builder) {
        if (builder.mapping.getName() == null) {
            builder.mapping.setName(mapping.getName());
        }
        if (builder.mapping.getPath() == null) {
            builder.mapping.setPath(mapping.getPath());
        }
        mapping.setElementMapping(builder.mapping);
        return this;
    }

    /**
     * Sets the array json element to a json array mapping definition.
     *
     * @see JSEM#array()
     */
    public JsonArrayMappingBuilder element(JsonArrayMappingBuilder builder) {
        if (builder.mapping.getName() == null) {
            builder.mapping.setName(mapping.getName());
        }
        if (builder.mapping.getPath() == null) {
            builder.mapping.setPath(mapping.getPath());
        }
        mapping.setElementMapping(builder.mapping);
        return this;
    }
}
