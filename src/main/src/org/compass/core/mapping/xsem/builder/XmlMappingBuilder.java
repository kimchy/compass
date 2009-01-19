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

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.xsem.XmlObjectMapping;

/**
 * A builder allowing to constrcut xml mapping definition.
 *
 * @author kimchy
 * @see XSEM#xml(String)
 */
public class XmlMappingBuilder implements ResourceMappingProvider {

    private final XmlObjectMapping mapping;

    /**
     * Constructs a new Xml mapping based on the specified alias.
     */
    public XmlMappingBuilder(String alias) {
        mapping = new XmlObjectMapping();
        mapping.setAlias(alias);
        mapping.setRoot(true);
    }

    /**
     * Returns the mapping constructed. Used in
     * {@link org.compass.core.config.CompassConfiguration#addMapping(org.compass.core.mapping.ResourceMappingProvider)}.
     */
    public ResourceMapping getMapping() {
        return this.mapping;
    }

    /**
     * An optional xpath expression to narrow down the actual xml elements that will represent the top level xml
     * object which will be mapped to the search engine. A nice benefit here, is that the xpath can return multiple
     * xml objects, which in turn will result in multiple Resources saved to the search engine.
     */
    public XmlMappingBuilder xpath(String xpath) {
        mapping.setXPath(xpath);
        return this;
    }

    /**
     * Sets a sub index that will be used for this resource. Basically uses
     * {@link org.compass.core.engine.subindex.ConstantSubIndexHash}.
     */
    public XmlMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    /**
     * Sets a custom sub index hashing strategy for the resource mapping.
     */
    public XmlMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    /**
     * Sets the list of other xml mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public XmlMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * Sets the spell check mode that will be used for this xml mapping (and for all the
     * internal mappings that do not explicitly set their own spell check mode). If not set
     * will use the global spell check setting.
     */
    public XmlMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
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
    public XmlMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the boost value for the xml mapping.
     */
    public XmlMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Sets the "all" level mapping definition.
     */
    public XmlMappingBuilder all(XmlAllMappingBuilder allMappingBuilder) {
        mapping.setAllMapping(allMappingBuilder.mapping);
        return this;
    }

    /**
     * Adds an xml id mapping definition.
     */
    public XmlMappingBuilder add(XmlIdMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml property mapping definition.
     */
    public XmlMappingBuilder add(XmlPropertyMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml analyzer property mapping definition.
     */
    public XmlMappingBuilder add(XmlAnalyzerMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an xml boost property mapping definition.
     */
    public XmlMappingBuilder add(XmlBoostMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an XML content mapping definition.
     */
    public XmlMappingBuilder add(XmlContentMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }
}
