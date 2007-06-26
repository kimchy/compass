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
 * An easy to use abstact class for Basic converters. Handles converters that usually deals with String
 * as a result of the conversion.
 *
 * <p>This base class will create a simple {@link Property} when marshalling,
 * calling {@link #toString(Object,org.compass.core.mapping.ResourcePropertyMapping)} as the {@link Property}
 * value. And will use the {@link #fromString(String,org.compass.core.mapping.ResourcePropertyMapping)} when
 * unmarhslling.
 *
 * <p>If special <code>null</code> values handling is required, the
 * {@link #handleNulls(org.compass.core.marshall.MarshallingContext)}, and
 * {@link #getNullValue(org.compass.core.marshall.MarshallingContext)} can be overriden. Note, that it is best
 * to call base implementations and extend the base funtionallity, since the base class takes special care
 * when using collections.
 *
 * @author kimchy
 */
public abstract class AbstractBasicConverter implements ResourcePropertyConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        SearchEngine searchEngine = context.getSearchEngine();

        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }
        String sValue = getNullValue(context);
        if (root != null) {
            sValue = toString(root, resourcePropertyMapping);
        }
        Property p = createProperty(sValue, resourcePropertyMapping, context);
        doSetBoost(p, root, resourcePropertyMapping, context);
        resource.addProperty(p);

        return resourcePropertyMapping.getStore() != Property.Store.NO;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;

        if (resourcePropertyMapping.getStore() == Property.Store.NO) {
            // it is not stored, so don't bother with converting it
            return null;
        }

        String propertyName = resourcePropertyMapping.getPath().getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || isNullValue(context, p.getStringValue())) {
            return null;
        }

        return fromString(p.getStringValue(), resourcePropertyMapping, context);
    }

    /**
     * Creates a new property to be added to the resource during the marshalling process. Allows
     * sub classes to override ti in order to modify the created property.
     *
     * @param value                   The value of the property
     * @param resourcePropertyMapping The resource mapping definition of the property
     * @param context                 The context (allows to get the search engine from it)
     * @return The property to be added to the Resource
     */
    protected Property createProperty(String value, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return context.getSearchEngine().createProperty(value, resourcePropertyMapping);
    }

    /**
     * <p>Should the converter handle nulls? Handling nulls means should the
     * converter process nulls or not. Usually the converter will not
     * persist null values, but sometimes it might be needed
     * ({@link org.compass.core.marshall.MarshallingContext#handleNulls()}).
     *
     * <p>Extracted to a method so special converters can control null handling.
     *
     * @param context The marshalling context
     * @return <code>true</code> if the converter should handle null values
     */
    protected boolean handleNulls(MarshallingContext context) {
        return context.handleNulls();
    }

    /**
     * If the converter handle nulls, the value that will be stored in the
     * search engine for <code>null</code> values (during the marshall process).
     *
     * @param context The marshalling context
     * @return Null value that will be inserted for <code>null</code>s.
     */
    protected String getNullValue(MarshallingContext context) {
        return context.getSearchEngine().getNullValue();
    }

    /**
     * Is the value read from the search engine is a <code>null</code> value
     * during the unmarshall process.
     *
     * @param context The marshalling context
     * @param value   The value to check for <code>null</code> value.
     * @return <code>true</code> if the value represents a null value.
     */
    protected boolean isNullValue(MarshallingContext context, String value) {
        return context.getSearchEngine().isNullValue(value);
    }

    /**
     * <p>A simple extension point that allows to set the boost value for the created {@link Property}.
     *
     * <p>The default implemenation uses the statically defined boost value in the mapping definition
     * ({@link org.compass.core.mapping.ResourcePropertyMapping#getBoost()}) to set the boost level
     * using {@link Property#setBoost(float)}
     *
     * @param property                The property to set the boost on
     * @param root                    The object that is marshalled into a property
     * @param resourcePropertyMapping The Resource Property Mapping definition
     * @throws ConversionException
     */
    protected void doSetBoost(Property property, Object root, ResourcePropertyMapping resourcePropertyMapping,
                              MarshallingContext context) throws ConversionException {
        property.setBoost(resourcePropertyMapping.getBoost());
    }

    /**
     * Override option of toString, simply calls {@link #toString(Object,org.compass.core.mapping.ResourcePropertyMapping)}
     * (without the marshalling context).
     */
    protected String toString(Object o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return toString(o, resourcePropertyMapping);
    }

    /**
     * Default implementation of toString, simply calls the Object toString.
     */
    public String toString(Object o, ResourcePropertyMapping resourcePropertyMapping) {
        return o.toString();
    }

    /**
     * An override option default to calling {@link #fromString(String,org.compass.core.mapping.ResourcePropertyMapping)}.
     * Allows to use the marshalling context.
     */
    protected Object fromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        return fromString(str, resourcePropertyMapping);
    }

}
