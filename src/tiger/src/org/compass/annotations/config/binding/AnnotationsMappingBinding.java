/*
 * Copyright 2004-2006 the original author or authors.
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

import org.compass.annotations.*;
import org.compass.core.Property;
import org.compass.core.config.CommonMetaDataLookup;
import org.compass.core.config.ConfigurationException;
import org.compass.core.config.binding.MappingBindingSupport;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyIdMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.metadata.Alias;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

/**
 * @author kimchy
 */
public class AnnotationsMappingBinding extends MappingBindingSupport {

    private CommonMetaDataLookup valueLookup;

    private CompassMapping mapping;

    public void setUpBinding(CompassMapping mapping, CompassMetaData metaData) {
        this.mapping = mapping;
        this.valueLookup = new CommonMetaDataLookup(metaData);
    }


    public boolean addClass(Class clazz) throws ConfigurationException, MappingException {
        Class<?> annotationClass = clazz;
        Searchable searchable = annotationClass.getAnnotation(Searchable.class);
        if (searchable == null) {
            return false;
        }
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
        // try and check is the class mapping is already defined
        // if it is, we will add mapping definitions on top of it
        boolean newClassMapping = false;
        ClassMapping classMapping;
        AliasMapping aliasMapping = mapping.getAliasMapping(alias);
        if (aliasMapping != null) {
            if (!(aliasMapping instanceof ClassMapping)) {
                throw new MappingException("Defined searchable annotation on a class with alias [" + alias + "] but it" +
                        " not of type class mapping");
            }
            classMapping = (ClassMapping) aliasMapping;
        } else {
            classMapping = new ClassMapping();
            newClassMapping = true;
        }
        classMapping.setAlias(alias);

        classMapping.setName(clazz.getName());
        classMapping.setClazz(clazz);

        // sub index
        String subIndex = searchable.subIndex();
        if (!StringUtils.hasLength(subIndex)) {
            subIndex = alias;
        }
        classMapping.setSubIndex(subIndex);

        String[] extend = searchable.extend();
        if (extend.length != 0 && (StringUtils.hasLength(extend[0]))) {
            ArrayList<String> extendedMappings = new ArrayList<String>();
            for (String extendedAlias : extend) {
                Alias extendedAliasLookup = valueLookup.lookupAlias(extendedAlias);
                if (extendedAliasLookup == null) {
                    extendedMappings.add(extendedAlias);
                } else {
                    extendedMappings.add(extendedAliasLookup.getName());
                }
            }
            classMapping.setExtendedMappings(extendedMappings.toArray(new String[extendedMappings.size()]));
        }

        SearchableAllMetaData allMetaData = annotationClass.getAnnotation(SearchableAllMetaData.class);
        if (allMetaData == null) {
            classMapping.setAllSupported(searchable.all());
        } else {
            classMapping.setAllSupported(allMetaData.enable());
            if (classMapping.isAllSupported()) {
                if (StringUtils.hasLength(allMetaData.name())) {
                    classMapping.setAllProperty(allMetaData.name());
                }
                if (StringUtils.hasLength(allMetaData.analyzer())) {
                    classMapping.setAllAnalyzer(allMetaData.analyzer());
                }
                classMapping.setAllTermVector(convert(allMetaData.termVector()));
            }
        }

        classMapping.setBoost(searchable.boost());
        classMapping.setRoot(searchable.root());
        classMapping.setPoly(searchable.poly());
        if (StringUtils.hasLength(searchable.analyzer())) {
            classMapping.setAnalyzer(searchable.analyzer());
        }

        processAnnotatedClass(annotationClass, classMapping);

        if (newClassMapping) {
            mapping.addMapping(classMapping);
        }

        return true;
    }

