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
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.converter.ResourcePropertyConverter;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneResource implements Resource, Map {

    private static final long serialVersionUID = 3904681565727306034L;

    private Document document;

    private ArrayList properties = new ArrayList();

    private String aliasProperty;

    private int docNum;

    private transient LuceneSearchEngine searchEngine;

    private transient ResourceMapping resourceMapping;

    public LuceneResource(LuceneSearchEngine searchEngine) {
        this(new Document(), -1, searchEngine);
    }

    public LuceneResource(Document document, int docNum, LuceneSearchEngine searchEngine) {
        this.document = document;
        this.searchEngine = searchEngine;
        this.aliasProperty = searchEngine.getSearchEngineFactory().getLuceneSettings().getAliasProperty();
        Enumeration fields = document.fields();
        while (fields.hasMoreElements()) {
            Field field = (Field) fields.nextElement();
            LuceneProperty lProperty = new LuceneProperty(field);
            properties.add(lProperty);
        }
        this.docNum = docNum;
    }

    public Document getDocument() {
        return this.document;
    }

    public String get(String name) {
        return document.get(name);
    }

    public String[] getValues(String name) {
        return document.getValues(name);
    }

    public String getAlias() {
        Property alias = getProperty(aliasProperty);
        if (alias == null) {
            return null;
        }
        return alias.getStringValue();
    }

    public Resource setAlias(String alias) {
        removeProperties(aliasProperty);
        Property aliasProp = new LuceneProperty(new Field(aliasProperty, alias, Field.Store.YES,
                Field.Index.UN_TOKENIZED));
        addProperty(aliasProp);
        return this;
    }

    public Resource addProperty(String name, Object value) throws SearchEngineException {
        String alias = getAlias();
        verifyRawResourceMapping();

        ResourcePropertyMapping propertyMapping = resourceMapping.getResourcePropertyMapping(name);
        if (propertyMapping == null) {
            throw new SearchEngineException("No resource property mapping is defined for alias [" + alias
                    + "] and resource property [" + name + "]");
        }
        ResourcePropertyConverter converter = (ResourcePropertyConverter) propertyMapping.getConverter();
        if (converter == null) {
            converter = (ResourcePropertyConverter) searchEngine.getSearchEngineFactory().getMapping().
                    getConverterLookup().lookupConverter(value.getClass());
        }
        String strValue = converter.toString(value, propertyMapping);

        Property property = searchEngine.createProperty(strValue, propertyMapping);
        property.setBoost(propertyMapping.getBoost());
        return addProperty(property);
    }

    public Resource addProperty(String name, Reader value) throws SearchEngineException {
        String alias = getAlias();
        verifyRawResourceMapping();

        ResourcePropertyMapping propertyMapping = resourceMapping.getResourcePropertyMapping(name);
        if (propertyMapping == null) {
            throw new SearchEngineException("No resource property mapping is defined for alias [" + alias
                    + "] and resource property [" + name + "]");
        }

        Field.TermVector fieldTermVector = LuceneUtils.getFieldTermVector(propertyMapping.getTermVector());
        Field field = new Field(name, value, fieldTermVector);
        LuceneProperty property = new LuceneProperty(field);
        property.setBoost(propertyMapping.getBoost());
        return addProperty(property);
    }

    public Resource addProperty(Property property) {
        LuceneProperty lProperty = (LuceneProperty) property;
        properties.add(property);
        document.add(lProperty.getField());
        return this;
    }

    public Resource removeProperty(String name) {
        document.removeField(name);
        Iterator it = properties.iterator();
        while (it.hasNext()) {
            Property property = (Property) it.next();
            if (property.getName().equals(name)) {
                it.remove();
                return this;
            }
        }
        return this;
    }

    public Resource removeProperties(String name) {
        document.removeFields(name);
        Iterator it = properties.iterator();
        while (it.hasNext()) {
            Property property = (Property) it.next();
            if (property.getName().equals(name)) {
                it.remove();
            }
        }
        return this;
    }

    public Property getProperty(String name) {
        for (int i = 0; i < properties.size(); i++) {
            Property property = (Property) properties.get(i);
            if (property.getName().equals(name))
                return property;
        }
        return null;
    }

    public Property[] getProperties(String name) {
        List result = new ArrayList();
        for (int i = 0; i < properties.size(); i++) {
            Property property = (Property) properties.get(i);
            if (property.getName().equals(name)) {
                result.add(property);
            }
        }

        if (result.size() == 0)
            return new Property[0];

        return (Property[]) result.toArray(new Property[result.size()]);
    }

    public Property[] getProperties() {
        return (Property[]) properties.toArray(new LuceneProperty[properties.size()]);
    }

    public float getBoost() {
        return document.getBoost();
    }

    public Resource setBoost(float boost) {
        document.setBoost(boost);
        return this;
    }

    public void setDocNum(int docNum) {
        this.docNum = docNum;
    }

    /**
     * Returns the Lucene document number. If not set (can be in case the
     * resource is newly created), than returns -1.
     */
    public int getDocNum() {
        return this.docNum;
    }

    private void verifyRawResourceMapping() throws SearchEngineException {
        String alias = getAlias();
        if (resourceMapping == null) {
            if (alias == null) {
                throw new SearchEngineException(
                        "Can't add a resource property based on resource mapping without an alias associated with the resource first");
            }
            if (!searchEngine.getSearchEngineFactory().getMapping().hasRootMappingByAlias(alias)) {
                throw new SearchEngineException("No mapping is defined for alias [" + alias + "]");
            }
            resourceMapping = searchEngine.getSearchEngineFactory().getMapping().getRootMappingByAlias(alias);
        }
    }

    public String toString() {
        return "{" + getAlias() + "} " + StringUtils.arrayToCommaDelimitedString(getProperties());
    }

    // methods from the Map interface
    // ------------------------------

    public void clear() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set entrySet() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public void putAll(Map t) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Set keySet() {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public Object put(Object key, Object value) {
        throw new UnsupportedOperationException("Map operations are supported for read operations only");
    }

    public boolean containsKey(Object key) {
        return getProperty(key.toString()) != null;
    }

    public int size() {
        return this.properties.size();
    }

    public boolean isEmpty() {
        return this.properties.isEmpty();
    }

    public Collection values() {
        return this.properties;
    }

    public Object get(Object key) {
        return getProperties(key.toString());
    }
}
