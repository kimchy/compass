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

import java.util.HashMap;

import org.compass.core.Resource;
import org.compass.core.spi.ResourceKey;

/**
 * A default implementation of first level cache. 
 *
 * @author kimchy
 */
public class PlainFirstLevelCache implements FirstLevelCache {

    private HashMap<ResourceKey, Object> objects = new HashMap<ResourceKey, Object>();

    private HashMap<ResourceKey, Resource> resources = new HashMap<ResourceKey, Resource>();

    public Object get(ResourceKey key) {
        return objects.get(key);
    }

    public Resource getResource(ResourceKey key) {
        return resources.get(key);
    }

    public void set(ResourceKey key, Object obj) {
        objects.put(key, obj);
    }

    public void setResource(ResourceKey key, Resource resource) {
        resources.put(key, resource);
    }

    public void evict(ResourceKey key) {
        objects.remove(key);
        resources.remove(key);
    }

    public void evictAll() {
        objects.clear();
        resources.clear();
    }

}
