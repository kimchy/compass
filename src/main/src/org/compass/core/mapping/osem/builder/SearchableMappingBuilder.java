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

package org.compass.core.mapping.osem.builder;

import java.util.Iterator;

import org.compass.core.converter.Converter;
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
 * <p>Note, a root searchable must be associated with at least one {@link SearchableIdMappingBuilder}.
 *
 * <p>By default, the searchable class is defined as a root class. A root class is
 * a top level searchable class. A non root class can be used to define mappings
 * definitions for {@link SearchableComponentMappingBuilder}, and it is preferable that classes
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
 * the "all" property using the {@link SearchableAllMappingBuilder} and setting it
 * using {@link #all(SearchableAllMappingBuilder)}.
 *
 * <p>The seachable class can have a specialized analyzer (different from the
 * default one) associated with it using {@link #analyzer(String)}. Note, that this
 * will associate the class statically with an analyzer. Dynamically associating
 * the class with an analyzer, the {@link SearchableAnalyzerMappingBuilder} can be
 * used to annotated the dynamic value for the analyzer to use.
 *
 * <p>The {@link #poly(boolean)} can be used to mapped polymprphic inheritance tree. This is the less
 * prefable way to map an inhertiance tree, since the fact that a searchable class extend other
 * searchable classes using the {@link #extendsAliases(String[])} mapping.
 *
 * @author kimchy
 * @see OSEM#searchable(Class)
 */
public class SearchableMappingBuilder implements ResourceMappingProvider {

    private final ClassMapping mapping;

    /**
     * Constructs a new class mapping builder for the specified class.
     */
    public SearchableMappingBuilder(Class clazz) {
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
    public SearchableMappingBuilder alias(String alias) {
        mapping.setAlias(alias);
        return this;
    }

    /**
     * Sets if the class is a root mapping or not. Root searchable classes are classes that will
     * have be mapped to a sub index, and will return as actual hits from search results. Mostly.
     * non root searchable classes are classes that are only used as component mappings. Defaults
     * to <code>true</code>.
     */
    public SearchableMappingBuilder root(boolean root) {
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
    public SearchableMappingBuilder supportUnmarshall(boolean supportUnmarshall) {
        mapping.setSupportUnmarshall(supportUnmarshall);
        return this;
    }

    /**
     * Should the searchable class filter out duplicates during unmarshalling. Defaults to
     * {@link org.compass.core.config.CompassEnvironment.Osem#FILTER_DUPLICATES}.
     */
    public SearchableMappingBuilder filterDuplicatesDuringUnmarshalling(boolean filterDuplicatesDuringUnamrshalling) {
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
    public SearchableMappingBuilder poly(boolean poly) {
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
    public SearchableMappingBuilder polyClass(Class polyClass) {
        mapping.setPolyClass(polyClass);
        return this;
    }

    /**
     * Controls the managed id value for all the mapped properties that have no explicit setting
     * of the managed id (also default to NA). The default value for the managed id is derived from
     * globabl Compass settings and defaults to {@link ManagedId#NO_STORE}.
     */
    public SearchableMappingBuilder managedId(ManagedId managedId) {
        mapping.setManagedId(managedId);
        return this;
    }

    /**
     * Sets a sub index that will be used for this resource. Basically uses
     * {@link org.compass.core.engine.subindex.ConstantSubIndexHash}.
     */
    public SearchableMappingBuilder subIndex(String subIndex) {
        mapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        return this;
    }

    /**
     * Sets a custom sub index hashing strategy for the resource mapping.
     */
    public SearchableMappingBuilder subIndex(SubIndexHash subIndexHash) {
        mapping.setSubIndexHash(subIndexHash);
        return this;
    }

    /**
     * Sets the list of other clas mappings that this mapping will extend and inherit
     * internal mappings from.
     */
    public SearchableMappingBuilder extendsAliases(String... extendedAliases) {
        mapping.setExtendedAliases(extendedAliases);
        return this;
    }

    /**
     * Sets the spell check mode that will be used for this class mapping (and for all the
     * internal mappings that do not explicitly set their own spell check mode). If not set
     * will use the global spell check setting.
     */
    public SearchableMappingBuilder spellCheck(SpellCheck spellCheck) {
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
    public SearchableMappingBuilder analyzer(String analyzer) {
        mapping.setAnalyzer(analyzer);
        return this;
    }

    /**
     * Sets the boost value for the class.
     */
    public SearchableMappingBuilder boost(float boost) {
        mapping.setBoost(boost);
        return this;
    }

    /**
     * Allows to set the "all" mapping definition.
     */
    public SearchableMappingBuilder all(SearchableAllMappingBuilder builder) {
        mapping.setAllMapping(builder.mapping);
        return this;
    }

    /**
     * Sets the mapping converter lookup name that will be used to convert the class. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassMappingConverter}.
     */
    public SearchableMappingBuilder mappingConverter(String converter) {
        mapping.setConverterName(converter);
        return this;
    }

    /**
     * Sets the mapping converter that will be used to convert the class. Defaults to
     * {@link org.compass.core.converter.mapping.osem.ClassMappingConverter}.
     */
    public SearchableMappingBuilder mappingConverter(Converter converter) {
        mapping.setConverter(converter);
        return this;
    }

    /**
     * Adds an id property mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableIdMappingBuilder builder) {
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
    public SearchableMappingBuilder add(SearchablePropertyMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        for (Iterator<Mapping> it = builder.mapping.mappingsIt(); it.hasNext();) {
            ((ClassPropertyMetaDataMapping) it.next()).setDefinedInAlias(mapping.getAlias());
        }
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a dynamic property mapping to searchable class.
     */
    public SearchableMappingBuilder add(SearchableDynamicPropertyMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an analyzer proeprty mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableAnalyzerMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a boost property mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableBoostMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a component mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableComponentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds an id component mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableIdComponentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a reference mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableReferenceMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a constant mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableConstantMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds a dynamic meta data mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableDynamicMetaDataMappingBuilder builder) {
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Addsa cascade mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableCascadeMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }

    /**
     * Adds parent mapping to the searchable class.
     */
    public SearchableMappingBuilder add(SearchableParentMappingBuilder builder) {
        builder.mapping.setDefinedInAlias(mapping.getAlias());
        mapping.addMapping(builder.mapping);
        return this;
    }
}
