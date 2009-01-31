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

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.json.Naming;
import org.compass.core.mapping.json.RootJsonObjectMapping;

/**
 * A builder allowing to constrcut json mapping definition.
 *
 * @author kimchy
 * @see JSEM#json(String)
 */
public class JsonMappingBuilder implements ResourceMappingProvider {

    private final RootJsonObjectMapping mapping;

    /**
     * Constructs a new (root) JSON Mapping based on the specified alias.
     */
    public JsonMappingBuilder(String alias) {
        this.mapping = new RootJsonObjectMapping();
        mapping.setRoot(true);
        mapping.setAlias(alias);
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
    public JsonMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    /**
     * Sets a custom sub index hashing strategy for the resource mapping.
     */
    public JsonMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    /**
     * Sets the list of other json mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public JsonMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * Sets the spell check mode that will be used for this json mapping (and for all the
     * internal mappings that do not explicitly set their own spell check mode). If not set
     * will use the global spell check setting.
     */
    public JsonMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
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
    public JsonMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the boost value for the json.
     */
    public JsonMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Should unmapped json elements be added to the search engine automatically (and recursively). Defaults
     * to <code>false</code>.
     */
    public JsonMappingBuilder dynamic(boolean dynamic) {
        mapping.setDynamic(dynamic);
        return this;
    }

    /**
     * Sets how dynamic objects, arrays and properties added through this object will have their respective
     * property names named.
     */
    public JsonMappingBuilder dynamicNaming(Naming dynamicNaming) {
        mapping.setDynamicNaming(dynamicNaming);
        return this;
    }

    /**
     * Sets the "all" level mapping definition.
     */
    public JsonMappingBuilder all(JsonAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    /**
     * Adds a json id mapping definition.
     */
    public JsonMappingBuilder add(JsonIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json property mapping definition.
     */
    public JsonMappingBuilder add(JsonPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json analyzer property mapping definition.
     */
    public JsonMappingBuilder add(JsonAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json boost property mapping definition.
     */
    public JsonMappingBuilder add(JsonBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json content mapping definition.
     */
    public JsonMappingBuilder add(JsonContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json object mapping definition.
     */
    public JsonMappingBuilder add(JsonObjectMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a json array mapping definition.
     */
    public JsonMappingBuilder add(JsonArrayMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
