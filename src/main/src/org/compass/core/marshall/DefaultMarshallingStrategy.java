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

package org.compass.core.marshall;

import java.lang.reflect.Array;
import java.util.HashMap;

import org.compass.core.CompassException;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.accessor.Setter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.engine.SearchEngine;
import org.compass.core.impl.InternalCompassSession;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;

/**
 * @author kimchy
 */
public class DefaultMarshallingStrategy implements MarshallingStrategy, MarshallingContext {

    private CompassMapping mapping;

    private SearchEngine searchEngine;

    private ConverterLookup converterLookup;

    private InternalCompassSession session;

    private HashMap attributes = new HashMap();

    private HashMap nullValuesPath = new HashMap();

    public DefaultMarshallingStrategy(CompassMapping mapping, SearchEngine searchEngine,
            ConverterLookup converterLookup, InternalCompassSession session) {
        this.mapping = mapping;
        this.searchEngine = searchEngine;
        this.converterLookup = converterLookup;
        this.session = session;
    }

    public Resource marshallIds(String alias, Object id) {
        Mapping m = mapping.getRootMappingByAlias(alias);
        if (m == null) {
            throw new MarshallingException("Failed to find mapping definitions for alias [" + alias + "]");
        }
        if (m instanceof ClassMapping) {
            return marshallIds((ClassMapping) m, id);
        } else {
            return marshalIds((RawResourceMapping) m, id);
        }
    }

