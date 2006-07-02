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

import java.io.Reader;
import java.lang.reflect.Array;
import java.util.Iterator;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.xsem.XmlContentMapping;
import org.compass.core.mapping.xsem.XmlObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.spi.MultiResource;
import org.compass.core.xml.RawXmlObject;
import org.compass.core.xml.XmlObject;

/**
 * Responsible for converting {@link XmlObject} based on {@link XmlObjectMapping}.
 * <p/>
 * Note, that marshalls might create several resources, if the {@link XmlObjectMapping} has
 * an xpath expression associated with it.
 * <p/>
 *
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

        rootXmlObject = getActualXmlObject(rootXmlObject, xmlObjectMapping, context, resource);

        if (xmlObjectMapping.getXPath() != null) {
            XmlObject[] xmlObjects = XmlConverterUtils.select(rootXmlObject, xmlObjectMapping);
            if (xmlObjects == null || xmlObjects.length == 0) {
                throw new ConversionException("xpath [" + xmlObjectMapping.getXPath() + "] returned no value for alias [" +
                        xmlObjectMapping.getAlias() + "]");
            }
            boolean store = false;
            MultiResource multiResource = (MultiResource) resource;
            multiResource.clear();
            for (int i = 0; i < xmlObjects.length; i++) {
                multiResource.addResource();
                for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
                    Mapping m = (Mapping) it.next();
                    store |= m.getConverter().marshall(multiResource.currentResource(), xmlObjects[i], m, context);
                }
            }
            return store;
        } else {
            boolean store = false;
            for (Iterator it = xmlObjectMapping.mappingsIt(); it.hasNext();) {
                Mapping m = (Mapping) it.next();
                store |= m.getConverter().marshall(resource, rootXmlObject, m, context);
            }
            return store;
        }
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        XmlObjectMapping xmlObjectMapping = (XmlObjectMapping) mapping;
        if (xmlObjectMapping.getXmlContentMapping() == null) {
            return null;
        }
        XmlContentMapping xmlContentMapping = xmlObjectMapping.getXmlContentMapping();
        return xmlContentMapping.getConverter().unmarshall(resource, xmlContentMapping, context);
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context) throws ConversionException {
        SearchEngine searchEngine = context.getSearchEngine();

        XmlObjectMapping xmlObjectMapping = (XmlObjectMapping) resourceMapping;
        ResourcePropertyMapping[] ids = resourceMapping.getIdMappings();
        if (id instanceof XmlObject) {
            XmlObject rootXmlObject = getActualXmlObject((XmlObject) id, xmlObjectMapping, context, idResource);
            if (xmlObjectMapping.getXPath() != null) {
                XmlObject[] xmlObjects = XmlConverterUtils.select(rootXmlObject, xmlObjectMapping);
                if (xmlObjects == null || xmlObjects.length == 0) {
                    throw new ConversionException("xpath [" + xmlObjectMapping.getXPath() + "] returned no value for alias [" +
                            xmlObjectMapping.getAlias() + "]");
                }
                MultiResource multiResource = (MultiResource) idResource;
                multiResource.clear();
                for (int i = 0; i < xmlObjects.length; i++) {
                    multiResource.addResource();
                    for (int j = 0; j < ids.length; j++) {
                        ids[j].getConverter().marshall(multiResource.currentResource(), xmlObjects[i], ids[j], context);
                    }
                }
            } else {
                for (int i = 0; i < ids.length; i++) {
                    ids[i].getConverter().marshall(idResource, rootXmlObject, ids[i], context);
                }
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
        throw new ConversionException("Not supported");
    }

    private XmlObject getActualXmlObject(XmlObject rootXmlObject, XmlObjectMapping xmlObjectMapping, MarshallingContext context, Resource resource) {
        // in case it is an xml string value, convert it into an xml object
        if (rootXmlObject instanceof RawXmlObject) {
            Reader xml = ((RawXmlObject) rootXmlObject).getXml();
            XmlContentMapping xmlContentMapping = xmlObjectMapping.getXmlContentMapping();
            XmlContentMappingConverter xmlContentMappingConverter;
            if (xmlContentMapping != null) {
                xmlContentMappingConverter = (XmlContentMappingConverter) xmlContentMapping.getConverter();
            } else {
                xmlContentMappingConverter = (XmlContentMappingConverter) context.getConverterLookup().
                        lookupConverter(CompassEnvironment.Converter.DefaultTypeNames.Mapping.XML_CONTENT_MAPPING);
            }
            rootXmlObject = xmlContentMappingConverter.getXmlContentConverter().fromXml(resource.getAlias(), xml);
        }
        return rootXmlObject;
    }

}
