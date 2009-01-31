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

import java.util.Iterator;

import org.compass.core.Resource;
import org.compass.core.json.JsonObject;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonObjectMapping;
import org.compass.core.mapping.json.Naming;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public abstract class AbstractJsonObjectMappingConverter extends AbstractDynamicJsonMappingConverter {

    protected boolean doMarshall(Resource resource, JsonObject jsonObject, JsonObjectMapping mapping, MarshallingContext context) {
        boolean store = false;

        for (Iterator<Mapping> it = mapping.mappingsIt(); it.hasNext();) {
            Mapping m = it.next();
            Object value = jsonObject.opt(m.getName());
            if (value != null && jsonObject.isNullValue(value)) {
                value = null;
            }
            store |= m.getConverter().marshall(resource, value, m, context);
        }

        if (mapping.isDynamic()) {
            for (Iterator<String> keyIt = jsonObject.keys(); keyIt.hasNext();) {
                String key = keyIt.next();
                if (mapping.getMapping(key) == null) {
                    Naming oldNaming = null;
                    if (mapping.getDynamicNaming() != null) {
                        oldNaming = (Naming) context.setAttribute(DYNAMIC_NAMING, mapping.getDynamicNaming());
                    }
                    doConvertDynamicValue(resource, key, jsonObject.opt(key), context);
                    if (mapping.getDynamicNaming() != null) {
                        // set the original naming
                        context.setAttribute(DYNAMIC_NAMING, oldNaming);
                    }
                }
            }
        }

        return store;
    }
}
