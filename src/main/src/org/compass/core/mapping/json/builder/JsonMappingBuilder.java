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
public class JsonMappingBuilder implements ResourceMappingProvider {

    private final RootJsonObjectMapping mapping;

    public JsonMappingBuilder(String alias) {
        this.mapping = new RootJsonObjectMapping();
        mapping.setRoot(true);
        mapping.setAlias(alias);
    }

    public ResourceMapping getMapping() {
        return mapping;
    }

    public JsonMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    public JsonMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    public JsonMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    public JsonMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }

    public JsonMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    public JsonMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public JsonMappingBuilder dynamic(boolean dynamic) {
        mapping.setDynamic(dynamic);
        return this;
    }

    public JsonMappingBuilder all(JsonAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonObjectMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    public JsonMappingBuilder add(JsonArrayMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
