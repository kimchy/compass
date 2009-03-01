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

package org.compass.core.test.map;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.converter.mapping.SuggestManagedIdConverter;
import org.compass.core.engine.naming.PropertyNamingStrategy;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ManagedId;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.util.StringUtils;

/**
 * This is a sample converter that can handle {@link Map} and save dynamic properties into the index.
 * <p/>
 * Saves dynamic properties, with the map keys as the properties names and map values as the property
 * values. Uses the Object toString to convert the key and values objects to strings.
 * <p/>
 * If the converter will support unmarshalling, than assumes that the key and the value are of type
 * {@link String}. Saves the keys as comma delimited string under an internal property, and the
 * values the same under a different property.
 * <p/>
 * Note, that using this converter might affect the Class mapping that uses the Map as a property.
 * If another meta-data is mapped to a name that might be one of the dynamic property names, it must
 * be set to have the intenal id always generated. It also applies to a class that has this class as
 * component mapping.
 * <p/>
 * This Converter is provided as a baseline for different maps converters that might hold different
 * object types than Strings.
 *
 * @author kimchy
 */
public class MapConverter implements Converter, CompassConfigurable, SuggestManagedIdConverter {

    private boolean supportUnmarshall = true;

    public void configure(CompassSettings settings) throws CompassException {
        supportUnmarshall = settings.getSettingAsBoolean("supportUnmarshall", true);
    }

    public ManagedId suggestManagedId() {
        return ManagedId.FALSE;
    }

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (root == null && !context.handleNulls()) {
            return false;
        }

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        ResourceFactory resourceFactory = context.getResourceFactory();

        Map map = (Map) root;
        for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Property p = resourceFactory.createProperty(entry.getKey().toString(), entry.getValue().toString(),
                    resourcePropertyMapping.getStore(), resourcePropertyMapping.getIndex(), resourcePropertyMapping.getTermVector());
            p.setBoost(resourcePropertyMapping.getBoost());
            resource.addProperty(p);
        }

        if (supportUnmarshall) {
            StringBuffer keys = new StringBuffer();
            StringBuffer values = new StringBuffer();
            for (Iterator it = map.entrySet().iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                keys.append(entry.getKey().toString()).append(",");
                values.append(entry.getValue().toString()).append(",");
            }
            PropertyNamingStrategy propertyNamingStrategy =
                    context.getSession().getCompass().getSearchEngineFactory().getPropertyNamingStrategy();
            // save keys (under an internal name)
            String keyPath = propertyNamingStrategy.buildPath(resourcePropertyMapping.getPath(), "keys").getPath();
            Property p = resourceFactory.createProperty(keyPath, keys.toString(),
                    Property.Store.YES, Property.Index.NOT_ANALYZED);
            resource.addProperty(p);
            // save values (under an internal name)
            String valuePath = propertyNamingStrategy.buildPath(resourcePropertyMapping.getPath(), "values").getPath();
            p = resourceFactory.createProperty(valuePath, values.toString(), Property.Store.YES, Property.Index.NOT_ANALYZED);
            resource.addProperty(p);
        }

        return true;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (!supportUnmarshall) {
            return null;
        }

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;

        PropertyNamingStrategy propertyNamingStrategy =
                context.getSession().getCompass().getSearchEngineFactory().getPropertyNamingStrategy();
        // save keys (under an internal name)
        String keyPath = propertyNamingStrategy.buildPath(resourcePropertyMapping.getPath(), "keys").getPath();
        String strKeys = resource.getValue(keyPath);
        if (strKeys == null) {
            return null;
        }
        String valuePath = propertyNamingStrategy.buildPath(resourcePropertyMapping.getPath(), "values").getPath();
        String strValues = resource.getValue(valuePath);
        String[] keys = StringUtils.tokenizeToStringArray(strKeys, ",");
        String[] values = StringUtils.tokenizeToStringArray(strValues, ",");
        if (keys.length != values.length) {
            throw new ConversionException("Keys with length [" + keys.length + "] does not match values length ["
                    + values.length + "]");
        }
        Map map = new HashMap();
        for (int i = 0; i < keys.length; i++) {
            map.put(keys[i], values[i]);
        }
        return map;
    }

    public boolean isSupportUnmarshall() {
        return supportUnmarshall;
    }

    public void setSupportUnmarshall(boolean supportUnmarshall) {
        this.supportUnmarshall = supportUnmarshall;
    }
}
