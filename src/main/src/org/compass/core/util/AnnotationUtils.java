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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author kimchy
 */
public abstract class AnnotationUtils {

    private AnnotationUtils() {

    }

    public static Method findAnnotatedMethod(Class<? extends Annotation> annotationType, Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class)) {
            return null;
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getAnnotation(annotationType) != null) {
                return method;
            }
        }
        return findAnnotatedMethod(annotationType, clazz.getSuperclass());
    }

    public static Field findAnnotatedField(Class<? extends Annotation> annotationType, Class<?> clazz) {
        if (clazz == null || clazz.equals(Object.class)) {
            return null;
        }
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(annotationType) != null) {
                return field;
            }
        }
        return findAnnotatedField(annotationType, clazz.getSuperclass());
    }
}
