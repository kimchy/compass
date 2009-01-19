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

package org.compass.core.json.grails;

import java.util.Iterator;

import org.codehaus.groovy.grails.web.json.JSONArray;
import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.compass.core.json.JsonObject;

/**
 * A wrapper around Grails {@link org.codehaus.groovy.grails.web.json.JSONObject}.
 *
 * @author kimchy
 */
public class GrailsJSONObject implements JsonObject {

    private JSONObject jsonObject;

    public GrailsJSONObject(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public GrailsJSONObject(JSONObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    public Iterator<String> keys() {
        return jsonObject.keys();
    }

    public Object opt(String key) {
        Object retVal = jsonObject.opt(key);
        if (retVal instanceof JSONArray) {
            return new GrailsJSONArray((JSONArray) retVal);
        } else if (retVal instanceof JSONObject) {
            return new GrailsJSONObject((JSONObject) retVal);
        } else {
            return retVal;
        }
    }

    public boolean isNullValue(Object value) {
        return JSONObject.NULL.equals(value);
    }
}
