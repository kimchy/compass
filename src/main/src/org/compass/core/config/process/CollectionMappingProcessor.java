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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.compass.core.config.CompassSettings;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.OverrideByNameMapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.mapping.osem.ArrayMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.CollectionMapping;
import org.compass.core.mapping.osem.LazyMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.mapping.osem.internal.InternalLazyMapping;

/**
 * Goes over all the OSEM {@link org.compass.core.mapping.osem.ClassMapping}s. For each
 * class mappings, goes over all of its mappings and checks if they represent a collection/array.
 * If they represent a collection/array, wraps them with either a {@link org.compass.core.mapping.osem.CollectionMapping}
 * or an {@link org.compass.core.mapping.osem.ArrayMapping}. Copies over the mappings types and set it as the
 * collection/array element mapping.
 *
 * <p>A note on element mapping: Compass simplifies mappings for collection. There is no need for different
 * property/component/reference mappings when handling collections/arrays. So, the actual mappings (property/
 * component/reference) actually refers to the element mapping, which is why the mapping constructed is copied
 * over as the element mapping.
 *
 * @author kimchy
 */
public class CollectionMappingProcessor implements MappingProcessor {

    private ConverterLookup converterLookup;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        this.converterLookup = converterLookup;

        ArrayList colMappingsToAdd = new ArrayList();

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
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
            } else if (LinkedHashSet.class.isAssignableFrom(collectionClass)) {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.LINKED_HASH_SET);
            } else if (EnumSet.class.isAssignableFrom(collectionClass)) {
                collectionMapping.setCollectionType(AbstractCollectionMapping.CollectionType.ENUM_SET);
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
            collectionMapping.setDefinedInAlias(objectMapping.getDefinedInAlias());
            if (objectMapping instanceof OverrideByNameMapping) {
                collectionMapping.setOverrideByName(((OverrideByNameMapping) objectMapping).isOverrideByName());
            } else {
                collectionMapping.setOverrideByName(true);
            }
            if ((collectionMapping instanceof LazyMapping) && (objectMapping instanceof LazyMapping)) {
                ((InternalLazyMapping) collectionMapping).setLazy(((LazyMapping) objectMapping).isLazy());
            }
            return collectionMapping;
        } else {
            return objectMapping;
        }
    }
}
