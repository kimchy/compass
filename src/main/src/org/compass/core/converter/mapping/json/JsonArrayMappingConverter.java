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
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.json.JsonArray;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.Naming;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class JsonArrayMappingConverter extends AbstractDynamicJsonMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (root == null) {
            return false;
        }
        JsonArray jsonArray = (JsonArray) root;
        JsonArrayMapping jsonArrayMapping = (JsonArrayMapping) mapping;

        Mapping elementMapping = jsonArrayMapping.getElementMapping();

        String propertyName;
        PropertyPath path = jsonArrayMapping.getPath();
        if (path == null) {
            propertyName = (String) context.getAttribute(DYNAMIC_PATH_CONTEXT_KEY);
        } else {
            propertyName = path.getPath();
        }
        
        boolean store = false;
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.opt(i);
            if (value != null && jsonArray.isNull(i)) {
                value = null;
            }
            if (jsonArrayMapping.isDynamic()) {
                Naming oldNaming = null;
                if (jsonArrayMapping.getDynamicNaming() != null) {
                    oldNaming = (Naming) context.setAttribute(DYNAMIC_NAMING, jsonArrayMapping.getDynamicNaming());
                }
                store |= doConvertDynamicValue(resource, propertyName, value, context);
                if (jsonArrayMapping.getDynamicNaming() != null) {
                    // set the original naming
                    context.setAttribute(DYNAMIC_NAMING, oldNaming);
                }
            } else {
                store |= elementMapping.getConverter().marshall(resource, value, elementMapping, context);
            }
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use json-content mapping");
    }
}