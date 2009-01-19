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

package org.compass.core.mapping.json;

import java.util.Iterator;

import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.MultipleMapping;

/**
 * @author kimchy
 */
public class JsonMappingIterator {

    public static interface JsonMappingCallback {

        void onJsonRootObject(RootJsonObjectMapping jsonObjectMapping);

        void onJsonObject(PlainJsonObjectMapping jsonObjectMapping);

        void onJsonContent(JsonContentMapping jsonContentMapping);

        void onJsonProperty(JsonPropertyMapping jsonPropertyMapping);

        void onJsonArray(JsonArrayMapping jsonArrayMapping);

        boolean onBeginMultipleMapping(JsonMapping mapping);

        void onEndMultipleMapping(JsonMapping mapping);
    }

    public static void iterateMappings(JsonMappingCallback callback, RootJsonObjectMapping rootJsonObjectMapping, boolean recursive) {
        if (!callback.onBeginMultipleMapping(rootJsonObjectMapping)) {
            return;
        }
        callback.onJsonRootObject(rootJsonObjectMapping);
        iterateMappings(callback, (MultipleMapping) rootJsonObjectMapping, recursive);
        callback.onEndMultipleMapping(rootJsonObjectMapping);
    }

    private static void iterateMappings(JsonMappingCallback callback, MultipleMapping mapping, boolean recursive) {
        for (Iterator<Mapping> mappingsIt = mapping.mappingsIt(); mappingsIt.hasNext();) {
            Mapping m = mappingsIt.next();
            iterateMapping(callback, m, recursive);
        }
    }

    private static void iterateMapping(JsonMappingCallback callback, Mapping mapping, boolean recursive) {
        if (mapping instanceof JsonPropertyMapping) {
            callback.onJsonProperty((JsonPropertyMapping) mapping);
        } else if (mapping instanceof JsonContentMapping) {
            callback.onJsonContent((JsonContentMapping) mapping);
        } else if (mapping instanceof PlainJsonObjectMapping) {
            PlainJsonObjectMapping jsonObjectMapping = (PlainJsonObjectMapping) mapping;
            if (!callback.onBeginMultipleMapping(jsonObjectMapping)) {
                return;
            }
            callback.onJsonObject(jsonObjectMapping);
            if (recursive) {
                iterateMappings(callback, jsonObjectMapping, recursive);
            }
            callback.onEndMultipleMapping(jsonObjectMapping);
        } else if (mapping instanceof JsonArrayMapping) {
            JsonArrayMapping jsonArrayMapping = (JsonArrayMapping) mapping;
            callback.onJsonArray(jsonArrayMapping);
            iterateMapping(callback, jsonArrayMapping.getElementMapping(), recursive);
        }
    }
}
