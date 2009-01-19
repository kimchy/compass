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

import org.codehaus.groovy.grails.web.json.JSONException;
import org.codehaus.groovy.grails.web.json.JSONObject;
import org.compass.core.json.AliasedJsonObject;

/**
 * An aliased wrapper around grails {@link JSONObject}.
 *
 * @author kimchy
 */
public class GrailsAliasedJSONObject extends GrailsJSONObject implements AliasedJsonObject {

    private String alias;

    public GrailsAliasedJSONObject(String alias, String json) throws JSONException {
        super(json);
        this.alias = alias;
    }

    public GrailsAliasedJSONObject(String alias, JSONObject jsonObject) {
        super(jsonObject);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}
