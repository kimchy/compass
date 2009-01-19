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

package org.compass.core.cache.first;

import org.compass.core.Resource;
import org.compass.core.spi.ResourceKey;

/**
 * First leve cache used to store resources and objects that are used within a session
 * level. Since sessions are not thread safe, there is no need to worry about thread
 * safety here.
 *
 * @author kimchy
 */
public interface FirstLevelCache {

    Object get(ResourceKey key);

    Resource getResource(ResourceKey key);

    void set(ResourceKey key, Object obj);

    void setResource(ResourceKey key, Resource resource);

    void evict(ResourceKey key);

    void evictAll();

}
