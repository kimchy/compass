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

package org.compass.core.config.process;

import java.lang.reflect.Modifier;
import java.util.Iterator;

import org.compass.core.accessor.PropertyAccessor;
import org.compass.core.accessor.PropertyAccessorFactory;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.AliasMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MappingException;
import org.compass.core.mapping.MultipleMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.mapping.osem.internal.InternalObjectMapping;
import org.compass.core.util.reflection.ReflectionFactory;

/**
 * <p>Goes through each {@link org.compass.core.mapping.osem.ClassMapping} and resolves
 * its reflection settings (constructor, getter, setter, and object class).
 *
 * <p>Since this processor happens before the flattening of the mappings, it is not
 * required to follow the ref aliases for component and reference mappings.
 *
 * @author kimchy
 */
public class PropertyAccessorMappingProcessor implements MappingProcessor {

    private PropertyAccessorFactory propertyAccessorFactory;

    public CompassMapping process(CompassMapping compassMapping, PropertyNamingStrategy namingStrategy,
                                  ConverterLookup converterLookup, CompassSettings settings) throws MappingException {

        // initalize the property accessor registry
        propertyAccessorFactory = new PropertyAccessorFactory();
        propertyAccessorFactory.configure(settings);

        for (AliasMapping aliasMapping : compassMapping.getMappings()) {
            if (aliasMapping instanceof ClassMapping) {
                ClassMapping classMapping = (ClassMapping) aliasMapping;

                // resolve the class mapping constructor
                classMapping.setConstructor(ReflectionFactory.getDefaultConstructor(settings, classMapping.getClazz()));
                // if it is not abstract and not an interface, it must have a default constructor
                if (!Modifier.isAbstract(classMapping.getClazz().getModifiers()) && !Modifier.isInterface(classMapping.getClazz().getModifiers()) && !classMapping.getClazz().isEnum()) {
                    if (classMapping.getConstructor() == null) {
                        throw new MappingException("No default constructor defined for class [" + classMapping.getName() + "]");
                    }

                }
                if (classMapping.getPolyClass() != null) {
                    classMapping.setPolyConstructor(ReflectionFactory.getDefaultConstructor(settings, classMapping.getPolyClass()));
                }
                Class clazz = classMapping.getClazz();
                if (classMapping.isPoly() && classMapping.getPolyClass() != null) {
                    clazz = classMapping.getPolyClass();
                }

                for (Iterator it = classMapping.mappingsIt(); it.hasNext();) {
                    processMapping((Mapping) it.next(), clazz, settings);
                }
            }
        }
        return compassMapping;
    }

    private void processMapping(Mapping mapping, Class clazz, CompassSettings settings)
            throws MappingException {
        if (!(mapping instanceof ObjectMapping)) {
            return;
        }
        ObjectMapping objectMapping = (ObjectMapping) mapping;
        PropertyAccessor pAccessor = propertyAccessorFactory.getPropertyAccessor(objectMapping.getAccessor(), settings);
        ((InternalObjectMapping) objectMapping).setGetter(pAccessor.getGetter(clazz, objectMapping.getPropertyName()));
        ((InternalObjectMapping) objectMapping).setSetter(pAccessor.getSetter(clazz, objectMapping.getPropertyName()));

        if (mapping instanceof MultipleMapping) {
            MultipleMapping multipleMapping = (MultipleMapping) mapping;
            for (Iterator it = multipleMapping.mappingsIt(); it.hasNext();) {
                processMapping((Mapping) it.next(), clazz, settings);
            }
        }
    }
}
