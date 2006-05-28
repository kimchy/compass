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

import java.util.HashMap;

import org.compass.core.CompassException;
import org.compass.core.Resource;
import org.compass.core.accessor.Setter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.osem.ClassMapping;
import org.compass.core.mapping.osem.ClassPropertyMetaDataMapping;
import org.compass.core.spi.AliasedObject;
import org.compass.core.spi.InternalCompassSession;

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
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            throw new MarshallingException("Failed to find mapping for alias [" + alias + "]");
        }
        return marshallIds(resourceMapping, id);
    }

    public Resource marshallIds(Class clazz, Object id) {
        ResourceMapping resourceMapping = mapping.findRootMappingByClass(clazz);
        if (resourceMapping == null) {
            throw new MarshallingException("Failed to find mapping for class [" + clazz.getName() + "]");
        }
        return marshallIds(resourceMapping, id);
    }

    public Resource marshallIds(ResourceMapping resourceMapping, Object id) {
        Resource idResource = searchEngine.createResource(resourceMapping.getAlias());
        marshallIds(idResource, resourceMapping, id);
        return idResource;
    }

    public boolean marshallIds(Resource resource, ResourceMapping resourceMapping, Object id) {
        return ((ResourceMappingConverter) resourceMapping.getConverter()).marshallIds(resource, id, resourceMapping, this);
    }


    public void marshallIds(Object root, Object id) {
        ClassMapping classMapping = (ClassMapping) mapping.getRootMappingByClass(root.getClass());
        ResourcePropertyMapping[] ids = classMapping.getIdMappings();
        Object[] idsValues = unmarshallIds(classMapping, id);
        for (int i = 0; i < idsValues.length; i++) {
            setId(root, idsValues[i], (ClassPropertyMetaDataMapping) ids[i]);
        }
    }

    private void setId(Object root, Object id, ClassPropertyMetaDataMapping mdMapping) {
        Setter setter = mdMapping.getSetter();
        setter.set(root, id);
    }

    public Object[] unmarshallIds(String alias, Object id) {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        return unmarshallIds(resourceMapping, id);
    }

    public Object[] unmarshallIds(Class clazz, Object id) {
        ResourceMapping resourceMapping = mapping.findRootMappingByClass(clazz);
        return unmarshallIds(resourceMapping, id);
    }

    public Object[] unmarshallIds(ResourceMapping resourceMapping, Object id) {
        return ((ResourceMappingConverter) resourceMapping.getConverter()).unmarshallIds(id, resourceMapping, this);
    }

    public Resource marshall(String alias, Object root) {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            throw new MarshallingException("No mapping is defined for alias [" + alias + "]");
        }
        Resource resource = searchEngine.createResource(alias);
        clearContext();
        resourceMapping.getConverter().marshall(resource, root, resourceMapping, this);
        clearContext();
        return resource;
    }

    public Resource marshall(Object root) {
        if (root instanceof AliasedObject) {
            return marshall(((AliasedObject) root).getAlias(), root);
        }
        ResourceMapping resourceMapping = mapping.findRootMappingByClass(root.getClass());
        if (resourceMapping == null) {
            throw new MarshallingException("No mapping is defined for class [" + root.getClass().getName() + "]");
        }
        Resource resource = searchEngine.createResource(resourceMapping.getAlias());
        clearContext();
        resourceMapping.getConverter().marshall(resource, root, resourceMapping, this);
        clearContext();
        return resource;
    }

    public Object unmarshall(Resource resource) throws CompassException {
        clearContext();
        Object retVal = unmarshall(resource, this);
        clearContext();
        return retVal;
    }

    public Object unmarshall(Resource resource, MarshallingContext context) throws CompassException {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(resource.getAlias());
        if (resourceMapping == null) {
            throw new MarshallingException("No mapping is defined for alias [ " + resource.getAlias() + "]");
        }
        return resourceMapping.getConverter().unmarshall(resource, resourceMapping, this);
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
