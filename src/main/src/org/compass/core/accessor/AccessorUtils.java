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

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

/**
 * @author kimchy
 */
public class AccessorUtils {

    /**
     * Returns the Java 5 generics collection parameter. Returns null
     * if working with Java 1.4 or the collection has no generics parameter.
     */
    public static Class getCollectionParameter(Getter getter) {
        if (!Collection.class.isAssignableFrom(getter.getReturnType())) {
            return null;
        }
        Type type = getter.getGenericReturnType();
        if (type == null) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            Object[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                return (Class) actualTypeArguments[0];
            }
        }
        return null;
    }

    /**
     * Returns the Java 5 generics collection parameter. Returns null
     * if working with Java 1.4 or the collection has no generics parameter.
     */
    public static Class getMapKeyParameter(Getter getter) {
        if (!Map.class.isAssignableFrom(getter.getReturnType())) {
            return null;
        }
        Type type = getter.getGenericReturnType();
        if (type == null) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            Object[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                return (Class) actualTypeArguments[0];
            }
        }
        return null;
    }

    public static boolean isMapValueParameterArray(Getter getter) {
        if (!Map.class.isAssignableFrom(getter.getReturnType())) {
            return false;
        }
        Type type = getter.getGenericReturnType();
        if (type == null) {
            return false;
        }
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                if (actualTypeArguments[1] instanceof GenericArrayType) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isMapValueParameterCollection(Getter getter) {
        if (!Map.class.isAssignableFrom(getter.getReturnType())) {
            return false;
        }
        Type type = getter.getGenericReturnType();
        if (type == null) {
            return false;
        }
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                if (actualTypeArguments[1] instanceof ParameterizedType) {
                    // collection?
                    Class paramType = (Class) ((ParameterizedType) actualTypeArguments[1]).getRawType();
                    if (Collection.class.isAssignableFrom(paramType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the Java 5 generics collection parameter. If it is an array,
     * returns the component of the array (use {@link #isMapValueParameterArray(Getter)} in order
     * to know if it was an array).
     */
    public static Class getMapValueParameter(Getter getter) {
        if (!Map.class.isAssignableFrom(getter.getReturnType())) {
            return null;
        }
        Type type = getter.getGenericReturnType();
        if (type == null) {
            return null;
        }
        if (type instanceof ParameterizedType) {
            Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length == 2) {
                if (actualTypeArguments[1] instanceof GenericArrayType) {
                    return (Class) ((GenericArrayType) actualTypeArguments[1]).getGenericComponentType();
                } else if (actualTypeArguments[1] instanceof ParameterizedType) {
                    // collection?
                    Class paramType = (Class) ((ParameterizedType) actualTypeArguments[1]).getRawType();
                    if (Collection.class.isAssignableFrom(paramType)) {
                        return (Class) ((ParameterizedType) actualTypeArguments[1]).getActualTypeArguments()[0];
                    }
                } else {
                    return (Class) actualTypeArguments[1];
                }
            }
        }
        return null;
    }
}
