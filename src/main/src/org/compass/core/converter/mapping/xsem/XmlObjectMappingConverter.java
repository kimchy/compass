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

import java.util.Iterator;
import java.lang.reflect.Array;

import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.converter.ConversionException;
import org.compass.core.Resource;
import org.compass.core.Property;
import org.compass.core.xml.XmlObject;
import org.compass.core.engine.SearchEngine;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;

/**
 * @author kimchy
 */
public class XmlObjectMappingConverter implements ResourceMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        // no need to marshall if it is null
        if (root == null && !context.handleNulls()) {
            return false;
        }
        XmlObjectMapping xmlObjectMapping = (XmlObjectMapping) mapping;
        XmlObject rootXmlObject = (XmlObject) root;
        if (xmlObjectMapping.getXPath() != null) {
            XmlObject[] xmlObjects = XmlConverterUtils.select(rootXmlObject, xmlObjectMapping);
            if (xmlObjects.length == 0) {
                throw new ConversionException("xpath [" + xmlObjectMapping.getXPath() + "] returned no value for alias [" +
                        xmlObjectMapping.getAlias() + "]");
            }
            if (xmlObjects.length > 1) {
                throw new ConversionException("xpath [" + xmlObjectMapping.getXPath() + "] returned multiple hits for alias [" +
                        xmlObjectMapping.getAlias() + "], it" +
                        " is not allowed since it is marshalled into one resource, you will have to explicitly iterate" +
                        " throug the xpath expression and save each XmlObject");
            }
            rootXmlObject = xmlObjects[0];
        }
        boolean store = false;
        for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
            Mapping m = (Mapping) it.next();
            store |= m.getConverter().marshall(resource, rootXmlObject, m, context);
        }
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("should not be called");
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context) throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();

        ResourcePropertyMapping[] ids = resourceMapping.getIdMappings();
        if (id instanceof XmlObject) {
            for (int i = 0; i < ids.length; i++) {
                ids[i].getConverter().marshall(idResource, id, ids[i], context);
            }
        } else if (id instanceof Resource) {
            for (int i = 0; i < ids.length; i++) {
                Resource rId = (Resource) id;
                idResource.addProperty(rId.getProperty(ids[i].getPath()));
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load resource with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            if (id.getClass().getComponentType().isAssignableFrom(Property.class)) {
                for (int i = 0; i < ids.length; i++) {
                    idResource.addProperty((Property) Array.get(id, i));
                }
            } else {
                for (int i = 0; i < ids.length; i++) {
                    idResource.addProperty(searchEngine.createProperty(ids[i].getPath(), Array.get(id, i).toString(),
                            Property.Store.YES, Property.Index.UN_TOKENIZED));
                }
            }
        } else {
            if (ids.length != 1) {
                throw new ConversionException(
                        "Trying to load resource which has more than one id mappings with only one id value");
            }
            if (id instanceof Property) {
                idResource.addProperty((Property) id);
            } else {
                idResource.addProperty(searchEngine.createProperty(ids[0].getPath(), id.toString(), Property.Store.YES,
                        Property.Index.UN_TOKENIZED));
            }
        }
        return true;
    }

    public Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("should not be called");
    }
}
