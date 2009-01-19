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

package org.compass.core.converter.xsem;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.engine.naming.PropertyPath;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.xml.XmlObject;

/**
 * A simple converter which uses the String value of {@link org.compass.core.xml.XmlObject#getValue()}.
 * It is the deafult converter associated with the value converter of {@link XmlPropertyMapping} if none
 * is specified. It can also be used as a base class for more specialized converters.
 *
 * @author kimchy
 */
public class SimpleXmlValueConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        XmlPropertyMapping xmlPropertyMapping = (XmlPropertyMapping) mapping;
        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(xmlPropertyMapping, context)) {
            return false;
        }
        XmlObject xmlObject = (XmlObject) root;
        String sValue = getNullValue(xmlPropertyMapping, context);
        if (root != null) {
            sValue = toString(xmlObject, xmlPropertyMapping);
        }
        PropertyPath path = xmlPropertyMapping.getPath();
        String propertyName = path == null ? null : path.getPath();
        if (propertyName == null) {
            if (xmlObject == null) {
                // nothing we can do here, no name, no nothing...
                return false;
            }
            propertyName = xmlObject.getName();
        }
        Property p = context.getResourceFactory().createProperty(propertyName, sValue, xmlPropertyMapping);
        doSetBoost(p, root, xmlPropertyMapping, context);
        resource.addProperty(p);

        return xmlPropertyMapping.getStore() != Property.Store.NO;
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
     * A simple extension point that allows to set the boost value for the created {@link Property}.
     * <p/>
     * The default implemenation uses the statically defined boost value in the mapping definition
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
     * Default implementation of toString, simply calls {@link org.compass.core.xml.XmlObject#getValue()}.
     */
    public String toString(XmlObject xmlObject, ResourcePropertyMapping resourcePropertyMapping) {
        return xmlObject.getValue();
    }

    /**
     * Not supported operation.
     */
    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported");
    }
}
