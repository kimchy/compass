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

package org.compass.core.converter.mapping.osem;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.accessor.Getter;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.CollectionResourceWrapper;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public abstract class AbstractCollectionMappingConverter implements Converter {

    public static final String COLLECTION_RESOURCE_WRAPPER_KEY = "$crwk";

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;
        ClassMapping rootClassMapping = (ClassMapping) context.getAttribute(ClassMappingConverter.ROOT_CLASS_MAPPING_KEY);


        // if we have a null value, we check if we need to handle null values
        // if so, we will note the fact that it is null under the size attribute
        if (root == null) {
            if (context.handleNulls() && rootClassMapping.isSupportUnmarshall()) {
                Property p = searchEngine.createProperty(colMapping.getColSizePath().getPath(), searchEngine.getNullValue(),
                        Property.Store.YES, Property.Index.UN_TOKENIZED);
                resource.addProperty(p);
                return true;
            } else {
                return false;
            }
        }

        if (rootClassMapping.isSupportUnmarshall()) {
            if (colMapping.getCollectionType() == AbstractCollectionMapping.CollectionType.UNKNOWN) {
                Property p = searchEngine.createProperty(colMapping.getCollectionTypePath().getPath(),
                        AbstractCollectionMapping.CollectionType.toString(getRuntimeCollectionType(root)),
                        Property.Store.YES, Property.Index.UN_TOKENIZED);
                resource.addProperty(p);
            }
            // for null values in entities within the collection, they must be saved
            // so the order will be maintained
            context.setHandleNulls(colMapping.getPath());
        }

        int size = marshallIterateData(root, colMapping, resource, context);

        if (rootClassMapping.isSupportUnmarshall()) {
            context.removeHandleNulls(colMapping.getPath());
            if (size > 0) {
                Property p = searchEngine.createProperty(colMapping.getColSizePath().getPath(), Integer.toString(size),
                        Property.Store.YES, Property.Index.UN_TOKENIZED);
                resource.addProperty(p);
            }
        }

        return size > 0;
    }

    protected abstract AbstractCollectionMapping.CollectionType getRuntimeCollectionType(Object root);

    /**
     * Marhall the data, returning the number of elements that were actually stored in the index
     * (and can later be read).
     */
    protected abstract int marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
                                               MarshallingContext context);

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        Property pColSize = resource.getProperty(colMapping.getColSizePath().getPath());
        if (pColSize == null) {
            // when we marshalled it, it was null
            return null;
        }
        String sColSize = pColSize.getStringValue();
        // if we marshalled it and marked it as null, return the null value
        if (searchEngine.isNullValue(sColSize)) {
            return null;
        }

        AbstractCollectionMapping.CollectionType collectionType = colMapping.getCollectionType();
        if (colMapping.getCollectionType() == AbstractCollectionMapping.CollectionType.UNKNOWN) {
            // try and read the collection from the index
            Property pColllectionType = resource.getProperty(colMapping.getCollectionTypePath().getPath());
            if (pColllectionType == null) {
                throw new ConversionException("Expected to find the collection/arraytype stored in the resource");
            }
            collectionType = AbstractCollectionMapping.CollectionType.fromString(pColllectionType.getStringValue());
        }

        int size = Integer.parseInt(sColSize);

        Object col = createColObject(colMapping.getGetter(), collectionType, size);

        // for null values in enteties within the collection, they must be saved
        // so the order will be maintained
        context.setHandleNulls(colMapping.getPath());

        // if we already wrapped the resource with a collection wrapper, use it
        // if not, create a new one. Also, mark if we created it so we can clean up afterwards
        boolean createdCollectionResourceWrapper = false;
        CollectionResourceWrapper crw = (CollectionResourceWrapper) context.getAttribute(COLLECTION_RESOURCE_WRAPPER_KEY);
        if (crw == null) {
            createdCollectionResourceWrapper = true;
            crw = new CollectionResourceWrapper(resource);
            context.setAttribute(COLLECTION_RESOURCE_WRAPPER_KEY, crw);
        }

        Mapping elementMapping = colMapping.getElementMapping();
        for (int i = 0; i < size; i++) {
            Object value = elementMapping.getConverter().unmarshall(crw, elementMapping, context);
            if (value != null) {
                addValue(col, i, value);
            }
        }

        if (createdCollectionResourceWrapper) {
            context.removeAttribute(COLLECTION_RESOURCE_WRAPPER_KEY);
        }

        context.removeHandleNulls(colMapping.getPath());

        return col;
    }

    protected abstract Object createColObject(Getter getter, AbstractCollectionMapping.CollectionType collectionType, int size);

    protected abstract void addValue(Object col, int index, Object value);
}
