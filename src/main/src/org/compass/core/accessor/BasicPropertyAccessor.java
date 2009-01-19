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

package org.compass.core.accessor;

import java.beans.Introspector;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.util.ClassUtils;
import org.compass.core.util.reflection.ReflectionFactory;
import org.compass.core.util.reflection.ReflectionMethod;

/**
 * Accesses property values via a get/set pair, which may be nonpublic. The
 * default (and recommended strategy).
 *
 * <p>
 * Initial version taken from hibernate.
 * </p>
 *
 * @author kimchy
 */
public class BasicPropertyAccessor implements PropertyAccessor, CompassConfigurable {

    private CompassSettings settings;

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
    }

    public static final class BasicSetter implements Setter {

        private static final long serialVersionUID = 3979266932753381168L;

        private Class clazz;

        private final transient ReflectionMethod method;

        private final String propertyName;

        private BasicSetter(Class clazz, ReflectionMethod method, String propertyName) {
            this.clazz = clazz;
            this.method = method;
            this.propertyName = propertyName;
        }

        public void set(Object target, Object value) throws CompassException {
            try {
                method.invoke(target, value);
            } catch (NullPointerException npe) {
                if (value == null && method.getParameterTypes()[0].isPrimitive()) {
                    throw new PropertyAccessException(npe, "Null value was assigned to a property of primitive type",
                            true, clazz, propertyName);
                } else {
                    throw new PropertyAccessException(npe, "NullPointerException occurred while calling", true, clazz,
                            propertyName);
                }
            } catch (InvocationTargetException ite) {
                throw new PropertyAccessException(ite, "Exception occurred inside", true, clazz, propertyName);
            } catch (IllegalAccessException iae) {
                throw new PropertyAccessException(iae, "IllegalAccessException occurred while calling", true, clazz,
                        propertyName);
                // cannot occur
            } catch (IllegalArgumentException iae) {
                if (value == null && method.getParameterTypes()[0].isPrimitive()) {
                    throw new PropertyAccessException(iae, "Null value was assigned to a property of primitive type",
                            true, clazz, propertyName);
                } else {
                    throw new PropertyAccessException(iae, "IllegalArgumentException occurred while calling", true,
                            clazz, propertyName);
                }
            }
        }

        public String getName() {
            return propertyName;
        }

        public String getMethodName() {
            return method.getName();
        }

        public String toString() {
            return "BasicSetter(" + clazz.getName() + '.' + propertyName + ')';
        }
    }

    public static final class BasicGetter implements Getter {

        private static final long serialVersionUID = 3978701788020880176L;

        private Class clazz;

        private final transient ReflectionMethod method;

        private final String propertyName;

        private BasicGetter(Class clazz, ReflectionMethod method, String propertyName) {
            this.clazz = clazz;
            this.method = method;
            this.propertyName = propertyName;
        }

        public Object get(Object target) throws CompassException {
            try {
                return method.invoke(target);
            } catch (InvocationTargetException ite) {
                throw new PropertyAccessException(ite, "Exception occurred inside", false, clazz, propertyName);
            } catch (IllegalAccessException iae) {
                throw new PropertyAccessException(iae, "IllegalAccessException occurred while calling", false, clazz,
                        propertyName);
                // cannot occur
            } catch (IllegalArgumentException iae) {
                throw new PropertyAccessException(iae, "IllegalArgumentException occurred calling", false, clazz,
                        propertyName);
            }
        }

        public String getName() {
            return propertyName;
        }

        public Class getReturnType() {
            return method.getReturnType();
        }

        public Type getGenericReturnType() {
            return method.getGenericReturnType();
        }

        public String toString() {
            return "BasicGetter(" + clazz.getName() + '.' + propertyName + ')';
        }
    }

    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return getSetterOrNull(theClass, propertyName);
    }

    private BasicSetter getSetterOrNull(Class theClass, String propertyName) {

        if (theClass == Object.class || theClass == null)
            return null;

        Method method = setterMethod(theClass, propertyName);

        if (method != null) {
            if (!ClassUtils.isPublic(theClass, method)) {
                method.setAccessible(true);
            }
            try {
                return new BasicSetter(theClass, ReflectionFactory.getMethod(settings, method), propertyName);
            } catch (NoSuchMethodException e) {
                throw new PropertyAccessException(e, "Failed to get method for reflection", true, theClass, method.getName());
            }
        } else {
            BasicSetter setter = getSetterOrNull(theClass.getSuperclass(), propertyName);
            if (setter == null) {
                Class[] interfaces = theClass.getInterfaces();
                for (int i = 0; setter == null && i < interfaces.length; i++) {
                    setter = getSetterOrNull(interfaces[i], propertyName);
                }
            }
            return setter;
        }

    }

    private Method setterMethod(Class theClass, String propertyName) {

        BasicGetter getter = getGetterOrNull(theClass, propertyName);
        Class returnType = (getter == null) ? null : getter.getReturnType();

        Method[] methods = theClass.getDeclaredMethods();
        Method potentialSetter = null;
        for (Method method : methods) {
            String methodName = method.getName();

            if (method.getParameterTypes().length == 1 && methodName.startsWith("set")) {
                String testStdMethod = Introspector.decapitalize(methodName.substring(3));
                String testOldMethod = methodName.substring(3);
                if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName)) {
                    potentialSetter = method;
                    if (returnType == null || method.getParameterTypes()[0].equals(returnType))
                        return potentialSetter;
                }
            }
        }
        return potentialSetter;
    }

    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        return createGetter(theClass, propertyName);
    }

    public Getter createGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
        BasicGetter result = getGetterOrNull(theClass, propertyName);
        if (result == null) {
            throw new PropertyNotFoundException("Could not find a getter for " + propertyName + " in class "
                    + theClass.getName());
        }
        return result;

    }

    private BasicGetter getGetterOrNull(Class theClass, String propertyName) {

        if (theClass == Object.class || theClass == null)
            return null;

        Method method = getterMethod(theClass, propertyName);

        if (method != null) {
            if (!ClassUtils.isPublic(theClass, method))
                method.setAccessible(true);
            try {
                return new BasicGetter(theClass, ReflectionFactory.getMethod(settings, method), propertyName);
            } catch (NoSuchMethodException e) {
                throw new PropertyAccessException(e, "Failed to get method for reflection", true, theClass, method.getName());
            }
        } else {
            BasicGetter getter = getGetterOrNull(theClass.getSuperclass(), propertyName);
            if (getter == null) {
                Class[] interfaces = theClass.getInterfaces();
                for (int i = 0; getter == null && i < interfaces.length; i++) {
                    getter = getGetterOrNull(interfaces[i], propertyName);
                }
            }
            return getter;
        }
    }

    private Method getterMethod(Class theClass, String propertyName) {

        // first try and find it directly
        try {
            return theClass.getMethod("get" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
        } catch (NoSuchMethodException e) {
            // continue our search
        }

        try {
            return theClass.getMethod("is" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1));
        } catch (NoSuchMethodException e) {
            // continue our search
        }

        Method[] methods = theClass.getDeclaredMethods();
        for (Method method : methods) {
            // only carry on if the method has no parameters
            if (method.getParameterTypes().length == 0) {
                String methodName = method.getName();

                // try "get"
                if (methodName.startsWith("get")) {
                    String testStdMethod = Introspector.decapitalize(methodName.substring(3));
                    String testOldMethod = methodName.substring(3);
                    if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName))
                        return method;

                }

                // if not "get" then try "is"
                /*
                 * boolean isBoolean =
                 * methods[i].getReturnType().equals(Boolean.class) ||
                 * methods[i].getReturnType().equals(boolean.class);
                 */
                if (methodName.startsWith("is")) {
                    String testStdMethod = Introspector.decapitalize(methodName.substring(2));
                    String testOldMethod = methodName.substring(2);
                    if (testStdMethod.equals(propertyName) || testOldMethod.equals(propertyName))
                        return method;
                }
            }
        }
        return null;
    }

}
