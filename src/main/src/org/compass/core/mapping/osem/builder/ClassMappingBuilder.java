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
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ManagedId;
import org.compass.core.util.ClassUtils;

/**
 * Marks a class as searchable.
 * A searchable class is assoiated with an alias, and allows to perform full
 * text search on it's mapped properties/fields.
 *
 * <p>The searchable class is associated with an alias, which can be used to
 * reference the class when performing search operations, or for other
 * mappings to extend it. By default, teh alias name will be the class short
 * name.
 *
 * <p>Note, a root searchable must be associated with at least one {@link ClassIdMappingBuilder}.
 *
 * <p>By default, the searchable class is defined as a root class. A root class is
 * a top level searchable class. A non root class can be used to define mappings
 * definitions for {@link ClassComponentMappingBuilder}, and it is preferable that classes
 * that are only used as component mapping definitions, will be defined with {@link #root(boolean)}
 * set to <code>false</code>.
 *
 * <p>A class mapping has it's own fully functional index, unless using the
 * {@link #subIndex(String)} to join several searchable classes into the same
 * index (when joining several searchalbe classes into the same index,
 * the search will be much faster, but updates perform locks on the sub index
 * level, so it might slow it down). More fine grained control can be acheived
 * using {@link #subIndex(org.compass.core.engine.subindex.SubIndexHash)} allowing
 * to parition the same class into several sub indexes using
 * {@link org.compass.core.engine.subindex.ModuloSubIndexHash} definition.
 *
 * <p>A searchable class creates an internal "all" meta-data, which holds
 * searchable information of all the class searchable content. Controlling
 * the "all" property using the {@link ClassAllMappingBuilder} and setting it
 * using {@link #all(ClassAllMappingBuilder)}.
 *
 * <p>The seachable class can have a specialized analyzer (different from the
 * default one) associated with it using {@link #analyzer(String)}. Note, that this
 * will associate the class statically with an analyzer. Dynamically associating
 * the class with an analyzer, the {@link ClassAnalyzerMappingBuilder} can be
 * used to annotated the dynamic value for the analyzer to use.
 *
 * <p>The {@link #poly(boolean)} can be used to mapped polymprphic inheritance tree. This is the less
 * prefable way to map an inhertiance tree, since the fact that a searchable class extend other
 * searchable classes using the {@link #extendsAliases(String[])} mapping.
 *
 * @author kimchy
 * @see OSEM#searchable(Class) 
 */
public class ClassMappingBuilder implements ResourceMappingProvider {

    private final ClassMapping mapping;

    /**
     * Constructs a new class mapping builder for the specified class.
     */
    public ClassMappingBuilder(Class clazz) {
        mapping = new ClassMapping();
        mapping.setClazz(clazz);
        mapping.setAlias(ClassUtils.getShortName(clazz));
        mapping.setName(clazz.getName());
        mapping.setRoot(true);
    }

    /**
     * Returns the mapping.
     */
    public ResourceMapping getMapping() {
        return this.mapping;
    }

    /**
     * Sets the alias of the searchable class. By default, will be set to the class short name.
     * Note, the alias must be set before any addition of child mappings using <code>add</code>.
     */
    public ClassMappingBuilder alias(String alias) {
        mapping.setAlias(alias);
        return this;
    }

    /**
     * Sets if the class is a root mapping or not. Root searchable classes are classes that will
     * have be mapped to a sub index, and will return as actual hits from search results. Mostly.
     * non root searchable classes are classes that are only used as component mappings. Defaults
     * to <code>true</code>.
     */
    public ClassMappingBuilder root(boolean root) {
        mapping.setRoot(root);
        return this;
    }

    /**
     * Controls if the searchable class will support unamrsahlling or not. When the searchable class
     * supprots unmarshalling, then when loading to from the index, it will be constructed fully
     * from the index. When it does not support unamrshalling, then only the ids of the searchable class
     * will be filled.
     *
     * <p>When supporting unmarshalling, Compass might store additional properties in the resource that
     * represents this searchable class (for example, to store the size of a collection). Note, when
     * support unamrshalling is set to <code>false</code>, then one can still use the {@link org.compass.core.Resource}
     * loaded from the index.
     *
     * <p>Defaults to the a globabl setting {@link org.compass.core.config.CompassEnvironment.Osem#SUPPORT_UNMARSHALL}
     * which in turn defaults to <code>true</code>.
     */
    public ClassMappingBuilder supportUnmarshall(boolean supportUnmarshall) {
        mapping.setSupportUnmarshall(supportUnmarshall);
        return this;
    }

    /**
     * Should the searchable class filter out duplicates during unmarshalling. Defaults to
     * {@link org.compass.core.config.CompassEnvironment.Osem#FILTER_DUPLICATES}.
     */
    public ClassMappingBuilder filterDuplicatesDuringUnmarshalling(boolean filterDuplicatesDuringUnamrshalling) {
        mapping.setFilterDuplicates(filterDuplicatesDuringUnamrshalling);
        return this;
    }

    /**
     * Used to mapped polymprphic inheritance tree. This is the less prefable way to map
     * an inheritance tree, since the searchable class can extend other searchable classes
     * using {@link #extendsAliases(String[])}.
     *
     * <p>If poly is set to <code>true</code>, the actual class implementation will be persisted
     * to the index, later be used to instantiate it when un-marhsalling. If a specific class
     * need to be used to instantiate all classes, use the {{@link #polyClass(Class)} to set it.
     */
    public ClassMappingBuilder poly(boolean poly) {
        mapping.setPoly(poly);
        return this;
    }

    /**
     * In cases where poly is set to <code>true</code>, allows to set the class that will
     * be used to instantiate in all inheritance tree cases.
     *
     * <p>If not set, the actual class will be saved to the index,
     * later be used to instantiate it when un-marhsalling
     */
    public ClassMappingBuilder polyClass(Class polyClass) {
        mapping.setPolyClass(polyClass);
        return this;
    }

    /**
     * Controls the managed id value for all the mapped properties that have no explicit setting
     * of the managed id (also default to NA). The default value for the managed id is derived from
     * globabl Compass settings and defaults to {@link ManagedId#NO_STORE}.
     */
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
    public ClassMappingBuilder spellCheck(SpellCheck spellCheck) {
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

    /**
     * Allows to set the "all" mapping definition.
     */
    public ClassMappingBuilder all(ClassAllMappingBuilder builder) {
        mapping.setAllMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an id property mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassIdMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        for (Iterator<Mapping> it = builder.mapping.mappingsIt(); it.hasNext();) {
            ((ClassPropertyMetaDataMapping) it.next()).setDefinedInAlias(mapping.getAlias());
        }
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a proeprty mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassPropertyMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        for (Iterator<Mapping> it = builder.mapping.mappingsIt(); it.hasNext();) {
            ((ClassPropertyMetaDataMapping) it.next()).setDefinedInAlias(mapping.getAlias());
        }
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an analyzer proeprty mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassAnalyzerMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a boost property mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassBoostMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a component mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassComponentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an id component mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassIdComponentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a reference mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassReferenceMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a constant mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassConstantMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a dynamic meta data mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassDynamicMetaDataMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Addsa cascade mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassCascadeMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds parent mapping to the searchable class.
     */
    public ClassMappingBuilder add(ClassParentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }
}