    private void processAnnotatedClass(Class<?> clazz, ClassMapping classMapping) {
        if (clazz.equals(Class.class)) {
            return;
        }
        Class<?> superClazz = clazz.getSuperclass();
        if (superClazz != null && !superClazz.equals(Object.class)) {
            processAnnotatedClass(superClazz, classMapping);
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> anInterface : interfaces) {
            processAnnotatedClass(anInterface, classMapping);
        }

        for (Field field : clazz.getDeclaredFields()) {
            for (Annotation annotation : field.getAnnotations()) {
                processsAnnotatedElement(ClassUtils.getShortNameForField(field), "field", annotation, field, classMapping);
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
                    processsAnnotatedElement(ClassUtils.getShortNameForMethod(method), "property", annotation, method, classMapping);
                }
            }
        }
    }

    private void processsAnnotatedElement(String name, String accessor, Annotation annotation,
                                          AnnotatedElement annotatedElement, ClassMapping classMapping) {

        if (annotation instanceof SearchableId) {
            ClassPropertyIdMapping classPropertyMapping = new ClassPropertyIdMapping();
            classPropertyMapping.setAccessor(accessor);
            classPropertyMapping.setName(name);
            classPropertyMapping.setObjClass(classMapping.getClazz());
            classPropertyMapping.setPropertyName(name);
            bindClassPropertyIdMapping((SearchableId) annotation, classPropertyMapping, annotatedElement);
            classMapping.addMapping(classPropertyMapping);
        } else if (annotation instanceof SearchableProperty) {
            ClassPropertyMapping classPropertyMapping = new ClassPropertyMapping();
            classPropertyMapping.setName(name);
            classPropertyMapping.setAccessor(accessor);
            classPropertyMapping.setObjClass(classMapping.getClazz());
            classPropertyMapping.setPropertyName(name);
            bindClassPropertyMapping((SearchableProperty) annotation, classPropertyMapping, annotatedElement);
            classMapping.addMapping(classPropertyMapping);
        }
    }

    /**
     * Need to be almost exactly as <code>bindClassPropertyMapping</code>.
     */
    private void bindClassPropertyIdMapping(SearchableId searchableProp, ClassPropertyIdMapping classPropertyMapping,
                                            AnnotatedElement annotatedElement) throws MappingException {

        classPropertyMapping.setBoost(searchableProp.boost());
        classPropertyMapping.setManagedId(convert(searchableProp.managedId()));
        classPropertyMapping.setManagedIdIndex(convert(searchableProp.managedIdIndex()));
        classPropertyMapping.setOverrideByName(searchableProp.override());

        SearchableMetaData metaData = annotatedElement.getAnnotation(SearchableMetaData.class);
        SearchableMetaDatas metaDatas = annotatedElement.getAnnotation(SearchableMetaDatas.class);

        boolean hasMetaDataAnnotations = metaData != null || metaDatas != null;

        // check if we need to create a metadata because of the SearchProperty
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
            mdMapping.setBoost(classPropertyMapping.getBoost());

            mdMapping.setAccessor(classPropertyMapping.getAccessor());
            mdMapping.setObjClass(classPropertyMapping.getObjClass());
            mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

            mdMapping.setStore(convert(searchableProp.store()));
            mdMapping.setIndex(convert(searchableProp.index()));
            mdMapping.setTermVector(convert(searchableProp.termVector()));
            mdMapping.setReverse(convert(searchableProp.reverse()));

            if (StringUtils.hasLength(searchableProp.analyzer())) {
                mdMapping.setAnalyzer(searchableProp.analyzer());
            }
            mdMapping.setExcludeFromAll(searchableProp.exceludeFromAll());

            classPropertyMapping.addMapping(mdMapping);
        }

        if (metaData != null) {
            bindMetaData(metaData, classPropertyMapping);
        }
        if (metaDatas != null) {
            for (SearchableMetaData searchableMetaData : metaDatas.value()) {
                bindMetaData(searchableMetaData, classPropertyMapping);
            }
        }
    }

    /**
     * Need to be almost exactly as <code>bindClassPropertyIdMapping</code>.
     */
    private void bindClassPropertyMapping(SearchableProperty searchableProp, ClassPropertyMapping classPropertyMapping,
                                          AnnotatedElement annotatedElement) throws MappingException {

        classPropertyMapping.setBoost(searchableProp.boost());
        classPropertyMapping.setManagedId(convert(searchableProp.managedId()));
        classPropertyMapping.setManagedIdIndex(convert(searchableProp.managedIdIndex()));
        classPropertyMapping.setOverrideByName(searchableProp.override());

        Class collectionClass = searchableProp.collectionClass();
        if (!collectionClass.equals(Object.class)) {
            classPropertyMapping.setColClassName(collectionClass.getName());
        }

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
            mdMapping.setBoost(classPropertyMapping.getBoost());

            mdMapping.setAccessor(classPropertyMapping.getAccessor());
            mdMapping.setObjClass(classPropertyMapping.getObjClass());
            mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

            mdMapping.setStore(convert(searchableProp.store()));
            mdMapping.setIndex(convert(searchableProp.index()));
            mdMapping.setTermVector(convert(searchableProp.termVector()));
            mdMapping.setReverse(convert(searchableProp.reverse()));

            mdMapping.setInternal(false);

            if (StringUtils.hasLength(searchableProp.analyzer())) {
                mdMapping.setAnalyzer(searchableProp.analyzer());
            }
            mdMapping.setExcludeFromAll(searchableProp.exceludeFromAll());

            classPropertyMapping.addMapping(mdMapping);
        }

        if (metaData != null) {
            bindMetaData(metaData, classPropertyMapping);
        }
        if (metaDatas != null) {
            for (SearchableMetaData searchableMetaData : metaDatas.value()) {
                bindMetaData(searchableMetaData, classPropertyMapping);
            }
        }
    }

    private void bindMetaData(SearchableMetaData searchableMetaData, ClassPropertyMapping classPropertyMapping) {

        ClassPropertyMetaDataMapping mdMapping = new ClassPropertyMetaDataMapping();
        String name = searchableMetaData.name();
        mdMapping.setName(valueLookup.lookupMetaDataName(name));
        mdMapping.setBoost(classPropertyMapping.getBoost());

        mdMapping.setAccessor(classPropertyMapping.getAccessor());
        mdMapping.setObjClass(classPropertyMapping.getObjClass());
        mdMapping.setPropertyName(classPropertyMapping.getPropertyName());

        mdMapping.setStore(convert(searchableMetaData.store()));
        mdMapping.setIndex(convert(searchableMetaData.index()));
        mdMapping.setTermVector(convert(searchableMetaData.termVector()));
        mdMapping.setReverse(convert(searchableMetaData.reverse()));

        mdMapping.setInternal(false);

        if (StringUtils.hasLength(searchableMetaData.analyzer())) {
            mdMapping.setAnalyzer(searchableMetaData.analyzer());
        }
        mdMapping.setExcludeFromAll(searchableMetaData.exceludeFromAll());

        classPropertyMapping.addMapping(mdMapping);
    }

    private ClassPropertyMapping.ManagedId convert(ManagedId managedId) {
        if (managedId == ManagedId.AUTO) {
            return ClassPropertyMapping.ManagedId.AUTO;
        } else if (managedId == ManagedId.TRUE) {
            return ClassPropertyMapping.ManagedId.TRUE;
        } else if (managedId == ManagedId.FALSE) {
            return ClassPropertyMapping.ManagedId.FALSE;
        }
        throw new IllegalArgumentException("Failed to convert managedId [" + managedId + "]");
    }

    private Property.TermVector convert(TermVector termVector) {
        if (termVector == TermVector.NO) {
            return Property.TermVector.NO;
        } else if (termVector == TermVector.YES) {
            return Property.TermVector.YES;
        } else if (termVector == TermVector.WITH_POSITIONS) {
            return Property.TermVector.WITH_POSITIONS;
        } else if (termVector == TermVector.WITH_OFFSETS) {
            return Property.TermVector.WITH_OFFSETS;
        } else if (termVector == TermVector.WITH_POSITIONS_OFFESTS) {
            return Property.TermVector.WITH_POSITIONS_OFFSETS;
        }
        throw new IllegalArgumentException("Failed to convert termVectory [" + termVector + "]");
    }

    private ResourcePropertyMapping.ReverseType convert(Reverse reverse) {
        if (reverse == Reverse.NO) {
            return ResourcePropertyMapping.ReverseType.NO;
        } else if (reverse == Reverse.READER) {
            return ResourcePropertyMapping.ReverseType.READER;
        } else if (reverse == Reverse.STRING) {
            return ResourcePropertyMapping.ReverseType.STRING;
        }
        throw new IllegalArgumentException("Failed to convert reverse [" + reverse + "]");
    }

    private Property.Store convert(Store store) {
        if (store == Store.NO) {
            return Property.Store.NO;
        } else if (store == Store.YES) {
            return Property.Store.YES;
        } else if (store == Store.COMPRESS) {
            return Property.Store.COMPRESS;
        }
        throw new IllegalArgumentException("Failed to convert store [" + store + "]");
    }

    private Property.Index convert(Index index) {
        if (index == Index.NO) {
            return Property.Index.NO;
        } else if (index == Index.TOKENIZED) {
            return Property.Index.TOKENIZED;
        } else if (index == Index.UN_TOKENIZED) {
            return Property.Index.UN_TOKENIZED;
        }
        throw new IllegalArgumentException("Failed to convert index [" + index + "]");
    }

    private Property.Index convert(ManagedIdIndex index) {
        if (index == ManagedIdIndex.NA) {
            return null;
        } else if (index == ManagedIdIndex.NO) {
            return Property.Index.NO;
        } else if (index == ManagedIdIndex.TOKENIZED) {
            return Property.Index.TOKENIZED;
        } else if (index == ManagedIdIndex.UN_TOKENIZED) {
            return Property.Index.UN_TOKENIZED;
        }
        throw new IllegalArgumentException("Failed to convert index [" + index + "]");
    }
}
