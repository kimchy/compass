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

package org.compass.core;

import java.io.Reader;
import java.io.Serializable;

import org.compass.core.engine.SearchEngineException;
import org.compass.core.spi.AliasedObject;

/**
 * A Resource holds a list of meta data properties. The Resource is the basic
 * data that is saved in Compass. Compass provides object to Resource mapping as
 * well in the CompassSession object.
 *
 * @author kimchy
 */
public interface Resource extends AliasedObject, Serializable {

    /**
     * Returns the mapping alias of the associated Resource
     *
     * @return The alias
     */
    String getAlias();

    /**
     * Returns the unique id of the resource. Note, the ids must be
     * set on this resource in order to get the uid, if not a
     * <code>CompassException</code> will be thrown.
     */
    String getUID() throws CompassException;

    /**
     * Returns the id of the resource. Used when there is only one id
     * for the resource.
     *
     * @return The id of the resource.
     */
    String getId();

    /**
     * Returns the id values of the resource.
     *
     * @return The id values of the resource
     */
    String[] getIds();

    /**
     * Returns the id property of the resource
     *
     * @return The id properties of the resource
     */
    Property getIdProperty();

    /**
     * Returns the id properties of the resource
     *
     * @return The id properties of the resource
     */
    Property[] getIdProperties();

    /**
     * Returns the string value of the property with the given name if any exist
     * in this resource, or null. If multiple properties exist with this name,
     * this method returns the first value added.
     *
     * @param name The name of the property
     * @return The first value that match the name
     */
    String getValue(String name);

    /**
     * Returns the object value of the property with the given name if any exists
     * in the resource, or null. If multiple properties exists with this name,
     * this methods returns the first value added.
     *
     * <p>If a converter is associated with the property in one of Compass mapping definitions,
     * it will be used to convert the string value to an object value. If there is no converter
     * associated with the property, the string value will be returned.
     *
     * @param name The name of the property
     * @return The first object value that match the name (converted if possible)
     */
    Object getObject(String name);


    /**
     * Returns an array of values of the proeprty with the given name. This method
     * returns an empty array if no values are associated with the given name.
     *
     * <p>If a converter is associated with the property in one of Compass mapping definitions,
     * it will be used to convert the string value to an object value. If there is no converter
     * associated with the property, the string value will be returned.
     */
    Object[] getObjects(String name);

    /**
     * Returns an array of values of the property specified as the method
     * parameter. This method can return <code>null</code>.
     *
     * @param name the name of the property
     * @return a <code>String[]</code> of property values
     */
    String[] getValues(String name);

    /**
     * Adds a property to the resource based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     *
     * @param name  the name of the property
     * @param value the value to be set (will be converted to a string).
     * @throws SearchEngineException
     */
    Resource addProperty(String name, Object value) throws SearchEngineException;

    /**
     * Adds a property to the resource based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     *
     * @param name  the name of the property
     * @param value the value to be set (will be converted to a string).
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
     * @param property The properyt to add
     */
    Resource addProperty(Property property);

    /**
     * Sets a property to the resource (removes then adds) based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     *
     * @param name  the name of the property
     * @param value the value to be set (will be converted to a string).
     * @throws SearchEngineException
     */
    Resource setProperty(String name, Object value) throws SearchEngineException;

    /**
     * Sets a property to the resource (removes then adds) based on resource mapping definitions. If
     * the property already exists in the resource (the name exists), it will be
     * added on top of it (won't replace it). ONLY use this method with resource
     * mapping.
     *
     * @param name  the name of the property
     * @param value the value to be set (will be converted to a string).
     * @throws SearchEngineException
     */
    Resource setProperty(String name, Reader value) throws SearchEngineException;

    /**
     * Sest a property to the resource (removes then adds). If the property already exists in the
     * resource (the name exists), it will be added on top of it (won't replace
     * it). Note: Compass adds all properties specified in mapping file, adding
     * extra properties to a Resource will make the index out of sync with
     * mapping.
     *
     * @param property The properyt to add
     */
    Resource setProperty(Property property);

    /**
     * Remove the latest property added under the given name.
     *
     * @param name The last property name to remove
     */
    Resource removeProperty(String name);

    /**
     * Removes all the properties under the given name.
     *
     * @param name The properties name to remove
     */
    Resource removeProperties(String name);

    /**
     * Returns the first property under the name.
     *
     * @param name The name of the property
     * @return The first proeprty that match the name
     */
    Property getProperty(String name);

    /**
     * Returns all the properties under the given name.
     *
     * @param name The name of the properties
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
     * Returns, at indexing time, the boost factor as set by {@link #setBoost(float)}.
     *
     * <p>Note that once a document is indexed this value is no longer available
     * from the index.  At search time, for retrieved documents, this method always
     * returns 1. This however does not mean that the boost value set at  indexing
     * time was ignored - it was just combined with other indexing time factors and
     * stored elsewhere, for better indexing and search performance. (For more
     * information see the "norm(t,d)" part of the scoring formula in
     * {@link org.apache.lucene.search.Similarity Similarity}.)
     *
     * @return The boost value
     */
    float getBoost();

    /**
     * /** Sets a boost factor for hits on any field of this document.  This value
     * will be multiplied into the score of all hits on this document.
     *
     * <p>The default value is 1.0.
     *
     * <p>Values are multiplied into the value of {@link org.compass.core.Property#getBoost()} of
     * each properties of the resource.  Thus, this method in effect sets a default
     * boost for the fields of this document.
     *
     * @param boost The boost level for the resource
     * @see org.compass.core.Property#setBoost(float)
     */
    Resource setBoost(float boost);

    /**
     * Copies the content of the give Resource into the current one
     *
     * @param resource The resource to copy from
     */
    void copy(Resource resource);
}
