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
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.CollectionResourceWrapper;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.util.ClassUtils;

/**
 * 
 * @author kimchy
 */
public abstract class AbstractCollectionMappingConverter implements Converter {

    public void marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        if (root == null) {
            return;
        }
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        if (colMapping.getColClass() == null) {
            Property p = searchEngine.createProperty(colMapping.getColClassPath(), getColClass(root),
                    Property.Store.YES, Property.Index.UN_TOKENIZED);
            resource.addProperty(p);
        }

        // for null values in enteties within the collection, they must be saved
        // so the order will be maintained
        context.setHandleNulls(colMapping.getPath());

        int actualSize = getActualSize(root, colMapping, resource, context);
        Property p = searchEngine.createProperty(colMapping.getColSizePath(), Integer.toString(actualSize),
                Property.Store.YES, Property.Index.UN_TOKENIZED);
        resource.addProperty(p);

        marshallIterateData(root, colMapping, resource, context);

        context.removeHandleNulls(colMapping.getPath());
    }

    protected abstract String getColClass(Object root);

    protected abstract void marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
            MarshallingContext context);

    protected abstract int getActualSize(Object root, AbstractCollectionMapping colMapping, Resource resource,
            MarshallingContext context);

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        AbstractCollectionMapping colMapping = (AbstractCollectionMapping) mapping;

        Property pColSize = resource.getProperty(colMapping.getColSizePath());
        if (pColSize == null) {
            // when we marshalled it, it was null
            return null;
        }
        String sColSize = pColSize.getStringValue();

        Class colClass = colMapping.getColClass();
        if (colClass == null) {
            Property pColClassName = resource.getProperty(colMapping.getColClassPath());
            if (pColClassName == null) {
                throw new ConversionException("Expected to find the collection/array class name store in the resource"
                        + " since no col-class mapping parameter defined");
            }
            try {
                colClass = ClassUtils.forName(pColClassName.getStringValue());
            } catch (ClassNotFoundException e) {
                throw new ConversionException("Failed to create collection / array class [" + pColClassName.getStringValue()
                        + "]");
            }
        }

        int size = Integer.valueOf(sColSize).intValue();
        
        Object col = createColObject(colClass, size);
        
        // for null values in enteties within the collection, they must be saved
        // so the order will be maintained
        context.setHandleNulls(colMapping.getPath());

        CollectionResourceWrapper crw = new CollectionResourceWrapper(resource);
        Mapping elementMapping = colMapping.getElementMapping();
        for (int i = 0; i < size; i++) {
            Object value = elementMapping.getConverter().unmarshall(crw, elementMapping,
                    context);
            if (value != null) {
                addValue(col, i, value);
            }
        }

        context.removeHandleNulls(colMapping.getPath());
        
        return col;
    }
    
    protected abstract Object createColObject(Class colClass, int size);
    
    protected abstract void addValue(Object col, int index, Object value);
}
