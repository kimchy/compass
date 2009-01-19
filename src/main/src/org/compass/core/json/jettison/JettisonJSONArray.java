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

package org.compass.core.json.jettison;

import org.codehaus.jettison.json.JSONArray;
import org.compass.core.json.JsonArray;

/**
 * A wrapper around jettison {@link org.codehaus.jettison.json.JSONArray}.
 *
 * @author kimchy
 */
public class JettisonJSONArray implements JsonArray {

    private JSONArray jsonArray;

    public JettisonJSONArray(JSONArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    public int length() {
        return jsonArray.length();
    }

    public boolean isNull(int index) {
        return jsonArray.isNull(index);
    }

    public Object opt(int index) {
        return jsonArray.opt(index);
    }
}