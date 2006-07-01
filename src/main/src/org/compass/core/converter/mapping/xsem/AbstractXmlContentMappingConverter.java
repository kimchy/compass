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

package org.compass.core.converter.mapping.xsem;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.xml.AliasedXmlObject;
import org.compass.core.xml.XmlObject;

/**
 * <p>A base class for xml content converters. The converters knows how to marshall
 * an {@link XmlObject} into xml, and un-marshall xml into an {@link AliasedXmlObject}.
 * <p/>
 * <p>Requires two methods to be implemented: {@link #toString(org.compass.core.xml.XmlObject)}
 * and {@link #fromString(String, String)}.
 *
 * @author kimchy
 */
public abstract class AbstractXmlContentMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();
        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }
        XmlObject xmlObject = (XmlObject) root;
        XmlContentMapping xmlContentMapping = (XmlContentMapping) mapping;
        String sValue = toString(xmlObject);
        String propertyName = xmlContentMapping.getPath();
        Property p = searchEngine.createProperty(propertyName, sValue, xmlContentMapping);
        resource.addProperty(p);

        return xmlContentMapping.getStore() != Property.Store.NO;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        XmlContentMapping xmlContentMapping = (XmlContentMapping) mapping;

        String propertyName = xmlContentMapping.getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || isNullValue(context, p.getStringValue())) {
            return null;
        }

        return fromString(resource.getAlias(), p.getStringValue());
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
     * Converts an {@link XmlObject} into an xml string.
     *
     * @param xmlObject The xml object to convert to an xml string
     * @return An xml string representation of the xml object
     * @throws ConversionException Failed to convert the xml object to an xml string
     */
    public abstract String toString(XmlObject xmlObject) throws ConversionException;

    /**
     * Converts an xml string into an {@link AliasedXmlObject}.
     *
     * @param alias The alias the aliases xml object is associated with
     * @param xml   The xml string that will be converted into an aliases xml object
     * @return The aliases xml object that is the restult of the xml parsed
     * @throws ConversionException
     */
    public abstract AliasedXmlObject fromString(String alias, String xml) throws ConversionException;
}
