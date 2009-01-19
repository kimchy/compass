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

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.internal.InternalCompassMapping;
import org.compass.core.mapping.xsem.XmlIdMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;

/**
 * @author kimchy
 */
public class LateBindingXsemMappingProcessor implements MappingProcessor {

    private PropertyNamingStrategy namingStrategy;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.namingStrategy = namingStrategy;

        ((InternalCompassMapping) compassMapping).setPath(namingStrategy.getRootPath());
        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof XmlObjectMapping) {
                secondPass((XmlObjectMapping) aliasMapping, compassMapping);
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
        }
    }
}
