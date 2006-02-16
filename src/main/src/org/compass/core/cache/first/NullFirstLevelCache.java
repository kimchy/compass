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

import java.util.HashMap;

import org.compass.core.Resource;
import org.compass.core.impl.ResourceIdKey;

/**
 * 
 * @author kimchy
 * 
 */
public class NullFirstLevelCache implements FirstLevelCache {

    private HashMap unmarshalled = new HashMap();

    public Object get(ResourceIdKey key) {
        return unmarshalled.get(key);
    }

    public Resource getResource(ResourceIdKey key) {
        return null;
    }

    public void set(ResourceIdKey key, Object obj) {
    }

    public void setResource(ResourceIdKey key, Resource resource) {
    }

    public void setUnmarshalled(ResourceIdKey key, Object obj) {
        unmarshalled.put(key, obj);
    }

    public void evictUnmarhsalled(ResourceIdKey key) {
        unmarshalled.put(key, null);
    }

    public void evictAllUnmarhsalled() {
        unmarshalled.clear();
    }

    public void evict(ResourceIdKey key) {
        unmarshalled.put(key, null);
    }

    public void evictAll() {
        unmarshalled.clear();
    }

}
