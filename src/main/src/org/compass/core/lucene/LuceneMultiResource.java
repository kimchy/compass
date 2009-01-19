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

package org.compass.core.lucene;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.MultiResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneMultiResource implements MultiResource, Map<String, Property[]> {

    private LuceneResource currentResource;

    private ArrayList<InternalResource> resources = new ArrayList<InternalResource>();

    private String alias;

    private transient LuceneSearchEngineFactory searchEngineFactory;

    public LuceneMultiResource(String alias, LuceneSearchEngineFactory searchEngineFactory) {
        this.alias = alias;
        this.searchEngineFactory = searchEngineFactory;
        currentResource = new LuceneResource(alias, searchEngineFactory);
        resources.add(currentResource);
    }

    // MultiResource inteface

    public int size() {
        return resources.size();
    }

    public Resource currentResource() {
        return currentResource;
    }

    public ResourceKey getResourceKey() {
        return currentResource.getResourceKey();
    }

    public ResourceMapping getResourceMapping() {
        return currentResource.getResourceMapping();
    }

    public String getSubIndex() {
        return currentResource.getSubIndex();
    }

    public void addResource() {
        currentResource = new LuceneResource(alias, searchEngineFactory);
        resources.add(currentResource);
    }

    public Resource resource(int i) {
        return resources.get(i);
    }

    public void clear() {
        resources.clear();
        currentResource = null;
    }

    // Resource interfaces

    public String getAlias() {
        return currentResource.getAlias();
    }

    public String getUID() {
        return currentResource.getUID();
    }

    public String getId() {
        return currentResource.getId();
    }

    public String[] getIds() {
        return currentResource.getIds();
    }

    public Property getIdProperty() {
        return currentResource.getIdProperty();
    }

    public Property[] getIdProperties() {
        return currentResource.getIdProperties();
    }

    public String getValue(String name) {
        return currentResource.getValue(name);
    }

    public Object getObject(String name) {
        return currentResource.getObject(name);
    }

    public Object[] getObjects(String name) {
        return currentResource.getObjects(name);
    }

    public String[] getValues(String name) {
        return currentResource.getValues(name);
    }

    public Resource addProperty(String name, Object value) throws SearchEngineException {
        currentResource.addProperty(name, value);
        return this;
    }

    public Resource addProperty(String name, Reader value) throws SearchEngineException {
        currentResource.addProperty(name, value);
        return this;
    }

    public Resource addProperty(Property property) {
        currentResource.addProperty(property);
        return this;
    }

    public Resource setProperty(String name, Object value) throws SearchEngineException {
        currentResource.setProperty(name, value);
        return this;
    }

    public Resource setProperty(String name, Reader value) throws SearchEngineException {
        currentResource.setProperty(name, value);
        return this;
    }

    public Resource setProperty(Property property) {
        currentResource.setProperty(property);
        return this;
    }

    public Resource removeProperty(String name) {
        currentResource.removeProperty(name);
        return this;
    }

    public Resource removeProperties(String name) {
        currentResource.removeProperties(name);
        return this;
    }

    public Property getProperty(String name) {
        return currentResource.getProperty(name);
    }

    public Property[] getProperties(String name) {
        return currentResource.getProperties(name);
    }

    public Property[] getProperties() {
        return currentResource.getProperties();
    }

    public float getBoost() {
        return currentResource.getBoost();
    }

    public Resource setBoost(float boost) {
        currentResource.setBoost(boost);
        return this;
    }

    public void addUID() {
        currentResource.addUID();
    }

    public void copy(Resource resource) {
        clear();
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                addResource();
                currentResource.copy(multiResource.resource(i));
            }
        } else {
            currentResource = (LuceneResource) resource;
            resources.add((InternalResource) resource);
        }
    }

    public void attach(SearchEngineFactory searchEngineFactory) {
        for (InternalResource resource : resources) {
            resource.attach(searchEngineFactory);
        }
    }

    public String toString() {
        if (resources.size() == 1) {
            return resource(0).toString();
        }
        return StringUtils.collectionToCommaDelimitedString(resources);
    }

    // methods from the map interface

    public boolean isEmpty() {
        return currentResource.isEmpty();
    }

    public boolean containsKey(Object key) {
        return currentResource.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return currentResource.containsValue(value);
    }

    public Collection<Property[]> values() {
        return currentResource.values();
    }

    public void putAll(Map<? extends String, ? extends Property[]> t) {
        currentResource.putAll(t);
    }

    public Set<Map.Entry<String, Property[]>> entrySet() {
        return currentResource.entrySet();
    }

    public Set<String> keySet() {
        return currentResource.keySet();
    }

    public Property[] get(Object key) {
        return currentResource.get(key);
    }

    public Property[] remove(Object key) {
        return currentResource.remove(key);
    }

    public Property[] put(String key, Property[] value) {
        return currentResource.put(key, value);
    }
}
