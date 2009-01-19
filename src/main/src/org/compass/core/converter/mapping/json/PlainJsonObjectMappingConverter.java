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

package org.compass.core.converter.mapping.json;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.json.JsonFullPathHolder;
import org.compass.core.json.JsonObject;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class PlainJsonObjectMappingConverter extends AbstractJsonObjectMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (root == null) {
            return false;
        }
        JsonObject jsonObject = (JsonObject) root;
        PlainJsonObjectMapping jsonObjectMapping = (PlainJsonObjectMapping) mapping;

        String name;
        if (jsonObjectMapping.getName() == null) {
            name = (String) context.getAttribute(AbstractJsonObjectMappingConverter.DYNAMIC_PATH_CONTEXT_KEY);
        } else {
            name = jsonObjectMapping.getName();
        }

        JsonFullPathHolder fullPathHolder = (JsonFullPathHolder) context.getAttribute(JsonFullPathHolder.CONTEXT_KEY);
        fullPathHolder.addPath(name);

        boolean store = doMarshall(resource, jsonObject, jsonObjectMapping, context);

        fullPathHolder.removePath();

        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use json-content mapping");
    }
}