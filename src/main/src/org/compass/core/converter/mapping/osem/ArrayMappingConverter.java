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

import java.lang.reflect.Array;

import org.compass.core.Resource;
import org.compass.core.accessor.Getter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.AbstractCollectionMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;

/**
 * @author kimchy
 */
public class ArrayMappingConverter extends AbstractCollectionMappingConverter {

    protected int marshallIterateData(Object root, AbstractCollectionMapping colMapping, Resource resource,
                                      MarshallingContext context) {
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        int count = 0;
        int size = Array.getLength(root);
        Mapping elementMapping = colMapping.getElementMapping();
        for (int i = 0; i < size; i++) {
            Object value = Array.get(root, i);
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, current);
            boolean stored = elementMapping.getConverter().marshall(resource, value, elementMapping, context);
            if (stored) {
                count++;
            }
        }
        return count;
    }

    protected AbstractCollectionMapping.CollectionType getRuntimeCollectionType(Object root) {
        throw new IllegalStateException("Should not be called, internal compass error");
    }

    protected Object createColObject(Getter getter, AbstractCollectionMapping.CollectionType collectionType, int size,
                                     AbstractCollectionMapping mapping, MarshallingContext context) {
        return Array.newInstance(getter.getReturnType().getComponentType(), size);
    }

    protected void addValue(Object col, int index, Object value, AbstractCollectionMapping mapping, MarshallingContext context) {
        Array.set(col, index, value);
    }
}
