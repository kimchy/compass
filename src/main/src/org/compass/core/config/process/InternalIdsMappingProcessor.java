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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.compass.core.Property;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.mapping.SuggestManagedIdConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassIdPropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ManagedId;
import org.compass.core.mapping.osem.OsemMappingIterator;

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

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) aliasMapping;
                if (classMapping.isSupportUnmarshall()) {
                    buildClassMetaDataIds(classMapping);
                } else {
                    buildInternalIdForIdProperties(classMapping);
                }
            }
        }
        return compassMapping;
    }

    /**
     * Build internal ids only for the class property id mappings when we
     * do not support un-marshalling.
     */
    private void buildInternalIdForIdProperties(ClassMapping classMapping) {
        List<ClassPropertyMapping> idMappings = classMapping.findClassPropertiesRequireProcessing();
        for (ClassPropertyMapping idMapping : idMappings) {
            MappingProcessorUtils.addInternalId(settings, converterLookup, idMapping, true);
        }
    }

    /**
     * <p>Go over all the attributes in the class (note that it takes all the
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
     * <p>When ref alias has more than one alias, duplicate mappings might exists.
     * Duplicate mappings are mappings that are shared by several mappings.
     * {@link org.compass.core.mapping.osem.OsemMappingIterator.ClassPropertyAndResourcePropertyGatherer}
     * ignores this duplicates, and only process the first one. Later, in the post process
     * stage ({@link org.compass.core.config.process.PostProcessorMappingProcessor}, the ones
     * that got skipped will be replced with the ones that were (they are the same).
     *
     * @param classMapping
     */
    private void buildClassMetaDataIds(ClassMapping classMapping) {
        OsemMappingIterator.ClassPropertyAndResourcePropertyGatherer callback =
                new OsemMappingIterator.ClassPropertyAndResourcePropertyGatherer();
        OsemMappingIterator.iterateMappings(callback, classMapping);

        HashMap<String, Integer> propertyMappingsMap = new HashMap<String, Integer>();
        List<ResourcePropertyMapping> pMappings = callback.getResourcePropertyMappings();
        for (Iterator<ResourcePropertyMapping> it = pMappings.iterator(); it.hasNext();) {
            ResourcePropertyMapping pMapping = it.next();
            // no need to count the ones we don't store since they won't
            // be reflected when we unmarshall the data
            if (pMapping.getStore() == Property.Store.NO) {
                continue;
            }
            Integer count = propertyMappingsMap.get(pMapping.getName());
            if (count == null) {
                count = 1;
            } else {
                count = count + 1;
            }
            propertyMappingsMap.put(pMapping.getName(), count);
        }

        List<ClassPropertyMapping> classPropertyMappings = callback.getClassPropertyMappings();
        for (ClassPropertyMapping classPropertyMapping : classPropertyMappings) {
            // first, set the managed id if not set usign default (up to class mapping, and if
            // not set, up to Compass settings, and if not there, default to auto).
            if (classPropertyMapping.getManagedId() == null) {
                // check if a converter has a ManagedId suggestion
                ManagedId suggestedManagedIdByConverter = null;
                // first check on the property converter
                if (classPropertyMapping.getConverter() instanceof SuggestManagedIdConverter) {
                    suggestedManagedIdByConverter = ((SuggestManagedIdConverter) classPropertyMapping.getConverter()).suggestManagedId();
                }
                // check if one of the meta data converter suggests a managed id
                if (suggestedManagedIdByConverter == null) {
                    for (Iterator<Mapping> it = classPropertyMapping.mappingsIt(); it.hasNext();) {
                        Mapping m = it.next();
                        if (m.getConverter() instanceof SuggestManagedIdConverter) {
                            suggestedManagedIdByConverter = ((SuggestManagedIdConverter) m.getConverter()).suggestManagedId();
                            if (suggestedManagedIdByConverter != null) {
                                break;
                            }
                        }
                    }
                }
                if (suggestedManagedIdByConverter != null) {
                    classPropertyMapping.setManagedId(suggestedManagedIdByConverter);
                } else {
                    if (classMapping.getManagedId() == null) {
                        String globalManagedId = settings.getSetting(CompassEnvironment.Osem.MANAGED_ID_DEFAULT, ManagedId.NO_STORE.toString());
                        classPropertyMapping.setManagedId(ManagedId.fromString(globalManagedId));
                    } else {
                        classPropertyMapping.setManagedId(classMapping.getManagedId());
                    }
                }
            }

            boolean mustBeUnTokenized = false;
            if (classPropertyMapping instanceof ClassIdPropertyMapping) {
                mustBeUnTokenized = true;
            }
            if (classPropertyMapping.isIdPropertySet()) {
                // the id has been set already (for example - in case of reference)
                continue;
            } else if (classPropertyMapping.getManagedId() == ManagedId.TRUE
                    || classPropertyMapping.mappingsSize() == 0) {
                MappingProcessorUtils.addInternalId(settings, converterLookup, classPropertyMapping, mustBeUnTokenized);
            } else if (classPropertyMapping.getManagedId() == ManagedId.AUTO) {
                autoAddIfRequiredInternalId(propertyMappingsMap, classPropertyMapping, mustBeUnTokenized);
            } else if (classPropertyMapping.getManagedId() == ManagedId.NO_STORE) {
                boolean allMetaDataHasStoreNo = true;
                for (int i = 0; i < classPropertyMapping.mappingsSize(); i++) {
                    ClassPropertyMetaDataMapping pMapping = (ClassPropertyMetaDataMapping) classPropertyMapping.getMapping(i);
                    if (!pMapping.canActAsPropertyId()) {
                        allMetaDataHasStoreNo = false;
                        break;
                    }
                    if (pMapping.getStore() != Property.Store.NO) {
                        allMetaDataHasStoreNo = false;
                        break;
                    }
                }
                if (!allMetaDataHasStoreNo) {
                    autoAddIfRequiredInternalId(propertyMappingsMap, classPropertyMapping, mustBeUnTokenized);
                } // else, don't set the id property, and don't unmarshall it

            } else if (classPropertyMapping.getManagedId() == ManagedId.NO) {
                // do nothing, don't set the managed id, won't be unmarshallled
            } else { // ManagedId.FALSE
                // mark the first one as the id, the user decides
                classPropertyMapping.setIdPropertyIndex(0);
            }
        }
    }

    private void autoAddIfRequiredInternalId(HashMap<String, Integer> propertyMappingsMap, ClassPropertyMapping classPropertyMapping, boolean mustBeNotAnalyzed) {
        boolean foundPropertyId = false;
        for (int i = 0; i < classPropertyMapping.mappingsSize(); i++) {
            ClassPropertyMetaDataMapping pMapping = (ClassPropertyMetaDataMapping) classPropertyMapping.getMapping(i);
            if (!pMapping.canActAsPropertyId()) {
                continue;
            }
            if (!propertyMappingsMap.containsKey(pMapping.getName())) {
                // there is none defined, this means that they got filtered out since they are not
                // stored. continue in case we manage to find a candidate for internal id, if not
                // we need to add an internal one in such cases
                continue;
            }
            // if there is only one mapping, and it is stored, use it as the id
            if (propertyMappingsMap.get(pMapping.getName()) == 1
                    && (pMapping.getStore() == Property.Store.YES || pMapping.getStore() == Property.Store.COMPRESS)) {
                if (mustBeNotAnalyzed && pMapping.getIndex() != Property.Index.UN_TOKENIZED && pMapping.getIndex() != Property.Index.NOT_ANALYZED) {
                    continue;
                }
                classPropertyMapping.setIdPropertyIndex(i);
                foundPropertyId = true;
                break;
            }
        }

        if (!foundPropertyId) {
            MappingProcessorUtils.addInternalId(settings, converterLookup, classPropertyMapping, mustBeNotAnalyzed);
        }
    }

}
