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

package org.compass.annotations.config.binding;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.compass.annotations.*;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.binding.AbstractClassMetaDataMappingBinding;
import org.compass.core.config.binding.metadata.ClassMetaData;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.support.FormatDelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.engine.subindex.ConstantSubIndexHash;
import org.compass.core.engine.subindex.SubIndexHash;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.SpellCheck;
import org.compass.core.mapping.internal.DefaultAllMapping;
import org.compass.core.mapping.internal.InternalCascadeMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.internal.InternalMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassBoostPropertyMapping;
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyAnalyzerController;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.IdComponentMapping;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.PlainCascadeMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.mapping.osem.internal.InternalObjectMapping;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class AnnotationsMappingBinding extends AbstractClassMetaDataMappingBinding {

    public static final Log log = LogFactory.getLog(AnnotationsMappingBinding.class);

    private static final Class[] applicableAnnotations = new Class[]{Searchable.class, SearchConverter.class, SearchConverters.class,
            SearchAnalyzer.class, SearchAnalyzers.class, SearchAnalyzerFilter.class, SearchAnalyzerFilters.class};

    private CommonMetaDataLookup valueLookup;

    private InternalCompassMapping mapping;

    private ClassMapping classMapping;

    private CompassSettings settings;

    public void setUpBinding(InternalCompassMapping mapping, CompassMetaData metaData, CompassSettings settings) {
        super.setUpBinding(mapping, metaData, settings);
        this.mapping = mapping;
        this.valueLookup = new CommonMetaDataLookup(metaData);
        this.settings = settings;
    }

    protected boolean isApplicable(ClassMetaData classMetaData) {
        for (Class applicableAnnotation : applicableAnnotations) {
            if (classMetaData.hasAnnotation(applicableAnnotation.getName())) return true;
        }
        return false;
    }

    public boolean addPackage(String packageName) throws ConfigurationException, MappingException {
        Package pckg;
        try {
            pckg = ClassUtils.forName(packageName + ".package-info", settings.getClassLoader()).getPackage();
        } catch (ClassNotFoundException e) {
            return false;
        }
        return processNonSearchableAnnotations(pckg);
    }

    public boolean addClass(Class clazz) throws ConfigurationException, MappingException {
        Class<?> annotationClass = clazz;
        boolean found = processNonSearchableAnnotations(clazz);
        Searchable searchable = annotationClass.getAnnotation(Searchable.class);
        if (searchable == null) {
            return found;
        }
        String alias = getAliasFromSearchableClass(clazz, searchable);
        // try and check is the class mapping is already defined
        // if it is, we will add mapping definitions on top of it
        boolean newClassMapping = false;
        AliasMapping aliasMapping = mapping.getAliasMapping(alias);
        if (aliasMapping != null) {
            if (!(aliasMapping instanceof ClassMapping)) {
                throw new MappingException("Defined searchable annotation on a class with alias [" + alias + "] which" +
                        " has other mapping definitions, but it not of type class mapping");
            }
            classMapping = (ClassMapping) aliasMapping;
        } else {
            classMapping = new ClassMapping();
            newClassMapping = true;
        }
        classMapping.setAlias(alias);

        classMapping.setName(clazz.getName());
        classMapping.setClazz(clazz);

        // sub index (including hash support)
        String subIndex = searchable.subIndex();
        if (!StringUtils.hasLength(subIndex)) {
            subIndex = alias;
        }
        SearchableSubIndexHash searchableSubIndexHash = annotationClass.getAnnotation(SearchableSubIndexHash.class);
        if (searchableSubIndexHash == null) {
            classMapping.setSubIndexHash(new ConstantSubIndexHash(subIndex));
        } else {
            SubIndexHash subIndexHash;
            try {
                subIndexHash = searchableSubIndexHash.value().newInstance();
            } catch (Exception e) {
                throw new MappingException("Failed to create sub index hash [" + searchableSubIndexHash.value().getName() + "]", e);
            }
            if (subIndexHash instanceof CompassConfigurable) {
                CompassSettings settings = new CompassSettings();
                for (int i = 0; i < searchableSubIndexHash.settings().length; i++) {
                    SearchSetting setting = searchableSubIndexHash.settings()[i];
                    settings.setSetting(setting.name(), setting.value());
                }
                ((CompassConfigurable) subIndexHash).configure(settings);
            }
            classMapping.setSubIndexHash(subIndexHash);
        }
        if (log.isTraceEnabled()) {
            log.trace("Alias [" + classMapping.getAlias() + "] is mapped to sub index hash [" + classMapping.getSubIndexHash() + "]");
        }

        DefaultAllMapping allMapping = new DefaultAllMapping();
        SearchableAllMetaData allMetaData = annotationClass.getAnnotation(SearchableAllMetaData.class);
        if (allMetaData != null) {
            if (allMetaData.enable() == EnableAll.TRUE) {
                allMapping.setSupported(true);
            } else if (allMetaData.enable() == EnableAll.FALSE) {
                allMapping.setSupported(false);
            }
            if (allMetaData.excludeAlias() == ExcludeAlias.TRUE) {
                allMapping.setExcludeAlias(true);
            } else if (allMetaData.excludeAlias() == ExcludeAlias.FALSE) {
                allMapping.setExcludeAlias(false);
            }
            if (StringUtils.hasLength(allMetaData.name())) {
                allMapping.setProperty(allMetaData.name());
            }
            if (allMetaData.termVector() != TermVector.NA) {
                allMapping.setTermVector(AnnotationsBindingUtils.convert(allMetaData.termVector()));
            }
            if (allMetaData.spellCheck() == org.compass.annotations.SpellCheck.EXCLUDE) {
                allMapping.setSpellCheck(SpellCheck.EXCLUDE);
            } else if (allMetaData.spellCheck() == org.compass.annotations.SpellCheck.INCLUDE) {
                allMapping.setSpellCheck(SpellCheck.INCLUDE);
            } else if (allMetaData.spellCheck() == org.compass.annotations.SpellCheck.NA) {
                allMapping.setSpellCheck(SpellCheck.NA);
            }
            if (allMetaData.includePropertiesWithNoMappings() == NABoolean.TRUE) {
                allMapping.setIncludePropertiesWithNoMappings(true);
            } else if (allMetaData.includePropertiesWithNoMappings() == NABoolean.FALSE) {
                allMapping.setIncludePropertiesWithNoMappings(false);
            }
            allMapping.setOmitNorms(AnnotationsBindingUtils.convert(allMetaData.omitNorms()));
            allMapping.setOmitTf(AnnotationsBindingUtils.convert(allMetaData.omitTf()));
        }
        classMapping.setAllMapping(allMapping);

        if (searchable.spellCheck() == org.compass.annotations.SpellCheck.NA) {
            classMapping.setSpellCheck(SpellCheck.NA);
        } else if (searchable.spellCheck() == org.compass.annotations.SpellCheck.EXCLUDE) {
            classMapping.setSpellCheck(SpellCheck.EXCLUDE);
        } else if (searchable.spellCheck() == org.compass.annotations.SpellCheck.INCLUDE) {
            classMapping.setSpellCheck(SpellCheck.INCLUDE);
        }

        classMapping.setBoost(searchable.boost());
        classMapping.setRoot(searchable.root());
        classMapping.setPoly(searchable.poly());
        if (!Object.class.equals(searchable.polyClass())) {
            classMapping.setPolyClass(searchable.polyClass());
        }
        if (StringUtils.hasLength(searchable.analyzer())) {
            classMapping.setAnalyzer(searchable.analyzer());
        }

        if (searchable.supportUnmarshall() == SupportUnmarshall.TRUE) {
            classMapping.setSupportUnmarshall(true);
        } else if (searchable.supportUnmarshall() == SupportUnmarshall.FALSE) {
            classMapping.setSupportUnmarshall(false);
        }

        if (searchable.filterDuplicates() == FilterDuplicates.TRUE) {
            classMapping.setFilterDuplicates(true);
        } else if (searchable.filterDuplicates() == FilterDuplicates.FALSE) {
            classMapping.setFilterDuplicates(false);
        }

        classMapping.setManagedId(AnnotationsBindingUtils.convert(searchable.managedId()));

        bindConverter(classMapping, searchable.converter());

        processAnnotatedClass(annotationClass);

        if (newClassMapping) {
            mapping.addMapping(classMapping);
        }

        classMapping = null;

        return true;
    }

    private boolean processNonSearchableAnnotations(AnnotatedElement annotatedElement) {
        boolean found = false;
        if (annotatedElement.isAnnotationPresent(SearchConverter.class)) {
            found = true;
            bindConverter(annotatedElement.getAnnotation(SearchConverter.class));
        }
        if (annotatedElement.isAnnotationPresent(SearchConverters.class)) {
            found = true;
            SearchConverters searchConverters = annotatedElement.getAnnotation(SearchConverters.class);
            for (SearchConverter searchConverter : searchConverters.value()) {
                bindConverter(searchConverter);
            }
        }
        if (annotatedElement.isAnnotationPresent(SearchAnalyzer.class)) {
            found = true;
            bindAnalyzer(annotatedElement.getAnnotation(SearchAnalyzer.class));
        }
        if (annotatedElement.isAnnotationPresent(SearchAnalyzers.class)) {
            found = true;
            SearchAnalyzers searchAnalyzers = annotatedElement.getAnnotation(SearchAnalyzers.class);
            for (SearchAnalyzer searchAnalyzer : searchAnalyzers.value()) {
                bindAnalyzer(searchAnalyzer);
            }
        }
        if (annotatedElement.isAnnotationPresent(SearchAnalyzerFilter.class)) {
            found = true;
            bindAnalyzerFilter(annotatedElement.getAnnotation(SearchAnalyzerFilter.class));
        }
        if (annotatedElement.isAnnotationPresent(SearchAnalyzerFilters.class)) {
            found = true;
            SearchAnalyzerFilters searchAnalyzerFilters = annotatedElement.getAnnotation(SearchAnalyzerFilters.class);
            for (SearchAnalyzerFilter searchAnalyzerFilter : searchAnalyzerFilters.value()) {
                bindAnalyzerFilter(searchAnalyzerFilter);
            }
        }
        return found;
    }

    private void bindAnalyzerFilter(SearchAnalyzerFilter searchAnalyzerFilter) throws ConfigurationException, MappingException {
        ArrayList<String> settingsNames = new ArrayList<String>();
        ArrayList<String> settingsValues = new ArrayList<String>();
        settingsNames.add(LuceneEnvironment.AnalyzerFilter.TYPE);
        settingsValues.add(searchAnalyzerFilter.type().getName());

        for (SearchSetting setting : searchAnalyzerFilter.settings()) {
            settingsNames.add(setting.name());
            settingsValues.add(setting.value());
        }

        settings.setGroupSettings(LuceneEnvironment.AnalyzerFilter.PREFIX, searchAnalyzerFilter.name(),
                settingsNames.toArray(new String[settingsNames.size()]),
                settingsValues.toArray(new String[settingsValues.size()]));
    }

    private void bindAnalyzer(SearchAnalyzer searchAnalyzer) throws ConfigurationException, MappingException {
        ArrayList<String> settingsNames = new ArrayList<String>();
        ArrayList<String> settingsValues = new ArrayList<String>();

        settingsNames.add(LuceneEnvironment.Analyzer.TYPE);
        if (searchAnalyzer.type() == AnalyzerType.CustomAnalyzer) {
            if (Analyzer.class.equals(searchAnalyzer.analyzerClass())) {
                throw new ConfigurationException("SearchableAnalyzer [" + searchAnalyzer.name() + "] has " +
                        "type of [" + AnalyzerType.CustomAnalyzer + "] but does not set analyzerClass");
            }
            settingsValues.add(searchAnalyzer.analyzerClass().getName());
        } else {
            settingsValues.add(searchAnalyzer.type().toString());
        }

        if (searchAnalyzer.type() == AnalyzerType.Snowball) {
            settingsNames.add(LuceneEnvironment.Analyzer.Snowball.NAME_TYPE);
            settingsValues.add(searchAnalyzer.snowballType().toString());
        }

        if (searchAnalyzer.stopWords().length > 0) {
            StringBuffer sb = new StringBuffer();
            if (searchAnalyzer.addStopWords()) {
                sb.append("+");
            }
            for (String stopword : searchAnalyzer.stopWords()) {
                sb.append(stopword).append(",");
            }
            settingsNames.add(LuceneEnvironment.Analyzer.STOPWORDS);
            settingsValues.add(sb.toString());
        }

        if (searchAnalyzer.filters().length > 0) {
            StringBuffer sb = new StringBuffer();
            for (String filter : searchAnalyzer.filters()) {
                sb.append(filter).append(",");
            }
            settingsNames.add(LuceneEnvironment.Analyzer.FILTERS);
            settingsValues.add(sb.toString());
        }

        for (SearchSetting setting : searchAnalyzer.settings()) {
            settingsNames.add(setting.name());
            settingsValues.add(setting.value());
        }

        settings.setGroupSettings(LuceneEnvironment.Analyzer.PREFIX, searchAnalyzer.name(),
                settingsNames.toArray(new String[settingsNames.size()]),
                settingsValues.toArray(new String[settingsValues.size()]));
    }

    private void bindConverter(SearchConverter searchConverter) throws ConfigurationException, MappingException {
        String[] settingsNames = new String[searchConverter.settings().length + 2];
        String[] settingsValues = new String[searchConverter.settings().length + 2];
        int i = 0;
        for (; i < searchConverter.settings().length; i++) {
            SearchSetting setting = searchConverter.settings()[i];
            settingsNames[i] = setting.name();
            settingsValues[i] = setting.value();
        }
        settingsNames[i] = CompassEnvironment.Converter.TYPE;
        settingsValues[i] = searchConverter.type().getName();
        settingsNames[++i] = CompassEnvironment.Converter.REGISTER_CLASS;
        if (!searchConverter.registerClass().equals(Object.class)) {
            settingsValues[i] = searchConverter.registerClass().getName();
        } // else, just leave it as null
        settings.setGroupSettings(CompassEnvironment.Converter.PREFIX, searchConverter.name(),
                settingsNames, settingsValues);
    }


    private String getAliasFromSearchableClass(Class clazz, Searchable searchable) {
        String alias = searchable.alias();
        if (!StringUtils.hasLength(alias)) {
            alias = ClassUtils.getShortName(clazz);
        } else {
            // check if it is a lookp alias
            Alias aliasLookup = valueLookup.lookupAlias(alias);
            if (aliasLookup != null) {
                alias = aliasLookup.getName();
            }
        }
        return alias;
    }

    /**
     * Recursivly process the class to find all it's annotations. Lower level
     * class/interfaces with annotations will be added first.
     */
    private void processAnnotatedClass(Class<?> clazz) {
        if (clazz.equals(Class.class)) {
            return;
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !superClazz.equals(Object.class)) {
            processAnnotatedClass(superClazz);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            processAnnotatedClass(anInterface);
        }

        SearchableConstant searchableConstant =
                clazz.getAnnotation(SearchableConstant.class);
        if (searchableConstant != null) {
            bindConstantMetaData(searchableConstant);
        }

        SearchableConstants searchableConstants =
                clazz.getAnnotation(SearchableConstants.class);
        if (searchableConstants != null) {
            for (SearchableConstant metaData : searchableConstants.value()) {
                bindConstantMetaData(metaData);
            }
        }

        SearchableDynamicMetaData searchableDynamicMetaData = clazz.getAnnotation(SearchableDynamicMetaData.class);
        if (searchableDynamicMetaData != null) {
            bindDynamicMetaData(searchableDynamicMetaData);
        }
        SearchableDynamicMetaDatas searchableDynamicMetaDatas = clazz.getAnnotation(SearchableDynamicMetaDatas.class);
        if (searchableDynamicMetaDatas != null) {
            for (SearchableDynamicMetaData metaData : searchableDynamicMetaDatas.value()) {
                bindDynamicMetaData(metaData);
            }
        }

        // handles recursive extends and the original extend
        if (clazz.isAnnotationPresent(Searchable.class)) {
            Searchable searchable = clazz.getAnnotation(Searchable.class);
            String[] extend = searchable.extend();
            if (extend.length != 0) {
                ArrayList<String> extendedMappings = new ArrayList<String>();
                if (classMapping.getExtendedAliases() != null) {
                    extendedMappings.addAll(Arrays.asList(classMapping.getExtendedAliases()));
                }
                for (String extendedAlias : extend) {
                    Alias extendedAliasLookup = valueLookup.lookupAlias(extendedAlias);
                    if (extendedAliasLookup == null) {
                        extendedMappings.add(extendedAlias);
                    } else {
                        extendedMappings.add(extendedAliasLookup.getName());
                    }
                }
                classMapping.setExtendedAliases(extendedMappings.toArray(new String[extendedMappings.size()]));
            }
        }

        // if the super class has Searchable annotation as well, add it to the list of extends
        ArrayList<Class> extendedClasses = new ArrayList<Class>();
        if (clazz.getSuperclass() != null) {
            extendedClasses.add(clazz.getSuperclass());
        }
        extendedClasses.addAll(Arrays.asList(clazz.getInterfaces()));
        for (Class<?> superClass : extendedClasses) {
            if (!superClass.isAnnotationPresent(Searchable.class)) {
                continue;
            }
            Searchable superSearchable = superClass.getAnnotation(Searchable.class);
            String alias = getAliasFromSearchableClass(superClass, superSearchable);
            HashSet<String> extendedMappings = new HashSet<String>();
            if (classMapping.getExtendedAliases() != null) {
                extendedMappings.addAll(Arrays.asList(classMapping.getExtendedAliases()));
            }
            extendedMappings.add(alias);
            classMapping.setExtendedAliases(extendedMappings.toArray(new String[extendedMappings.size()]));
        }

        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                processsAnnotatedElement(clazz, field.getName(), "field", field.getType(),
                        field.getGenericType(), annotation, field);
            }
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (!method.isSynthetic() &&
                    !method.isBridge() &&
                    !Modifier.isStatic(method.getModifiers()) &&
                    method.getParameterTypes().length == 0 &&
                    method.getReturnType() != void.class &&
                    (method.getName().startsWith("get") || method.getName().startsWith("is"))) {

                for (Annotation annotation : method.getAnnotations()) {
                    processsAnnotatedElement(clazz, ClassUtils.getShortNameForMethod(method), "property", method.getReturnType(),
                            method.getGenericReturnType(), annotation, method);
                }
            }
        }
    }

    private void processsAnnotatedElement(Class<?> searchableClass, String name, String accessor, Class<?> clazz,
                                          Type type, Annotation annotation, AnnotatedElement annotatedElement) {

        if (annotation instanceof SearchableId) {
            ClassIdPropertyMapping classPropertyMapping = new ClassIdPropertyMapping();
            SearchableId searchableId = (SearchableId) annotation;
            bindObjectMapping(classPropertyMapping, accessor, name, searchableId.accessor(), searchableClass);
            bindClassPropertyIdMapping(searchableId, classPropertyMapping, clazz, type, annotatedElement);
            classMapping.addMapping(classPropertyMapping);
        } else if (annotation instanceof SearchableIdComponent) {
            IdComponentMapping componentMapping = new IdComponentMapping();
            SearchableIdComponent searchableComponent = (SearchableIdComponent) annotation;
            bindObjectMapping(componentMapping, accessor, name, searchableComponent.accessor(), searchableClass);
            bindComponent(searchableComponent, componentMapping, clazz, type);
            classMapping.addMapping(componentMapping);
        } else if (annotation instanceof SearchableProperty) {
            ClassPropertyMapping classPropertyMapping = new ClassPropertyMapping();
            SearchableProperty searchableProperty = (SearchableProperty) annotation;
            bindObjectMapping(classPropertyMapping, accessor, name, searchableProperty.accessor(), searchableClass);
            bindClassPropertyMapping(searchableProperty, classPropertyMapping, annotatedElement, clazz, type);
            classMapping.addMapping(classPropertyMapping);
        } else if (annotation instanceof SearchableDynamicProperty) {
            ClassDynamicPropertyMapping dynamicPropertyMapping = new ClassDynamicPropertyMapping();
            SearchableDynamicProperty searchableDynamicProperty = (SearchableDynamicProperty) annotation;
            bindObjectMapping(dynamicPropertyMapping, accessor, name, searchableDynamicProperty.accessor(), searchableClass);
            bindClassDynamicPropertyMapping(searchableDynamicProperty, dynamicPropertyMapping, annotatedElement, clazz, type);
            classMapping.addMapping(dynamicPropertyMapping);
        } else if (annotation instanceof SearchableComponent) {
            ComponentMapping componentMapping = new ComponentMapping();
            SearchableComponent searchableComponent = (SearchableComponent) annotation;
            bindObjectMapping(componentMapping, accessor, name, searchableComponent.accessor(), searchableClass);
            bindComponent(searchableComponent, componentMapping, clazz, type);
            classMapping.addMapping(componentMapping);
        } else if (annotation instanceof SearchableReference) {
            ReferenceMapping referenceMapping = new ReferenceMapping();
            SearchableReference searchableReference = (SearchableReference) annotation;
            bindObjectMapping(referenceMapping, accessor, name, searchableReference.accessor(), searchableClass);
            bindReference(searchableReference, referenceMapping, clazz, type);
            classMapping.addMapping(referenceMapping);
        } else if (annotation instanceof SearchableAnalyzerProperty) {
            ClassPropertyAnalyzerController analyzerMapping = new ClassPropertyAnalyzerController();
            SearchableAnalyzerProperty searchableAnalyzerProperty = (SearchableAnalyzerProperty) annotation;
            bindObjectMapping(analyzerMapping, accessor, name, searchableAnalyzerProperty.accessor(), searchableClass);
            bindAnalyzer(searchableAnalyzerProperty, analyzerMapping, clazz, type);
            classMapping.addMapping(analyzerMapping);
        } else if (annotation instanceof SearchableBoostProperty) {
            ClassBoostPropertyMapping boostPropertyMapping = new ClassBoostPropertyMapping();
            SearchableBoostProperty searchableBoostProperty = (SearchableBoostProperty) annotation;
            bindObjectMapping(boostPropertyMapping, accessor, name, searchableBoostProperty.accessor(), searchableClass);
            bindBoost(searchableBoostProperty, boostPropertyMapping, clazz, type);
            classMapping.addMapping(boostPropertyMapping);
        } else if (annotation instanceof SearchableParent) {
            ParentMapping parentMapping = new ParentMapping();
            SearchableParent searchableParent = (SearchableParent) annotation;
            bindObjectMapping(parentMapping, accessor, name, searchableParent.accessor(), searchableClass);
            bindParent(searchableParent, parentMapping, clazz, type);
            classMapping.addMapping(parentMapping);
        } else if (annotation instanceof SearchableCascading) {
            PlainCascadeMapping cascadeMapping = new PlainCascadeMapping();
            SearchableCascading searchableCascading = (SearchableCascading) annotation;
            bindObjectMapping(cascadeMapping, accessor, name, searchableCascading.accessor(), searchableClass);
            bindCascade(searchableCascading, cascadeMapping, clazz, type);
            classMapping.addMapping(cascadeMapping);
        } else if ((annotation instanceof SearchableMetaData) ||
                (annotation instanceof SearchableMetaDatas)) {
            if (!annotatedElement.isAnnotationPresent(SearchableProperty.class) &&
                    !annotatedElement.isAnnotationPresent(SearchableId.class)) {
                throw new MappingException("SearchableMetaData/s annotation exists without a SearchableProperty/Id, it will be ignored");
            }
        }
    }

    private void bindCascade(SearchableCascading searchableCascading, PlainCascadeMapping cascadeMapping, Class<?> clazz, Type type) {
        bindConverter(cascadeMapping, searchableCascading.converter(), clazz, type);
        bindCascades(searchableCascading.cascade(), cascadeMapping);
    }

    private void bindParent(SearchableParent searchableParent, ParentMapping parentMapping, Class<?> clazz, Type type) {
        bindConverter(parentMapping, searchableParent.converter(), clazz, type);
        bindCascades(searchableParent.cascade(), parentMapping);
    }

    private void bindBoost(SearchableBoostProperty searchableBoostProperty, ClassBoostPropertyMapping boostPropertyMapping,
                           Class<?> clazz, Type type) {
        bindConverter(boostPropertyMapping, searchableBoostProperty.converter(), clazz, type);
        boostPropertyMapping.setDefaultBoost(searchableBoostProperty.defaultValue());
    }

    private void bindAnalyzer(SearchableAnalyzerProperty searchableAnalyzerProperty, ClassPropertyAnalyzerController analyzerMapping,
                              Class<?> clazz, Type type) {
        bindConverter(analyzerMapping, searchableAnalyzerProperty.converter(), clazz, type);

        if (StringUtils.hasLength(searchableAnalyzerProperty.nullAnalyzer())) {
            analyzerMapping.setNullAnalyzer(searchableAnalyzerProperty.nullAnalyzer());
        }
    }

    private void bindReference(SearchableReference searchableReference, ReferenceMapping referenceMapping,
                               Class<?> clazz, Type type) {

        bindConverter(referenceMapping, searchableReference.converter(), clazz, type);

        if (StringUtils.hasLength(searchableReference.refAlias())) {
            referenceMapping.setRefAliases(getAliases(searchableReference.refAlias()));
        } else {
            referenceMapping.setRefClass(AnnotationsBindingUtils.getCollectionParameterClass(clazz, type));
        }

        if (StringUtils.hasLength(searchableReference.refComponentAlias())) {
            referenceMapping.setRefCompAlias(searchableReference.refComponentAlias());
        }

        if (searchableReference.lazy() == Lazy.FALSE) {
            referenceMapping.setLazy(false);
        } else if (searchableReference.lazy() == Lazy.TRUE) {
            referenceMapping.setLazy(true);
        }

        bindCascades(searchableReference.cascade(), referenceMapping);
    }

    private void bindComponent(SearchableComponent searchableComponent, ComponentMapping componentMapping,
                               Class<?> clazz, Type type) {

        bindConverter(componentMapping, searchableComponent.converter(), clazz, type);

        if (StringUtils.hasLength(searchableComponent.refAlias())) {
            componentMapping.setRefAliases(getAliases(searchableComponent.refAlias()));
        } else {
            componentMapping.setRefClass(AnnotationsBindingUtils.getCollectionParameterClass(clazz, type));
        }

        componentMapping.setMaxDepth(searchableComponent.maxDepth());
        if (StringUtils.hasText(searchableComponent.prefix())) {
            componentMapping.setPrefix(searchableComponent.prefix());
        }

        componentMapping.setOverrideByName(searchableComponent.override());

        bindCascades(searchableComponent.cascade(), componentMapping);
    }

    private void bindComponent(SearchableIdComponent searchableComponent, ComponentMapping componentMapping,
                               Class<?> clazz, Type type) {

        bindConverter(componentMapping, searchableComponent.converter(), clazz, type);

        if (StringUtils.hasLength(searchableComponent.refAlias())) {
            componentMapping.setRefAliases(getAliases(searchableComponent.refAlias()));
        } else {
            componentMapping.setRefClass(AnnotationsBindingUtils.getCollectionParameterClass(clazz, type));
        }

        componentMapping.setMaxDepth(searchableComponent.maxDepth());
        if (StringUtils.hasText(searchableComponent.prefix())) {
            componentMapping.setPrefix(searchableComponent.prefix());
        }

        componentMapping.setOverrideByName(searchableComponent.override());

        bindCascades(searchableComponent.cascade(), componentMapping);
    }

    private void bindCascades(Cascade[] cascades, InternalCascadeMapping cascadeMapping) {
        if (cascades == null || cascades.length == 0) {
            return;
        }
        org.compass.core.mapping.Cascade[] mappingCascades = new org.compass.core.mapping.Cascade[cascades.length];
        for (int i = 0; i < cascades.length; i++) {
            mappingCascades[i] = AnnotationsBindingUtils.convert(cascades[i]);
        }
        cascadeMapping.setCascades(mappingCascades);
    }

    /**
     * Need to be almost exactly as <code>bindClassPropertyMapping</code>.
     */
    private void bindClassPropertyIdMapping(SearchableId searchableProp, ClassIdPropertyMapping classPropertyMapping,
                                            Class<?> clazz, Type type, AnnotatedElement annotatedElement) throws MappingException {

        bindConverter(classPropertyMapping, searchableProp.idConverter());

        // No need for type in property id, since it will not be a collection

        classPropertyMapping.setBoost(searchableProp.boost());
        classPropertyMapping.setManagedId(AnnotationsBindingUtils.convert(searchableProp.managedId()));
        classPropertyMapping.setManagedIdIndex(AnnotationsBindingUtils.convert(searchableProp.managedIdIndex()));
        classPropertyMapping.setOverrideByName(searchableProp.override());

        SearchableMetaData metaData = annotatedElement.getAnnotation(SearchableMetaData.class);
        SearchableMetaDatas metaDatas = annotatedElement.getAnnotation(SearchableMetaDatas.class);

        if (StringUtils.hasLength(searchableProp.converter())) {
            classPropertyMapping.setManagedIdConverterName(searchableProp.converter());
        } else {
            classPropertyMapping.setManagedIdConverter(getConverter(clazz, type));
        }

        // check if we need to create a metadata because of the SearchId
        // here we differ from the searchProperty mapping, since it is perfectly
        // fine not to create one if there are no meta-data definitions (an internal
        // one will be created during the process phase)
        if (StringUtils.hasLength(searchableProp.name())) {
            ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
            String name = searchableProp.name();
            if (!StringUtils.hasLength(name)) {
                name = classPropertyMapping.getName();
            }
            mdMapping.setName(valueLookup.lookupMetaDataName(name));
            mdMapping.setPath(new StaticPropertyPath(mdMapping.getName()));
            mdMapping.setBoost(classPropertyMapping.getBoost());

            mdMapping.setAccessor(classPropertyMapping.getAccessor());
            mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

            bindConverter(mdMapping, searchableProp.converter(), clazz, type);
            bindSpellCheck(mdMapping, searchableProp.spellCheck());

            mdMapping.setStore(AnnotationsBindingUtils.convert(searchableProp.store()));
            mdMapping.setIndex(AnnotationsBindingUtils.convert(searchableProp.index()));
            mdMapping.setTermVector(AnnotationsBindingUtils.convert(searchableProp.termVector()));
            mdMapping.setOmitNorms(AnnotationsBindingUtils.convert(searchableProp.omitNorms()));
            mdMapping.setOmitTf(AnnotationsBindingUtils.convert(searchableProp.omitTf()));
            mdMapping.setReverse(AnnotationsBindingUtils.convert(searchableProp.reverse()));

            handleFormat(mdMapping, name, searchableProp.format());

            if (StringUtils.hasLength(searchableProp.analyzer())) {
                mdMapping.setAnalyzer(searchableProp.analyzer());
            } else {
                mdMapping.setAnalyzer(classMapping.getAnalyzer());
            }
            mdMapping.setExcludeFromAll(AnnotationsBindingUtils.convert(searchableProp.excludeFromAll()));

            classPropertyMapping.addMapping(mdMapping);
        }

        if (metaData != null) {
            bindMetaData(metaData, classPropertyMapping, clazz, type);
        }
        if (metaDatas != null) {
            for (SearchableMetaData searchableMetaData : metaDatas.value()) {
                bindMetaData(searchableMetaData, classPropertyMapping, clazz, type);
            }
        }
    }

    private void bindClassDynamicPropertyMapping(SearchableDynamicProperty searchableDynamicProperty, ClassDynamicPropertyMapping dynamicPropertyMapping,
                                                 AnnotatedElement annotatedElement, Class<?> clazz, Type type) {
        bindConverter(dynamicPropertyMapping, searchableDynamicProperty.converter(), clazz, type);

        dynamicPropertyMapping.setOverrideByName(searchableDynamicProperty.override());
        if (StringUtils.hasText(searchableDynamicProperty.namePrefix())) {
            dynamicPropertyMapping.setNamePrefix(searchableDynamicProperty.namePrefix());
        }
        if (StringUtils.hasText(searchableDynamicProperty.nameProperty())) {
            dynamicPropertyMapping.setNameProperty(searchableDynamicProperty.nameProperty());
        }
        if (StringUtils.hasText(searchableDynamicProperty.valueProperty())) {
            dynamicPropertyMapping.setValueProperty(searchableDynamicProperty.valueProperty());
        }
        if (StringUtils.hasText(searchableDynamicProperty.nameFormat())) {
            dynamicPropertyMapping.setNameFormat(searchableDynamicProperty.nameFormat());
        }
        if (StringUtils.hasText(searchableDynamicProperty.valueFormat())) {
            dynamicPropertyMapping.setValueFormat(searchableDynamicProperty.valueFormat());
        }
        if (StringUtils.hasText(searchableDynamicProperty.nameConverter())) {
            dynamicPropertyMapping.setNameConverterName(searchableDynamicProperty.nameConverter());
        }
        if (StringUtils.hasText(searchableDynamicProperty.valueConverter())) {
            dynamicPropertyMapping.setValueConverterName(searchableDynamicProperty.valueConverter());
        }

        dynamicPropertyMapping.getResourcePropertyMapping().setBoost(searchableDynamicProperty.boost());
        dynamicPropertyMapping.getResourcePropertyMapping().setStore(AnnotationsBindingUtils.convert(searchableDynamicProperty.store()));
        dynamicPropertyMapping.getResourcePropertyMapping().setIndex(AnnotationsBindingUtils.convert(searchableDynamicProperty.index()));
        dynamicPropertyMapping.getResourcePropertyMapping().setTermVector(AnnotationsBindingUtils.convert(searchableDynamicProperty.termVector()));
        dynamicPropertyMapping.getResourcePropertyMapping().setOmitNorms(AnnotationsBindingUtils.convert(searchableDynamicProperty.omitNorms()));
        dynamicPropertyMapping.getResourcePropertyMapping().setOmitTf(AnnotationsBindingUtils.convert(searchableDynamicProperty.omitTf()));

        if (StringUtils.hasLength(searchableDynamicProperty.nullValue())) {
            dynamicPropertyMapping.getResourcePropertyMapping().setNullValue(searchableDynamicProperty.nullValue());
        }
    }

    /**
     * Need to be almost exactly as <code>bindClassPropertyIdMapping</code>.
     */
    private void bindClassPropertyMapping(SearchableProperty searchableProp, ClassPropertyMapping classPropertyMapping,
                                          AnnotatedElement annotatedElement, Class<?> clazz, Type type) throws MappingException {

        bindConverter(classPropertyMapping, searchableProp.propertyConverter());

        if (!searchableProp.type().equals(Object.class)) {
            classPropertyMapping.setClassName(searchableProp.type().getName());
        } else {
            // check if it is a colleciton. If it is, try to infer the
            // type using generics
            classPropertyMapping.setClassName(AnnotationsBindingUtils.getCollectionParameterClassName(clazz, type));
        }

        if (StringUtils.hasLength(searchableProp.converter())) {
            classPropertyMapping.setManagedIdConverterName(searchableProp.converter());
        } else {
            classPropertyMapping.setManagedIdConverter(getConverter(clazz, type));
        }

        classPropertyMapping.setBoost(searchableProp.boost());
        classPropertyMapping.setManagedId(AnnotationsBindingUtils.convert(searchableProp.managedId()));
        classPropertyMapping.setManagedIdIndex(AnnotationsBindingUtils.convert(searchableProp.managedIdIndex()));
        classPropertyMapping.setOverrideByName(searchableProp.override());

        SearchableMetaData metaData = annotatedElement.getAnnotation(SearchableMetaData.class);
        SearchableMetaDatas metaDatas = annotatedElement.getAnnotation(SearchableMetaDatas.class);

        boolean hasMetaDataAnnotations = metaData != null || metaDatas != null;

        // check if we need to create a metadata because of the SearchProperty
        if (StringUtils.hasLength(searchableProp.name()) || !hasMetaDataAnnotations) {
            ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
            String name = searchableProp.name();
            if (!StringUtils.hasLength(name)) {
                name = classPropertyMapping.getName();
            }
            mdMapping.setName(valueLookup.lookupMetaDataName(name));
            mdMapping.setPath(new StaticPropertyPath(mdMapping.getName()));
            mdMapping.setBoost(classPropertyMapping.getBoost());

            bindConverter(mdMapping, searchableProp.converter(), clazz, type);
            bindSpellCheck(mdMapping, searchableProp.spellCheck());

            mdMapping.setAccessor(classPropertyMapping.getAccessor());
            mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

            mdMapping.setStore(AnnotationsBindingUtils.convert(searchableProp.store()));
            mdMapping.setIndex(AnnotationsBindingUtils.convert(searchableProp.index()));
            mdMapping.setTermVector(AnnotationsBindingUtils.convert(searchableProp.termVector()));
            mdMapping.setOmitNorms(AnnotationsBindingUtils.convert(searchableProp.omitNorms()));
            mdMapping.setOmitTf(AnnotationsBindingUtils.convert(searchableProp.omitTf()));
            mdMapping.setReverse(AnnotationsBindingUtils.convert(searchableProp.reverse()));

            handleFormat(mdMapping, name, searchableProp.format());

            mdMapping.setInternal(false);

            if (StringUtils.hasLength(searchableProp.analyzer())) {
                mdMapping.setAnalyzer(searchableProp.analyzer());
            } else {
                mdMapping.setAnalyzer(classMapping.getAnalyzer());
            }
            if (StringUtils.hasLength(searchableProp.nullValue())) {
                mdMapping.setNullValue(searchableProp.nullValue());
            }
            mdMapping.setExcludeFromAll(AnnotationsBindingUtils.convert(searchableProp.excludeFromAll()));

            classPropertyMapping.addMapping(mdMapping);
        }

        if (metaData != null) {
            bindMetaData(metaData, classPropertyMapping, clazz, type);
        }
        if (metaDatas != null) {
            for (SearchableMetaData searchableMetaData : metaDatas.value()) {
                bindMetaData(searchableMetaData, classPropertyMapping, clazz, type);
            }
        }
    }

    private void bindMetaData(SearchableMetaData searchableMetaData, ClassPropertyMapping classPropertyMapping,
                              Class<?> clazz, Type type) {

        ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
        String name = searchableMetaData.name();
        mdMapping.setName(valueLookup.lookupMetaDataName(name));
        mdMapping.setPath(new StaticPropertyPath(mdMapping.getName()));
        if (searchableMetaData.boost() == 1.0f) {
            mdMapping.setBoost(classPropertyMapping.getBoost());
        } else {
            mdMapping.setBoost(searchableMetaData.boost());
        }

        bindConverter(mdMapping, searchableMetaData.converter(), clazz, type);
        bindSpellCheck(mdMapping, searchableMetaData.spellCheck());

        mdMapping.setAccessor(classPropertyMapping.getAccessor());
        mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

        mdMapping.setStore(AnnotationsBindingUtils.convert(searchableMetaData.store()));
        mdMapping.setIndex(AnnotationsBindingUtils.convert(searchableMetaData.index()));
        mdMapping.setTermVector(AnnotationsBindingUtils.convert(searchableMetaData.termVector()));
        mdMapping.setOmitNorms(AnnotationsBindingUtils.convert(searchableMetaData.omitNorms()));
        mdMapping.setOmitTf(AnnotationsBindingUtils.convert(searchableMetaData.omitTf()));
        mdMapping.setReverse(AnnotationsBindingUtils.convert(searchableMetaData.reverse()));

        handleFormat(mdMapping, name, searchableMetaData.format());

        mdMapping.setInternal(false);

        if (StringUtils.hasLength(searchableMetaData.analyzer())) {
            mdMapping.setAnalyzer(searchableMetaData.analyzer());
        } else {
            mdMapping.setAnalyzer(classMapping.getAnalyzer());
        }
        if (StringUtils.hasLength(searchableMetaData.nullValue())) {
            mdMapping.setNullValue(searchableMetaData.nullValue());
        }
        mdMapping.setExcludeFromAll(AnnotationsBindingUtils.convert(searchableMetaData.excludeFromAll()));

        classPropertyMapping.addMapping(mdMapping);
    }

    private void bindDynamicMetaData(SearchableDynamicMetaData searchableMetaData) {

        DynamicMetaDataMapping mdMapping = new DynamicMetaDataMapping();
        String name = searchableMetaData.name();
        mdMapping.setName(valueLookup.lookupMetaDataName(name));
        mdMapping.setPath(new StaticPropertyPath(mdMapping.getName()));
        mdMapping.setBoost(searchableMetaData.boost());

        mdMapping.setOverrideByName(searchableMetaData.override());

        mdMapping.setConverterName(searchableMetaData.converter());
        mdMapping.setExpression(searchableMetaData.expression());
        if (StringUtils.hasLength(searchableMetaData.format())) {
            mdMapping.setFormat(searchableMetaData.format());
        }
        mdMapping.setType(searchableMetaData.type());

        mdMapping.setStore(AnnotationsBindingUtils.convert(searchableMetaData.store()));
        mdMapping.setIndex(AnnotationsBindingUtils.convert(searchableMetaData.index()));
        mdMapping.setTermVector(AnnotationsBindingUtils.convert(searchableMetaData.termVector()));
        mdMapping.setReverse(AnnotationsBindingUtils.convert(searchableMetaData.reverse()));

        mdMapping.setInternal(false);

        bindSpellCheck(mdMapping, searchableMetaData.spellCheck());

        if (StringUtils.hasLength(searchableMetaData.analyzer())) {
            mdMapping.setAnalyzer(searchableMetaData.analyzer());
        } else {
            mdMapping.setAnalyzer(classMapping.getAnalyzer());
        }
        if (StringUtils.hasLength(searchableMetaData.nullValue())) {
            mdMapping.setNullValue(searchableMetaData.nullValue());
        }
        mdMapping.setExcludeFromAll(AnnotationsBindingUtils.convert(searchableMetaData.excludeFromAll()));

        classMapping.addMapping(mdMapping);
    }

    private void bindConstantMetaData(SearchableConstant searchableConstant) {
        ConstantMetaDataMapping constantMapping = new ConstantMetaDataMapping();
        constantMapping.setName(valueLookup.lookupMetaDataName(searchableConstant.name()));
        constantMapping.setBoost(searchableConstant.boost());
        constantMapping.setStore(AnnotationsBindingUtils.convert(searchableConstant.store()));
        constantMapping.setIndex(AnnotationsBindingUtils.convert(searchableConstant.index()));
        constantMapping.setTermVector(AnnotationsBindingUtils.convert(searchableConstant.termVector()));
        constantMapping.setOmitNorms(AnnotationsBindingUtils.convert(searchableConstant.omitNorms()));
        constantMapping.setOmitTf(AnnotationsBindingUtils.convert(searchableConstant.omitTf()));
        if (StringUtils.hasLength(searchableConstant.analyzer())) {
            constantMapping.setAnalyzer(searchableConstant.analyzer());
        } else {
            constantMapping.setAnalyzer(classMapping.getAnalyzer());
        }
        constantMapping.setExcludeFromAll(AnnotationsBindingUtils.convert(searchableConstant.excludeFromAll()));
        constantMapping.setOverrideByName(searchableConstant.override());
        for (String value : searchableConstant.values()) {
            constantMapping.addMetaDataValue(valueLookup.lookupMetaDataValue(value));
        }
        bindSpellCheck(constantMapping, searchableConstant.spellCheck());
        classMapping.addMapping(constantMapping);
    }

    private void bindConverter(InternalMapping mapping, String converterName) {
        bindConverter(mapping, converterName, null, null);
    }

    private void bindSpellCheck(InternalResourcePropertyMapping mapping, org.compass.annotations.SpellCheck spellCheck) {
        if (spellCheck == org.compass.annotations.SpellCheck.EXCLUDE) {
            mapping.setSpellCheck(SpellCheck.EXCLUDE);
        } else if (spellCheck == org.compass.annotations.SpellCheck.INCLUDE) {
            mapping.setSpellCheck(SpellCheck.INCLUDE);
        } else if (spellCheck == org.compass.annotations.SpellCheck.NA) {
            mapping.setSpellCheck(SpellCheck.NA);
        }
    }

    private void bindConverter(InternalMapping mapping, String converterName, Class<?> clazz, Type type) {
        if (StringUtils.hasLength(converterName)) {
            mapping.setConverterName(converterName);
            return;
        }
        if (clazz == null) {
            return;
        }
        mapping.setConverter(getConverter(clazz, type));
    }

    public Converter getConverter(Class<?> clazz, Type type) {
        Class<?> actualClass = AnnotationsBindingUtils.getCollectionParameterClass(clazz, type);
        if (actualClass == null) {
            actualClass = clazz;
        }
        SearchableClassConverter searchableClassConverter = actualClass.getAnnotation(SearchableClassConverter.class);
        if (searchableClassConverter == null) {
            return null;
        }
        Object objConverter;
        try {
            objConverter = searchableClassConverter.value().newInstance();
        } catch (Exception e) {
            throw new MappingException("Failed to create converter [" + searchableClassConverter.value().getName() + "]", e);
        }
        if (!(objConverter instanceof Converter)) {
            throw new MappingException("[" + searchableClassConverter + "] does not implement Converter interface");
        }
        Converter converter = (Converter) objConverter;
        if (searchableClassConverter.settings().length > 0 && !(converter instanceof CompassConfigurable)) {
            throw new MappingException("[" + searchableClassConverter + "] does not implement CompassConfigurable" +
                    " interface, but has settings set, please implement it so settings can be injected");
        }
        if (converter instanceof CompassConfigurable) {
            CompassSettings settings = new CompassSettings();
            for (int i = 0; i < searchableClassConverter.settings().length; i++) {
                SearchSetting setting = searchableClassConverter.settings()[i];
                settings.setSetting(setting.name(), setting.value());
            }
            ((CompassConfigurable) converter).configure(settings);
        }
        return converter;
    }

    private void bindObjectMapping(InternalObjectMapping objectMapping, String actualAccessor, String name,
                                   String annotationAccessor, Class<?> searchableClass) {
        if (!StringUtils.hasLength(annotationAccessor)) {
            objectMapping.setAccessor(actualAccessor);
        } else {
            objectMapping.setAccessor(annotationAccessor);
        }
        objectMapping.setName(name);
        objectMapping.setPropertyName(name);
        // set the defined in alias for multiple ref aliases
        // note, with annotation, we might not have @Searchable defined on
        // the class, so we are using the FQN class name instead
        if (searchableClass.isAnnotationPresent(Searchable.class)) {
            Searchable searchable = searchableClass.getAnnotation(Searchable.class);
            objectMapping.setDefinedInAlias(getAliasFromSearchableClass(searchableClass, searchable));
        } else {
            objectMapping.setDefinedInAlias(ClassUtils.getShortName(searchableClass));
        }
    }

    /**
     * Returns a string array of aliases from a comma separated string
     */
    private String[] getAliases(String commaSeparatedAliases) {
        ArrayList<String> aliases = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(commaSeparatedAliases, ",");
        while (st.hasMoreTokens()) {
            String extendedAlias = st.nextToken().trim();
            Alias alias = valueLookup.lookupAlias(extendedAlias);
            if (alias == null) {
                aliases.add(extendedAlias);
            } else {
                aliases.add(alias.getName());
            }
        }
        return aliases.toArray(new String[aliases.size()]);
    }

    private void handleFormat(ClassPropertyMetaDataMapping mdMapping, String name, String format) {
        if (!StringUtils.hasLength(format)) {
            return;
        }
        if (mdMapping.getConverter() == null) {
            if (format == null) {
                format = valueLookup.lookupMetaDataFormat(name);
            }
            if (format != null) {
                mdMapping.setConverter(new FormatDelegateConverter(format));
            }
        }
    }
}
