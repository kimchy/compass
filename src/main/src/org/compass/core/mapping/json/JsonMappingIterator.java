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

package org.compass.core.mapping.json;

import java.util.Iterator;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MultipleMapping;

/**
 * @author kimchy
 */
public class JsonMappingIterator {

    public static interface JsonMappingCallback {

        void onJsonRootObject(JsonRootObjectMapping jsonObjectMapping);

        void onJsonObject(JsonObjectMapping jsonObjectMapping);

        void onJsonContent(JsonContentMapping jsonContentMapping);

        void onJsonProperty(JsonPropertyMapping jsonPropertyMapping);

        void onJsonArray(JsonArrayMapping jsonArrayMapping);

        boolean onBeginMultipleMapping(Mapping mapping);

        void onEndMultipleMapping(Mapping mapping);
    }

    public static void iterateMappings(JsonMappingCallback callback, JsonRootObjectMapping rootObjectMapping, boolean recursive) {
        if (!callback.onBeginMultipleMapping(rootObjectMapping)) {
            return;
        }
        callback.onJsonRootObject(rootObjectMapping);
        iterateMappings(callback, (MultipleMapping) rootObjectMapping, recursive);
        callback.onEndMultipleMapping(rootObjectMapping);
    }

    private static void iterateMappings(JsonMappingCallback callback, MultipleMapping mapping, boolean recursive) {
        for (Iterator<Mapping> mappingsIt = mapping.mappingsIt(); mappingsIt.hasNext();) {
            Mapping m = mappingsIt.next();
            if (m instanceof JsonPropertyMapping) {
                callback.onJsonProperty((JsonPropertyMapping) m);
            } else if (m instanceof JsonContentMapping) {
                callback.onJsonContent((JsonContentMapping) m);
            } else if (m instanceof JsonObjectMapping) {
                if (!callback.onBeginMultipleMapping(m)) {
                    return;
                }
                callback.onJsonObject((JsonObjectMapping) m);
                if (recursive) {
                    iterateMappings(callback, mapping, recursive);
                }
                callback.onEndMultipleMapping(m);
            } else if (m instanceof JsonArrayMapping) {
                if (!callback.onBeginMultipleMapping(m)) {
                    return;
                }
                callback.onJsonArray((JsonArrayMapping) m);
                if (recursive) {
                    iterateMappings(callback, mapping, recursive);
                }
                callback.onEndMultipleMapping(m);
            }
        }
    }
}
