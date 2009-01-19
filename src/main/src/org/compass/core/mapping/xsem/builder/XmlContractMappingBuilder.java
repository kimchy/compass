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

package org.compass.core.mapping.xsem.builder;

import org.compass.core.mapping.ContractMapping;
import org.compass.core.mapping.ContractMappingProvider;
import org.compass.core.mapping.internal.DefaultContractMapping;

/**
 * A builder allowing to constrcut xml contract mapping definition. Contract mappings allow to share
 * common mapping definitions.
 *
 * @author kimchy
 * @see org.compass.core.mapping.xsem.builder.XSEM#contract(String)
 */
public class XmlContractMappingBuilder implements ContractMappingProvider {

    private final DefaultContractMapping mapping;

    /**
     * Constructs a new Xml mapping based on the specified alias.
     */
    public XmlContractMappingBuilder(String alias) {
        mapping = new DefaultContractMapping();
        mapping.setAlias(alias);
    }

    /**
     * Returns the contract mappings.
     */
    public ContractMapping getMapping() {
        return this.mapping;
    }

    /**
     * Sets the list of other xml mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public XmlContractMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * The name of the analyzer that will be used to analyze ANALYZED properties. Defaults to the default analyzer
     * which is one of the internal analyzers that comes with Compass. If not set, will use the <code>default</code>
     * analyzer.
     *
     * <p>Note, that when using the xml-analyzer mapping (a child mapping of xml mapping)
     * (for an xml property value that controls the analyzer), the analyzer attribute will have no effects.
     */
    public XmlContractMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Adds an xml id mapping definition.
     */
    public XmlContractMappingBuilder add(XmlIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml property mapping definition.
     */
    public XmlContractMappingBuilder add(XmlPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml analyzer property mapping definition.
     */
    public XmlContractMappingBuilder add(XmlAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml boost property mapping definition.
     */
    public XmlContractMappingBuilder add(XmlBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an XML content mapping definition.
     */
    public XmlContractMappingBuilder add(XmlContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}