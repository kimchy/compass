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

package org.compass.core.json.jackson;

import java.util.Iterator;
import java.util.Map;

import org.compass.core.json.JsonObject;

/**
 * A simple {@link org.compass.core.json.JsonObject} implementation that works with
 * the {@link org.compass.core.json.jackson.converter.JacksonContentConverter} parser.
 *
 * @author kimchy
 */
public class JacksonJsonObject implements JsonObject {

    private final Map<String, Object> nodes;

    public JacksonJsonObject(Map<String, Object> nodes) {
        this.nodes = nodes;
    }

    public Map<String, Object> getNodes() {
        return this.nodes;
    }

    public Iterator<String> keys() {
        return nodes.keySet().iterator();
    }

    public Object opt(String key) {
        return nodes.get(key);
    }

    public boolean isNullValue(Object value) {
        return value == null;
    }
}
