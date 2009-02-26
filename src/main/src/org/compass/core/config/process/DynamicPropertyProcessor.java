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

package org.compass.core.config.process;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.compass.annotations.SearchableDynamicName;
import org.compass.annotations.SearchableDynamicValue;
import org.compass.core.accessor.AccessorUtils;
import org.compass.core.accessor.Getter;
import org.compass.core.accessor.PropertyAccessor;
import org.compass.core.accessor.PropertyAccessorFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.mapping.support.DynamicFormatDelegateConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.util.AnnotationUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class DynamicPropertyProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy, ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        // initalize the property accessor registry
        PropertyAccessorFactory propertyAccessorFactory = new PropertyAccessorFactory();
        propertyAccessorFactory.configure(settings);

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) aliasMapping;
                for (Iterator<Mapping> it = classMapping.mappingsIt(); it.hasNext();) {
                    Mapping m = it.next();
                    if (m instanceof ClassDynamicPropertyMapping) {
                        processDynamicMapping((ClassDynamicPropertyMapping) m, settings, propertyAccessorFactory, converterLookup);
                    }
                }
            }
        }

        return compassMapping;
    }

    private void processDynamicMapping(ClassDynamicPropertyMapping dynamicPropertyMapping, CompassSettings settings,
                                       PropertyAccessorFactory propertyAccessorFactory, ConverterLookup converterLookup) {

        PropertyAccessor pAccessor = propertyAccessorFactory.getPropertyAccessor(dynamicPropertyMapping.getAccessor(), settings);

        Class getterType = dynamicPropertyMapping.getGetter().getReturnType();
        Class dynaType = getterType;
        if (getterType.isArray()) {
            dynaType = getterType.getComponentType();
            dynamicPropertyMapping.setObjectType(ClassDynamicPropertyMapping.ObjectType.ARRAY);
        } else if (Collection.class.isAssignableFrom(getterType)) {
            dynamicPropertyMapping.setObjectType(ClassDynamicPropertyMapping.ObjectType.COLLECTION);
            dynaType = AccessorUtils.getCollectionParameter(dynamicPropertyMapping.getGetter());
        } else if (Map.class.isAssignableFrom(getterType)) {
            dynamicPropertyMapping.setObjectType(ClassDynamicPropertyMapping.ObjectType.MAP);
        } else {
            dynamicPropertyMapping.setObjectType(ClassDynamicPropertyMapping.ObjectType.PLAIN);
        }

        if (dynamicPropertyMapping.getObjectType() == ClassDynamicPropertyMapping.ObjectType.MAP) {

            Class keyType = AccessorUtils.getMapKeyParameter(dynamicPropertyMapping.getGetter());
            Class valueType = AccessorUtils.getMapValueParameter(dynamicPropertyMapping.getGetter());

            if (dynamicPropertyMapping.getNameProperty() != null && keyType == null) {
                throw new MappingException("Dynamic property mapping [" + dynamicPropertyMapping.getName() + "] defined in alias [" + dynamicPropertyMapping.getDefinedInAlias() + "] defines name property but Map is not generified to find it");
            }
            if (dynamicPropertyMapping.getValueProperty() != null && valueType == null) {
                throw new MappingException("Dynamic property mapping [" + dynamicPropertyMapping.getName() + "] defined in alias [" + dynamicPropertyMapping.getDefinedInAlias() + "] defines value property but Map is not generified to find it");
            }

            if (keyType != null) {
                if (dynamicPropertyMapping.getNameProperty() != null) {
                    dynamicPropertyMapping.setNameGetter(pAccessor.getGetter(keyType, dynamicPropertyMapping.getNameProperty()));
                } else {
                    processSearchableDynamicName(dynamicPropertyMapping, settings, propertyAccessorFactory, keyType, converterLookup);
                }
            }

            if (valueType != null) {
                if (dynamicPropertyMapping.getValueProperty() != null) {
                    dynamicPropertyMapping.setValueGetter(pAccessor.getGetter(valueType, dynamicPropertyMapping.getValueProperty()));
                } else {
                    processSearchableDynamicValue(dynamicPropertyMapping, settings, propertyAccessorFactory, valueType, converterLookup);
                }
            }

            if (dynamicPropertyMapping.getValueGetter() != null) {
                if (AccessorUtils.isMapValueParameterArray(dynamicPropertyMapping.getGetter())) {
                    dynamicPropertyMapping.setMapValueType(ClassDynamicPropertyMapping.ValueType.ARRAY);
                } else if (AccessorUtils.isMapValueParameterCollection(dynamicPropertyMapping.getGetter())) {
                    dynamicPropertyMapping.setMapValueType(ClassDynamicPropertyMapping.ValueType.COLLECTION);
                } else {
                    dynamicPropertyMapping.setMapValueType(ClassDynamicPropertyMapping.ValueType.PLAIN);
                }
                processValueType(dynamicPropertyMapping, dynamicPropertyMapping.getValueGetter().getReturnType());
            } else {
                // can't be a complex array within an object, since no getter
                dynamicPropertyMapping.setMapValueType(ClassDynamicPropertyMapping.ValueType.PLAIN);

                if (AccessorUtils.isMapValueParameterArray(dynamicPropertyMapping.getGetter())) {
                    dynamicPropertyMapping.setValueType(ClassDynamicPropertyMapping.ValueType.ARRAY);
                } else if (AccessorUtils.isMapValueParameterCollection(dynamicPropertyMapping.getGetter())) {
                    dynamicPropertyMapping.setValueType(ClassDynamicPropertyMapping.ValueType.COLLECTION);
                } else if (valueType != null) {
                    processValueType(dynamicPropertyMapping, valueType);
                }
            }
        } else {
            if (dynamicPropertyMapping.getNameProperty() != null) {
                dynamicPropertyMapping.setNameGetter(pAccessor.getGetter(dynaType, dynamicPropertyMapping.getNameProperty()));
            } else if (dynaType != null) {
                processSearchableDynamicName(dynamicPropertyMapping, settings, propertyAccessorFactory, dynaType, converterLookup);
            }

            if (dynamicPropertyMapping.getValueProperty() != null) {
                dynamicPropertyMapping.setValueGetter(pAccessor.getGetter(dynaType, dynamicPropertyMapping.getValueProperty()));
            } else {
                processSearchableDynamicValue(dynamicPropertyMapping, settings, propertyAccessorFactory, dynaType, converterLookup);
            }

            // ok, we set the value getter, we can now derive the value type
            Getter valueGetter = dynamicPropertyMapping.getValueGetter();
            processValueType(dynamicPropertyMapping, valueGetter.getReturnType());
        }

        if (dynamicPropertyMapping.getNameFormat() != null) {
            dynamicPropertyMapping.setNameConverter(new DynamicFormatDelegateConverter(dynamicPropertyMapping.getNameFormat(), converterLookup));
        }
        if (dynamicPropertyMapping.getValueFormat() != null) {
            dynamicPropertyMapping.setValueConverter(new DynamicFormatDelegateConverter(dynamicPropertyMapping.getValueFormat(), converterLookup));
        }
    }

    private void processValueType(ClassDynamicPropertyMapping dynamicPropertyMapping, Class valueType) {
        if (valueType.isArray()) {
            dynamicPropertyMapping.setValueType(ClassDynamicPropertyMapping.ValueType.ARRAY);
        } else if (Collection.class.isAssignableFrom(valueType)) {
            dynamicPropertyMapping.setValueType(ClassDynamicPropertyMapping.ValueType.COLLECTION);
        } else {
            dynamicPropertyMapping.setValueType(ClassDynamicPropertyMapping.ValueType.PLAIN);
        }
    }

    private void processSearchableDynamicValue(ClassDynamicPropertyMapping dynamicPropertyMapping, CompassSettings settings,
                                               PropertyAccessorFactory propertyAccessorFactory, Class dynaType,
                                               ConverterLookup converterLookup) {
        SearchableDynamicValue dynamicValue = null;
        Method method = AnnotationUtils.findAnnotatedMethod(SearchableDynamicValue.class, dynaType);
        if (method != null) {
            dynamicValue = method.getAnnotation(SearchableDynamicValue.class);
            dynamicPropertyMapping.setValueGetter(propertyAccessorFactory.getPropertyAccessor("property", settings).getGetter(method.getDeclaringClass(), method.getName()));
        } else {
            Field field = AnnotationUtils.findAnnotatedField(SearchableDynamicValue.class, dynaType);
            if (field != null) {
                dynamicValue = field.getAnnotation(SearchableDynamicValue.class);
                dynamicPropertyMapping.setValueGetter(propertyAccessorFactory.getPropertyAccessor("field", settings).getGetter(field.getDeclaringClass(), field.getName()));
            }
        }
        if (dynamicValue != null) {
            if (dynamicPropertyMapping.getValueConverterName() == null && StringUtils.hasText(dynamicValue.converter())) {
                dynamicPropertyMapping.setValueConverterName(dynamicValue.converter());
            }
            if (dynamicPropertyMapping.getValueFormat() == null && StringUtils.hasText(dynamicValue.format())) {
                dynamicPropertyMapping.setValueFormat(dynamicValue.format());
            }
            if (dynamicPropertyMapping.getResourcePropertyMapping().getNullValue().length() == 0 && StringUtils.hasText(dynamicValue.nullValue())) {
                dynamicPropertyMapping.getResourcePropertyMapping().setNullValue(dynamicValue.nullValue());
            }
        }
    }

    private void processSearchableDynamicName(ClassDynamicPropertyMapping dynamicPropertyMapping, CompassSettings settings,
                                              PropertyAccessorFactory propertyAccessorFactory, Class dynaType,
                                              ConverterLookup converterLookup) {
        SearchableDynamicName dynamicName = null;
        Method method = AnnotationUtils.findAnnotatedMethod(SearchableDynamicName.class, dynaType);
        if (method != null) {
            dynamicName = method.getAnnotation(SearchableDynamicName.class);
            dynamicPropertyMapping.setNameGetter(propertyAccessorFactory.getPropertyAccessor("property", settings).getGetter(method.getDeclaringClass(), method.getName()));
        } else {
            Field field = AnnotationUtils.findAnnotatedField(SearchableDynamicName.class, dynaType);
            if (field != null) {
                dynamicName = field.getAnnotation(SearchableDynamicName.class);
                dynamicPropertyMapping.setNameGetter(propertyAccessorFactory.getPropertyAccessor("field", settings).getGetter(field.getDeclaringClass(), field.getName()));
            }
        }
        if (dynamicName != null) {
            if (dynamicPropertyMapping.getNamePrefix() == null && StringUtils.hasText(dynamicName.prefix())) {
                dynamicPropertyMapping.setNamePrefix(dynamicName.prefix());
            }
            if (dynamicPropertyMapping.getNameConverterName() == null && StringUtils.hasText(dynamicName.converter())) {
                dynamicPropertyMapping.setNameConverterName(dynamicName.converter());
            }
            if (dynamicPropertyMapping.getNameFormat() == null && StringUtils.hasText(dynamicName.format())) {
                dynamicPropertyMapping.setNameFormat(dynamicName.format());
            }
        }
    }
}
