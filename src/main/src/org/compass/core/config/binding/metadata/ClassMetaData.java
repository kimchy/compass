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

package org.compass.core.config.binding.metadata;

import java.util.Map;
import java.util.Set;

/**
 * A class meta data interface allowing to access the actual class metadata.
 *
 * @author kimchy
 */
public interface ClassMetaData {

    /**
     * Return the name of the underlying class.
     */
    String getClassName();

    /**
     * Return whether the underlying class represents an interface.
     */
    boolean isInterface();

    /**
     * Return whether the underlying class is marked as abstract.
     */
    boolean isAbstract();

    /**
     * Return whether the underlying class represents a concrete class,
     * i.e. neither an interface nor an abstract class.
     */
    boolean isConcrete();

    /**
     * Determine whether the underlying class is independent,
     * i.e. whether it is a top-level class or a nested class
     * (static inner class) that can be constructed independent
     * from an enclosing class.
     */
    boolean isIndependent();

    /**
     * Return whether the underlying class has an enclosing class
     * (i.e. the underlying class is an inner/nested class or
     * a local class within a method).
     * <p>If this method returns <code>false</code>, then the
     * underlying class is a top-level class.
     */
    boolean hasEnclosingClass();

    /**
     * Return the name of the enclosing class of the underlying class,
     * or <code>null</code> if the underlying class is a top-level class.
     */
    String getEnclosingClassName();

    /**
     * Return whether the underlying class has a super class.
     */
    boolean hasSuperClass();

    /**
     * Return the name of the super class of the underlying class,
     * or <code>null</code> if there is no super class defined.
     */
    String getSuperClassName();

    /**
     * Return the name of all interfaces that the underlying class
     * implements, or an empty array if there are none.
     */
    String[] getInterfaceNames();

    /**
     * Return the names of all annotation types defined on the underlying class.
     * @return the annotation type names
     */
    Set<String> getAnnotationTypes();

    /**
     * Determine whether the underlying class has an annotation of the given
     * type defined.
     * @param annotationType the annotation type to look for
     * @return whether a matching annotation is defined
     */
    boolean hasAnnotation(String annotationType);

    /**
     * Return the names of all meta-annotation types defined on the
     * given annotation type of the underlying class.
     * @return the meta-annotation type names
     */
    Set<String> getMetaAnnotationTypes(String annotationType);

    /**
     * Determine whether the underlying class has an annotation that
     * is itself annotated with the meta-annotation of the given type.
     * @param metaAnnotationType the meta-annotation type to look for
     * @return whether a matching meta-annotation is defined
     */
    boolean hasMetaAnnotation(String metaAnnotationType);

    /**
     * Retrieve the attributes of the annotation of the given type,
     * if any (i.e. if defined on the underlying class).
     * @param annotationType the annotation type to look for
     * @return a Map of attributes, with the attribute name as key
     * (e.g. "value") and the defined attribute value as Map value.
     * This return value will be <code>null</code> if no matching
     * annotation is defined.
     */
    Map<String, Object> getAnnotationAttributes(String annotationType);
}
