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

import org.compass.core.CompassException;

/**
 * Sets values to a particular attribute
 * 
 * @author kimchy
 */
public interface Setter extends Serializable {
    /**
     * Set the property value from the given instance
     */
    public void set(Object target, Object value) throws CompassException;

    /**
     * Get the property name
     */
    public String getName();

    /**
     * Optional operation (return null)
     */
    public String getMethodName();
}
