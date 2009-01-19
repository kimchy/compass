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

import org.compass.core.Resource;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.Converter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.xsem.XmlPropertyMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.xml.XmlObject;

/**
 * Responsible for converting {@link XmlObject} based on {@link XmlPropertyMapping}.
 * Uses the {@link XmlPropertyMapping} value converter to actually convert the given
 * value.
 * <p/>
 * The converter executes the given xpath expression associated with {@link XmlPropertyMapping}
 * and for each xml object will use the value converter to marshall it.
 * <p/>
 * Note, that un-marshalling is not supported.
 *
 * @author kimchy
 */
public class XmlPropertyMappingConverter implements Converter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        if (root == null) {
            return false;
        }

        XmlObject xmlObject = (XmlObject) root;
        XmlPropertyMapping xmlPropertyMapping = (XmlPropertyMapping) mapping;

        XmlObject[] xmlObjects = XmlConverterUtils.select(xmlObject, xmlPropertyMapping);
        if (xmlObjects == null) {
            return false;
        }
        boolean store = false;
        for (int i = 0; i < xmlObjects.length; i++) {
            store |= xmlPropertyMapping.getValueConverter().marshall(resource, xmlObjects[i], xmlPropertyMapping, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use xml-content mapping");
    }
}
