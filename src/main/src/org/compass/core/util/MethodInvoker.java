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

package org.compass.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Helper class that allows to specify a method to invoke in a
 * declarative fashion, be it static or non-static.
 *
 * <p>Usage: Specify targetClass/targetMethod respectively
 * targetObject/targetMethod, optionally specify arguments,
 * prepare the invoker. Afterwards, you can invoke the method
 * any number of times.
 *
 * <p>Typically not used directly but via its subclasses
 * MethodInvokingFactoryBean and MethodInvokingJobDetailFactoryBean.
 *
 * @author kimchy
 * @see #prepare
 * @see #invoke
 */
public class MethodInvoker {

    private Class targetClass;

    private Object targetObject;

    private String targetMethod;

    private Object[] arguments;

    // the method we will call
    private Method methodObject;

    // booleans marker if the method is static
    private boolean isStatic;


    /**
     * Set the target class on which to call the target method.
     * Only necessary when the target method is static; else,
     * a target object needs to be specified anyway.
     * @see #setTargetObject
     * @see #setTargetMethod
     */
    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Return the target class on which to call the target method.
     */
    public Class getTargetClass() {
        return targetClass;
    }

    /**
     * Set the target object on which to call the target method.
     * Only necessary when the target method is not static;
     * else, a target class is sufficient.
     * @see #setTargetClass
     * @see #setTargetMethod
     */
    public void setTargetObject(Object targetObject) {
        this.targetObject = targetObject;
        if (targetObject != null) {
            this.targetClass = targetObject.getClass();
        }
    }

    /**
     * Return the target object on which to call the target method.
     */
    public Object getTargetObject() {
        return targetObject;
    }

    /**
     * Set the name of the method to be invoked.
     * Refers to either a static method or a non-static method,
     * depending on a target object being set.
     * @see #setTargetClass
     * @see #setTargetObject
     */
    public void setTargetMethod(String targetMethod) {
        this.targetMethod = targetMethod;
    }

    /**
     * Return the name of the method to be invoked.
     */
    public String getTargetMethod() {
        return targetMethod;
    }

    /**
     * Set a fully qualified static method name to invoke,
     * e.g. "example.MyExampleClass.myExampleMethod".
     * Convenient alternative to specifying targetClass and targetMethod.
     * @see #setTargetClass
     * @see #setTargetMethod
     */
    public void setStaticMethod(String staticMethod) throws ClassNotFoundException {
        int lastDotIndex = staticMethod.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == staticMethod.length()) {
            throw new IllegalArgumentException("staticMethod must be a fully qualified class plus method name: " +
                    "e.g. 'example.MyExampleClass.myExampleMethod'");
        }
        String className = staticMethod.substring(0, lastDotIndex);
        String methodName = staticMethod.substring(lastDotIndex + 1);
        setTargetClass(Class.forName(className, true, Thread.currentThread().getContextClassLoader()));
        setTargetMethod(methodName);
    }

    /**
     * Set arguments for the method invocation. If this property is not set,
     * or the Object array is of length 0, a method with no arguments is assumed.
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    /**
     * Retrun the arguments for the method invocation.
     */
    public Object[] getArguments() {
        return arguments;
    }


    /**
     * Prepare the specified method.
     * The method can be invoked any number of times afterwards.
     * @see #getPreparedMethod
     * @see #invoke
     */
    public MethodInvoker prepare() throws ClassNotFoundException, NoSuchMethodException {
        if (this.targetClass == null) {
            throw new IllegalArgumentException("Either targetClass or targetObject is required");
        }
        if (this.targetMethod == null) {
            throw new IllegalArgumentException("targetMethod is required");
        }

        if (this.arguments == null) {
            this.arguments = new Object[0];
        }

        Class[] argTypes = new Class[this.arguments.length];
        for (int i = 0; i < this.arguments.length; ++i) {
            argTypes[i] = (this.arguments[i] != null ? this.arguments[i].getClass() : Object.class);
        }

        // Try to get the exact method first.
        try {
            this.methodObject = this.targetClass.getMethod(this.targetMethod, argTypes);
        }
        catch (NoSuchMethodException ex) {
            // Just rethrow exception if we can't get any match.
            this.methodObject = findMatchingMethod();
            if (this.methodObject == null) {
                throw ex;
            }
        }

        isStatic = Modifier.isStatic(this.methodObject.getModifiers());

        return this;
    }

    /**
     * Find a matching method with the specified name for the specified arguments.
     * @return a matching method, or <code>null</code> if none
     * @see #getTargetClass()
     * @see #getTargetMethod()
     * @see #getArguments()
     */
    protected Method findMatchingMethod() {
        Method[] candidates = getTargetClass().getMethods();
        int argCount = getArguments().length;
        Method matchingMethod = null;
        int numberOfMatchingMethods = 0;

        for (int i = 0; i < candidates.length; i++) {
            // Check if the inspected method has the correct name and number of parameters.
            if (candidates[i].getName().equals(getTargetMethod()) &&
                    candidates[i].getParameterTypes().length == argCount) {
                matchingMethod = candidates[i];
                numberOfMatchingMethods++;
            }
        }

        // Only return matching method if exactly one found.
        if (numberOfMatchingMethods == 1) {
            return matchingMethod;
        }
        else {
            return null;
        }
    }

    /**
     * Return the prepared Method object that will be invoker.
     * Can for example be used to determine the return type.
     * @see #prepare
     * @see #invoke
     */
    public Method getPreparedMethod() {
        return this.methodObject;
    }

    /**
     * Invoke the specified method.
     * The invoker needs to have been prepared before.
     * @return the object (possibly null) returned by the method invocation,
     * or <code>null</code> if the method has a void return type
     * @see #prepare
     */
    public Object invoke() throws InvocationTargetException, IllegalAccessException {
        if (this.methodObject == null) {
            throw new IllegalStateException("prepare() must be called prior to invoke() on MethodInvoker");
        }
        // In the static case, target will just be <code>null</code>.
        return this.methodObject.invoke(this.targetObject, this.arguments);
    }

    /**
     * Another invoke option. Here, the target object and the arguments can
     * be specified after the prerpare method has been called. Note, that the
     * arguments should be provided before the prepare as well so the method
     * can be found (or a template of the arguments).
     */
    public Object invoke(Object targetObject, Object[] arguments) throws InvocationTargetException, IllegalAccessException {
        if (this.methodObject == null) {
            throw new IllegalStateException("prepare() must be called prior to invoke() on MethodInvoker");
        }
        if (targetObject == null && !this.isStatic) {
            throw new IllegalArgumentException("Target method must not be non-static without a target");
        }
        if (arguments == null) {
            arguments = new Object[0];
        }
        // In the static case, target will just be <code>null</code>.
        return this.methodObject.invoke(targetObject, arguments);
    }
}
