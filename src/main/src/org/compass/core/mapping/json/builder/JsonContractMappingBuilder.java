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

import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.ContractMappingProvider;
import org.compass.core.mapping.internal.DefaultContractMapping;

/**
 * A builder allowing to constrcut json contract mapping definition. Contract mappings allow to
 * share common mapping definitions.
 *
 * @author kimchy
 * @see org.compass.core.mapping.json.builder.JSEM#contract(String)
 */
public class JsonContractMappingBuilder implements ContractMappingProvider {

    private final DefaultContractMapping mapping;

    /**
     * Constructs a new contract JSON Mapping based on the specified alias.
     */
    public JsonContractMappingBuilder(String alias) {
        this.mapping = new DefaultContractMapping();
        mapping.setAlias(alias);
    }

    /**
     * Returns the contract mapping built.
     */
    public ContractMapping getMapping() {
        return this.mapping;
    }

    /**
     * Sets the list of other json mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public JsonContractMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * The name of the analyzer that will be used to analyze ANALYZED properties. Defaults to the default analyzer
     * which is one of the internal analyzers that comes with Compass. If not set, will use the <code>default</code>
     * analyzer.
     *
     * <p>Note, that when using the json-analyzer mapping (a child mapping of json mapping)
     * (for a json property value that controls the analyzer), the analyzer attribute will have no effects.
     */
    public JsonContractMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Adds a json id mapping definition.
     */
    public JsonContractMappingBuilder add(JsonIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json property mapping definition.
     */
    public JsonContractMappingBuilder add(JsonPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json analyzer property mapping definition.
     */
    public JsonContractMappingBuilder add(JsonAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json boost property mapping definition.
     */
    public JsonContractMappingBuilder add(JsonBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json content mapping definition.
     */
    public JsonContractMappingBuilder add(JsonContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json object mapping definition.
     */
    public JsonContractMappingBuilder add(JsonObjectMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json array mapping definition.
     */
    public JsonContractMappingBuilder add(JsonArrayMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}