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

/**
 * Abstracts the notion of a "property". Defines a strategy for accessing the
 * value of a class property.
 * <p>
 * Custom implementation can implement {@link org.compass.core.config.CompassConfiguration}
 * for external configuration settings.
 *
 * @author kimchy
 */
public interface PropertyAccessor {
    /**
     * Create a "getter" for the named attribute
     */
    public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException;

    /**
     * Create a "setter" for the named attribute
     */
    public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException;
}
