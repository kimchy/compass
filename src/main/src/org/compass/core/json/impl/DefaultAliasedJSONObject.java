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

package org.compass.core.json.impl;

import org.compass.core.json.AliasedJsonObject;

/**
 * An aliased {@link DefaultJSONObject}.
 *
 * @author kimchy
 */
public class DefaultAliasedJSONObject extends DefaultJSONObject implements AliasedJsonObject {

    private String alias;

    /**
     * Constructs a new aliased json object using the provided alias.
     *
     * @param alias The alias name
     */
    public DefaultAliasedJSONObject(String alias) {
        super();
        this.alias = alias;
    }

    /**
     * Constructs a new aliased json object using the provided alias and json string.
     *
     * @param alias The alias name
     * @param json  The JSON string
     */
    public DefaultAliasedJSONObject(String alias, String json) {
        super(json);
        this.alias = alias;
    }

    /**
     * Constructs a new aliased json object using the provided alias and a json tokener.
     *
     * @param alias   The alias name
     * @param tokener The JSON tokener
     * @throws DefaultJSONException
     */
    public DefaultAliasedJSONObject(String alias, DefaultJSONTokener tokener) throws DefaultJSONException {
        super(tokener);
        this.alias = alias;
    }

    /**
     * Returns the given alias.
     */
    public String getAlias() {
        return this.alias;
    }
}
