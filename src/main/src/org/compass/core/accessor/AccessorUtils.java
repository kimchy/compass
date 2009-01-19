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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

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
}
