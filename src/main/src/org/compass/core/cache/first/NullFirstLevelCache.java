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
 * A null first level cache, not storing anything.
 *
 * @author kimchy
 */
public class NullFirstLevelCache implements FirstLevelCache {

    public static final NullFirstLevelCache INSTANCE = new NullFirstLevelCache();
    
    public Object get(ResourceKey key) {
        return null;
    }

    public Resource getResource(ResourceKey key) {
        return null;
    }

    public void set(ResourceKey key, Object obj) {
    }

    public void setResource(ResourceKey key, Resource resource) {
    }

    public void evict(ResourceKey key) {
    }

    public void evictAll() {
    }

}
