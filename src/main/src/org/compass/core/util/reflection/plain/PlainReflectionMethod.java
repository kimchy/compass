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

package org.compass.core.util.reflection.plain;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.compass.core.util.reflection.ReflectionMethod;

/**
 * A plain implemenation of {@link org.compass.core.util.reflection.ReflectionMethod}
 * that simply delegates operations to {@link java.lang.reflect.Method}.
 *
 * @author kimchy
 */
public class PlainReflectionMethod<T> implements ReflectionMethod {

    private Method method;

    public PlainReflectionMethod(Method method) {
        this.method = method;
    }

    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    public String getName() {
        return method.getName();
    }

    public int getModifiers() {
        return method.getModifiers();
    }

    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    public Class<?>[] getParameterTypes() {
        return method.getParameterTypes();
    }

    public Class[] getExceptionTypes() {
        return method.getExceptionTypes();
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
        return method.getAnnotation(annotationType);
    }

    public Type getGenericReturnType() {
        return method.getGenericReturnType();
    }

    public Object invoke(Object obj, Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return method.invoke(obj, args);
    }

    public Method getMethod() {
        return method;
    }
}
