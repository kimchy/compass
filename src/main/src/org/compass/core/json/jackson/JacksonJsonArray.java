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

import java.util.List;

import org.compass.core.json.JsonArray;

/**
 * A simple {@link org.compass.core.json.JsonArray} implementation that works with
 * the {@link org.compass.core.json.jackson.converter.JacksonContentConverter} parser.
 *
 * @author kimchy
 */
public class JacksonJsonArray implements JsonArray {

    private final List<Object> values;

    public JacksonJsonArray(List<Object> values) {
        this.values = values;
    }

    public int length() {
        return values.size();
    }

    public boolean isNull(int index) {
        return values.get(index) == null;
    }

    public Object opt(int index) {
        return values.get(index);
    }
}
