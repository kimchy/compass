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

package org.compass.core.converter.mapping.osem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.Resource;
import org.compass.core.accessor.AccessorUtils;
import org.compass.core.accessor.Getter;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceCollection;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceEntry;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceList;
import org.compass.core.converter.mapping.osem.collection.LazyReferenceSet;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.mapping.osem.CollectionMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.util.proxy.extractor.ProxyExtractorHelper;

/**
 * @author kimchy
 */
public class CollectionMappingConverter extends AbstractCollectionMappingConverter implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(CollectionMappingConverter.class);

    private ProxyExtractorHelper proxyExtractorHelper;

    public void configure(CompassSettings settings) throws CompassException {
        proxyExtractorHelper = new ProxyExtractorHelper();
        proxyExtractorHelper.configure(settings);
    }

    protected int marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
                                      MarshallingContext context) {

        root = proxyExtractorHelper.initializeProxy(root);
        
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        int count = 0;
        Mapping elementMapping = colMapping.getElementMapping();
        Collection col = (Collection) root;
        for (Iterator it = col.iterator(); it.hasNext();) {
            Object value = it.next();
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, current);
            boolean stored = elementMapping.getConverter().marshall(resource, value, elementMapping, context);
            if (stored) {
                count++;
            }
        }
        return count;
    }

    protected AbstractCollectionMapping.CollectionType getRuntimeCollectionType(Object root) {
        if (root instanceof List) {
            return AbstractCollectionMapping.CollectionType.LIST;
        } else if (root instanceof LinkedHashSet) {
            return AbstractCollectionMapping.CollectionType.LINKED_HASH_SET;
        } else if (root instanceof EnumSet) {
            return AbstractCollectionMapping.CollectionType.ENUM_SET;
        } else if (root instanceof SortedSet) {
            return AbstractCollectionMapping.CollectionType.SORTED_SET;
        } else if (root instanceof Set) {
            return AbstractCollectionMapping.CollectionType.SET;
        } else {
            throw new IllegalStateException("Compass does not support collection class [" + root.getClass().getName()
                    + "], please consider using either List or Set implementations");
        }
    }

    protected Object createColObject(Getter getter, AbstractCollectionMapping.CollectionType collectionType, int size,
                                     AbstractCollectionMapping mapping, MarshallingContext context) {
        if (((CollectionMapping) mapping).isLazy()) {
            if (collectionType == AbstractCollectionMapping.CollectionType.LIST) {
                return new LazyReferenceList(context.getSession(), size);
            } else if (collectionType == AbstractCollectionMapping.CollectionType.SET ||
                    collectionType == AbstractCollectionMapping.CollectionType.SORTED_SET ||
                    collectionType == AbstractCollectionMapping.CollectionType.LINKED_HASH_SET) {
                return new LazyReferenceSet(context.getSession(), size, collectionType);
            } else {
                throw new IllegalStateException("Lazy not supported for this type of collection [" + collectionType + "]");
            }
        } else {
            if (collectionType == AbstractCollectionMapping.CollectionType.LIST) {
                return new ArrayList(size);
            } else if (collectionType == AbstractCollectionMapping.CollectionType.ENUM_SET) {
                return EnumSet.noneOf(AccessorUtils.getCollectionParameter(getter));
            } else if (collectionType == AbstractCollectionMapping.CollectionType.SET) {
                return new HashSet(size);
            } else if (collectionType == AbstractCollectionMapping.CollectionType.SORTED_SET) {
                return new TreeSet();
            } else if (collectionType == AbstractCollectionMapping.CollectionType.LINKED_HASH_SET) {
                return new LinkedHashSet(size);
            } else {
                throw new IllegalStateException("Should not happen, internal compass error");
            }
        }
    }

    protected void addValue(Object col, int index, Object value, AbstractCollectionMapping mapping, MarshallingContext context) {
        if (((CollectionMapping) mapping).isLazy()) {
            ((LazyReferenceCollection) col).addLazyEntry((LazyReferenceEntry) value);
        } else {
            ((Collection) col).add(value);
        }
    }
}
