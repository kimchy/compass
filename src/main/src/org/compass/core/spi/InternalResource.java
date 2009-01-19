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

package org.compass.core.spi;

import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.mapping.ResourceMapping;

/**
 * An intenral SPI of Resource.
 *
 * @author kimchy
 */
public interface InternalResource extends Resource {

    /**
     * Returns the resource key associated with this resource.
     */
    ResourceKey getResourceKey();

    /**
     * Returns the resource mapping associated with this resource.
     */
    ResourceMapping getResourceMapping();

    /**
     * Returns the sub index this resource is associated with.
     */
    String getSubIndex();

    /**
     * Sets the UID for the resource based on the current ids existing withing
     * the resource and the resource mapping associated with it.
     */
    void addUID();

    /**
     * Attaches the given resource to the search engine factory.
     */
    void attach(SearchEngineFactory searchEngineFactory);
}
