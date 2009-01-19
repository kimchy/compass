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

package org.compass.core.json;

import java.util.Iterator;

/**
 * <p>An {@link JsonObject} that has an json string representation. Mainly used for simpliciy,
 * where Compass will use the configured {@link org.compass.core.converter.json.JsonContentConverter}
 * in order to convert to xml string into the actual {@link org.compass.core.xml.XmlObject} implementation.
 *
 * <p>This object will only be used when saving json object into Compass. When Compass returns json objects
 * as a restult of a query or get/load operations, the actual {@link JsonObject} will be returned.
 *
 * <p>Naturally, since the json string will only be parsed when Compass will convert this object, all the
 * {@link JsonObject} methods are not implemented. The {@link JsonObject} is just used
 * as a marker interface to use the correct json supported converters.
 *
 * @author kimchy
 */
public class RawJsonObject implements JsonObject {

    private String json;

    /**
     * Creates a new String based xml object using a String holding the actual xml content.
     */
    public RawJsonObject(String json) {
        this.json = json;
    }

    public String getJson() {
        return this.json;
    }

    public Iterator<String> keys() {
        throw new UnsupportedOperationException("Operation not allows on RawJsonObject");
    }

    public boolean isNullValue(Object value) {
        throw new UnsupportedOperationException("Operation not allows on RawJsonObject");
    }

    public Object opt(String key) {
        throw new UnsupportedOperationException("Operation not allows on RawJsonObject");
    }
}