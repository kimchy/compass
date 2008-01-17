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

package org.compass.core.config.process;

import org.compass.core.Property;
import org.compass.core.accessor.AccessorUtils;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DelegateConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public abstract class MappingProcessorUtils {

    public static void lookupConverter(ConverterLookup converterLookup, Mapping mapping) {
        lookupConverter(converterLookup, mapping, true);
    }

    public static void lookupConverter(ConverterLookup converterLookup, Mapping mapping, boolean forceConverter) {
        if (mapping.getConverter() == null) {
            if (mapping.getConverterName() != null) {
                String converterName = mapping.getConverterName();
                mapping.setConverter(converterLookup.lookupConverter(converterName));
                if (mapping.getConverter() == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter [" + converterName + "] for mapping " +
                            "[" + mapping.getName() + "]");
                }
            } else {
                mapping.setConverter(converterLookup.lookupConverter(mapping.getClass()));
                if (mapping.getConverter() == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter for class [" + mapping.getClass() + "]" +
                            " for mapping [" + mapping.getName() + "]");
                }
            }
        }
    }

    public static void lookupConverter(ConverterLookup converterLookup, ClassPropertyMetaDataMapping mdMapping,
                                           ClassPropertyMapping classPropertyMapping) {
        if (mdMapping.getConverter() == null) {

            if (mdMapping.getConverterName() != null) {
                String converterName = mdMapping.getConverterName();
                mdMapping.setConverter(converterLookup.lookupConverter(converterName));
                if (mdMapping.getConverter() == null) {
                    throw new ConfigurationException("Failed to find converter [" + converterName + "] for property " +
                            "[" + classPropertyMapping.getName() + "]");
                }
            } else {
                Converter converter = resolveConverterByClass(classPropertyMapping, converterLookup);
                mdMapping.setConverter(converter);
            }
        } else if (mdMapping.getConverter() instanceof DelegateConverter) {
            Converter converter = resolveConverterByClass(classPropertyMapping, converterLookup);
            ((DelegateConverter) mdMapping.getConverter()).setDelegatedConverter(converter);
        }
    }

    public static void addInternalId(CompassSettings settings, ConverterLookup converterLookup,
                                     ClassPropertyMapping classPropertyMapping) throws MappingException {
        ClassPropertyMetaDataMapping internalIdMapping = new ClassPropertyMetaDataMapping();
        internalIdMapping.setInternal(true);
        internalIdMapping.setName(classPropertyMapping.getName());
        internalIdMapping.setStore(Property.Store.YES);
        internalIdMapping.setOmitNorms(true);
        Property.Index index = classPropertyMapping.getManagedIdIndex();
        if (index == null) {
            String indexSetting = settings.getSetting(CompassEnvironment.Osem.MANAGED_ID_INDEX, "no");
            index = Property.Index.fromString(indexSetting);
            if (index == Property.Index.TOKENIZED) {
                throw new ConfigurationException("Set the setting [" + CompassEnvironment.Osem.MANAGED_ID_INDEX
                        + "] with value of [tokenized], must be either [no] or [un_tokenized]");
            }
        }
        internalIdMapping.setIndex(index);
        internalIdMapping.setBoost(1.0f);
        internalIdMapping.setGetter(classPropertyMapping.getGetter());
        internalIdMapping.setSetter(classPropertyMapping.getSetter());
        internalIdMapping.setConverter(classPropertyMapping.getManagedIdConverter());
        internalIdMapping.setConverterName(classPropertyMapping.getManagedIdConverterName());
        process(internalIdMapping, classPropertyMapping, converterLookup);
        int propertyIndex = classPropertyMapping.addMapping(internalIdMapping);
        classPropertyMapping.setIdPropertyIndex(propertyIndex);
    }

    public static void process(ClassPropertyMetaDataMapping mdMapping, ClassPropertyMapping classPropertyMapping,
                               ConverterLookup converterLookup) throws MappingException {
        lookupConverter(converterLookup, mdMapping, classPropertyMapping);
        mdMapping.setPropertyName(classPropertyMapping.getPropertyName());
        if (mdMapping.isInternal()) {
            // Thats the key, save it directly under the class property mapping path
            mdMapping.setPath(classPropertyMapping.getPath().hintStatic());
        } else {
            mdMapping.setPath(new StaticPropertyPath(mdMapping.getName()));
        }
    }

    private static Converter resolveConverterByClass(ClassPropertyMapping classPropertyMapping, ConverterLookup converterLookup) {
        String className = classPropertyMapping.getClassName();
        Class clazz = null;

        try {
            if (className != null) {
                clazz = ClassUtils.forName(className, converterLookup.getSettings().getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            throw new MappingException("Failed to find class [" + className + "]", e);
        }

        if (clazz == null) {
            clazz = AccessorUtils.getCollectionParameter(classPropertyMapping.getGetter());
        }

        Converter converter;
        if (clazz == null) {
            clazz = classPropertyMapping.getGetter().getReturnType();
            converter = converterLookup.lookupConverter(clazz);
            // Not sure how pretty it is, but here we go
            // if we did not set a converter for the array type, see if we
            // set a converter to the component type, and than we use the
            // array mapping as well
            if (converter == null && clazz.isArray()) {
                clazz = clazz.getComponentType();
                converter = converterLookup.lookupConverter(clazz);
            }
        } else {
            converter = converterLookup.lookupConverter(clazz);
        }

        if (converter == null) {
            throw new MappingException("No converter defined for type ["
                    + classPropertyMapping.getGetter().getReturnType().getName() + "]");
        }
        return converter;
    }

}
