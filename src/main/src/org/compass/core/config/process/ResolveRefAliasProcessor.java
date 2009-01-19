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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.compass.core.accessor.AccessorUtils;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.internal.InternalRefAliasObjectMapping;

/**
 * @author kimchy
 */
public class ResolveRefAliasProcessor implements MappingProcessor {

    private CompassMapping compassMapping;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {
        this.compassMapping = compassMapping;
        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) aliasMapping;
                for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
                    Mapping innerMapping = (Mapping) it.next();
                    if (innerMapping instanceof InternalRefAliasObjectMapping) {
                        processMapping(classMapping, (InternalRefAliasObjectMapping) innerMapping);
                    }
                }
            }
        }
        return compassMapping;
    }

    void processMapping(ClassMapping classMapping, InternalRefAliasObjectMapping mapping) throws MappingException {
        // if there are no ref aliases, try to infer them from the class
        if (mapping.getRefAliases() == null) {
            Class clazz = mapping.getRefClass();
            if (clazz == null) {
                if (mapping.getGetter().getReturnType().isArray()) {
                    clazz = mapping.getGetter().getReturnType().getComponentType();
                } else {
                    clazz = AccessorUtils.getCollectionParameter(mapping.getGetter());
                    if (clazz == null) {
                        clazz = mapping.getGetter().getReturnType();
                    }
                }
            }
            if (clazz == null) {
                throw new MappingException("This should not happen");
            }
            if (compassMapping.hasMultipleRootClassMapping(clazz.getName())) {
                throw new MappingException("Tried to resolve ref-alias for property [" + mapping.getName() + "] in alias [" +
                        classMapping.getAlias() + "], but there are multiple class mappings for [" + clazz.getName()
                        + "]. Please set the ref-alias explicitly.");
            }
            HashSet<String> aliases = new HashSet<String>();
            for (AliasMapping aliasMapping : compassMapping.getMappings()) {
                if (aliasMapping instanceof ClassMapping) {
                    ClassMapping iterateClassMapping = (ClassMapping) aliasMapping;
                    if (clazz.isAssignableFrom(iterateClassMapping.getClazz())) {
                        aliases.add(iterateClassMapping.getAlias());
                    }
                }
            }
            if (aliases.size() == 0) {
                if (Collection.class.isAssignableFrom(mapping.getGetter().getReturnType())) {
                    throw new MappingException("Failed to resolve ref-alias for collection property [" + mapping.getName() + "] in alias [" +
                            classMapping.getAlias() + "]. You must set the ref-alias for it, or use Java 5 generics for the collection type." +
                            " Have you added the class mapping to Compass?");
                } else if (mapping.getGetter().getReturnType().isArray()) {
                    throw new MappingException("Failed to resolve ref-alias for array property [" + mapping.getName() + "] in alias [" +
                            classMapping.getAlias() + "]. You must set the ref-alias for it." +
                            " Have you added the class mapping to Compass?");
                } else {
                    throw new MappingException("Tried to resolve ref-alias for property [" + mapping.getName() + "] in alias [" +
                            classMapping.getAlias() + "], but no class mapping was found for [" + clazz.getName() + "]");
                }
            }
            mapping.setRefAliases(aliases.toArray(new String[aliases.size()]));
        }
        // go over the ref alias mappings and resolve them. Also add extending
        // ref aliases
        String[] aliases = mapping.getRefAliases();
        LinkedHashSet<String> aliasesSet = new LinkedHashSet<String>();
        ArrayList<ClassMapping> refMappings = new ArrayList<ClassMapping>();
        for (String alias : aliases) {
            AliasMapping refAliasMapping = compassMapping.getAliasMapping(alias);
            if (refAliasMapping == null) {
                throw new MappingException("Failed to resolve ref-alias [" + alias + "] for ["
                        + mapping.getName() + "] in alias [" + classMapping.getAlias() + "]");
            }
            if (aliasesSet.contains(alias)) {
                continue;
            }
            // it might be a contract mapping, so we will not add it here
            // but add all the class mappings that extend it
            if (refAliasMapping instanceof ClassMapping) {
                aliasesSet.add(alias);
                refMappings.add((ClassMapping) refAliasMapping);
            }
            // now add all the ones that extend the ref mapping
            String[] extendingAliases = refAliasMapping.getExtendingAliases();
            if (extendingAliases != null) {
                for (String extendingAlias : extendingAliases) {
                    AliasMapping aliasMapping = compassMapping.getAliasMapping(extendingAlias);
                    if (aliasesSet.contains(aliasMapping.getAlias())) {
                        continue;
                    }
                    // check that it is ClassMapping (it might be ContractMapping for example)
                    if (aliasMapping instanceof ClassMapping) {
                        aliasesSet.add(aliasMapping.getAlias());
                        refMappings.add((ClassMapping) aliasMapping);
                    }
                }
            }
        }
        mapping.setRefAliases(aliasesSet.toArray(new String[aliasesSet.size()]));
        mapping.setRefClassMappings(refMappings.toArray(new ClassMapping[refMappings.size()]));
        // if we have more than one ref class mapping, all of them *must* be poly, change it to be one
        if (mapping.getRefClassMappings().length > 1) {
            for (ClassMapping refClassMapping : mapping.getRefClassMappings()) {
                refClassMapping.setPoly(true);
            }
        }
    }
}
