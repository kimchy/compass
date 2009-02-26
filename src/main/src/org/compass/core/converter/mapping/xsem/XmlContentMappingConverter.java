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

package org.compass.core.converter.mapping.xsem;

import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.xsem.PoolXmlContentConverterWrapper;
import org.compass.core.converter.xsem.PrototypeXmlContentConverterWrapper;
import org.compass.core.converter.xsem.SingletonXmlContentConverterWrapper;
import org.compass.core.converter.xsem.XmlContentConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.util.ClassUtils;
import org.compass.core.xml.XmlObject;

/**
 * Handles xml content mapping definition. Saves the raw xml content from {@link XmlObject}
 * into the search engine, and unmarshalls raw xml content into {@link XmlObject}.
 *
 * <p>In order to perform the conversion between {@link XmlObject} and raw xml, the converter
 * uses a {@link XmlContentConverter} implementation. There is no default one, and it must
 * be cofigured for this converter to funtion using {@link CompassEnvironment.Xsem.XmlContent#TYPE).
 *
 * <p>{@link XmlContentConverter} implementations are wrapped by one of the three built in strategies:
 * {@link PrototypeXmlContentConverterWrapper}, {@link PoolXmlContentConverterWrapper},
 * {@link SingletonXmlContentConverterWrapper}, or a user provided fully qualified class name.
 *
 * @author kimchy
 */
public class XmlContentMappingConverter implements Converter, CompassConfigurable {

    private static final Log log = LogFactory.getLog(XmlContentMappingConverter.class);

    private XmlContentConverter xmlContentConverter;

    public void configure(CompassSettings settings) throws CompassException {
        String wrapper = settings.getGloablSettings().getSetting(CompassEnvironment.Xsem.XmlContent.WRAPPER, CompassEnvironment.Xsem.XmlContent.WRAPPER_PROTOTYPE);
        if (log.isDebugEnabled()) {
            String type = settings.getGloablSettings().getSetting(CompassEnvironment.Xsem.XmlContent.TYPE);
            log.debug("Using XSEM content converter [" + type + "] with wrapper [" + wrapper + "]");
        }
        if (CompassEnvironment.Xsem.XmlContent.WRAPPER_PROTOTYPE.equals(wrapper)) {
            xmlContentConverter = new PrototypeXmlContentConverterWrapper();
        } else if (CompassEnvironment.Xsem.XmlContent.WRAPPER_SINGLETON.equals(wrapper)) {
            xmlContentConverter = new SingletonXmlContentConverterWrapper();
        } else if (CompassEnvironment.Xsem.XmlContent.WRAPPER_POOL.equals(wrapper)) {
            xmlContentConverter = new PoolXmlContentConverterWrapper();
        } else {
            try {
                xmlContentConverter = (XmlContentConverter) ClassUtils.forName(wrapper, settings.getClassLoader()).newInstance();
            } catch (Exception e) {
                throw new ConfigurationException("Failed to create wrapper [" + wrapper +
                        "], either the class name or the short name for existing wrappers is wrong", e);
            }
        }
        if (xmlContentConverter instanceof CompassConfigurable) {
            ((CompassConfigurable) xmlContentConverter).configure(settings);
        }
    }

    public XmlContentConverter getXmlContentConverter() {
        return this.xmlContentConverter;
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }
        XmlObject xmlObject = (XmlObject) root;
        XmlContentMapping xmlContentMapping = (XmlContentMapping) mapping;
        String sValue = xmlContentConverter.toXml(xmlObject);
        String propertyName = xmlContentMapping.getPath().getPath();
        Property p = context.getResourceFactory().createProperty(propertyName, sValue, xmlContentMapping);
        resource.addProperty(p);

        return xmlContentMapping.getStore() != Property.Store.NO;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        XmlContentMapping xmlContentMapping = (XmlContentMapping) mapping;

        String propertyName = xmlContentMapping.getPath().getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || isNullValue(context, p.getStringValue())) {
            return null;
        }

        return xmlContentConverter.fromXml(resource.getAlias(), new StringReader(p.getStringValue()));
    }

    /**
     * Should the converter handle nulls? Handling nulls means should the
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
     * Is the value read from the search engine is a <code>null</code> value
     * during the unmarshall process.
     *
     * @param context The marshalling context
     * @param value   The value to check for <code>null</code> value.
     * @return <code>true</code> if the value represents a null value.
     */
    protected boolean isNullValue(MarshallingContext context, String value) {
        return context.getResourceFactory().isNullValue(value);
    }
}