    public Resource marshalIds(RawResourceMapping resourceMapping, Object id) {
        Resource idResource = searchEngine.createResource(resourceMapping.getAlias());
        ResourcePropertyMapping[] ids = resourceMapping.getIdMappings();
        if (id instanceof Resource) {
            for (int i = 0; i < ids.length; i++) {
                Resource rId = (Resource) id;
                idResource.addProperty(rId.getProperty(ids[i].getPath()));
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new MarshallingException("Trying to load resource with [" + Array.getLength(id)
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
                throw new MarshallingException(
                        "Trying to load resource which has more than one id mappings with only one id value");
            }
            if (id instanceof Property) {
                idResource.addProperty((Property) id);
            } else {
                idResource.addProperty(searchEngine.createProperty(ids[0].getPath(), id.toString(), Property.Store.YES,
                        Property.Index.UN_TOKENIZED));
            }
        }
        return idResource;
    }

    public Resource marshallIds(Class clazz, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.findRootMappingByClass(clazz);
        if (classMapping == null) {
            throw new MarshallingException("Failed to find class mapping for class [" + clazz.getName() + "]");
        }
        return marshallIds(classMapping, id);
    }

    public void marshallIds(Resource resource, String alias, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.getRootMappingByAlias(alias);
        marshallIds(resource, classMapping, id);
    }

    public Resource marshallIds(ClassMapping classMapping, Object id) {
        Resource idResource = searchEngine.createResource(classMapping.getAlias());
        marshallIds(idResource, classMapping, id);
        return idResource;
    }

    public void marshallIds(Object root, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.getRootMappingByClass(root.getClass());
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        Object[] idsValues = unmarshallIds(classMapping, id);
        for (int i = 0; i < idsValues.length; i++) {
            setId(root, idsValues[i], (ClassPropertyMetaDataMapping) ids[i]);
        }
    }

    public Object[] unmarshallIds(String alias, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.getRootMappingByAlias(alias);
        return unmarshallIds(classMapping, id);
    }

    public Object[] unmarshallIds(Class clazz, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.findRootMappingByClass(clazz);
        return unmarshallIds(classMapping, id);
    }

    public Object[] unmarshallIds(ClassMapping classMapping, Object id) {
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        Object[] idsValues = new Object[ids.length];
        if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (int i = 0; i < ids.length; i++) {
                ClassPropertyMetaDataMapping classPropertyMetaDataMapping = (ClassPropertyMetaDataMapping) ids[i];
                idsValues[i] = classPropertyMetaDataMapping.getGetter().get(id);
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new MarshallingException("Trying to load class with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                idsValues[i] = Array.get(id, i);
            }
        } else if (ids.length == 1) {
            idsValues[0] = id;
        } else {
            String type = id.getClass().getName();
            throw new MarshallingException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }
        return idsValues;
    }

    private void setId(Object root, Object id, ClassPropertyMetaDataMapping mdMapping) {
        Setter setter = mdMapping.getSetter();
        setter.set(root, id);
    }

    public void marshallIds(Resource idResource, ClassMapping classMapping, Object id) {
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        if (classMapping.getClazz().isAssignableFrom(id.getClass())) {
            // the object is the key
            for (int i = 0; i < ids.length; i++) {
                ClassPropertyMetaDataMapping classPropertyMetaDataMapping = (ClassPropertyMetaDataMapping) ids[i];
                convertId(idResource, classPropertyMetaDataMapping.getGetter().get(id), classPropertyMetaDataMapping);
            }
        } else if (id.getClass().isArray()) {
            if (Array.getLength(id) != ids.length) {
                throw new MarshallingException("Trying to load class with [" + Array.getLength(id)
                        + "] while has ids mappings of [" + ids.length + "]");
            }
            for (int i = 0; i < ids.length; i++) {
                convertId(idResource, Array.get(id, i), (ClassPropertyMetaDataMapping) ids[i]);
            }
        } else if (ids.length == 1) {
            convertId(idResource, id, (ClassPropertyMetaDataMapping) ids[0]);
        } else {
            String type = id.getClass().getName();
            throw new MarshallingException("Cannot marshall ids, not supported id object type [" + type
                    + "] and value [" + id + "], or you have not defined ids in the mapping files");
        }
    }

    private void convertId(Resource resource, Object root, ClassPropertyMetaDataMapping mdMapping) {
        if (root == null) {
            throw new MarshallingException("Trying to unmarshall a null id [" + mdMapping.getName() + "]");
        }
        mdMapping.getConverter().marshall(resource, root, mdMapping, this);
    }

    public Object[] unmarshallIds(Resource resource, ClassMapping classMapping) {
        ResourcePropertyMapping[] idMappings = classMapping.getIdMappings();
        Object[] ids = new Object[idMappings.length];
        for (int i = 0; i < idMappings.length; i++) {
            ids[i] = idMappings[i].getConverter().unmarshall(resource, idMappings[i], this);
            if (ids[i] == null) {
                // the reference was not marshalled
                return null;
            }
        }
        return ids;
    }

    public Resource marshall(String alias, Object root) {
        ClassMapping classMapping = (ClassMapping) mapping.getRootMappingByAlias(alias);
        if (classMapping == null) {
            throw new MarshallingException("No class is defined for class [" + root.getClass().getName() + "]");
        }
        Resource resource = searchEngine.createResource(alias);
        clearContext();
        classMapping.getConverter().marshall(resource, root, classMapping, this);
        clearContext();
        return resource;
    }

    public Resource marshall(Object root) {
        ClassMapping classMapping = (ClassMapping) mapping.findRootMappingByClass(root.getClass());
        if (classMapping == null) {
            throw new MarshallingException("No class is defined for class [" + root.getClass().getName() + "]");
        }
        Resource resource = searchEngine.createResource(classMapping.getAlias());
        clearContext();
        classMapping.getConverter().marshall(resource, root, classMapping, this);
        clearContext();
        return resource;
    }

    public Object unmarshall(String alias, Resource resource) throws CompassException {
        clearContext();
        Object retVal = unmarshall(alias, resource, this);
        clearContext();
        return retVal;
    }

    public Object unmarshall(String alias, Resource resource, MarshallingContext context) throws CompassException {
        Mapping classMapping = mapping.getRootMappingByAlias(alias);
        if (classMapping == null) {
            throw new MarshallingException("No class is defined for alias [ " + alias + "]");
        }
        if (!(classMapping instanceof ClassMapping)) {
            throw new MarshallingException(
                    "Can not unmarshall to an object from a non class mapping (i.e. resource mapping)");
        }
        return classMapping.getConverter().unmarshall(resource, classMapping, this);
    }

    public Object unmarshall(Resource resource, MarshallingContext context) throws CompassException {
        return unmarshall(resource.getAlias(), resource, context);
    }

    public Object unmarshall(Resource resource) throws CompassException {
        return unmarshall(resource.getAlias(), resource);
    }

    public void clearContext() {
        this.attributes.clear();
        this.nullValuesPath.clear();
        this.session.getFirstLevelCache().evictAllUnmarhsalled();
    }

    public void setHandleNulls(String path) {
        nullValuesPath.put(path, "");
    }

    public void removeHandleNulls(String path) {
        nullValuesPath.remove(path);
    }

    public boolean handleNulls() {
        return nullValuesPath.size() > 0;
    }

    public ConverterLookup getConverterLookup() {
        return converterLookup;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public CompassMapping getCompassMapping() {
        return mapping;
    }

    public InternalCompassSession getSession() {
        return session;
    }

    public MarshallingStrategy getMarshallingStrategy() {
        return this;
    }

    public Object getAttribute(Object key) {
        return attributes.get(key);
    }

    public void setAttribute(Object key, Object value) {
        attributes.put(key, value);
    }
}
