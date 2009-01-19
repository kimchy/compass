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

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.rsem.RawResourceMapping;

/**
 * A builder allowing to constrcut resource mapping definition.
 *
 * @author kimchy
 * @see RSEM#resource(String)
 */
public class ResourceMappingBuilder implements ResourceMappingProvider {

    private final RawResourceMapping mapping;

    /**
     * Constructs the builder based on the specified alias.
     */
    public ResourceMappingBuilder(String alias) {
        this.mapping = new RawResourceMapping();
        mapping.setAlias(alias);
        mapping.setRoot(true);
    }

    /**
     * Returns the mapping constructed. Used in
     * {@link org.compass.core.config.CompassConfiguration#addMapping(org.compass.core.mapping.ResourceMappingProvider)}.
     */
    public ResourceMapping getMapping() {
        return mapping;
    }

    /**
     * Sets a sub index that will be used for this resource. Basically uses
     * {@link org.compass.core.engine.subindex.ConstantSubIndexHash}.
     */
    public ResourceMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    /**
     * Sets a custom sub index hashing strategy for the resource mapping.
     */
    public ResourceMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    /**
     * Sets the list of other resource/contract mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public ResourceMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * Sets the spell check mode that will be used for this resource mapping (and for all the
     * internal mappings that do not explicitly set their own spell check mode). If not set
     * will use the global spell check setting.
     */
    public ResourceMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
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
    public ResourceMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the boost value for the resource.
     */
    public ResourceMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the "all" level mapping definition.
     */
    public ResourceMappingBuilder all(ResourceAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    /**
     * Adds a resource id mapping definition.
     */
    public ResourceMappingBuilder add(ResourceIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource property mapping definition.
     */
    public ResourceMappingBuilder add(ResourcePropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource analyzer property mapping definition.
     */
    public ResourceMappingBuilder add(ResourceAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a resource boost property mapping definition.
     */
    public ResourceMappingBuilder add(ResourceBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
