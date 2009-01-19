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

import java.io.Serializable;
import java.lang.reflect.Type;

import org.compass.core.CompassException;

/**
 * Gets values of a particular attribute.
 *
 * @author kimchy
 */
public interface Getter extends Serializable {
    
    /**
     * Get the property value from the given instance
     */
    Object get(Object target) throws CompassException;

    /**
     * Get the declared Java type
     */
    Class getReturnType();

    /**
     * Returns the generic return type of the return type. Can return
     * <code>null</code> if not known or not set.
     *
     * @see java.lang.reflect.Field#getGenericType()
     * @see java.lang.reflect.Method#getGenericReturnType()
     */
    Type getGenericReturnType();

    /**
     * Get the property name
     */
    String getName();
}
