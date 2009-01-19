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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * A wrapper around a {@link Method} allowing have different implemenatons of it.
 *
 * @author kimchy
 */
public interface ReflectionMethod {

    /**
     * Returns the Class object representing the class or interface
     * that declares the member or constructor represented by this Member.
     *
     * @return an object representing the declaring class of the
     *         underlying member
     */
    public Class<?> getDeclaringClass();

    /**
     * Returns the simple name of the underlying member or constructor
     * represented by this Member.
     *
     * @return the simple name of the underlying member
     */
    public String getName();

    /**
     * Returns the Java language modifiers for the member or
     * constructor represented by this Member, as an integer.  The
     * Modifier class should be used to decode the modifiers in
     * the integer.
     *
     * @return the Java language modifiers for the underlying member
     * @see java.lang.reflect.Modifier
     */
    public int getModifiers();

    /**
     * Returns a <code>Class</code> object that represents the formal return type
     * of the method represented by this <code>IMethod</code> object.
     *
     * @return the return type for the method this object represents
     */
    Class<?> getReturnType();

    /**
     * Returns an array of <code>Class</code> objects that represent the formal
     * parameter types, in declaration order, of the method
     * represented by this <code>Method</code> object.  Returns an array of length
     * 0 if the underlying method takes no parameters.
     *
     * @return the parameter types for the method this object
     *         represents
     */
    Class<?>[] getParameterTypes();

    /**
     * Returns an array of <code>Class</code> objects that represent
     * the types of the exceptions declared to be thrown
     * by the underlying method
     * represented by this <code>IMethod</code> object.  Returns an array of length
     * 0 if the method declares no exceptions in its <code>throws</code> clause.
     *
     * @return the exception types declared as being thrown by the
     *         method this object represents
     */
    Class[] getExceptionTypes();

    /**
     * Returns this element's annotation for the specified type if
     * such an annotation is present, else null.
     *
     * @param annotationType the Class object corresponding to the
     *                       annotation type
     * @return this element's annotation for the specified annotation type if
     *         present on this element, else null
     * @throws NullPointerException if annotationType is null
     * @since 1.5
     */
    <A extends Annotation> A getAnnotation(Class<A> annotationType);

    /**
     * Returns a <tt>Type</tt> object that represents the formal return
     * type of the method represented by this <tt>Method</tt> object.
     *
     * <p>If the return type is a parameterized type,
     * the <tt>Type</tt> object returned must accurately reflect
     * the actual type parameters used in the source code.
     *
     * <p>If the return type is a type variable or a parameterized type, it
     * is created. Otherwise, it is resolved.
     *
     * @return a <tt>Type</tt> object that represents the formal return
     *         type of the underlying  method
     * @throws java.lang.reflect.GenericSignatureFormatError
     *                                 if the generic method signature does not conform to the format
     *                                 specified in the Java Virtual Machine Specification, 3rd edition
     * @throws TypeNotPresentException if the underlying method's
     *                                 return type refers to a non-existent type declaration
     * @throws java.lang.reflect.MalformedParameterizedTypeException
     *                                 if the
     *                                 underlying method's return typed refers to a parameterized
     *                                 type that cannot be instantiated for any reason
     * @since 1.5
     */
    Type getGenericReturnType();

    /**
     * Invokes the underlying method represented by this <code>IMethod</code>
     * object, on the specified object with the specified parameters.
     * Individual parameters are automatically unwrapped to match
     * primitive formal parameters, and both primitive and reference
     * parameters are subject to method invocation conversions as
     * necessary.
     *
     * @param obj  the object the underlying method is invoked from
     * @param args the arguments used for the method call
     * @return the result of dispatching the method represented by
     *         this object on <code>obj</code> with parameters
     *         <code>args</code>
     * @throws IllegalAccessException      if this <code>Method</code> object
     *                                     enforces Java language access control and the underlying
     *                                     method is inaccessible.
     * @throws IllegalArgumentException    if the method is an
     *                                     instance method and the specified object argument
     *                                     is not an instance of the class or interface
     *                                     declaring the underlying method (or of a subclass
     *                                     or implementor thereof); if the number of actual
     *                                     and formal parameters differ; if an unwrapping
     *                                     conversion for primitive arguments fails; or if,
     *                                     after possible unwrapping, a parameter value
     *                                     cannot be converted to the corresponding formal
     *                                     parameter type by a method invocation conversion.
     * @throws java.lang.reflect.InvocationTargetException
     *                                     if the underlying method
     *                                     throws an exception.
     * @throws NullPointerException        if the specified object is null
     *                                     and the method is an instance method.
     * @throws ExceptionInInitializerError if the initialization
     *                                     provoked by this method fails.
     */
    Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

    /**
     * Return a reference to the {@link java.lang.reflect.Method} that this {@link org.compass.core.util.reflection.ReflectionMethod}
     * represent.
     */
    Method getMethod();
}
