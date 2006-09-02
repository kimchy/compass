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

import java.util.Iterator;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.basic.FormatConverter;
import org.compass.core.converter.dynamic.DynamicConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.*;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;

/**
 * @author kimchy
 */
public class ConverterLookupMappingProcessor implements MappingProcessor {

    private ConverterLookup converterLookup;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.converterLookup = converterLookup;

        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof RawResourceMapping) {
                lookupConverter((RawResourceMapping) m);
            } else if (m instanceof XmlObjectMapping) {
                lookupConverter((XmlObjectMapping) m);
            } else if (m instanceof ClassMapping) {
                lookupConverter((ClassMapping) m);
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
        }
    }

    private void lookupConverter(ClassMapping classMapping) throws MappingException {
        MappingProcessorUtils.lookupConverter(converterLookup, classMapping);
        OsemMappingUtils.iterateMappings(new OsemConverterLookup(), classMapping.mappingsIt(), false);
    }

    private class OsemConverterLookup implements OsemMappingUtils.ClassMappingCallback {

        private ClassPropertyMapping classPropertyMapping;

        public void onBeginMultipleMapping(Mapping mapping) {
        }

        public void onEndMultiplMapping(Mapping mapping) {
        }

        public void onBeginCollectionMapping(AbstractCollectionMapping collectionMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, collectionMapping);
        }

        public void onEndCollectionMapping(AbstractCollectionMapping collectionMapping) {
        }

        public void onClassPropertyMapping(ClassPropertyMapping classPropertyMapping) {
            this.classPropertyMapping = classPropertyMapping;
            MappingProcessorUtils.lookupConverter(converterLookup, classPropertyMapping);
        }

        public void onComponentMapping(ComponentMapping componentMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, componentMapping);
        }

        public void onReferenceMapping(ReferenceMapping referenceMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, referenceMapping);
        }

        public void onParentMapping(ParentMapping parentMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, parentMapping);
        }

        public void onConstantMetaDataMappaing(ConstantMetaDataMapping constantMetaDataMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, constantMetaDataMapping);
        }


        public void onClassPropertyMetaDataMapping(ClassPropertyMetaDataMapping classPropertyMetaDataMapping) {
            MappingProcessorUtils.lookupConverter(converterLookup, classPropertyMetaDataMapping, classPropertyMapping);
        }

        public void onDynamicMetaDataMapping(DynamicMetaDataMapping dynamicMetaDataMapping) {
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
        }
    }
}
