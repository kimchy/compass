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

package org.compass.core.converter.basic;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ContextResourcePropertyConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * An easy to use abstact class for Basic converters. Handles converters that usually deals with String
 * as a result of the conversion.
 *
 * <p>Allows to override the actual marshalling and un-marshalling of object to strings. In order to override
 * marshalling, override {@link #doToString(Object,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}
 * and in order to override un-marshalling overrode
 * {@link #doFromString(String,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}.
 *
 * @author kimchy
 */
public abstract class AbstractBasicConverter<T> implements ContextResourcePropertyConverter<T> {

    public boolean marshall(Resource resource, T root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;

        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(resourcePropertyMapping, context)) {
            return false;
        }
        String sValue;
        if (root instanceof String && context.getResourceFactory().isNullValue((String) root)) {
            // check if this is a null value (since sometimes we get injected with a null value for non string types)
            sValue = (String) root;
        } else {
            sValue = toString(root, resourcePropertyMapping, context);
        }
        Property p = createProperty(sValue, resourcePropertyMapping, context);
        doSetBoost(p, root, resourcePropertyMapping, context);
        resource.addProperty(p);

        return resourcePropertyMapping.getStore() != Property.Store.NO;
    }

    public T unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;

        if (resourcePropertyMapping.getStore() == Property.Store.NO) {
            // it is not stored, so don't bother with converting it
            return null;
        }

        String propertyName = resourcePropertyMapping.getPath().getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null) {
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
        return context.getResourceFactory().createProperty(value, resourcePropertyMapping);
    }

    /**
     * <p>Should the converter handle nulls? Handling nulls means should the
     * converter process nulls or not. Usually the converter will not
     * persist null values, but sometimes it might be needed
     * ({@link org.compass.core.marshall.MarshallingContext#handleNulls()}).
     *
     * <p>If a specific null value is configured with the {@link org.compass.core.mapping.ResourcePropertyMapping}
     * then the converter will always handle nulls and write it.
     *
     * @param context The marshalling context
     * @return <code>true</code> if the converter should handle null values
     */
    protected boolean handleNulls(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return resourcePropertyMapping.hasNullValue() || context.handleNulls();
    }

    /**
     * If the converter handle nulls, the value that will be stored in the
     * search engine for <code>null</code> values (during the marshall process). Uses
     * {@link org.compass.core.mapping.ResourcePropertyMapping#getNullValue()}.
     *
     * @param resourcePropertyMapping The resource proeprty mapping to get the null value from
     * @param context                 The marshalling context
     * @return Null value that will be inserted for <code>null</code>s.
     */
    protected String getNullValue(ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return resourcePropertyMapping.getNullValue();
    }

    /**
     * Does this value represents a null value. If the {@link org.compass.core.mapping.ResourcePropertyMapping}
     * is configured with a null value, then returns <code>true</code> if the null value equals the value read
     * from the index. If the resource property mapping is not configured with a null value, checks if this
     * it has the default value representing a null value.
     */
    protected boolean isNullValue(String value, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        if (value == null) {
            return true;
        }
        if (resourcePropertyMapping.hasNullValue()) {
            return resourcePropertyMapping.getNullValue().equals(value);
        }
        // the default null value is an empty string
        return value.length() == 0;
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
    protected void doSetBoost(Property property, T root, ResourcePropertyMapping resourcePropertyMapping,
                              MarshallingContext context) throws ConversionException {
        property.setBoost(resourcePropertyMapping.getBoost());
    }

    /**
     * Implementation calls {@link #toString(Object,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}
     * with <code>null</code> value for the context parameter.
     *
     * <p>Note, please don't override this method, please override {@link #doToString(Object,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}
     * to change the how the object gets marshalled into a String.
     */
    public String toString(T o, ResourcePropertyMapping resourcePropertyMapping) {
        return toString(o, resourcePropertyMapping, null);
    }

    /**
     * Implementation handle nulls and if the object is not null, delegates to
     * {@link #doToString(Object,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}.
     *
     * <p>Note, please don't override this method, please override {@link #doToString(Object,org.compass.core.mapping.ResourcePropertyMapping,org.compass.core.marshall.MarshallingContext)}
     * to change the how the object gets marshalled into a String.
     */
    public String toString(T o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        String sValue;
        if (o != null) {
            if (o instanceof String) {
                // if we got a string, try and convert it
                // if we got a class cast exception, just assume that this is the value that we want to store
                // (we expect number for example, but the use provided a String, assuming he knows what he wants)
                try {
                    sValue = doToString(o, resourcePropertyMapping, context);
                } catch (ClassCastException e) {
                    sValue = (String) o;
                }
            } else {
                sValue = doToString(o, resourcePropertyMapping, context);
            }
        } else {
            sValue = getNullValue(resourcePropertyMapping, context);
        }
        return sValue;
    }

    /**
     * Allows to override to toString operation. Default implementation calls the object <code>toString</code>.
     *
     * <p>Note, the marshalling context might be null.
     */
    protected String doToString(T o, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) {
        return o.toString();
    }

    /**
     * Calls {@link #fromString(String, org.compass.core.mapping.ResourcePropertyMapping, org.compass.core.marshall.MarshallingContext)}
     * with a null value for the context.
     */
    public T fromString(String str, ResourcePropertyMapping resourcePropertyMapping) throws ConversionException {
        return fromString(str, resourcePropertyMapping, null);
    }

    /**
     * Performs null checks (by calling {@link #isNullValue(String, org.compass.core.mapping.ResourcePropertyMapping, org.compass.core.marshall.MarshallingContext)})
     * and then calls {@link #doFromString(String, org.compass.core.mapping.ResourcePropertyMapping, org.compass.core.marshall.MarshallingContext)}
     * if the value is not <code>null</code>.
     */
    public T fromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException {
        if (isNullValue(str, resourcePropertyMapping, context)) {
            return null;
        }
        return doFromString(str, resourcePropertyMapping, context);
    }

    /**
     * Override the from String in order to un-marshall the String back into its object representation.
     */
    protected abstract T doFromString(String str, ResourcePropertyMapping resourcePropertyMapping, MarshallingContext context) throws ConversionException;

    /**
     * Return <code>false</code>. Specific parsers that can convert on query string should override this method
     * and return <code>true</code>.
     */
    public boolean canNormalize() {
        return false;
    }

    /**
     * By default for all converters simply return <code>null</code>.
     */
    public Property.Index suggestIndex() {
        return null;
    }

    public Property.TermVector suggestTermVector() {
        return null;
    }

    public Property.Store suggestStore() {
        return null;
    }

    /**
     * By default for all converters simply return <code>null</code>.
     */
    public Boolean suggestOmitNorms() {
        return null;
    }

    /**
     * By default for all converters simply return <code>null</code>.
     */
    public Boolean suggestOmitTf() {
        return null;
    }
}
