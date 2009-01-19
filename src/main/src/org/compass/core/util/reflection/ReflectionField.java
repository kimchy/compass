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

package org.compass.core.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * A wrapper around {@link Field} allowing different implementation of it.
 *
 * @author kimchy
 */
public interface ReflectionField {

    Object get(Object obj) throws IllegalArgumentException, IllegalAccessException;

    void set(Object obj, Object value) throws IllegalArgumentException, IllegalAccessException;

    /**
     * Returns the name of the field represented by this <code>Field</code> object.
     */
    String getName();

    /**
     * Returns the <code>Class</code> object representing the class or interface
     * that declares the field represented by this <code>Field</code> object.
     */
    Class<?> getDeclaringClass();

    /**
     * Returns the Java language modifiers for the field represented
     * by this <code>Field</code> object, as an integer. The <code>Modifier</code> class should
     * be used to decode the modifiers.
     *
     * @see java.lang.reflect.Modifier
     */
    int getModifiers();

    /**
     * Returns a <code>Class</code> object that identifies the
     * declared type for the field represented by this
     * <code>Field</code> object.
     *
     * @return a <code>Class</code> object identifying the declared
     *         type of the field represented by this object
     */
    Class<?> getType();

    /**
     * Returns a <tt>Type</tt> object that represents the declared type for
     * the field represented by this <tt>Field</tt> object.
     *
     * <p>If the <tt>Type</tt> is a parameterized type, the
     * <tt>Type</tt> object returned must accurately reflect the
     * actual type parameters used in the source code.
     *
     * <p>If an the  type of the underlying field is a type variable or a
     * parameterized type, it is created. Otherwise, it is resolved.
     *
     * @return a <tt>Type</tt> object that represents the declared type for
     *         the field represented by this <tt>Field</tt> object
     * @throws java.lang.reflect.GenericSignatureFormatError
     *                                 if the generic field
     *                                 signature does not conform to the format specified in the Java
     *                                 Virtual Machine Specification, 3rd edition
     * @throws TypeNotPresentException if the generic type
     *                                 signature of the underlying field refers to a non-existent
     *                                 type declaration
     * @throws java.lang.reflect.MalformedParameterizedTypeException
     *                                 if the generic
     *                                 signature of the underlying field refers to a parameterized type
     *                                 that cannot be instantiated for any reason
     * @since 1.5
     */
    Type getGenericType();

    /**
     * Returns the wrapped field.
     */
    Field getField();
}
