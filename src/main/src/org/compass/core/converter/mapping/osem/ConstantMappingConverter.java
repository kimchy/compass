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

package org.compass.core.converter.mapping.osem;

import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ConstantMetaDataMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */

public class ConstantMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {
        ConstantMetaDataMapping cm = (ConstantMetaDataMapping) mapping;
        ResourceFactory resourceFactory = context.getResourceFactory();
        boolean stored = false;
        for (Iterator it = cm.metaDataValuesIt(); it.hasNext();) {
            Property p = resourceFactory.createProperty((String) it.next(), cm);
            p.setBoost(cm.getBoost());
            resource.addProperty(p);
            stored |= cm.getStore() != Property.Store.NO;
        }
        return stored;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        // Nothing to do here
        return null;
    }
}
