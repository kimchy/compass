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

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.osem.ClassPropertyMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.marshall.MarshallingContext;

/**
 * @author kimchy
 */
public class ClassPropertyMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context)
            throws ConversionException {

        ClassPropertyMapping aMapping = (ClassPropertyMapping) mapping;
        boolean store = false;
        boolean disableInternalMappings = false;
        if (context.getAttribute(ClassMappingConverter.DISABLE_INTERNAL_MAPPINGS) != null) {
            disableInternalMappings = true;
        }
        for (Iterator it = aMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            if (disableInternalMappings) {
                if (context.getPropertyNamingStrategy().isInternal(m.getPath().getPath())) {
                    continue;
                }
            }
            store |= m.getConverter().marshall(resource, root, m, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        ClassPropertyMapping aMapping = (ClassPropertyMapping) mapping;
        // if we have not set an id property, it means that we do not know how to
        // unmarshall this property, simply return null.
        if (!aMapping.isIdPropertySet()) {
            return null;
        }
        ClassPropertyMetaDataMapping m = aMapping.getIdMapping();
        return m.getConverter().unmarshall(resource, m, context);
    }
}
