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
import org.compass.core.marshall.MarshallingContext;

/**
 * 
 * @author kimchy
 */
public abstract class AbstractCollectionMappingConverter implements Converter {

    public static final String COLLECTION_RESOURCE_WRAPPER_KEY = "$crwk";

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        if (root == null) {
            return false;
        }
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        if (colMapping.getCollectionType() == AbstractCollectionMapping.CollectionType.UNKNOWN) {
            Property p = searchEngine.createProperty(colMapping.getCollectionTypePath(),
                    AbstractCollectionMapping.CollectionType.toString(getRuntimeCollectionType(root)),
                    Property.Store.YES, Property.Index.UN_TOKENIZED);
            resource.addProperty(p);
        }

        // for null values in enteties within the collection, they must be saved
        // so the order will be maintained
        context.setHandleNulls(colMapping.getPath());

        int size = marshallIterateData(root, colMapping, resource, context);

        context.removeHandleNulls(colMapping.getPath());
        
        if (size > 0) {
            Property p = searchEngine.createProperty(colMapping.getColSizePath(), Integer.toString(size),
                    Property.Store.YES, Property.Index.UN_TOKENIZED);
            resource.addProperty(p);
            return true;
        }
        return false;
    }

    protected abstract AbstractCollectionMapping.CollectionType getRuntimeCollectionType(Object root);

    /**
     * Marhall the data, returning the number of elements that were actually stored in the index
     * (and can later be read).
     * 
     * @param root
     * @param colMapping
     * @param resource
     * @param context
     * @return
     */
    protected abstract int marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
            MarshallingContext context);

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;

        Property pColSize = resource.getProperty(colMapping.getColSizePath());
        if (pColSize == null) {
            // when we marshalled it, it was null
            return null;
        }
        String sColSize = pColSize.getStringValue();

        AbstractCollectionMapping.CollectionType collectionType = colMapping.getCollectionType();
        if (colMapping.getCollectionType() == AbstractCollectionMapping.CollectionType.UNKNOWN) {
            // try and read the collection from the index
            Property pColllectionType = resource.getProperty(colMapping.getCollectionTypePath());
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
            context.setAttribute(COLLECTION_RESOURCE_WRAPPER_KEY, null);
        }

        context.removeHandleNulls(colMapping.getPath());
        
        return col;
    }
    
    protected abstract Object createColObject(Getter getter, AbstractCollectionMapping.CollectionType collectionType, int size);
    
    protected abstract void addValue(Object col, int index, Object value);
}
