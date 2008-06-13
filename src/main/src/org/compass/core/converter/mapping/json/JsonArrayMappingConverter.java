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

package org.compass.core.converter.mapping.json;

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.json.JsonArray;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonCompoundArrayMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class JsonArrayMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        JsonArray jsonArray = (JsonArray) root;
        JsonCompoundArrayMapping jsonArrayMapping = (JsonCompoundArrayMapping) mapping;

        Mapping elementMapping = jsonArrayMapping.getElementMapping();

        boolean store = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (jsonArray.isNull(i)) {
                value = null;
            }
            store |= elementMapping.getConverter().marshall(resource, value, elementMapping, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use json-content mapping");
    }
}