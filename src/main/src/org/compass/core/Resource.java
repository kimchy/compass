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

package org.compass.core;

import java.io.Reader;
import java.io.Serializable;

import org.compass.core.engine.SearchEngineException;

/**
 * A Resource holds a list of meta data properties. The Resource is the basic
 * data that is saved in Compass. Compass provides object to Resource mapping as
 * well in the CompassSession object.
 * 
 * @author kimchy
 */
public interface Resource extends Serializable {

    /**
     * Returns the mapping alias of the associated Resource
     * 
     * @return The alias
     */
    String getAlias();

    /**
     * Sets the alias of the Resource. Advance method, since it is handled by
     * Compass marshlling based on mapping file if using OSEM.
     * 
     * @param alias
     */
    Resource setAlias(String alias);

    /**
     * Returns the string value of the property with the given name if any exist
     * in this resource, or null. If multiple properties exist with this name,
     * this method returns the first value added.
     * 
     * @param name
     * @return The first value that match the name
     */
    String get(String name);

    /**
     * Returns an array of values of the property specified as the method
     * parameter. This method can return <code>null</code>.
     * 
     * @param name
     *            the name of the property
     * @return a <code>String[]</code> of property values
     */
    String[] getValues(String name);

    /**
     * Adds a property to the resource based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     * 
     * @param name
     *            the name of the property
     * @param value
     *            the value to be set (will be converted to a string).
     * @throws SearchEngineException
     */
    Resource addProperty(String name, Object value) throws SearchEngineException;

    /**
     * Adds a property to the resource based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     * 
     * @param name
     *            the name of the property
     * @param value
     *            the value to be set (will be converted to a string).
     * @throws SearchEngineException
     */
    Resource addProperty(String name, Reader value) throws SearchEngineException;

    /**
     * Add a property to the resource. If the property already exists in the
     * resource (the name exists), it will be added on top of it (won't replace
     * it). Note: Compass adds all properties specified in mapping file, adding
     * extra properties to a Resource will make the index out of sync with
     * mapping.
     * 
     * @param property
     */
    Resource addProperty(Property property);

    /**
     * Remove the latest property added under the given name.
     * 
     * @param name
     */
    Resource removeProperty(String name);

    /**
     * Removes all the properties under the given name.
     * 
     * @param name
     */
    Resource removeProperties(String name);

    /**
     * Returns the first property under the name.
     * 
     * @param name
     * @return The first proeprty that match the name
     */
    Property getProperty(String name);

    /**
     * Returns all the properties under the given name.
     * 
     * @param name
     * @return An array of properties that match the name
     */
    Property[] getProperties(String name);

    /**
     * Returns all the properties for the resource.
     * 
     * @return All the properties
     */
    Property[] getProperties();

    /**
     * Returns the boost for the property.
     * 
     * @return The boost value
     */
    float getBoost();

    /**
     * Sets the boost level for the property.
     * 
     * @param boost
     */
    Resource setBoost(float boost);
}
