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

import java.util.Collection;
import java.util.Iterator;

import org.compass.core.Resource;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingException;

/**
 * @author kimchy
 */
public class CollectionMappingConverter extends AbstractCollectionMappingConverter {

    protected String getColClass(Object root) {
        return root.getClass().getName();
    }

    protected void marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
            MarshallingContext context) {
        Mapping elementMapping = colMapping.getElementMapping();
        Collection col = (Collection) root;
        for (Iterator it = col.iterator(); it.hasNext();) {
            Object value = it.next();
            elementMapping.getConverter().marshall(resource, value, elementMapping, context);
        }
    }

    protected int getActualSize(Object root, AbstractCollectionMapping colMapping, Resource resource, MarshallingContext context) {
        Collection col = (Collection) root;
        int actualSize = 0;
        for (Iterator it = col.iterator(); it.hasNext();) {
            Object value = it.next();
            if (value != null) {
                actualSize++;
            }
        }
        return actualSize;
    }

    protected Object createColObject(Class colClass, int size) {
        try {
            return colClass.newInstance();
        } catch (Exception e) {
            throw new MarshallingException("Failed to create class [" + colClass.getName() + "] for unmarshalling", e);
        }
    }

    protected void addValue(Object col, int index, Object value) {
        ((Collection) col).add(value);
    }
}
