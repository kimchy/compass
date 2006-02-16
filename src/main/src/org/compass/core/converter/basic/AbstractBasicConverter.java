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

package org.compass.core.converter.basic;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.ResourcePropertyConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * An easy to use abstact class for Basic converters.
 *
 * @author kimchy
 */
public abstract class AbstractBasicConverter implements Converter, ResourcePropertyConverter {

    public void marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        // don't save a null value if the context does not states so
        if (root == null && !context.handleNulls()) {
            return;
        }
        String sValue = searchEngine.getNullValue();
        if (root != null) {
            sValue = toString(root, resourcePropertyMapping);
        }
        Property p = searchEngine.createProperty(sValue, resourcePropertyMapping);
        p.setBoost(resourcePropertyMapping.getBoost());
        resource.addProperty(p);
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        if (resourcePropertyMapping.getStore() == Property.Store.NO) {
            // it is not stored, so don't bother with converting it
            return null;
        }

        String propertyName = resourcePropertyMapping.getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || searchEngine.isNullValue(p.getStringValue())) {
            return null;
        }

        return fromString(p.getStringValue(), resourcePropertyMapping);
    }

    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return o.toString();
    }
}
