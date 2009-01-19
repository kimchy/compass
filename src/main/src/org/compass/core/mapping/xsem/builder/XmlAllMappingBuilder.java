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

import org.compass.core.Property;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.DefaultAllMapping;

/**
 * A builder allowing to constrcut xml all mapping definition.
 *
 * @author kimchy
 * @see XSEM#all()
 */
public class XmlAllMappingBuilder {

    final DefaultAllMapping mapping;

    /**
     * Constructs a new all mapping builder.
     */
    public XmlAllMappingBuilder() {
        this.mapping = new DefaultAllMapping();
    }

    /**
     * Should the all mapping be enabled or not. Defaults to the global
     * {@link org.compass.core.config.CompassEnvironment.All#ENABLED} setting which
     * in turn defaults to <code>true</code>.
     */
    public XmlAllMappingBuilder enable(boolean enable) {
        mapping.setSupported(enable);
        return this;
    }

    /**
     * Should the alias be exlcuded from the all mapping or not. Default to the
     * {@link org.compass.core.config.CompassEnvironment.All#EXCLUDE_ALIAS} setting which in turn
     * defaults to <code>true</code>.
     */
    public XmlAllMappingBuilder excludeAlias(boolean excludeAlias) {
        mapping.setExcludeAlias(excludeAlias);
        return this;
    }

    /**
     * Should the all mapping include properties that do not have mappings. Defaults to the global
     * {@link org.compass.core.config.CompassEnvironment.All#INCLUDE_UNMAPPED_PROPERTIES} setting which
     * in turn defaults to <code>true</code>.
     */
    public XmlAllMappingBuilder includePropertiesWithNoMappings(boolean includePropertiesWithNoMappings) {
        mapping.setIncludePropertiesWithNoMappings(includePropertiesWithNoMappings);
        return this;
    }

    /**
     * The term vector of the all property. Default to the
     * {@link org.compass.core.config.CompassEnvironment.All#TERM_VECTOR} setting which in turn
     * defaults to <code>NO</code>.
     */
    public XmlAllMappingBuilder termVector(Property.TermVector termVector) {
        mapping.setTermVector(termVector);
        return this;
    }

    /**
     * The omit norms of the all property. Default to the
     * {@link org.compass.core.config.CompassEnvironment.All#OMIT_NORMS} setting which in turn
     * defaults to <code>false</code>.
     */
    public XmlAllMappingBuilder omitNorms(boolean omitNorms) {
        mapping.setOmitNorms(omitNorms);
        return this;
    }

    /**
     * The omit tf of the all property. Default to the
     * {@link org.compass.core.config.CompassEnvironment.All#OMIT_TF} setting which in turn
     * defaults to <code>false</code>.
     */
    public XmlAllMappingBuilder omitTf(boolean omitTf) {
        mapping.setOmitTf(omitTf);
        return this;
    }

    /**
     * The spell check type of the all property.
     */
    public XmlAllMappingBuilder spellCheck(SpellCheck spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }
}