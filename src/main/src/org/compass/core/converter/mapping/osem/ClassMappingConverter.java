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

import java.lang.reflect.Array;
import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.accessor.Getter;
import org.compass.core.accessor.Setter;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.utils.ResourceHelper;
import org.compass.core.impl.ResourceIdKey;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class ClassMappingConverter implements ResourceMappingConverter {

    public static final String ROOT_CLASS_MAPPING_KEY = "$rcmk";

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();
        ClassMapping classMapping = (ClassMapping) mapping;
        // Note that even if a component is root, it will not be root when
        // treated as a component (the binding part of the configuration takes
        // care and "unroots" it)
        if (classMapping.isRoot()) {
            resource.setAlias(classMapping.getAlias());
            doSetBoost(resource, root, classMapping, context);
            context.setAttribute(ROOT_CLASS_MAPPING_KEY, classMapping);
        }

        // only add specilized properties for un-marshalling when it is supported
        if (classMapping.isSupportUnmarshall()) {
            if (classMapping.isPoly() && classMapping.getPolyClass() == null) {
                // if the class is defined as poly, persist the class name as well
                String className = root.getClass().getName();
                Property p = searchEngine.createProperty(classMapping.getClassPath().getPath(), className, Property.Store.YES,
                        Property.Index.UN_TOKENIZED);
                resource.addProperty(p);
            }
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
            store |= m.getConverter().marshall(resource, value, m, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ClassMapping classMapping = (ClassMapping) mapping;
        ResourceIdKey resourceIdKey = null;
        // handle a cache of all the unmarshalled objects already, used for
        // cyclic references
        if (classMapping.isRoot()) {
            if (!classMapping.isSupportUnmarshall()) {
                throw new ConversionException("Class Mapping [" + classMapping.getAlias() + "] is configured not to support un-marshalling");
            }
            Property[] ids = ResourceHelper.toIds(resource, context.getCompassMapping());
            resourceIdKey = new ResourceIdKey(classMapping.getAlias(), ids);
            Object cached = context.getUnmarshalled(resourceIdKey);
            if (cached != null) {
                return cached;
            }
        }
        String className = classMapping.getName();
        if (classMapping.isPoly()) {
            if (classMapping.getPolyClass() != null) {
                className = classMapping.getPolyClass().getName();
            } else {
                Property pClassName = resource.getProperty(classMapping.getClassPath().getPath());
                if (pClassName == null) {
                    throw new ConversionException("The class [" + className
                            + "] is configured as poly, but no class information is stored in the resource");
                }
                className = pClassName.getStringValue();
                if (className == null) {
                    throw new ConversionException("The class [" + className
                            + "] is configured as poly, but no class information is stored in the resource");
                }
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
                context.setUnmarshalled(resourceIdKey, obj);
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
            throw new ConversionException("Failed to create class [" + className + "] for unmarshalling", e);
        }
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        ClassMapping classMapping = (ClassMapping) resourceMapping;
        boolean stored = false;
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (int i = 0; i < ids.length; i++) {
                ClassPropertyMetaDataMapping classPropertyMetaDataMapping = (ClassPropertyMetaDataMapping) ids[i];
                stored |= convertId(idResource, classPropertyMetaDataMapping.getGetter().get(id), classPropertyMetaDataMapping, context);
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load class with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                stored |= convertId(idResource, Array.get(id, i), (ClassPropertyMetaDataMapping) ids[i], context);
            }
        } else if (ids.length == 1) {
            stored = convertId(idResource, id, (ClassPropertyMetaDataMapping) ids[0], context);
        } else {
            String type = id.getClass().getName();
            throw new ConversionException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }
        return stored;
    }

    private boolean convertId(Resource resource, Object root, ClassPropertyMetaDataMapping mdMapping, MarshallingContext context) {
        if (root == null) {
            throw new ConversionException("Trying to marshall a null id [" + mdMapping.getName() + "]");
        }
        return mdMapping.getConverter().marshall(resource, root, mdMapping, context);
    }

    public Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        ClassMapping classMapping = (ClassMapping) resourceMapping;
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        Object[] idsValues = new Object[ids.length];
        if (id instanceof Resource) {
            Resource resource = (Resource) id;
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = ids[i].getConverter().unmarshall(resource, ids[i], context);
                if (idsValues[i] == null) {
                    // the reference was not marshalled
                    return null;
                }
            }
        } else if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (int i = 0; i < ids.length; i++) {
                ClassPropertyMetaDataMapping classPropertyMetaDataMapping = (ClassPropertyMetaDataMapping) ids[i];
                idsValues[i] = classPropertyMetaDataMapping.getGetter().get(id);
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load class with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = Array.get(id, i);
            }
        } else if (ids.length == 1) {
            idsValues[0] = id;
        } else {
            String type = id.getClass().getName();
            throw new ConversionException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }
        return idsValues;
    }

    /**
     * A simple extension point that allows to set the boost value for the created {@link Resource}.
     * <p/>
     * The default implemenation uses the statically defined boost value in the mapping definition
     * ({@link org.compass.core.mapping.osem.ClassMapping#getBoost()}) to set the boost level
     * using {@link Resource#setBoost(float)}
     * <p/>
     * Note, that this method will only be called on a root level (root=true) mapping.
     *
     * @param resource     The resource to set the boost on
     * @param root         The Object that is marshalled into the respective Resource
     * @param classMapping The Class Mapping deifnition
     * @throws ConversionException
     */
    protected void doSetBoost(Resource resource, Object root, ClassMapping classMapping,
                              MarshallingContext context) throws ConversionException {
        resource.setBoost(classMapping.getBoost());
    }

}
