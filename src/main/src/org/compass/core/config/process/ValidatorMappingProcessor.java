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
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ExcludeFromAll;
import org.compass.core.mapping.InvalidMappingException;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.RefAliasObjectMapping;

/**
 * @author kimchy
 */
public class ValidatorMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        ResourceMapping[] rootMappings = compassMapping.getRootMappings();
        for (int i = 0; i < rootMappings.length; i++) {
            ResourceMapping resourceMapping = rootMappings[i];
            validateRootMapping(resourceMapping);
        }
        return compassMapping;
    }

    private void validateRootMapping(ResourceMapping resourceMapping) throws MappingException {
        validatieHasAtLeastOneId(resourceMapping);
        validateMulitRefAliasHasPoly(resourceMapping);
        String[] resourcePropertyNames = resourceMapping.getResourcePropertyNames();
        for (int i = 0; i < resourcePropertyNames.length; i++) {
            String propertyName = resourcePropertyNames[i];
            ResourcePropertyMapping[] resourcePropertyMapping = resourceMapping.getResourcePropertyMappings(propertyName);
            validateDuplicateExcludeFromAll(resourceMapping, propertyName, resourcePropertyMapping);
            validateDuplicateAnalyzer(resourceMapping, propertyName, resourcePropertyMapping);
        }
    }

    private void validatieHasAtLeastOneId(ResourceMapping resourceMapping) {
        Mapping[] idMappings = resourceMapping.getIdMappings();
        if (idMappings.length == 0) {
            throw new MappingException("Mapping for alias [" + resourceMapping.getAlias() + "] has no id mappings defined. " +
                    "Either you forgot to add id mappings for it, or it is a component mapping that requires no ids and it " +
                    "is not configured with root=false");
        }
    }
    
    private void validateMulitRefAliasHasPoly(ResourceMapping resourceMapping) {
        if (!(resourceMapping instanceof ClassMapping)) {
            return;
        }
        ClassMapping classMapping = (ClassMapping) resourceMapping;
        for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
            Mapping innerMapping = (Mapping) it.next();
            if (innerMapping instanceof RefAliasObjectMapping) {
                ClassMapping[] refMappings = ((RefAliasObjectMapping) innerMapping).getRefClassMappings();
                if (refMappings.length > 1) {
                    for (int i = 0; i < refMappings.length; i++) {
                        if (!refMappings[i].isPoly()) {
                            throw new MappingException("Mapping for alias [" + classMapping.getAlias() + "] and reference/component mapping [" 
                                    + innerMapping.getName() + "] has more than one ref-alias mappings, but class mapping [" 
                                    + refMappings[i].getAlias() + "] is not defined as poly");
                        }
                    }
                }
            }
        }

    }

    private void validateDuplicateExcludeFromAll(ResourceMapping resourceMapping, String propertyName,
                                                 ResourcePropertyMapping[] resourcePropertyMapping) throws MappingException {
        if (resourcePropertyMapping.length == 1) {
            return;
        }
        ExcludeFromAll excludeFromAll = resourcePropertyMapping[0].getExcludeFromAll();
        for (int i = 1; i < resourcePropertyMapping.length; i++) {
            if (resourcePropertyMapping[i].isInternal()) {
                continue;
            }
            if (excludeFromAll != resourcePropertyMapping[i].getExcludeFromAll()) {
                throw new InvalidMappingException("Resource property / meta-data [" + propertyName + "] of alias ["
                        + resourceMapping.getAlias() + "] has different exclude from all settings");
            }
        }
    }

    private void validateDuplicateAnalyzer(ResourceMapping resourceMapping, String propertyName,
                                           ResourcePropertyMapping[] resourcePropertyMapping) throws MappingException {
        if (resourcePropertyMapping.length == 1) {
            return;
        }
        boolean first = true;
        String lastAnalyzer = null;
        for (int i = 0; i < resourcePropertyMapping.length; i++) {
            ResourcePropertyMapping propertyMapping = resourcePropertyMapping[i];
            // don't worry about internal properties, since they have nothing to
            // do with the analyzer
            if (propertyMapping.isInternal()) {
                continue;
            }
            if (propertyMapping.getAnalyzer() != null) {
                if (lastAnalyzer == null && !first) {
                    // we passed several mappings with no analyzers, and now we
                    // found one that has
                    throw new InvalidMappingException("Resource property / meta-data [" + propertyName + "] of alias ["
                            + resourceMapping.getAlias()
                            + "] has an anlyzer set, and some do not. Please set for all of them the same analyzer.");
                }
                if (first) {
                    lastAnalyzer = propertyMapping.getAnalyzer();
                } else {
                    if (!propertyMapping.getAnalyzer().equals(lastAnalyzer)) {
                        throw new InvalidMappingException("Resource property / meta-data [" + propertyName
                                + "] of alias [" + resourceMapping.getAlias()
                                + "] has several anlyzers set. Please set for all of them the same analyzer.");
                    }
                }
            } else {
                if (lastAnalyzer != null) {
                    throw new InvalidMappingException("Resource property / meta-data [" + propertyName + "] of alias ["
                            + resourceMapping.getAlias()
                            + "] has several anlyzers set. Please set for all of them the same analyzer.");
                }
            }
            if (first) {
                first = false;
            }
        }
    }

}
