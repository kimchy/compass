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

/**
 * Static builder allowing to construct XSEM (XML to Search Engine Mapping)
 * definitions.
 *
 * <p>Here is an exmaple how it can be used:
 *
 * <p><pre>
 * import static org.compass.core.mapping.xsem.builder.XSEM.*;
 *
 *
 * conf.addMapping(
 *      xml("a")
 *          .add(id("/xml-fragment/data/id/@value").indexName("id"))
 *          .add(property("/xml-fragment/data/data1/@value"))
 *          .add(property("/xml-fragment/data/data1").indexName("eleText"))
 * );
 * </pre>
 *
 * @author kimchy
 */
public abstract class XSEM {

    private XSEM() {

    }

    /**
     * Constructs a new xml based mapping for the specific alias. Note, at least one
     * id mapping must be added to the xml mapping.
     */
    public static XmlMappingBuilder xml(String alias) {
        return new XmlMappingBuilder(alias);
    }

    /**
     * Constrcuts a new contract xml mapping builder that can later be extended by other
     * contracts / xml mappings. Contract mappings allow to share common mapping definitions.
     */
    public static XmlContractMappingBuilder contract(String alias) {
        return new XmlContractMappingBuilder(alias);
    }

    /**
     * Constructs a new xml id mapping using the specified xpath. Can then be added
     * to a root xml mapping builder using {@link XmlMappingBuilder#add(XmlIdMappingBuilder)}.
     */
    public static XmlIdMappingBuilder id(String xpath) {
        return new XmlIdMappingBuilder(xpath);
    }

    /**
     * Constructs a new xml property mapping using the specified xpath. Can then be added
     * to a root xml mapping builder using {@link XmlMappingBuilder#add(XmlPropertyMappingBuilder)}.
     */
    public static XmlPropertyMappingBuilder property(String xpath) {
        return new XmlPropertyMappingBuilder(xpath);
    }

    /**
     * Constructs a new xml analyzer mapping using the name and the xpath defined. Can be added
     * to a root xml mapping builder using {@link XmlMappingBuilder#add(XmlAnalyzerMappingBuilder)}
     */
    public static XmlAnalyzerMappingBuilder analyzer(String name, String xpath) {
        return new XmlAnalyzerMappingBuilder(name, xpath);
    }

    /**
     * Constructs a new xml boost mapping using the name and the xpath defined. Can be added
     * to a root xml mapping builder using {@link XmlMappingBuilder#add(XmlBoostMappingBuilder)}
     */
    public static XmlBoostMappingBuilder boost(String name, String xpath) {
        return new XmlBoostMappingBuilder(name, xpath);
    }

    /**
     * Constructs a new XML content mapping using the specified name. Can be added to xml
     * mapping builder using {@link XmlMappingBuilder#add(XmlContentMappingBuilder)}.
     */
    public static XmlContentMappingBuilder content(String name) {
        return new XmlContentMappingBuilder(name);
    }

    /**
     * Constructs a new all mapping definition that can be added to a xml mapping builder using
     * {@link XmlMappingBuilder#all(XmlAllMappingBuilder)}.
     */
    public static XmlAllMappingBuilder all() {
        return new XmlAllMappingBuilder();
    }
}
