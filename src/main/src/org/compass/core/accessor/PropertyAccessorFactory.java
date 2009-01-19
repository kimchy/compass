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

package org.compass.core.accessor;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.ConfigurationException;
import org.compass.core.mapping.MappingException;
import org.compass.core.util.ClassUtils;

/**
 * A factory that creates a {@link PropertyAccessor}.
 * <p/>
 * Acts as a registry for property accessors, with two default implementations,
 * {@link BasicPropertyAccessor} registered under "property", and {@link DirectPropertyAccessor}
 * registered under "field".
 * <p/>
 * Allows for configuration of new property accessors, and register them under new/same names.
 * Configuration is using the {@link org.compass.core.config.CompassEnvironment.PropertyAccessor}.
 *
 * @author kimchy
 */
public class PropertyAccessorFactory implements CompassConfigurable {

    private static final Log log = LogFactory.getLog(PropertyAccessorFactory.class);

    private Map<String, PropertyAccessor> propertyAccessorsRegistry = new HashMap<String, PropertyAccessor>();

    /**
     * Configures the property accessor registry, using the two default ones (field and property),
     * and any external registered ones.
     */
    public void configure(CompassSettings settings) throws CompassException {
        PropertyAccessor propertyAccessor = new BasicPropertyAccessor();
        if (propertyAccessor instanceof CompassConfigurable) {
            ((CompassConfigurable) propertyAccessor).configure(settings);
        }
        propertyAccessorsRegistry.put("property", propertyAccessor);

        propertyAccessor = new DirectPropertyAccessor();
        if (propertyAccessor instanceof CompassConfigurable) {
            ((CompassConfigurable) propertyAccessor).configure(settings);
        }
        propertyAccessorsRegistry.put("field", propertyAccessor);

        Map<String, CompassSettings> paGroups = settings.getSettingGroups(CompassEnvironment.PropertyAccessor.PREFIX);
        for (Map.Entry<String, CompassSettings> entry : paGroups.entrySet()) {
            String paName = entry.getKey();
            if (log.isDebugEnabled()) {
                log.debug("Property Accessor [" + paName + "] building...");
            }
            CompassSettings paSettings = entry.getValue();
            propertyAccessor = (PropertyAccessor) paSettings.getSettingAsInstance(CompassEnvironment.PropertyAccessor.TYPE);
            if (propertyAccessor == null) {
                throw new ConfigurationException("Must define type for property accessor [" + paName + "]");
            }
            if (propertyAccessor instanceof CompassConfigurable) {
                ((CompassConfigurable) propertyAccessor).configure(settings);
            }
            propertyAccessorsRegistry.put(paName, propertyAccessor);
        }
    }

    /**
     * Returns a new property accessor that match the given type. If the type is <code>null</code>
     * will return the {@link BasicPropertyAccessor}. If the type can be found in the pre
     * configured registry of property accessors, will return it. If nothing is found in
     * the registry will try to instanciate it using the type as the class name.
     */
    public PropertyAccessor getPropertyAccessor(String type, CompassSettings settings) throws MappingException {

        if (type == null) {
            PropertyAccessor propertyAccessor =
                    propertyAccessorsRegistry.get(CompassEnvironment.PropertyAccessor.DEFAULT_GROUP);
            if (propertyAccessor != null) {
                return propertyAccessor;
            }
            return propertyAccessorsRegistry.get("property");
        }

        PropertyAccessor propertyAccessor = propertyAccessorsRegistry.get(type);
        if (propertyAccessor != null) {
            return propertyAccessor;
        }

        Class accessorClass;
        try {
            accessorClass = ClassUtils.forName(type, settings.getClassLoader());
        } catch (ClassNotFoundException cnfe) {
            throw new MappingException("Could not find PropertyAccessor class [" + type + "] or bounded under name [" + type + "]", cnfe);
        }
        try {
            propertyAccessor = (PropertyAccessor) accessorClass.newInstance();
            if (propertyAccessor instanceof CompassConfigurable) {
                ((CompassConfigurable) propertyAccessor).configure(settings);
            }
            return propertyAccessor;
        } catch (Exception e) {
            throw new MappingException("Could not instantiate PropertyAccessor class [" + type + "]", e);
        }

    }
}
