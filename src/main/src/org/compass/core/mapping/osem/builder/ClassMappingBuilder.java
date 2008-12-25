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

package org.compass.core.mapping.osem.builder;

import java.util.Iterator;

import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourceMappingProvider;
import org.compass.core.mapping.SpellCheckType;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ManagedId;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class ClassMappingBuilder implements ResourceMappingProvider {

    private final ClassMapping mapping;

    public ClassMappingBuilder(Class clazz) {
        mapping = new ClassMapping();
        mapping.setClazz(clazz);
        mapping.setAlias(ClassUtils.getShortName(clazz));
        mapping.setName(clazz.getName());
        mapping.setRoot(true);
    }

    public ResourceMapping getMapping() {
        return this.mapping;
    }

    public ClassMappingBuilder alias(String alias) {
        mapping.setAlias(alias);
        return this;
    }

    public ClassMappingBuilder root(boolean root) {
        mapping.setRoot(root);
        return this;
    }

    public ClassMappingBuilder supportUnmarshall(boolean supportUnmarshall) {
        mapping.setSupportUnmarshall(supportUnmarshall);
        return this;
    }

    public ClassMappingBuilder filterDuplicatesDuringUnmarshalling(boolean filterDuplicatesDuringUnamrshalling) {
        mapping.setFilterDuplicates(filterDuplicatesDuringUnamrshalling);
        return this;
    }

    public ClassMappingBuilder poly(boolean poly) {
        mapping.setPoly(poly);
        return this;
    }

    public ClassMappingBuilder polyClass(Class polyClass) {
        mapping.setPolyClass(polyClass);
        return this;
    }

    public ClassMappingBuilder managedId(ManagedId managedId) {
        mapping.setManagedId(managedId);
        return this;
    }

    /**
     * Sets a sub index that will be used for this resource. Basically uses
     * {@link org.compass.core.engine.subindex.ConstantSubIndexHash}.
     */
    public ClassMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    /**
     * Sets a custom sub index hashing strategy for the resource mapping.
     */
    public ClassMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    /**
     * Sets the list of other clas mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public ClassMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * Sets the spell check mode that will be used for this class mapping (and for all the
     * internal mappings that do not explicitly set their own spell check mode). If not set
     * will use the global spell check setting.
     */
    public ClassMappingBuilder spellCheck(SpellCheckType spellCheck) {
        mapping.setSpellCheck(spellCheck);
        return this;
    }

    /**
     * The name of the analyzer that will be used to analyze ANALYZED properties. Defaults to the default analyzer
     * which is one of the internal analyzers that comes with Compass. If not set, will use the <code>default</code>
     * analyzer.
     *
     * <p>Note, that when using the class-analyzer mapping (a child mapping of class mapping)
     * (for a class property value that controls the analyzer), the analyzer attribute will have no effects.
     */
    public ClassMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the boost value for the class.
     */
    public ClassMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    public ClassMappingBuilder add(ClassIdMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    public ClassMappingBuilder add(ClassPropertyMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        for (Iterator<Mapping> it = builder.mapping.mappingsIt(); it.hasNext();) {
            ((ClassPropertyMetaDataMapping) it.next()).setDefinedInAlias(mapping.getAlias());
        }
        mapping.addMapping(builder.mapping);
        return this;
    }
}
