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

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.CollectionResourceWrapper;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.RefAliasObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingEnvironment;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public abstract class AbstractRefAliasMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        RefAliasObjectMapping refAliasMapping = (RefAliasObjectMapping) mapping;
        ClassMapping[] classMappings = refAliasMapping.getRefClassMappings();
        ClassMapping classMapping;
        if (root == null || classMappings.length == 1) {
            classMapping = classMappings[0];
        } else {
            classMapping = extractClassMapping(context, root.getClass(), resource, refAliasMapping);
        }
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        Object parent = context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        try {
            return doMarshall(resource, root, refAliasMapping, classMapping, context);
        } finally {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, parent);
        }
    }

    protected abstract boolean doMarshall(Resource resource, Object root, RefAliasObjectMapping hasRefAliasMapping,
                                          ClassMapping refMapping, MarshallingContext context) throws ConversionException;

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        RefAliasObjectMapping hasRefAliasMapping = (RefAliasObjectMapping) mapping;
        ClassMapping[] classMappings = hasRefAliasMapping.getRefClassMappings();
        ClassMapping classMapping = null;
        if (classMappings.length == 1) {
            classMapping = classMappings[0];
        } else {
            // this for loop is really not required, since all of them
            // will have the same class path
            for (int i = 0; i < classMappings.length; i++) {
                if (classMappings[i].isPoly()) {
                    String classPath = classMappings[i].getClassPath().getPath();
                    Property pClassName = resource.getProperty(classPath);
                    // HACK HACK HACK
                    // since ClassMappingConverter will also read the path, we need
                    // to take special care when working with a collection resource wrapper
                    // and rollback the fact that we read the path
                    if ((resource instanceof CollectionResourceWrapper) && rollbackClassNameOnPoly()) {
                        ((CollectionResourceWrapper) resource).rollbackGetProperty(classPath);
                    }
                    if (pClassName != null && pClassName.getStringValue() != null) {
                        // we stored the class, use it to find the "nearset" class mappings
                        Class clazz;
                        try {
                            clazz = ClassUtils.forName(pClassName.getStringValue(), context.getSession().getCompass().getSettings().getClassLoader());
                        } catch (ClassNotFoundException e) {
                            throw new ConversionException("Failed to create class [" + pClassName.getStringValue() + "]", e);
                        }
                        classMapping = extractClassMapping(context, clazz, resource, hasRefAliasMapping);
                        break;
                    }
                }
            }
            if (classMapping == null) {
                // we did not find anything stored in the index, it must have poly-class set
                for (int i = 0; i < classMappings.length; i++) {
                    if (classMappings[i].getPolyClass() != null) {
                        classMapping = classMappings[i];
                        break;
                    }
                }
            }
            if (classMapping == null) {
                // just try and use the first one and hope it is what we need
                classMapping = classMappings[0];
            }
        }
        Object current = context.getAttribute(MarshallingEnvironment.ATTRIBUTE_CURRENT);
        Object parent = context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, current);
        try {
            return doUnmarshall(resource, hasRefAliasMapping, classMapping, context);
        } finally {
            context.setAttribute(MarshallingEnvironment.ATTRIBUTE_PARENT, parent);
        }
    }

    /**
     * Extracts the given class mappings based on the provided class. Will find the "nearest"
     * class mapping that match the class, then will check if it was set in the ref-alias,
     * and return the class mapping set against the ref-alias.
     */
    private ClassMapping extractClassMapping(MarshallingContext context, Class clazz, Resource resource,
                                             RefAliasObjectMapping hasRefAliasMapping) throws ConversionException {
        ClassMapping classMapping;
        ClassMapping origClassMapping = (ClassMapping) context.getCompassMapping().getMappingByClass(clazz);
        if (origClassMapping == null) {
            throw new ConversionException("No class mapping found when marshalling root alias ["
                    + resource.getAlias() + "] and class [" + clazz + "]");
        }
        classMapping = hasRefAliasMapping.getRefClassMapping(origClassMapping.getAlias());
        if (classMapping == null) {
            throw new ConversionException("Mapping for root alias [" + resource.getAlias() +
                    "] with one of its mappings with multiple ref-alias ["
                    + StringUtils.arrayToCommaDelimitedString(hasRefAliasMapping.getRefAliases())
                    + "] did not match [" + origClassMapping.getAlias() + "]");
        }
        return classMapping;
    }

    protected abstract Object doUnmarshall(Resource resource, RefAliasObjectMapping hasRefAliasMapping,
                                           ClassMapping refMapping, MarshallingContext context) throws ConversionException;

    protected boolean rollbackClassNameOnPoly() {
        return true;
    }
}
