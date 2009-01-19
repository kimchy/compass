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

package org.compass.core.mapping.rsem.builder;

import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.ContractMappingProvider;
import org.compass.core.mapping.internal.DefaultContractMapping;

/**
 * A builder for resource contract mapping allowing to define abstract mappings (not bounded to a
 * specific resource) which can then be extended by actual resource or other contract mappings.
 *
 * @author kimchy
 * @see RSEM#contract(String) 
 */
public class ResourceContractMappingBuilder implements ContractMappingProvider {

    private final DefaultContractMapping mapping;

    /**
     * Constructs a new resource contract mapping builder.
     */
    public ResourceContractMappingBuilder(String alias) {
        this.mapping = new DefaultContractMapping();
        mapping.setAlias(alias);
    }

    /**
     * Returns the contract mapping built.
     */
    public ContractMapping getMapping() {
        return mapping;
    }

    /**
     * Sets the list of other resource/contract mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public ResourceContractMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * The name of the analyzer that will be used to analyze ANALYZED properties. Defaults to the default analyzer
     * which is one of the internal analyzers that comes with Compass. If not set, will use the <code>default</code>
     * analyzer.
     *
     * <p>Note, that when using the resource-analyzer mapping (a child mapping of resource mapping)
     * (for a resource property value that controls the analyzer), the analyzer attribute will have no effects.
     */
    public ResourceContractMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Adds a resource id mapping definition.
     */
    public ResourceContractMappingBuilder add(ResourceIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource property mapping definition.
     */
    public ResourceContractMappingBuilder add(ResourcePropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource analyzer property mapping definition.
     */
    public ResourceContractMappingBuilder add(ResourceAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource boost property mapping definition.
     */
    public ResourceContractMappingBuilder add(ResourceBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}