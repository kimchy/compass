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
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.DelegateConverter;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.converter.xsem.ResourcePropertyValueConverter;
import org.compass.core.converter.xsem.SimpleXmlValueConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.xsem.XmlIdMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;

/**
 * @author kimchy
 */
public class LateBindingXsemMappingProcessor implements MappingProcessor {

    private PropertyNamingStrategy namingStrategy;

    private ConverterLookup converterLookup;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.namingStrategy = namingStrategy;
        this.converterLookup = converterLookup;

        compassMapping.setPath(namingStrategy.getRootPath());
        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof XmlObjectMapping) {
                secondPass((XmlObjectMapping) m, compassMapping);
            }
        }

        return compassMapping;
    }

    private void secondPass(XmlObjectMapping xmlObjectMapping, CompassMapping fatherMapping) {
        xmlObjectMapping.setPath(namingStrategy.buildPath(fatherMapping.getPath(), xmlObjectMapping.getAlias()));
        for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
            Mapping mapping = (Mapping) it.next();
            if (mapping instanceof XmlIdMapping) {
                XmlIdMapping xmlIdMapping = (XmlIdMapping) mapping;
                // in case of xml id mapping, we always use it as internal id
                // and build its own internal path (because other xml properties names might be dynamic)
                xmlIdMapping.setInternal(true);
                xmlIdMapping.setPath(namingStrategy.buildPath(xmlObjectMapping.getPath(), xmlIdMapping.getName()));
            }
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
                    if (xmlPropertyMapping.getValueConverter() == null) {
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
}
