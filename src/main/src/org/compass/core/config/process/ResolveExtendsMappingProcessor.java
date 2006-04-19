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

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author kimchy
 */
public class ResolveExtendsMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        ArrayList innerMappingsCopy = new ArrayList();
        for (Iterator it = compassMapping.mappingsIt(); it.hasNext();) {
            AliasMapping origAliasMapping = (AliasMapping) it.next();
            AliasMapping aliasMapping = origAliasMapping.shallowCopy();
            resolveExtends(compassMapping, aliasMapping, origAliasMapping);
            innerMappingsCopy.add(aliasMapping);
        }
        compassMapping.clearMappings();
        for (Iterator it = innerMappingsCopy.iterator(); it.hasNext();) {
            compassMapping.addMapping((AliasMapping) it.next());
        }

        return compassMapping;
    }

    private void resolveExtends(CompassMapping compassMapping, AliasMapping aliasMapping, AliasMapping copyFromAliasMapping)
            throws MappingException {

        if (copyFromAliasMapping.getExtendedMappings() != null) {
            for (int i = 0; i < copyFromAliasMapping.getExtendedMappings().length; i++) {
                String extendedAlias = copyFromAliasMapping.getExtendedMappings()[i];
                AliasMapping extendedAliasMapping = compassMapping.getAliasMapping(extendedAlias);
                if (extendedAliasMapping == null) {
                    throw new MappingException("Failed to find alias [" + extendedAlias + "] in alias ["
                            + aliasMapping.getAlias() + "] extends section");
                }
                resolveExtends(compassMapping, aliasMapping, (AliasMapping) extendedAliasMapping.copy());
            }
        }

        for (Iterator aliasMappingIt = copyFromAliasMapping.mappingsIt(); aliasMappingIt.hasNext();) {
            aliasMapping.addMapping((Mapping) aliasMappingIt.next());
        }
    }
}
