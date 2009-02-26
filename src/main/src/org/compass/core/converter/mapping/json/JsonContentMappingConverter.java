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
import org.compass.core.converter.json.JsonContentConverter;
import org.compass.core.json.JsonObject;
import org.compass.core.json.impl.converter.DefaultJSONContentConverterImpl;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.util.ClassUtils;

/**
 * @author kimchy
 */
public class JsonContentMappingConverter implements Converter, CompassConfigurable {

    private static final Log log = LogFactory.getLog(JsonContentMappingConverter.class);

    private JsonContentConverter contentConverter;

    public void configure(CompassSettings settings) throws CompassException {
        String type = settings.getGloablSettings().getSetting(CompassEnvironment.Jsem.JsonContent.TYPE);
        if (type == null) {
            type = DefaultJSONContentConverterImpl.class.getName();
        }

        if (log.isDebugEnabled()) {
            log.debug("Usign Json content converter [" + type + "]");
        }

        try {
            contentConverter = (JsonContentConverter) ClassUtils.forName(type, settings.getClassLoader()).newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("Failed to create jsonContent [" + type + "]", e);
        }
        if (contentConverter instanceof CompassConfigurable) {
            ((CompassConfigurable) contentConverter).configure(settings);
        }
    }

    public JsonContentConverter getContentConverter() {
        return contentConverter;
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }
        JsonContentMapping jsonContentMapping = (JsonContentMapping) mapping;

        String sValue;
        if (root instanceof String) {
            sValue = (String) root;
        } else {
            JsonObject jsonObject = (JsonObject) root;
            sValue = contentConverter.toJSON(jsonObject);
        }

        String propertyName = jsonContentMapping.getPath().getPath();
        Property p = context.getResourceFactory().createProperty(propertyName, sValue, jsonContentMapping);
        resource.addProperty(p);

        return jsonContentMapping.getStore() != Property.Store.NO;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        JsonContentMapping jsonContentMapping = (JsonContentMapping) mapping;

        String propertyName = jsonContentMapping.getPath().getPath();
        Property p = resource.getProperty(propertyName);

        // don't set anything if null
        if (p == null || isNullValue(context, p.getStringValue())) {
            return null;
        }

        return contentConverter.fromJSON(resource.getAlias(), p.getStringValue());
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