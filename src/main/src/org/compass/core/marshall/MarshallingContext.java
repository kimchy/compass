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

package org.compass.core.marshall;

import java.util.Map;

import org.compass.core.ResourceFactory;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public interface MarshallingContext {

    ResourceFactory getResourceFactory();

    SearchEngine getSearchEngine();

    CompassMapping getCompassMapping();

    ConverterLookup getConverterLookup();

    InternalCompassSession getSession();

    MarshallingStrategy getMarshallingStrategy();

    PropertyNamingStrategy getPropertyNamingStrategy();

    Object setAttribute(Object key, Object value);

    Object getAttribute(Object key);

    Object removeAttribute(Object key);

    boolean hasAttribute(Object key);

    Map<Object, Object> removeAttributes();

    void restoreAttributes(Map<Object, Object> attributes);

    /**
     * Sets an unmarshalled cache of objects already loaded during
     * unmarshalling. Allows for handling cyclic references.
     */
    void setUnmarshalled(ResourceKey key, Object obj);

    /**
     * Gets an unmarshalled cache of objects already loaded during
     * unmarshalling. Allows for handling cyclic references.
     */
    Object getUnmarshalled(ResourceKey key);

    void setMarshalled(Object key, Object value);

    Object getMarshalled(Object key);

    void clearContext();

    /**
     * Means that when creating a property, a special null value should be saved
     * to mark it as null
     */
    boolean handleNulls();

    /**
     * Sets on the path to use null values
     */
    void setHandleNulls(PropertyPath path);

    /**
     * Removes the fact that a null value should be used.
     */
    void removeHandleNulls(PropertyPath path);
}
