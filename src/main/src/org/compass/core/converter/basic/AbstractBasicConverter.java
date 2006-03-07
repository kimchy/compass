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
public abstract class AbstractBasicConverter implements ResourcePropertyConverter {

    public void marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return;
        }
        String sValue = getNullValue(context);
        if (root != null) {
            sValue = toString(root, resourcePropertyMapping);
        }
        Property p = searchEngine.createProperty(sValue, resourcePropertyMapping);
        p.setBoost(resourcePropertyMapping.getBoost());
        resource.addProperty(p);
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;

        if (resourcePropertyMapping.getStore() == Property.Store.NO) {
            // it is not stored, so don't bother with converting it
            return null;
        }

        String propertyName = resourcePropertyMapping.getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || isNullValue(context, p.getStringValue())) {
            return null;
        }

        return fromString(p.getStringValue(), resourcePropertyMapping);
    }

    /**
     * Should the converter handle nulls? Handling nulls means should the
     * converter process nulls or not. Usually the converter will not
     * persist null values, but sometimes it might be needed
     * ({@link org.compass.core.marshall.MarshallingContext#handleNulls()}).
     * <p/>
     * Extracted to a method so special converters can control null handling.
     *
     * @param context
     * @return <code>true</code> if the converter should handle null values
     */
    protected boolean handleNulls(MarshallingContext context) {
        return context.handleNulls();
    }

    /**
     * If the converter handle nulls, the value that will be stored in the
     * search engine for <code>null</code> values (during the marshall process).
     *
     * @param context
     * @return Null value that will be inserted for <code>null</code>s.
     */
    protected String getNullValue(MarshallingContext context) {
        return context.getSearchEngine().getNullValue();
    }

    /**
     * Is the value read from the search engine is a <code>null</code> value
     * during the unmarshall process.
     *
     * @param context
     * @param value   The value to check for <code>null</code> value.
     * @return <code>true</code> if the value represents a null value.
     */
    protected boolean isNullValue(MarshallingContext context, String value) {
        return context.getSearchEngine().isNullValue(value);
    }

    /**
     * Default implementation of toString, simply calls the Object toString.
     */
    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return o.toString();
    }
}
