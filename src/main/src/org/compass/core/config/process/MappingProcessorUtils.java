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

import org.compass.core.Property;
import org.compass.core.accessor.AccessorUtils;
import org.compass.core.accessor.Getter;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DelegateConverter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.internal.InternalMapping;
import org.compass.core.mapping.internal.InternalResourcePropertyMapping;
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
        if (mapping.getConverter() == null || mapping.getConverter() instanceof DelegateConverter) {
            Converter converter;
            if (mapping.getConverterName() != null) {
                String converterName = mapping.getConverterName();
                converter = converterLookup.lookupConverter(converterName);
                if (converter == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter [" + converterName + "] for mapping " +
                            "[" + mapping.getName() + "]");
                }
            } else {
                converter = converterLookup.lookupConverter(mapping.getClass());
                if (converter == null && forceConverter) {
                    throw new ConfigurationException("Failed to find converter for class [" + mapping.getClass() + "]" +
                            " for mapping [" + mapping.getName() + "]");
                }
            }
            if (mapping.getConverter() instanceof DelegateConverter) {
                ((DelegateConverter) mapping.getConverter()).setDelegatedConverter(converter);
            } else {
                ((InternalMapping) mapping).setConverter(converter);
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
                            "[" + classPropertyMapping.getName() + "] and alias [" + classPropertyMapping.getDefinedInAlias() + "]");
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
                                     ClassPropertyMapping classPropertyMapping, boolean mustBeNotAnalyzed) throws MappingException {
        ClassPropertyMetaDataMapping internalIdMapping = new ClassPropertyMetaDataMapping();
        internalIdMapping.setInternal(true);
        internalIdMapping.setName(classPropertyMapping.getName());
        internalIdMapping.setStore(Property.Store.YES);
        internalIdMapping.setTermVector(Property.TermVector.NO);
        internalIdMapping.setOmitNorms(true);
        internalIdMapping.setOmitTf(true);
        Property.Index index;
        if (mustBeNotAnalyzed) {
            index = Property.Index.NOT_ANALYZED;
        } else {
            index = classPropertyMapping.getManagedIdIndex();
        }
        // note, index might be null, but it will be resolved later in applyResourcePropertySettings
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

    public static Converter resolveConverter(String converterName, String className, Getter getter, ConverterLookup converterLookup) {
        if (converterName != null) {
            return converterLookup.lookupConverter(converterName);
        }
        return resolveConverterByClass(className, getter, converterLookup);
    }

    public static Converter resolveConverter(String converterName, String className, Class type, ConverterLookup converterLookup) {
        if (converterName != null) {
            return converterLookup.lookupConverter(converterName);
        }
        return resolveConverterByClass(className, type, converterLookup);
    }

    public static Converter resolveConverterByClass(ClassPropertyMapping classPropertyMapping, ConverterLookup converterLookup) {
        return resolveConverterByClass(classPropertyMapping.getClassName(), classPropertyMapping.getGetter(), converterLookup);
    }
    
    public static Converter resolveConverterByClass(String className, Class type, ConverterLookup converterLookup) {
        Class clazz = null;

        try {
            if (className != null) {
                clazz = ClassUtils.forName(className, converterLookup.getSettings().getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            throw new MappingException("Failed to find class [" + className + "]", e);
        }

        Converter converter;
        if (clazz == null) {
            clazz = type;
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
            throw new MappingException("No converter defined for type [" + type.getName() + "]");
        }
        return converter;
    }

    public static Converter resolveConverterByClass(String className, Getter getter, ConverterLookup converterLookup) {
        Class clazz = null;

        try {
            if (className != null) {
                clazz = ClassUtils.forName(className, converterLookup.getSettings().getClassLoader());
            }
        } catch (ClassNotFoundException e) {
            throw new MappingException("Failed to find class [" + className + "]", e);
        }

        if (clazz == null) {
            clazz = AccessorUtils.getCollectionParameter(getter);
        }

        Converter converter;
        if (clazz == null) {
            clazz = getter.getReturnType();
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
            throw new MappingException("No converter defined for type [" + getter.getReturnType().getName() + "]");
        }
        return converter;
    }

    public static void applyResourcePropertySettings(ResourcePropertyMapping mapping, CompassSettings settings) {
        InternalResourcePropertyMapping intMapping = (InternalResourcePropertyMapping) mapping;
        ResourcePropertyConverter converter = mapping.getResourcePropertyConverter();
        if (intMapping.getIndex() == null) {
            if (!isNullConverter(converter)) {
                intMapping.setIndex(converter.suggestIndex());
            }
            if (intMapping.getIndex() == null) {
                if (intMapping.isInternal()) {
                    intMapping.setIndex(Property.Index.fromString(settings.getSetting(CompassEnvironment.Osem.MANAGED_ID_INDEX, "analyzed")));
                } else {
                    intMapping.setIndex(Property.Index.fromString(settings.getSetting(CompassEnvironment.Mapping.GLOBAL_INDEX, Property.Index.ANALYZED.toString())));
                }
            }
        }
        if (intMapping.getStore() == null) {
            if (!isNullConverter(converter)) {
                intMapping.setStore(converter.suggestStore());
            }
            if (intMapping.getStore() == null) {
                intMapping.setStore(Property.Store.fromString(settings.getSetting(CompassEnvironment.Mapping.GLOBAL_STORE, Property.Store.YES.toString())));
            }
        }
        if (intMapping.getTermVector() == null) {
            if (!isNullConverter(converter)) {
                intMapping.setTermVector(converter.suggestTermVector());
            }
            if (intMapping.getTermVector() == null) {
                intMapping.setTermVector(Property.TermVector.fromString(settings.getSetting(CompassEnvironment.Mapping.GLOBAL_TERM_VECTOR, Property.TermVector.NO.toString())));
            }
        }
        if (intMapping.isOmitNorms() == null) {
            if (!isNullConverter(converter)) {
                intMapping.setOmitNorms(converter.suggestOmitNorms());
            }
            if (intMapping.isOmitNorms() == null) {
                intMapping.setOmitNorms(settings.getSettingAsBoolean(CompassEnvironment.Mapping.GLOBAL_OMIT_NORMS, false));
            }
        }
        if (intMapping.isOmitTf() == null) {
            if (!isNullConverter(converter)) {
                intMapping.setOmitTf(converter.suggestOmitTf());
            }
            if (intMapping.isOmitTf() == null) {
                intMapping.setOmitTf(settings.getSettingAsBoolean(CompassEnvironment.Mapping.GLOBAL_OMIT_TF, false));
            }
        }
    }

    public static boolean isNullConverter(Converter converter) {
        if (converter == null) {
            return true;
        }
        if (converter instanceof DelegateConverter) {
            converter = ((DelegateConverter) converter).getDelegatedConverter();
        }
        if (converter == null) {
            return true;
        }
        return false;
    }
}
