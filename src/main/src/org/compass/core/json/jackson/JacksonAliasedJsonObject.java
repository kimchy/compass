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

import java.util.Map;

import org.compass.core.json.AliasedJsonObject;

/**
 * A simple aliased {@link org.compass.core.json.JsonObject} implementation that works with
 * the {@link org.compass.core.json.jackson.converter.JacksonContentConverter} parser.
 *
 * @author kimchy
 */
public class JacksonAliasedJsonObject extends JacksonJsonObject implements AliasedJsonObject {

    private final String alias;

    public JacksonAliasedJsonObject(String alias, Map<String, Object> nodes) {
        super(nodes);
        this.alias = alias;
    }

    public String getAlias() {
        return this.alias;
    }
}