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

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.json.RootJsonObjectMapping;

/**
 * @author kimchy
 */
public class RootJsonObjectMappingBuilder implements ResourceMappingProvider {

    private final RootJsonObjectMapping mapping;

    RootJsonObjectMappingBuilder(RootJsonObjectMapping mapping) {
        this.mapping = mapping;
    }

    public ResourceMapping getMapping() {
        return mapping;
    }

    public RootJsonObjectMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    public RootJsonObjectMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    public RootJsonObjectMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    public RootJsonObjectMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }

    public RootJsonObjectMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public RootJsonObjectMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public RootJsonObjectMappingBuilder dynamic(boolean dynamic) {
        mapping.setDynamic(dynamic);
        return this;
    }

    public RootJsonObjectMappingBuilder all(JsonAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(PlainJsonObjectMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public RootJsonObjectMappingBuilder add(JsonArrayMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
