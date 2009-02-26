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

import java.util.Iterator;

import org.compass.core.accessor.AccessorUtils;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DelegateConverter;
import org.compass.core.converter.basic.FormatConverter;
import org.compass.core.converter.dynamic.DynamicConverter;
import org.compass.core.converter.json.ContextResourcePropertyJsonValueConverter;
import org.compass.core.converter.json.ResourcePropertyJsonValueConverter;
import org.compass.core.converter.json.SimpleJsonValueConverter;
import org.compass.core.converter.mapping.ContextResourcePropertyConverter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.xsem.ResourcePropertyValueConverter;
import org.compass.core.converter.xsem.SimpleXmlValueConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.JsonMapping;
import org.compass.core.mapping.json.JsonMappingIterator;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ComponentMapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.mapping.osem.DynamicMetaDataMapping;
import org.compass.core.mapping.osem.OsemMappingIterator;
import org.compass.core.mapping.osem.ParentMapping;
import org.compass.core.mapping.osem.PlainCascadeMapping;
import org.compass.core.mapping.osem.ReferenceMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;

/**
 * @author kimchy
 */
public class ConverterLookupMappingProcessor implements MappingProcessor {

    private ConverterLookup converterLookup;

    private CompassSettings settings;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.converterLookup = converterLookup;
        this.settings = settings;

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof RawResourceMapping) {
                lookupConverter((RawResourceMapping) aliasMapping);
            } else if (aliasMapping instanceof XmlObjectMapping) {
                lookupConverter((XmlObjectMapping) aliasMapping);
            } else if (aliasMapping instanceof ClassMapping) {
                lookupConverter((ClassMapping) aliasMapping);
            } else if (aliasMapping instanceof RootJsonObjectMapping) {
                lookupConverter((RootJsonObjectMapping) aliasMapping);
            }
        }

        return compassMapping;
    }

    private void lookupConverter(RawResourceMapping resourceMapping) throws MappingException {
        MappingProcessorUtils.lookupConverter(converterLookup, resourceMapping, true);
        for (Iterator it = resourceMapping.mappingsIt(); it.hasNext();) {
            MappingProcessorUtils.lookupConverter(converterLookup, (Mapping) it.next(), false);
        }
    }

    private void lookupConverter(XmlObjectMapping xmlObjectMapping) throws MappingException {
        MappingProcessorUtils.lookupConverter(converterLookup, xmlObjectMapping);
        for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            MappingProcessorUtils.lookupConverter(converterLookup, mapping, true);

            if (mapping instanceof XmlPropertyMapping) {
                XmlPropertyMapping xmlPropertyMapping = (XmlPropertyMapping) mapping;
                Converter converter;
                if (xmlPropertyMapping.getValueConverterName() != null) {
                    String converterName = xmlPropertyMapping.getValueConverterName();
                    converter = converterLookup.lookupConverter(converterName);
                    if (xmlPropertyMapping.getValueConverter() instanceof DelegateConverter) {
                        ((DelegateConverter) xmlPropertyMapping.getValueConverter()).setDelegatedConverter(converter);
                        converter = xmlPropertyMapping.getValueConverter();
                    }
                    if (converter instanceof ResourcePropertyConverter) {
                        converter = new ResourcePropertyValueConverter((ResourcePropertyConverter) converter);
                    }
                    if (converter == null) {
                        throw new ConfigurationException("Failed to find converter [" + converterName
                                + "] for mapping " + "[" + xmlPropertyMapping.getName() + "]");
                    }
                } else {
                    // this should probably be handled in the actual converteres
                    converter = new SimpleXmlValueConverter();
                }
                xmlPropertyMapping.setValueConverter(converter);
            }
        }
    }

    private void lookupConverter(ClassMapping classMapping) throws MappingException {
        MappingProcessorUtils.lookupConverter(converterLookup, classMapping);

        // initalize the managed id converter
        for (Iterator<Mapping> it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping m = it.next();
            if (m instanceof ClassPropertyMapping) {
                ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) m;
                if (classPropertyMapping.getManagedIdConverterName() == null) {
                    if (classPropertyMapping.mappingsSize() == 1) {
                        Mapping m2 = classPropertyMapping.mappingsIt().next();
                        if (m2 instanceof ClassPropertyMetaDataMapping) {
                            classPropertyMapping.setManagedIdConverterName(m2.getConverterName());
                        }
                    }
                }
                if (classPropertyMapping.getManagedIdConverter() == null) {
                    if (classPropertyMapping.mappingsSize() == 1) {
                        Mapping m2 = classPropertyMapping.mappingsIt().next();
                        if (m2 instanceof ClassPropertyMetaDataMapping) {
                            classPropertyMapping.setManagedIdConverter(m2.getConverter());
                        }
                    }
                }
            }
        }

        OsemMappingIterator.iterateMappings(new OsemConverterLookup(), classMapping, false);
    }

    private void lookupConverter(RootJsonObjectMapping jsonMapping) throws MappingException {
        MappingProcessorUtils.lookupConverter(converterLookup, jsonMapping);
        JsonMappingIterator.iterateMappings(new JsonConverterLookup(), jsonMapping, true);
    }

    private class JsonConverterLookup implements JsonMappingIterator.JsonMappingCallback {

        public void onJsonRootObject(RootJsonObjectMapping jsonObjectMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, jsonObjectMapping);
        }

        public void onJsonObject(PlainJsonObjectMapping jsonObjectMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, jsonObjectMapping);
        }

        public void onJsonContent(JsonContentMapping jsonContentMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, jsonContentMapping);
        }

        public void onJsonProperty(JsonPropertyMapping mapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, mapping);
            Converter converter;
            if (mapping.getValueConverterName() != null) {
                String converterName = mapping.getValueConverterName();
                converter = converterLookup.lookupConverter(converterName);
                if (mapping.getValueConverter() instanceof DelegateConverter) {
                    ((DelegateConverter) mapping.getValueConverter()).setDelegatedConverter(converter);
                    converter = mapping.getValueConverter();
                }
                if (converter instanceof ContextResourcePropertyConverter) {
                    converter = new ContextResourcePropertyJsonValueConverter((ContextResourcePropertyConverter) converter);
                } else if (converter instanceof ResourcePropertyConverter) {
                    converter = new ResourcePropertyJsonValueConverter((ResourcePropertyConverter) converter);
                }
                if (converter == null) {
                    throw new ConfigurationException("Failed to find converter [" + converterName
                            + "] for mapping " + "[" + mapping.getName() + "]");
                }
            } else {
                // this should probably be handled in the actual converteres
                converter = new SimpleJsonValueConverter();
            }
            mapping.setValueConverter(converter);
        }

        public void onJsonArray(JsonArrayMapping jsonArrayMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, jsonArrayMapping);
        }

        public boolean onBeginMultipleMapping(JsonMapping mapping) {
            return true;
        }

        public void onEndMultipleMapping(JsonMapping mapping) {
        }
    }

    private class OsemConverterLookup implements OsemMappingIterator.ClassMappingCallback {

        private ClassPropertyMapping classPropertyMapping;

        public boolean onBeginClassMapping(ClassMapping classMapping) {
            return true;
        }

        public void onEndClassMapping(ClassMapping classMapping) {
        }

        public boolean onBeginMultipleMapping(ClassMapping classMapping, Mapping mapping) {
            return true;
        }

        public void onEndMultiplMapping(ClassMapping classMapping, Mapping mapping) {
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, collectionMapping);
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassMapping classMapping, ClassPropertyMapping classPropertyMapping) {
            this.classPropertyMapping = classPropertyMapping;
            MappingProcessorUtils.lookupConverter(converterLookup, classPropertyMapping);
        }

        public void onClassDynamicPropertyMapping(ClassMapping classMapping, ClassDynamicPropertyMapping dynamicPropertyMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, dynamicPropertyMapping);
            if (dynamicPropertyMapping.getNameConverter() == null || dynamicPropertyMapping.getNameConverter() instanceof DelegateConverter) {
                ResourcePropertyConverter converter = null;
                if (dynamicPropertyMapping.getObjectType() == ClassDynamicPropertyMapping.ObjectType.MAP) {
                    Class keyType;
                    if (dynamicPropertyMapping.getNameGetter() != null) {
                        keyType = dynamicPropertyMapping.getNameGetter().getReturnType();
                    } else {
                        keyType = AccessorUtils.getMapKeyParameter(dynamicPropertyMapping.getGetter());
                    }
                    if (dynamicPropertyMapping.getNameConverterName() != null || keyType != null) {
                        converter = (ResourcePropertyConverter) MappingProcessorUtils.resolveConverter(dynamicPropertyMapping.getNameConverterName(), null, keyType, converterLookup);
                    }
                } else {
                    converter = (ResourcePropertyConverter) MappingProcessorUtils.resolveConverter(dynamicPropertyMapping.getNameConverterName(), null, dynamicPropertyMapping.getNameGetter(), converterLookup);
                }

                if (dynamicPropertyMapping.getNameConverter() instanceof DelegateConverter) {
                    if (converter != null) {
                        ((DelegateConverter) dynamicPropertyMapping.getNameConverter()).setDelegatedConverter(converter);
                    }
                } else {
                    dynamicPropertyMapping.setNameConverter(converter);
                }
            }

            if (dynamicPropertyMapping.getValueConverter() == null || dynamicPropertyMapping.getValueConverter() instanceof DelegateConverter) {
                ResourcePropertyConverter converter = null;
                if (dynamicPropertyMapping.getObjectType() == ClassDynamicPropertyMapping.ObjectType.MAP) {
                    Class valueType;
                    if (dynamicPropertyMapping.getValueGetter() != null) {
                        valueType = dynamicPropertyMapping.getValueGetter().getReturnType();
                    } else {
                        valueType = AccessorUtils.getMapValueParameter(dynamicPropertyMapping.getGetter());
                    }
                    if (dynamicPropertyMapping.getValueConverterName() != null || valueType != null) {
                        converter = (ResourcePropertyConverter) MappingProcessorUtils.resolveConverter(dynamicPropertyMapping.getValueConverterName(), null, valueType, converterLookup);
                    }
                } else {
                    converter = (ResourcePropertyConverter) MappingProcessorUtils.resolveConverter(dynamicPropertyMapping.getValueConverterName(), null, dynamicPropertyMapping.getValueGetter(), converterLookup);
                }

                if (dynamicPropertyMapping.getValueConverter() instanceof DelegateConverter) {
                    if (converter != null) {
                        ((DelegateConverter) dynamicPropertyMapping.getValueConverter()).setDelegatedConverter(converter);
                    }
                } else {
                    dynamicPropertyMapping.setValueConverter(converter);
                }
            }

            if (dynamicPropertyMapping.getValueConverter() != null) {
                dynamicPropertyMapping.getResourcePropertyMapping().setConverter(dynamicPropertyMapping.getValueConverter());
            }

            MappingProcessorUtils.applyResourcePropertySettings(dynamicPropertyMapping.getResourcePropertyMapping(), settings);
        }

        public void onComponentMapping(ClassMapping classMapping, ComponentMapping componentMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, componentMapping);
        }

        public void onReferenceMapping(ClassMapping classMapping, ReferenceMapping referenceMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, referenceMapping);
        }

        public void onParentMapping(ClassMapping classMapping, ParentMapping parentMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, parentMapping);
        }

        public void onCascadeMapping(ClassMapping classMapping, PlainCascadeMapping cascadeMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, cascadeMapping);
        }

        public void onConstantMetaDataMappaing(ClassMapping classMapping, ConstantMetaDataMapping constantMetaDataMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, constantMetaDataMapping);
        }

        public void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, classPropertyMetaDataMapping, classPropertyMapping);
        }

        public void onDynamicMetaDataMapping(ClassMapping classMapping, DynamicMetaDataMapping dynamicMetaDataMapping) {
            Converter converter = converterLookup.lookupConverter(dynamicMetaDataMapping.getConverterName());
            if (!(converter instanceof DynamicConverter)) {
                throw new MappingException("Dynamic meta-data [" + dynamicMetaDataMapping + "] converter name [" +
                        dynamicMetaDataMapping.getConverterName() + "] is not a dynamic converter");
            }
            DynamicConverter dynamicConverter = ((DynamicConverter) converter).copy();
            dynamicConverter.setType(dynamicMetaDataMapping.getType());
            dynamicConverter.setExpression(dynamicMetaDataMapping.getExpression());

            if (dynamicMetaDataMapping.getFormat() != null) {
                converter = converterLookup.lookupConverter(dynamicMetaDataMapping.getType());
                if (!(converter instanceof FormatConverter)) {
                    throw new MappingException("Dynamic meta data [" + dynamicMetaDataMapping.getName() +
                            "] type [" + dynamicMetaDataMapping.getType().getName() + "] is not a formattable type");
                }
                FormatConverter formatConverter = ((FormatConverter) converter).copy();
                formatConverter.setFormat(dynamicMetaDataMapping.getFormat());
                dynamicConverter.setFormatConverter(formatConverter);
            }

            dynamicMetaDataMapping.setConverter(dynamicConverter);
        }

        /**
         * Called for any resource property mapping type (for example, {@link ClassPropertyMetaDataMapping}
         * and {@link ConstantMetaDataMapping}.
         *
         * @param resourcePropertyMapping The mapping of the callback
         */
        public void onResourcePropertyMapping(ResourcePropertyMapping resourcePropertyMapping) {
            // we do it here as well as later in the processing since with OSEM, the index, for example,
            // is important to be resolved as fast as possible
            MappingProcessorUtils.applyResourcePropertySettings(resourcePropertyMapping, settings);
        }
    }
}
