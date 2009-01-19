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

package org.compass.core.converter.mapping.json;

import java.lang.reflect.Array;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.config.CompassEnvironment;
import org.compass.core.converter.ConversionException;
import org.compass.core.converter.json.JsonFullPathHolder;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.json.JsonObject;
import org.compass.core.json.RawJsonObject;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.json.JsonContentMapping;
import org.compass.core.mapping.json.RootJsonObjectMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.spi.InternalResource;

/**
 * @author kimchy
 */
public class RootJsonObjectMappingConverter extends AbstractJsonObjectMappingConverter implements ResourceMappingConverter {

    public boolean marshall(Resource resource, Object root, Mapping mapping, MarshallingContext context) throws ConversionException {
        // no need to marshall if it is null
        if (root == null && !context.handleNulls()) {
            return false;
        }

        if (root instanceof Resource) {
            Resource rootResource = (Resource) root;
            resource.copy(rootResource);
            ((InternalResource) resource).addUID();
            return true;
        }

        RootJsonObjectMapping jsonObjectMapping = (RootJsonObjectMapping) mapping;
        JsonObject jsonObject = (JsonObject) root;

        jsonObject = getActualJsonObject(jsonObject, jsonObjectMapping, context, resource);

        // initialize full path
        JsonFullPathHolder fullPathHolder = new JsonFullPathHolder();
        context.setAttribute(JsonFullPathHolder.CONTEXT_KEY, fullPathHolder);

        boolean store = doMarshall(resource, jsonObject, jsonObjectMapping, context);

        JsonContentMapping contentMapping = jsonObjectMapping.getContentMapping();
        if (contentMapping != null) {
            Object content = jsonObject;
            if (root instanceof RawJsonObject) {
                content = ((RawJsonObject) root).getJson();
            }
            contentMapping.getConverter().marshall(resource, content, contentMapping, context);
        }
        ((InternalResource) resource).addUID();
        return store;
    }

    public Object unmarshall(Resource resource, Mapping mapping, MarshallingContext context) throws ConversionException {
        RootJsonObjectMapping jsonObjectMapping = (RootJsonObjectMapping) mapping;
        if (jsonObjectMapping.getContentMapping() == null) {
            return null;
        }
        JsonContentMapping contentMapping = jsonObjectMapping.getContentMapping();
        return contentMapping.getConverter().unmarshall(resource, contentMapping, context);
    }

    public boolean marshallIds(Resource idResource, Object id, ResourceMapping resourceMapping, MarshallingContext context) throws ConversionException {
        ResourceFactory resourceFactory = context.getResourceFactory();

        RootJsonObjectMapping jsonObjectMapping = (RootJsonObjectMapping) resourceMapping;
        Mapping[] ids = resourceMapping.getIdMappings();
        if (id instanceof JsonObject) {
            JsonObject jsonObject = getActualJsonObject((JsonObject) id, jsonObjectMapping, context, idResource);
            for (Mapping id1 : ids) {
                Object value = jsonObject.opt(id1.getName());
                if (jsonObject.isNullValue(value)) {
                    throw new ConversionException("Trying to load resource with id name [" + id1.getName() + "] null");
                }
                id1.getConverter().marshall(idResource, value, id1, context);
            }
        } else if (id instanceof Resource) {
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

    public Object[] unmarshallIds(Object id, ResourceMapping resourceMapping, MarshallingContext context) throws ConversionException {
        throw new ConversionException("Not supported, please use json-content mapping");
    }

    private JsonObject getActualJsonObject(JsonObject jsonObject, RootJsonObjectMapping jsonObjectMapping, MarshallingContext context, Resource resource) {
        // in case it is an xml string value, convert it into an xml object
        if (jsonObject instanceof RawJsonObject) {
            String json = ((RawJsonObject) jsonObject).getJson();
            JsonContentMapping jsonContentMapping = jsonObjectMapping.getContentMapping();
            JsonContentMappingConverter jsonContentMappingConverter;
            if (jsonContentMapping != null) {
                jsonContentMappingConverter = (JsonContentMappingConverter) jsonContentMapping.getConverter();
            } else {
                jsonContentMappingConverter = (JsonContentMappingConverter) context.getConverterLookup().
                        lookupConverter(CompassEnvironment.Converter.DefaultTypeNames.Mapping.JSON_CONTENT_MAPPING);
            }
            jsonObject = jsonContentMappingConverter.getContentConverter().fromJSON(resource.getAlias(), json);
        }
        return jsonObject;
    }

}