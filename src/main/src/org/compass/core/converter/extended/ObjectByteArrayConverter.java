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

package org.compass.core.converter.extended;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class ObjectByteArrayConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        ResourceFactory resourceFactory = context.getResourceFactory();

        // don't save a null value if the context does not states so
        if (root == null && !handleNulls(context)) {
            return false;
        }

        String propertyName = resourcePropertyMapping.getPath().getPath();
        Byte[] oValue = (Byte[]) root;
        byte value[] = new byte[oValue.length];
        for (int i = 0; i < oValue.length; i++) {
            value[i] = oValue[i].byteValue();
        }
        Property p = resourceFactory.createProperty(propertyName, value, resourcePropertyMapping.getStore());
        p.setBoost(resourcePropertyMapping.getBoost());
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
        if (p == null) {
            return null;
        }

        byte[] value = p.getBinaryValue();
        Byte[] oValue = new Byte[value.length];
        for (int i = 0; i < value.length; i++) {
            oValue[i] = new Byte(value[i]);
        }
        return oValue;
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
}
