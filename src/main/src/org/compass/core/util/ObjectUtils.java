/*
 * Copyright 2004-2008 the original author or authors.
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

import java.lang.reflect.Array;

/**
 * Miscellaneous object utility methods. Mainly for internal use within the
 * framework; consider Jakarta's Commons Lang for a more comprehensive suite
 * of object utilities.
 *
 * @author kimchy
 */
public abstract class ObjectUtils {

    /**
     * Determine if the given objects are equal, returning true if both are null
     * or false if only one is null.
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     */
    public static boolean nullSafeEquals(Object o1, Object o2) {
        return (o1 == o2 || (o1 != null && o1.equals(o2)));
    }

    /**
     * Return a hex string form of an object's identity hash code.
     * @param o the object
     * @return the object's identity code in hex
     */
    public static String getIdentityHexString(Object o) {
        return Integer.toHexString(System.identityHashCode(o));
    }

    /**
     * Return whether the given throwable is a checked exception,
     * i.e. an Exception but not a RuntimeException.
     * @param ex the throwable to check
     * @return whether the throwable is a checked exception
     * @see java.lang.Exception
     * @see java.lang.RuntimeException
     */
    public static boolean isCheckedException(Throwable ex) {
        return (ex instanceof Exception) && (!(ex instanceof RuntimeException));
    }

    /**
     * Check whether the given exception is compatible with the exceptions
     * declared in a throws clause.
     * @param ex the exception to checked
     * @param declaredExceptions the exceptions declared in the throws clause
     * @return whether the given exception is compatible
     */
    public static boolean isCompatibleWithThrowsClause(Throwable ex, Class[] declaredExceptions) {
        if (ex instanceof RuntimeException) {
            return true;
        }
        if (declaredExceptions != null) {
            for (int i = 0; i < declaredExceptions.length; i++) {
                if (declaredExceptions[i].isAssignableFrom(ex.getClass())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return whether the given array is empty: that is, null or of zero length.
     * @param array the array to check
     */
    public static boolean isEmpty(Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Convert a primitive array to an object array of primitive wrapper objects.
     * @param primitiveArray the primitive array
     * @return the object array
     * @throws IllegalArgumentException if the parameter is not a primitive array
     */
    public static Object[] toObjectArray(Object primitiveArray) {
        if (primitiveArray == null) {
            return new Object[0];
        }
        Class clazz = primitiveArray.getClass();
        Assert.isTrue(clazz.isArray(),
                "The specified parameter is not an array - it must be a primitive array.");
        Assert.isTrue(clazz.getComponentType().isPrimitive(),
                "The specified parameter is not a primitive array.");
        int length = Array.getLength(primitiveArray);
        if (length == 0) {
            return new Object[0];
        }
        Class wrapperType = Array.get(primitiveArray, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(primitiveArray, i);
        }
        return newArray;
    }

}
