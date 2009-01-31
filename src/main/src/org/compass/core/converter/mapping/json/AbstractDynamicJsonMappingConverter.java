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
import org.compass.core.converter.Converter;
import org.compass.core.converter.json.SimpleJsonValueConverter;
import org.compass.core.json.JsonArray;
import org.compass.core.json.JsonObject;
import org.compass.core.mapping.json.JsonArrayMapping;
import org.compass.core.mapping.json.JsonPropertyMapping;
import org.compass.core.mapping.json.PlainJsonObjectMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public abstract class AbstractDynamicJsonMappingConverter implements Converter {

    public static final String DYNAMIC_PATH_CONTEXT_KEY = "$jsonDynamicPath";

    public static final String DYNAMIC_NAMING = "$jsonDynamicNaming";

    private JsonPropertyMappingConverter propertyMappingConverter;

    private JsonPropertyMapping propertyMapping;

    private PlainJsonObjectMappingConverter objectMappingConverter;

    private PlainJsonObjectMapping objectMapping;

    private JsonArrayMappingConverter arrayMappingConverter;

    private JsonArrayMapping arrayMapping;


    protected boolean doConvertDynamicValue(Resource resource, String name, Object value, MarshallingContext context) {
        boolean store = false;
        if (value == null) {
            return store;
        }
        if (value instanceof JsonArray) {
            if (arrayMappingConverter == null) {
                arrayMappingConverter = new JsonArrayMappingConverter();
                arrayMapping = new JsonArrayMapping();
                arrayMapping.setDynamic(true);
            }
            Object oldValue = context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, name);
            store = arrayMappingConverter.marshall(resource, value, arrayMapping, context);
            context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, oldValue);
        } else if (value instanceof JsonObject) {
            if (objectMappingConverter == null) {
                objectMappingConverter = new PlainJsonObjectMappingConverter();
                objectMapping = new PlainJsonObjectMapping();
                objectMapping.setDynamic(true);
            }
            Object oldValue = context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, name);
            objectMappingConverter.marshall(resource, value, objectMapping, context);
            context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, oldValue);
        } else {
            if (propertyMappingConverter == null) {
                propertyMappingConverter = new JsonPropertyMappingConverter();
                propertyMapping = new JsonPropertyMapping();
                propertyMapping.setDynamic(true);
                propertyMapping.setValueConverter(new SimpleJsonValueConverter());
            }
            Object oldValue = context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, name);
            store = propertyMappingConverter.marshall(resource, value, propertyMapping, context);
            context.setAttribute(DYNAMIC_PATH_CONTEXT_KEY, oldValue);
        }
        return store;
    }
}
