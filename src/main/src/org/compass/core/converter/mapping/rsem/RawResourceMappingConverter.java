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

package org.compass.core.converter.mapping.rsem;

import java.lang.reflect.Array;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.spi.InternalResource;

/**
 * @author kimchy
 */
public class RawResourceMappingConverter implements ResourceMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        Resource rootResource = (Resource) root;
        resource.copy(rootResource);
        ((InternalResource) resource).addUID();
        return true;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        return resource;
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        ResourceFactory resourceFactory = context.getResourceFactory();

        Mapping[] ids = resourceMapping.getIdMappings();
        if (id instanceof Resource) {
            for (Mapping id1 : ids) {
                Resource rId = (Resource) id;
                idResource.addProperty(rId.getProperty(id1.getPath().getPath()));
            }
        } else if (id instanceof Object[]) {
            if (Array.getLength(id) != ids.length) {
                throw new ConversionException("Trying to load resource with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            if (Property.class.isAssignableFrom(id.getClass().getComponentType())) {
                for (int i = 0; i < ids.length; i++) {
                    idResource.addProperty((Property) Array.get(id, i));
                }
            } else {
                for (int i = 0; i < ids.length; i++) {
                    idResource.addProperty(resourceFactory.createProperty(ids[i].getPath().getPath(), Array.get(id, i).toString(),
                            Property.Store.YES, Property.Index.NOT_ANALYZED));
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
                idResource.addProperty(resourceFactory.createProperty(ids[0].getPath().getPath(), id.toString(), Property.Store.YES,
                        Property.Index.NOT_ANALYZED));
            }
        }

        ((InternalResource) idResource).addUID();

        return true;
    }

    public Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context)
            throws ConversionException {
        Mapping[] ids = resourceMapping.getIdMappings();
        Object[] idsValues = new Object[ids.length];
        if (id instanceof Resource) {
            Resource resource = (Resource) id;
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = resource.getProperty(ids[i].getPath().getPath());
            }
        } else {
            throw new ConversionException("Object [" + id + "] not supported");
        }
        return idsValues;
    }
}
