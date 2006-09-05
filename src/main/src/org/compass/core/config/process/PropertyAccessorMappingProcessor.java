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

package org.compass.core.config.process;

import java.util.Iterator;

import org.compass.core.accessor.PropertyAccessor;
import org.compass.core.accessor.PropertyAccessorFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.MultipleMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class PropertyAccessorMappingProcessor implements MappingProcessor {

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        // initalize the property accessor registry
        PropertyAccessorFactory propertyAccessorFactory = new PropertyAccessorFactory();
        propertyAccessorFactory.configure(settings);

        for (Iterator rIt = compassMapping.mappingsIt(); rIt.hasNext();) {
            Mapping mapping = (Mapping) rIt.next();
            if (mapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) mapping;

                // resolve the class mapping constructor
                classMapping.setConstructor(ClassUtils.getDefaultConstructor(classMapping.getClazz()));
                if (classMapping.getPolyClass() != null) {
                    classMapping.setPolyConstructor(ClassUtils.getDefaultConstructor(classMapping.getPolyClass()));
                }

                for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
                    processMapping((Mapping) it.next(), classMapping.getClazz(), propertyAccessorFactory);
                }
            }
        }
        return compassMapping;
    }

    private void processMapping(Mapping mapping, Object fatherObject, PropertyAccessorFactory propertyAccessorFactory)
            throws MappingException {
        if (!(mapping instanceof ObjectMapping)) {
            return;
        }
        ObjectMapping objectMapping = (ObjectMapping) mapping;
        Class clazz = objectMapping.getObjClass();
        if (clazz == null) {
            if (fatherObject instanceof ObjectMapping) {
                clazz = ((ObjectMapping) fatherObject).getObjClass();
            } else {
                clazz = (Class) fatherObject;
            }
            objectMapping.setObjClass(clazz);
        }
        PropertyAccessor pAccessor = propertyAccessorFactory.getPropertyAccessor(objectMapping.getAccessor());
        objectMapping.setGetter(pAccessor.getGetter(clazz, objectMapping.getPropertyName()));
        objectMapping.setSetter(pAccessor.getSetter(clazz, objectMapping.getPropertyName()));

        if (mapping instanceof MultipleMapping) {
            MultipleMapping multipleMapping = (MultipleMapping) mapping;
            for (Iterator it = multipleMapping.mappingsIt(); it.hasNext();) {
                processMapping((Mapping) it.next(), objectMapping, propertyAccessorFactory);
            }
        }
    }
}
