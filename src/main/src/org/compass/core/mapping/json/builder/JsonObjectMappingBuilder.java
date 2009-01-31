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
import org.compass.core.mapping.json.Naming;
import org.compass.core.mapping.json.PlainJsonObjectMapping;

/**
 * A builder allowing to constrcut json object mapping definition.
 *
 * @author kimchy
 * @see JSEM#object(String) 
 */
public class JsonObjectMappingBuilder {

    final PlainJsonObjectMapping mapping;

    /**
     * Constructs a new JSON object mapping with the given name. The name can be
     * <code>null</code> when used with array mapping.
     */
    public JsonObjectMappingBuilder(String name) {
        this.mapping = new PlainJsonObjectMapping();
        mapping.setName(name);
        if (name != null) {
            mapping.setPath(new StaticPropertyPath(name));
        }
    }

    /**
     * Should unmapped json elements be added to the search engine automatically (and recursively). Defaults
     * to <code>false</code>.
     */
    public JsonObjectMappingBuilder dynamic(boolean dynamic) {
        mapping.setDynamic(dynamic);
        return this;
    }

    /**
     * Sets how dynamic objects, arrays and properties added through this object will have their respective
     * property names named.
     */
    public JsonObjectMappingBuilder dynamicNaming(Naming dynamicNaming) {
        mapping.setDynamicNaming(dynamicNaming);
        return this;
    }

    /**
     * Adds a json property mapping definition.
     */
    public JsonObjectMappingBuilder add(JsonPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json object mapping definition.
     */
    public JsonObjectMappingBuilder add(JsonObjectMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json array mapping definition.
     */
    public JsonObjectMappingBuilder add(JsonArrayMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
