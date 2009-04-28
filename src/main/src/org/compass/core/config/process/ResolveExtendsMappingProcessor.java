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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.internal.InternalAliasMapping;
import org.compass.core.mapping.internal.InternalCompassMapping;

/**
 * Goes over all the {@link org.compass.core.mapping.AliasMapping}s in Compass and resolves
 * which one the mapping extends ({@link org.compass.core.mapping.AliasMapping#getExtendedAliases()}
 * and which one are extending this alias ({@link org.compass.core.mapping.AliasMapping#getExtendingAliases()}.
 *
 * @author kimchy
 */
public class ResolveExtendsMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        ArrayList<AliasMapping> innerMappingsCopy = new ArrayList<AliasMapping>();
        for (AliasMapping origAliasMapping : compassMapping.getMappings()) {
            InternalAliasMapping aliasMapping = (InternalAliasMapping) origAliasMapping.shallowCopy();

            Set<String> recursiveExtendedAliases = new HashSet<String>();
            resolveExtends(compassMapping, aliasMapping, origAliasMapping, recursiveExtendedAliases);
            aliasMapping.setRecursiveExtendedAliases(recursiveExtendedAliases.toArray(new String[recursiveExtendedAliases.size()]));

            innerMappingsCopy.add(aliasMapping);
        }
        ((InternalCompassMapping) compassMapping).clearMappings();
        for (AliasMapping anInnerMappingsCopy : innerMappingsCopy) {
            ((InternalCompassMapping) compassMapping).addMapping(anInnerMappingsCopy);
        }

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            resolveExtending(compassMapping, aliasMapping, new HashSet<String>());
        }

        return compassMapping;
    }

    /**
     * Resolves (recursivly) and sets the extending mapping section of {@link org.compass.core.mapping.AliasMapping}.
     */
    private void resolveExtending(CompassMapping compassMapping, AliasMapping aliasMapping, HashSet<String> extendingAliases) {

        if (aliasMapping.getExtendedAliases() != null) {
            for (int i = 0; i < aliasMapping.getExtendedAliases().length; i++) {
                String extendedAlias = aliasMapping.getExtendedAliases()[i];
                InternalAliasMapping extendedAliasMapping = (InternalAliasMapping) compassMapping.getAliasMapping(extendedAlias);

                if (extendedAliasMapping.getExtendingAliases() != null) {
                    for (int j = 0; j < extendedAliasMapping.getExtendingAliases().length; j++) {
                        extendingAliases.add(extendedAliasMapping.getExtendingAliases()[j]);
                    }
                }
                extendingAliases.add(aliasMapping.getAlias());
                extendedAliasMapping.setExtendingAliases(extendingAliases.toArray(new String[extendingAliases.size()]));

                resolveExtending(compassMapping, extendedAliasMapping, extendingAliases);
            }
        }
    }

    /**
     * Resolves (recursivly) all the extended aliases and addes their mappings (copy) into the alias mapping.
     */
    private void resolveExtends(CompassMapping compassMapping, InternalAliasMapping aliasMapping, AliasMapping copyFromAliasMapping, Set<String> recuresiveExtendedAliases)
            throws MappingException {

        Collections.addAll(recuresiveExtendedAliases, copyFromAliasMapping.getExtendedAliases());
        
        if (copyFromAliasMapping.getExtendedAliases() != null) {
            for (int i = 0; i < copyFromAliasMapping.getExtendedAliases().length; i++) {
                String extendedAlias = copyFromAliasMapping.getExtendedAliases()[i];

                AliasMapping extendedAliasMapping = compassMapping.getAliasMapping(extendedAlias);
                if (extendedAliasMapping == null) {
                    throw new MappingException("Failed to find alias [" + extendedAlias + "] in alias ["
                            + aliasMapping.getAlias() + "] extends section");
                }

                // recursivly call in order to resolve extends. Note, we copy the extended alias mapping
                // since we do not share mappings
                resolveExtends(compassMapping, aliasMapping, (AliasMapping) extendedAliasMapping.copy(), recuresiveExtendedAliases);
            }
        }

        for (Iterator aliasMappingIt = copyFromAliasMapping.mappingsIt(); aliasMappingIt.hasNext();) {
            aliasMapping.addMapping((Mapping) aliasMappingIt.next());
        }
    }
}
