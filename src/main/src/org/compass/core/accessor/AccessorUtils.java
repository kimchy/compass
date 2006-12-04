/*
 * Copyright 2004-2006 the original author or authors.
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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import org.compass.core.util.JdkVersion;
import org.compass.core.util.MethodInvoker;

/**
 * @author kimchy
 */
public class AccessorUtils {

    /**
     * Returns the Java 5 generics collection parameter. Returns null
     * if working with Java 1.4 or the collection has no generics parameter.
     */
    public static Class getCollectionParameter(Getter getter) {
        if (JdkVersion.getMajorJavaVersion() <= JdkVersion.JAVA_14) {
            return null;
        }
        if (!Collection.class.isAssignableFrom(getter.getReturnType())) {
            return null;
        }
        try {
            Object type = null;
            if (getter instanceof DirectPropertyAccessor.DirectGetter) {
                Field field = ((DirectPropertyAccessor.DirectGetter) getter).getField();
                MethodInvoker methodInvoker = new MethodInvoker();
                methodInvoker.setTargetMethod("getGenericType");
                methodInvoker.setTargetObject(field);
                type = methodInvoker.prepare().invoke();
            } else if (getter instanceof BasicPropertyAccessor.BasicGetter) {
                Method method = ((BasicPropertyAccessor.BasicGetter) getter).getMethod();
                MethodInvoker methodInvoker = new MethodInvoker();
                methodInvoker.setTargetMethod("getGenericReturnType");
                methodInvoker.setTargetObject(method);
                type = methodInvoker.prepare().invoke();
            }
            MethodInvoker methodInvoker = new MethodInvoker();
            methodInvoker.setTargetObject(type);
            methodInvoker.setTargetMethod("getActualTypeArguments");
            Object[] actualTypeArguments = (Object[]) methodInvoker.prepare().invoke();
            if (actualTypeArguments != null && actualTypeArguments.length == 1) {
                return (Class) actualTypeArguments[0];
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
