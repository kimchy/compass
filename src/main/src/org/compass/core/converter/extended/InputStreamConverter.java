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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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
public class InputStreamConverter implements Converter {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1;

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ResourcePropertyMapping resourcePropertyMapping = (ResourcePropertyMapping) mapping;
        ResourceFactory resourceFactory = context.getResourceFactory();

        // don't save a null value
        if (root == null) {
            return false;
        }

        String propertyName = resourcePropertyMapping.getPath().getPath();

        InputStream input = (InputStream) root;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
        } catch (IOException e) {
            throw new ConversionException("Failed to read input stream for [" + propertyName + "]", e);
        }

        Property p = resourceFactory.createProperty(propertyName, output.toByteArray(), resourcePropertyMapping.getStore());
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

        return new ByteArrayInputStream(p.getBinaryValue());
    }

}
