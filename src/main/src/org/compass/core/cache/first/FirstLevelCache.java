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

package org.compass.core.cache.first;

import org.compass.core.Resource;
import org.compass.core.impl.ResourceIdKey;

/**
 * 
 * @author kimchy
 * 
 */
public interface FirstLevelCache {

    Object get(ResourceIdKey key);

    Resource getResource(ResourceIdKey key);

    void set(ResourceIdKey key, Object obj);

    void setResource(ResourceIdKey key, Resource resource);

    void setUnmarshalled(ResourceIdKey key, Object obj);

    void evictUnmarhsalled(ResourceIdKey key);

    void evictAllUnmarhsalled();

    void evict(ResourceIdKey key);

    void evictAll();

}
