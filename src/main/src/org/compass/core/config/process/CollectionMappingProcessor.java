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

import java.util.*;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.*;
import org.compass.core.mapping.osem.*;

/**
 * @author kimchy
 */
public class CollectionMappingProcessor implements MappingProcessor {

    private ConverterLookup converterLookup;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        this.converterLookup = converterLookup;

        ArrayList colMappingsToAdd = new ArrayList();

        for (Iterator mappIt = compassMapping.mappingsIt(); mappIt.hasNext();) {
            AliasMapping aliasMapping = (AliasMapping) mappIt.next();
            if (!(aliasMapping instanceof ClassMapping)) {
                continue;
            }
            colMappingsToAdd.clear();

            ClassMapping classMapping = (ClassMapping) aliasMapping;
            for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
                Mapping mapping = (Mapping) it.next();
                if ((mapping instanceof ObjectMapping)) {
                    ObjectMapping objectMapping = (ObjectMapping) mapping;
                    if (objectMapping.canBeCollectionWrapped()) {
                        Mapping maybeColMapping = checkCollection(objectMapping);
                        // check if we wrapped the mapping as a collection, if we did
                        // remove id and re-add it later
                        if (maybeColMapping instanceof AbstractCollectionMapping) {
                            colMappingsToAdd.add(maybeColMapping);
                            it.remove();
                        }
                    }
                }
            }

            for (Iterator it = colMappingsToAdd.iterator(); it.hasNext();) {
                Mapping mapping = (Mapping) it.next();
                classMapping.addMapping(mapping);
            }
        }

        return compassMapping;
    }

    /**
     * An attibute/component/reference might be a collection/array. The method
     * check if they represent a collection, and if they are, configures a
     * collection mapping with them as the collection element mapping.
     */
    private Mapping checkCollection(ObjectMapping objectMapping) throws MappingException {
        AbstractCollectionMapping collectionMapping = null;
        Class collectionClass = objectMapping.getGetter().getReturnType();
        if (Collection.class.isAssignableFrom(collectionClass)) {
            collectionMapping = new CollectionMapping();
            if (List.class.isAssignableFrom(collectionClass)) {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.LIST);
            } else if (SortedSet.class.isAssignableFrom(collectionClass)) {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.SORTED_SET);
            } else if (Set.class.isAssignableFrom(collectionClass)) {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.SET);
            } else {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.UNKNOWN);
            }
        } else if (collectionClass.isArray()) {
            Converter converter = converterLookup.lookupConverter(objectMapping.getGetter().getReturnType());
            if (converter == null) {
                // there is no converter assigned to that array, use the array
                // mapping to convert each element inside, otherwise the
                // converter will be responsible to convert the whole array
                collectionMapping = new ArrayMapping();
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.NOT_REQUIRED);
            }
        }
        if (collectionMapping != null) {
            collectionMapping.setElementMapping(objectMapping);
            // setting the collection (the same as the inner element mapping)
            collectionMapping.setGetter(objectMapping.getGetter());
            collectionMapping.setSetter(objectMapping.getSetter());
            collectionMapping.setName(objectMapping.getName());
            collectionMapping.setPath(objectMapping.getPath());
            collectionMapping.setPropertyName(objectMapping.getPropertyName());
            if (objectMapping instanceof OverrideByNameMapping) {
                collectionMapping.setOverrideByName(((OverrideByNameMapping) objectMapping).isOverrideByName());
            } else {
                collectionMapping.setOverrideByName(true);
            }
            return collectionMapping;
        } else {
            return objectMapping;
        }
    }
}
