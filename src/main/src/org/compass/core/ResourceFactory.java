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

import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.mapping.ResourcePropertyMapping;

/**
 * A {@link org.compass.core.Resource} and {@link org.compass.core.Property} factory allowing to
 * create new instances.
 *
 * <p>Note, this does not means that the resources will be created in the search engine.
 * {@link org.compass.core.CompassSession#create(Object)}, or one of the other APIs still need
 * to be called.
 *
 * @author kimchy
 */
public interface ResourceFactory {

    /**
     * Returns a null value that represents no entry in the search engine.
     * Usefull when the system needs to store an actual data entry, but for it
     * to represent a business null value.
     */
    String getNullValue();

    /**
     * Returns true if the value is marked as a null value.
     */
    boolean isNullValue(String value);
    
    /**
     * Creates a resource, that is used with the actual Search Engine
     * implementation.
     */
    Resource createResource(String alias) throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String value, ResourcePropertyMapping mapping)
            throws SearchEngineException;

    Property createProperty(String value, ResourcePropertyMapping mapping, Property.Store store, Property.Index index)
            throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String name, String value, ResourcePropertyMapping mapping)
            throws SearchEngineException;

    /**
     * Creates a property based on the converter, using the suggested values the converter has for
     * index, store and so on. If they are not suggested, defaults to sensible values.
     */
    Property createProperty(String name, String value, ResourcePropertyConverter converter);

    /**
     * Creates a Property that is used with the actual Search Engine
     */
    Property createProperty(String name, String value, Property.Store store, Property.Index index)
            throws SearchEngineException;

    /**
     * Creates a Property that is used with the actual Search Engine. The
     * available values for the store and index parameters are provided in the
     * Property interface (Property.Store, Property.Index, Property.TermVector).
     */
    Property createProperty(String name, String value, Property.Store store, Property.Index index,
                            Property.TermVector termVector) throws SearchEngineException;

    /**
     * Creates a property (TEXT type) for the specified reader.
     */
    Property createProperty(String name, Reader value) throws SearchEngineException;

    /**
     * Creates a property (indexed, and not stored) for the specified reader.
     */
    Property createProperty(String name, Reader value, Property.TermVector termVector) throws SearchEngineException;

    /**
     * Creates a binary property.
     */
    Property createProperty(String name, byte[] value, Property.Store store) throws SearchEngineException;
}
