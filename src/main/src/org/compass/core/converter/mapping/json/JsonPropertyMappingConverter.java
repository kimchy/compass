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
import org.compass.core.converter.Converter;
import org.compass.core.converter.json.JsonFullPathHolder;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class JsonPropertyMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        JsonPropertyMapping jsonPropertyMapping = (JsonPropertyMapping) mapping;

        String name;
        if (jsonPropertyMapping.getName() == null) {
            name = (String) context.getAttribute(AbstractJsonObjectMappingConverter.DYNAMIC_PATH_CONTEXT_KEY);
        } else {
            name = jsonPropertyMapping.getName();
        }

        JsonFullPathHolder fullPathHolder = (JsonFullPathHolder) context.getAttribute(JsonFullPathHolder.CONTEXT_KEY);
        fullPathHolder.addPath(name);

        boolean store = jsonPropertyMapping.getValueConverter().marshall(resource, root, jsonPropertyMapping, context);

        fullPathHolder.removePath();

        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use json-content mapping");
    }
}