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

import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.impl.ResourceIdKey;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.marshall.MarshallingException;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.ResourceHelper;

/**
 * @author kimchy
 */
public class ClassMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();
        ClassMapping classMapping = (ClassMapping) mapping;
        // Note that even if a component is root, it will not be root when
        // treated as a component (the binding part of the configuration takes
        // care and "unroots" it)
        if (classMapping.isRoot()) {
            resource.setAlias(classMapping.getAlias());
            resource.setBoost(classMapping.getBoost());
        }
        if (classMapping.isPoly()) {
            // if the class is defined as poly, persist the class name as well
            String className = root.getClass().getName();
            Property p = searchEngine.createProperty(classMapping.getClassPath(), className, Property.Store.YES,
                    Property.Index.UN_TOKENIZED);
            resource.addProperty(p);
        }

        boolean store = false;
        for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, root);
            Mapping m = (Mapping) mappingsIt.next();
            Object value;
            if (m instanceof ObjectMapping) {
                Getter getter = ((ObjectMapping) m).getGetter();
                value = getter.get(root);
            } else {
                value = root;
            }
            store |=  m.getConverter().marshall(resource, value, m, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ClassMapping classMapping = (ClassMapping) mapping;
        ResourceIdKey resourceIdKey = null;
        // handle a cache of all the unmarshalled objects already, used for
        // cyclic references
        if (classMapping.isRoot()) {
            Property[] ids = ResourceHelper.toIds(classMapping.getAlias(), resource, context.getCompassMapping());
            resourceIdKey = new ResourceIdKey(classMapping.getAlias(), ids);
            Object cached = context.getSession().getFirstLevelCache().get(resourceIdKey);
            if (cached != null) {
                return cached;
            }
        }
        String className = classMapping.getName();
        if (classMapping.isPoly()) {
            Property pClassName = resource.getProperty(classMapping.getClassPath());
            if (pClassName == null) {
                throw new MarshallingException("The class [" + className
                        + "] is configured as poly, but no class information is stored in the resource");
            }
            className = pClassName.getStringValue();
            if (className == null) {
                throw new MarshallingException("The class [" + className
                        + "] is configured as poly, but no class information is stored in the resource");
            }
        }
        try {
            Class clazz = ClassUtils.forName(className);
            Object obj = ClassUtils.getDefaultConstructor(clazz).newInstance(null);

            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, obj);
            // we will set here the object, even though no ids have been set,
            // since the ids are the first mappings that will be unmarshalled,
            // and it's all we need to handle cyclic refernces in case of
            // references
            if (classMapping.isRoot()) {
                context.getSession().getFirstLevelCache().setUnmarshalled(resourceIdKey, obj);
            }

            boolean isNullClass = true;
            for (Iterator mappingsIt = classMapping.mappingsIt(); mappingsIt.hasNext();) {
                context.setAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT, obj);
                Mapping m = (Mapping) mappingsIt.next();
                if (m instanceof ObjectMapping) {
                    Setter setter = ((ObjectMapping) m).getSetter();
                    if (setter == null) {
                        continue;
                    }
                    Object value = m.getConverter().unmarshall(resource, m, context);
                    if (value == null) {
                        continue;
                    }
                    setter.set(obj, value);
                    if (m.controlsObjectNullability()) {
                        isNullClass = false;
                    }
                } else {
                    m.getConverter().unmarshall(resource, m, context);
                }
            }
            if (isNullClass) {
                return null;
            }
            return obj;
        } catch (Exception e) {
            throw new MarshallingException("Failed to create class [" + className + "] for unmarshalling", e);
        }
    }
}
