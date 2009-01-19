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

package org.compass.core.marshall;

import org.compass.core.CompassException;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.accessor.Setter;
import org.compass.core.converter.ConverterLookup;
import org.compass.core.converter.mapping.ResourceMappingConverter;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.osem.ObjectMapping;
import org.compass.core.spi.AliasedObject;
import org.compass.core.spi.InternalCompassSession;

/**
 * @author kimchy
 */
public class DefaultMarshallingStrategy implements MarshallingStrategy {

    private CompassMapping mapping;

    private SearchEngine searchEngine;

    private ConverterLookup converterLookup;

    private InternalCompassSession session;

    private ResourceFactory resourceFactory;

    public DefaultMarshallingStrategy(CompassMapping mapping, SearchEngine searchEngine,
                                      ConverterLookup converterLookup, InternalCompassSession session) {
        this.mapping = mapping;
        this.searchEngine = searchEngine;
        this.converterLookup = converterLookup;
        this.session = session;
        this.resourceFactory = session.getCompass().getResourceFactory();
    }

    public Resource marshallIds(Object id) {
        if (id instanceof AliasedObject) {
            return marshallIds(((AliasedObject) id).getAlias(), id);
        }
        return marshallIds(id.getClass(), id);
    }

    public Resource marshallIds(String alias, Object id) {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            return null;
        }
        return marshallIds(resourceMapping, id);
    }

    public Resource marshallIds(Class clazz, Object id) {
        ResourceMapping resourceMapping = mapping.getRootMappingByClass(clazz);
        if (resourceMapping == null) {
            return null;
        }
        return marshallIds(resourceMapping, id);
    }

    public Resource marshallIds(ResourceMapping resourceMapping, Object id) {
        Resource idResource = resourceFactory.createResource(resourceMapping.getAlias());
        marshallIds(idResource, resourceMapping, id, createContext());
        return idResource;
    }

    public boolean marshallIds(Resource resource, ResourceMapping resourceMapping, Object id, MarshallingContext context) {
        return ((ResourceMappingConverter) resourceMapping.getConverter()).marshallIds(resource, id, resourceMapping, context);
    }

    public void marshallIds(Object root, Object id) {
        ResourceMapping resourceMapping = mapping.getMappingByClass(root.getClass());
        if (resourceMapping == null) {
            throw new MarshallingException("No resource mapping is defined for class [" + root.getClass() + "]");
        }
        Mapping[] ids = resourceMapping.getIdMappings();
        if (ids == null || ids.length == 0) {
            return;
        }
        Object[] idsValues = unmarshallIds(resourceMapping, id, createContext());
        for (int i = 0; i < idsValues.length; i++) {
            setId(root, idsValues[i], (ObjectMapping) ids[i]);
        }
    }

    public void marshallIds(ResourceMapping resourceMapping, Object root, Object id) {
        Mapping[] ids = resourceMapping.getIdMappings();
        if (ids.length == 0) {
            return;
        }
        Object[] idsValues = unmarshallIds(resourceMapping, id, createContext());
        for (int i = 0; i < idsValues.length; i++) {
            setId(root, idsValues[i], (ObjectMapping) ids[i]);
        }
    }

    private void setId(Object root, Object id, ObjectMapping mdMapping) {
        Setter setter = mdMapping.getSetter();
        setter.set(root, id);
    }

    public Object[] unmarshallIds(String alias, Object id) {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        return unmarshallIds(resourceMapping, id, createContext());
    }

    public Object[] unmarshallIds(Class clazz, Object id) {
        ResourceMapping resourceMapping = mapping.findRootMappingByClass(clazz);
        return unmarshallIds(resourceMapping, id, createContext());
    }

    public Object[] unmarshallIds(ResourceMapping resourceMapping, Object id, MarshallingContext context) {
        return ((ResourceMappingConverter) resourceMapping.getConverter()).unmarshallIds(id, resourceMapping, context);
    }

    public Resource marshall(String alias, Object root) {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            return null;
        }
        Resource resource = resourceFactory.createResource(alias);
        resourceMapping.getConverter().marshall(resource, root, resourceMapping, createContext());
        return resource;
    }

    public Resource marshall(Object root) {
        if (root instanceof AliasedObject) {
            return marshall(((AliasedObject) root).getAlias(), root);
        }
        ResourceMapping resourceMapping = mapping.getRootMappingByClass(root.getClass());
        if (resourceMapping == null) {
            return null;
        }
        Resource resource = resourceFactory.createResource(resourceMapping.getAlias());
        resourceMapping.getConverter().marshall(resource, root, resourceMapping, createContext());
        return resource;
    }

    public Object unmarshall(Resource resource) throws CompassException {
        return unmarshall(resource, createContext());
    }

    public Object unmarshall(Resource resource, MarshallingContext context) throws CompassException {
        ResourceMapping resourceMapping = mapping.getRootMappingByAlias(resource.getAlias());
        if (resourceMapping == null) {
            throw new MarshallingException("No mapping is defined for alias [ " + resource.getAlias() + "]");
        }
        return resourceMapping.getConverter().unmarshall(resource, resourceMapping, context);
    }

    private MarshallingContext createContext() {
        return new DefaultMarshallingContext(mapping, searchEngine, converterLookup, session, this);
    }
}
