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

package org.compass.core.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.compass.core.CompassException;
import org.compass.core.spi.InternalCompassHighlightedText;

/**
 * @author kimchy
 */
public class DefaultCompassHighlightedText implements InternalCompassHighlightedText, Map {

    private HashMap highlightedText = new HashMap();

    public void setHighlightedText(String propertyName, String highlightedText) {
        this.highlightedText.put(propertyName, highlightedText);
    }

    public String getHighlightedText() throws CompassException {
        if (highlightedText.size() == 0) {
            return null;
        }
        return (String) highlightedText.values().iterator().next();
    }

    public String getHighlightedText(String propertyName) throws CompassException {
        return (String) highlightedText.get(propertyName);
    }

    // methods from the Map interface
    // ------------------------------

    public void clear() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set entrySet() {
        return highlightedText.entrySet();
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set keySet() {
        return highlightedText.keySet();
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsKey(Object key) {
        return highlightedText.containsKey(key);
    }

    public int size() {
        return highlightedText.size();
    }

    public boolean isEmpty() {
        return highlightedText.isEmpty();
    }

    public Collection values() {
        return highlightedText.values();
    }

    public Object get(Object key) {
        return highlightedText.get(key);
    }


}
