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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.compass.core.Property;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.*;

/**
 * @author kimchy
 */
public class InternalIdsMappingProcessor implements MappingProcessor {

    private CompassSettings settings;

    private ConverterLookup converterLookup;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        this.settings = settings;
        this.converterLookup = converterLookup;

        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (m instanceof ClassMapping) {
                buildClassMetaDataIds((ClassMapping) m);
            }
        }
        return compassMapping;
    }

    /**
     * Go over all the attributes in the class (note that it takes all the
     * component attributes and so on) and does the following:
     * <li>If the attributed is marked with <code>managedId="true"</code>,
     * or it has no meta data associated with it, compass will create a new
     * internal id</li>
     * <li>If the class property is marked with <code>managedId="auto"</code>
     * and there is a meta data in the attribute that is unique, use it as the
     * attribute id, otherwise create a new internal id</li>
     * <li>If the attributed is marked with <code>managedId="false"</code>,
     * the id will be the first meta data</li>
     *
     * @param classMapping
     */
    private void buildClassMetaDataIds(ClassMapping classMapping) {
        OsemMappingUtils.ClassPropertyAndResourcePropertyGatherer callback =
                new OsemMappingUtils.ClassPropertyAndResourcePropertyGatherer();
        OsemMappingUtils.iterateMappings(callback, classMapping.mappingsIt());

        HashMap propertyMappingsMap = new HashMap();
        List pMappings = callback.getResourcePropertyMappings();
        for (Iterator it = pMappings.iterator(); it.hasNext();) {
            ResourcePropertyMapping pMapping = (ResourcePropertyMapping) it.next();
            Integer count = (Integer) propertyMappingsMap.get(pMapping.getName());
            if (count == null) {
                count = new Integer(1);
            } else {
                count = new Integer(count.intValue() + 1);
            }
            propertyMappingsMap.put(pMapping.getName(), count);
        }

        List classPropertyMappings = callback.getClassPropertyMappings();
        for (Iterator it = classPropertyMappings.iterator(); it.hasNext();) {
            ClassPropertyMapping classPropertyMapping = (ClassPropertyMapping) it.next();
            boolean mustBeUnTokenized = false;
            if (classPropertyMapping instanceof ClassIdPropertyMapping) {
                mustBeUnTokenized = true;
            }
            if (classPropertyMapping.isIdPropertySet()) {
                // the id has been set already (for example - in case of
                // reference)
                continue;
            } else if (classPropertyMapping.getManagedId() == ClassPropertyMapping.ManagedId.TRUE
                    || classPropertyMapping.mappingsSize() == 0) {
                MappingProcessorUtils.addInternalId(settings, converterLookup, classPropertyMapping);
            } else if (classPropertyMapping.getManagedId() == ClassPropertyMapping.ManagedId.AUTO) {
                boolean foundPropertyId = false;
                for (int i = 0; i < classPropertyMapping.mappingsSize(); i++) {
                    ClassPropertyMetaDataMapping pMapping = (ClassPropertyMetaDataMapping) classPropertyMapping.getMapping(i);
                    if (!pMapping.canActAsPRopertyId()) {
                        continue;
                    }
                    // if there is only one mapping, and it is stored, use it as
                    // the id
                    if (((Integer) propertyMappingsMap.get(pMapping.getName())).intValue() == 1
                            && pMapping.getStore() == Property.Store.YES) {
                        if (mustBeUnTokenized && pMapping.getIndex() != Property.Index.UN_TOKENIZED) {
                            continue;
                        }
                        classPropertyMapping.setIdPropertyIndex(i);
                        foundPropertyId = true;
                        break;
                    }
                }

                if (!foundPropertyId) {
                    MappingProcessorUtils.addInternalId(settings, converterLookup, classPropertyMapping);
                }
            } else { // ManagedId.FALSE
                // mark the first one as the id, the user decides
                classPropertyMapping.setIdPropertyIndex(0);
            }
        }
    }

}
