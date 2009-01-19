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

/**
 * Static builder allowing to construct JSEM (JSON to Search Engine Mapping)
 * definitions.
 *
 * <p>Here is an exmaple how it can be used:
 *
 * <p><pre>
 * import static org.compass.core.mapping.jsem.builder.JSEM.*;
 *
 *
 * conf.addResourceMapping(
 *          json("a")
 *              .add(id("id"))
 *              .add(property("value1"))
 *              .add(property("value2").store(Property.Store.YES).index(Property.Index.ANALYZED))
 * );
 * </pre>
 *
 * @author kimchy
 */
public abstract class JSEM {

    private JSEM() {

    }

    /**
     * Constrcuts a new root json mapping builder. Note, at least one id mapping
     * must be added to it.
     */
    public static JsonMappingBuilder json(String alias) {
        return new JsonMappingBuilder(alias);
    }

    /**
     * Constrcuts a new contract json mapping builder that can later be extended by other
     * contracts / json mappings. Contract mappings allow to share common mapping definitions.
     */
    public static JsonContractMappingBuilder contract(String alias) {
        return new JsonContractMappingBuilder(alias);
    }

    /**
     * Constructs a new json id mapping using the specified name. Can then be added
     * to a root json mapping builder using {@link JsonMappingBuilder#add(JsonIdMappingBuilder)}.
     */
    public static JsonIdMappingBuilder id(String name) {
        return new JsonIdMappingBuilder(name);
    }

    /**
     * Constrcuts a new json property mapping that can be used with {@link JsonArrayMappingBuilder#element(JsonPropertyMappingBuilder)}.
     */
    public static JsonPropertyMappingBuilder property() {
        return property(null);
    }

    /**
     * Constructs a new json property mapping using the specified name. Can then be added
     * to a json mapping builder using {@link JsonMappingBuilder#add(JsonPropertyMappingBuilder)}
     * or to a json object mapping builder using {@link JsonObjectMappingBuilder#add(JsonPropertyMappingBuilder)}.
     */
    public static JsonPropertyMappingBuilder property(String name) {
        return new JsonPropertyMappingBuilder(name);
    }

    /**
     * Constructs a new json analyzer property mapping using the specified name. Can then be added
     * to a json mapping builder using {@link JsonMappingBuilder#add(JsonAnalyzerMappingBuilder)}.
     */
    public static JsonAnalyzerMappingBuilder analyzer(String name) {
        return new JsonAnalyzerMappingBuilder(name);
    }

    /**
     * Constructs a new json content mapping using the specified name. Can be added to json
     * mapping builder using {@link JsonMappingBuilder#add(JsonContentMappingBuilder)}.
     */
    public static JsonContentMappingBuilder content(String name) {
        return new JsonContentMappingBuilder(name);
    }

    /**
     * Constructs a new json object mapping that can be used with {@link JsonArrayMappingBuilder#element(JsonObjectMappingBuilder)}.
     */
    public static JsonObjectMappingBuilder object() {
        return object(null);
    }

    /**
     * Constructs a new json object mapping using the specified name. Can be added to json
     * mapping builder using {@link JsonMappingBuilder#add(JsonObjectMappingBuilder)}.
     */
    public static JsonObjectMappingBuilder object(String name) {
        return new JsonObjectMappingBuilder(name);
    }

    /**
     * Constructs a new json array mapping that can be used with {@link JsonArrayMappingBuilder#element(JsonArrayMappingBuilder)}.
     */
    public static JsonArrayMappingBuilder array() {
        return new JsonArrayMappingBuilder(null);
    }

    /**
     * Constructs a enw json array mapping using the specified name. Can be added to json
     * mapping builder using {@link JsonMappingBuilder#add(JsonArrayMappingBuilder)}.
     */
    public static JsonArrayMappingBuilder array(String name) {
        return new JsonArrayMappingBuilder(name);
    }

    /**
     * Constructs a new json boost property mapping using the specified name. Can then be added
     * to a json mapping builder using {@link JsonMappingBuilder#add(JsonBoostMappingBuilder)}.
     */
    public static JsonBoostMappingBuilder boost(String name) {
        return new JsonBoostMappingBuilder(name);
    }

    /**
     * Constructs a new all mapping definition that can be added to a json mapping builder using
     * {@link JsonMappingBuilder#all(JsonAllMappingBuilder)}.
     */
    public static JsonAllMappingBuilder all() {
        return new JsonAllMappingBuilder();
    }
}
