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

/**
 * @author kimchy
 */
public abstract class JSEM {

    private JSEM() {

    }

    public static RootJsonObjectMappingBuilder json(String alias) {
        return new RootJsonObjectMappingBuilder(alias);
    }

    public static JsonAnalyzerMappingBuilder analyzer(String name) {
        return new JsonAnalyzerMappingBuilder(name);
    }

    public static JsonIdMappingBuilder id(String name) {
        return new JsonIdMappingBuilder(name);
    }

    /**
     * Used with array.
     */
    public static JsonPropertyMappingBuilder property() {
        return property(null);
    }

    public static JsonPropertyMappingBuilder property(String name) {
        return new JsonPropertyMappingBuilder(name);
    }

    public static JsonContentMappingBuilder content(String name) {
        return new JsonContentMappingBuilder(name);
    }

    public static PlainJsonObjectMappingBuilder object() {
        return object(null);
    }

    public static PlainJsonObjectMappingBuilder object(String name) {
        return new PlainJsonObjectMappingBuilder(name);
    }

    public static JsonArrayMappingBuilder array(String name) {
        return new JsonArrayMappingBuilder(name);
    }

    public static JsonBoostMappingBuilder boost(String name) {
        return new JsonBoostMappingBuilder(name);
    }

    public static JsonAllMappingBuilder all() {
        return new JsonAllMappingBuilder();
    }
}
