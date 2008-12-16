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

package org.compass.core.mapping.rsem.builder;

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheckType;
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
     * Constructs the builder based on raw resource mapping.
     */
    public ResourceMappingBuilder(String alias) {
        this.mapping = new RawResourceMapping();
        mapping.setAlias(alias);
        mapping.setRoot(true);
    }

    public ResourceMapping getMapping() {
        return mapping;
    }

    public ResourceMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    public ResourceMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    public ResourceMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    public ResourceMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }

    public ResourceMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public ResourceMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public ResourceMappingBuilder all(ResourceAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    public ResourceMappingBuilder add(ResourceIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public ResourceMappingBuilder add(ResourcePropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public ResourceMappingBuilder add(ResourceAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public ResourceMappingBuilder add(ResourceBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
