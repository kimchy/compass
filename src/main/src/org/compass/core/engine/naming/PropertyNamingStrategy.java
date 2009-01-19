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

package org.compass.core.engine.naming;


/**
 * The property naming strategy that compass will use for hidden properties.
 * Created using {@link PropertyNamingStrategyFactory}
 *
 * @author kimchy
 * @see PropertyNamingStrategyFactory
 * @see StaticPropertyNamingStrategy
 * @see DynamicPropertyNamingStrategy
 */
public interface PropertyNamingStrategy {

    /**
     * Returns true if the property name is an internal property.
     *
     * @param name
     * @return <code>true</code> if the name stands for an internal property.
     */
    boolean isInternal(String name);

    /**
     * Returns the root path for hidden properties.
     *
     * @return The root path for intenral properties.
     */
    PropertyPath getRootPath();

    /**
     * Builds the path for a root property, base on the root part and the
     * property name.
     *
     * @param root The root path to build the path from
     * @param name The name to add to the path
     * @return The generated path from the root and the name
     */
    PropertyPath buildPath(PropertyPath root, String name);

}
