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

package org.compass.core.lucene;

import java.io.Reader;
import java.util.ArrayList;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.spi.MultiResource;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneMultiResource implements MultiResource {

    private Resource currentResource;

    private ArrayList resources = new ArrayList();

    private String alias;

    private transient LuceneSearchEngine searchEngine;

    public LuceneMultiResource(String alias, LuceneSearchEngine searchEngine) {
        this.alias = alias;
        this.searchEngine = searchEngine;
        currentResource = new LuceneResource(alias, searchEngine);
        resources.add(currentResource);
    }

    // MultiResource inteface

    public int size() {
        return resources.size();
    }

    public Resource currentResource() {
        return currentResource;
    }

    public void addResource() {
        currentResource = new LuceneResource(alias, searchEngine);
        resources.add(currentResource);
    }

    public Resource resource(int i) {
        return (Resource) resources.get(i);
    }

    public void clear() {
        resources.clear();
        currentResource = null;
    }

    // Resource interfaces

    public String getAlias() {
        return currentResource.getAlias();
    }

    public Resource setAlias(String alias) {
        currentResource.setAlias(alias);
        return this;
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

    public String get(String name) {
        return currentResource.get(name);
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

    public void copy(Resource resource) {
        clear();
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                addResource();
                currentResource.copy(multiResource.resource(i));
            }
        } else {
            currentResource = resource;
            resources.add(resource);
        }
    }

    public String toString() {
        if (resources.size() == 1) {
            return resource(0).toString();
        }
        return StringUtils.collectionToCommaDelimitedString(resources);
    }
}
