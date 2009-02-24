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
import java.util.Collection;
import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassDynamicPropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class ClassDynamicPropertyMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        ClassDynamicPropertyMapping dynamicPropertyMapping = (ClassDynamicPropertyMapping) mapping;
        if (root == null) {
            return false;
        }
        Object nameObj = dynamicPropertyMapping.getNameGetter().get(root);
        if (nameObj == null) {
            return false;
        }
        String name = dynamicPropertyMapping.getNameConverter().toString(nameObj, null);
        if (dynamicPropertyMapping.getNamePrefix() != null) {
            name = dynamicPropertyMapping.getNamePrefix() + name;
        }
        Object valueObj = dynamicPropertyMapping.getValueGetter().get(root);
        // don't save a null value if the context does not states so
        if (valueObj == null && !handleNulls(dynamicPropertyMapping.getResourcePropertyMapping(), context)) {
            return false;
        }
        if (dynamicPropertyMapping.getValueType() == ClassDynamicPropertyMapping.ValueType.ARRAY) {
            int size = Array.getLength(valueObj);
            for (int i = 0; i < size; i++) {
                Object valueItem = Array.get(valueObj, i);
                if (valueItem == null && !handleNulls(dynamicPropertyMapping.getResourcePropertyMapping(), context)) {
                    continue;
                }
                addProperty(resource, dynamicPropertyMapping, context, name, valueItem);
            }
        } else if (dynamicPropertyMapping.getValueType() == ClassDynamicPropertyMapping.ValueType.COLLECTION) {
            Collection valueCol = (Collection) valueObj;
            for (Iterator it = valueCol.iterator(); it.hasNext();) {
                Object valueItem = it.next();
                if (valueItem == null && !handleNulls(dynamicPropertyMapping.getResourcePropertyMapping(), context)) {
                    continue;
                }
                addProperty(resource, dynamicPropertyMapping, context, name, valueItem);
            }
        } else {
            String value = dynamicPropertyMapping.getValueConverter().toString(valueObj, dynamicPropertyMapping.getResourcePropertyMapping());
            Property property = context.getResourceFactory().createProperty(name, value, dynamicPropertyMapping.getResourcePropertyMapping());
            resource.addProperty(property);
        }

        return dynamicPropertyMapping.getResourcePropertyMapping().getStore() != Property.Store.NO;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        // does not support unmarshalling, simply return null
        return null;
    }

    protected void addProperty(Resource resource, ClassDynamicPropertyMapping mapping, MarshallingContext context, String name, Object valueObj) {
        String value = mapping.getValueConverter().toString(valueObj, mapping.getResourcePropertyMapping());
        Property property = context.getResourceFactory().createProperty(name, value, mapping.getResourcePropertyMapping());
        resource.addProperty(property);
    }

    /**
     * <p>Should the converter handle nulls? Handling nulls means should the
     * converter process nulls or not. Usually the converter will not
     * persist null values, but sometimes it might be needed
     * ({@link org.compass.core.marshall.MarshallingContext#handleNulls()}).
     *
     * <p>If a specific null value is configured with the {@link org.compass.core.mapping.ResourcePropertyMapping}
     * then the converter will always handle nulls and write it.
     *
     * @param context The marshalling context
     * @return <code>true</code> if the converter should handle null values
     */
    protected boolean handleNulls(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return resourcePropertyMapping.hasNullValue() || context.handleNulls();
    }

    /**
     * If the converter handle nulls, the value that will be stored in the
     * search engine for <code>null</code> values (during the marshall process). Uses
     * {@link org.compass.core.mapping.ResourcePropertyMapping#getNullValue()}.
     *
     * @param resourcePropertyMapping The resource proeprty mapping to get the null value from
     * @param context                 The marshalling context
     * @return Null value that will be inserted for <code>null</code>s.
     */
    protected String getNullValue(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return resourcePropertyMapping.getNullValue();
    }
}
