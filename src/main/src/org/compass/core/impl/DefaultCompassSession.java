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

package org.compass.core.impl;

import java.io.Reader;

import org.compass.core.CompassAnalyzerHelper;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTermFreqsBuilder;
import org.compass.core.CompassTransaction;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.cache.first.FirstLevelCache;
import org.compass.core.cache.first.NullFirstLevelCache;
import org.compass.core.cascade.CascadingManager;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.mapping.CascadeMapping;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.marshall.DefaultMarshallingStrategy;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingException;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.TransactionFactory;

/**
 * @author kimchy
 */
// TODO we need to support multiple resource with ResourceKey and first cache
public class DefaultCompassSession implements InternalCompassSession {

    private InternalCompass compass;

    private CompassMapping mapping;

    private CompassMetaData compassMetaData;

    private SearchEngine searchEngine;

    private TransactionFactory transactionFactory;

    private MarshallingStrategy marshallingStrategy;

    private FirstLevelCache firstLevelCache;

    private boolean closed = false;

    private RuntimeCompassSettings runtimeSettings;

    private CascadingManager cascadingManager;

    public DefaultCompassSession(RuntimeCompassSettings runtimeSettings, InternalCompass compass, SearchEngine searchEngine,
                                 FirstLevelCache firstLevelCache) {
        this.compass = compass;
        this.mapping = compass.getMapping();
        this.compassMetaData = compass.getMetaData();
        this.transactionFactory = compass.getTransactionFactory();
        this.runtimeSettings = runtimeSettings;
        this.searchEngine = searchEngine;
        this.firstLevelCache = firstLevelCache;
        this.marshallingStrategy = new DefaultMarshallingStrategy(mapping, searchEngine, compass.getConverterLookup(),
                this);
        this.cascadingManager = new CascadingManager(this);

        transactionFactory.tryJoinExistingTransaction(this);
    }

    public CompassSettings getSettings() {
        return runtimeSettings;
    }

    public Resource createResource(String alias) throws CompassException {
        checkClosed();
        return searchEngine.createResource(alias);
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index)
            throws CompassException {
        checkClosed();
        return searchEngine.createProperty(name, value, store, index);
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index,
                                   Property.TermVector termVector) throws CompassException {
        checkClosed();
        return searchEngine.createProperty(name, value, store, index, termVector);
    }

    public Property createProperty(String name, Reader value) throws CompassException {
        checkClosed();
        return searchEngine.createProperty(name, value);
    }

    public Property createProperty(String name, Reader value, Property.TermVector termVector) throws CompassException {
        checkClosed();
        return searchEngine.createProperty(name, value, termVector);
    }

    public Property createProperty(String name, byte[] value, Property.Store store) throws CompassException {
        checkClosed();
        return searchEngine.createProperty(name, value, store);
    }

    public CompassQueryBuilder queryBuilder() throws CompassException {
        checkClosed();
        SearchEngineQueryBuilder searchEngineQueryBuilder = searchEngine.queryBuilder();
        return new DefaultCompassQueryBuilder(searchEngineQueryBuilder, this);
    }

    public CompassQueryFilterBuilder queryFilterBuilder() throws CompassException {
        checkClosed();
        SearchEngineQueryFilterBuilder searchEngineQueryFilterBuilder = searchEngine.queryFilterBuilder();
        return new DefaultCompassQueryFilterBuilder(searchEngineQueryFilterBuilder, this);
    }

    public CompassTermFreqsBuilder termFreqsBuilder(String[] names) throws CompassException {
        checkClosed();
        return new DefaultCompassTermFreqsBuilder(this, names);
    }

    public CompassAnalyzerHelper analyzerHelper() throws CompassException {
        checkClosed();
        SearchEngineAnalyzerHelper analyzerHelper = searchEngine.analyzerHelper();
        return new DefaultCompassAnalyzerHelper(analyzerHelper, this);
    }

    public CompassTransaction beginTransaction() throws CompassException {
        checkClosed();
        return transactionFactory.beginTransaction(this, null);
    }

    public CompassTransaction beginTransaction(TransactionIsolation transactionIsolation) throws CompassException {
        checkClosed();
        if (transactionIsolation == TransactionIsolation.BATCH_INSERT) {
            firstLevelCache = new NullFirstLevelCache();
        }
        return transactionFactory.beginTransaction(this, transactionIsolation);
    }

    public void flush() throws CompassException {
        checkClosed();
        searchEngine.flush();
    }

    public Resource getResource(Class clazz, Object id) throws CompassException {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(clazz, id);
        return getResourceByIdResource(idResource);
    }

    public Resource getResource(String alias, Object id) throws CompassException {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        return getResourceByIdResource(idResource);
    }

    public Resource getResourceByIdResource(Resource idResource) {
        checkClosed();
        ResourceKey key = ((InternalResource) idResource).resourceKey();
        Resource cachedValue = firstLevelCache.getResource(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        Resource value = searchEngine.get(idResource);
        firstLevelCache.setResource(key, value);
        return value;
    }

    public <T> T get(Class<T> clazz, Object id) throws CompassException {
        checkClosed();
        Resource resource = getResource(clazz, id);
        if (resource == null) {
            return null;
        }
        //noinspection unchecked
        return (T) getByResource(resource);
    }

    public Object get(String alias, Object id) throws CompassException {
        checkClosed();
        Resource resource = getResource(alias, id);
        if (resource == null) {
            return null;
        }
        return getByResource(resource);
    }

    public Object get(String alias, Object id, MarshallingContext context) throws CompassException {
        checkClosed();
        Resource resource = getResource(alias, id);
        if (resource == null) {
            return null;
        }
        return getByResource(resource, context);
    }

    public Object getByResource(Resource resource) {
        checkClosed();
        return getByResource(resource, null);
    }

    public Object getByResource(Resource resource, MarshallingContext context) {
        checkClosed();
        ResourceKey key = ((InternalResource) resource).resourceKey();
        Object cachedValue = firstLevelCache.get(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        Object value;
        if (context == null) {
            value = marshallingStrategy.unmarshall(resource);
        } else {
            value = marshallingStrategy.unmarshall(resource, context);
        }
        firstLevelCache.set(key, value);
        return value;
    }

    public Resource loadResource(Class clazz, Object id) throws CompassException {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(clazz, id);
        return loadResourceByIdResource(idResource);
    }

    public Resource loadResource(String alias, Object id) throws CompassException {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        return loadResourceByIdResource(idResource);
    }

    public Resource loadResourceByIdResource(Resource idResource) {
        checkClosed();
        ResourceKey key = ((InternalResource) idResource).resourceKey();
        Resource cachedValue = firstLevelCache.getResource(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        Resource value = searchEngine.load(idResource);
        firstLevelCache.setResource(key, value);
        return value;
    }

    public <T> T load(Class<T> clazz, Object id) throws CompassException {
        checkClosed();
        Resource resource = loadResource(clazz, id);
        //noinspection unchecked
        return (T) getByResource(resource);
    }

    public Object load(String alias, Object id) throws CompassException {
        checkClosed();
        Resource resource = loadResource(alias, id);
        return getByResource(resource);
    }

    public CompassHits find(String query) throws CompassException {
        checkClosed();
        return queryBuilder().queryString(query).toQuery().hits();
    }

    public void create(String alias, Object object) throws CompassException {
        checkClosed();
        Resource resource = marshallingStrategy.marshall(alias, object);
        if (resource != null) {
            searchEngine.create(resource);
            ResourceKey key = ((InternalResource) resource).resourceKey();
            firstLevelCache.set(key, object);
        }
        boolean performedCascading = cascadingManager.cascade(alias, object, CascadeMapping.Cascade.CREATE);
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void create(Object object) throws CompassException {
        checkClosed();
        boolean performedCascading;
        Resource resource = marshallingStrategy.marshall(object);
        if (resource != null) {
            searchEngine.create(resource);
            ResourceKey key = ((InternalResource) resource).resourceKey();
            firstLevelCache.set(key, object);
            // if we found a resource, we perform the cascading based on its alias
            performedCascading = cascadingManager.cascade(key.getAlias(), object, CascadeMapping.Cascade.CREATE);
        } else {
            // actuall, no root mapping to create a resource, try and create one based on the object
            performedCascading = cascadingManager.cascade(object, CascadeMapping.Cascade.CREATE);
        }
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Object [" + object.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void save(String alias, Object object) throws CompassException {
        checkClosed();
        Resource resource = marshallingStrategy.marshall(alias, object);
        if (resource != null) {
            searchEngine.save(resource);
            ResourceKey key = ((InternalResource) resource).resourceKey();
            firstLevelCache.set(key, object);
        }
        boolean performedCascading = cascadingManager.cascade(alias, object, CascadeMapping.Cascade.SAVE);
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void save(Object object) throws CompassException {
        checkClosed();
        boolean performedCascading;
        Resource resource = marshallingStrategy.marshall(object);
        if (resource != null) {
            searchEngine.save(resource);
            ResourceKey key = ((InternalResource) resource).resourceKey();
            firstLevelCache.set(key, object);
            performedCascading = cascadingManager.cascade(key.getAlias(), CascadeMapping.Cascade.SAVE);
        } else {
            performedCascading = cascadingManager.cascade(object, CascadeMapping.Cascade.SAVE);
        }
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Object [" + object.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void delete(String alias, Object obj) throws CompassException {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(alias, obj);
        if (idResource != null) {
            delete(idResource);
        }
        boolean performedCascading = cascadingManager.cascade(alias, obj, CascadeMapping.Cascade.DELETE);
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void delete(Class clazz, Object obj) throws CompassException {
        checkClosed();
        boolean performedCascading;
        Resource idResource = marshallingStrategy.marshallIds(clazz, obj);
        if (idResource != null) {
            delete(idResource);
            performedCascading = cascadingManager.cascade(idResource.getAlias(), obj, CascadeMapping.Cascade.DELETE);
        } else {
            performedCascading = cascadingManager.cascade(clazz, obj, CascadeMapping.Cascade.DELETE);
        }
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Object [" + clazz + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void delete(Object obj) throws CompassException {
        checkClosed();
        boolean performedCascading;
        Resource idResource = marshallingStrategy.marshallIds(obj);
        if (idResource != null) {
            delete(idResource);
            performedCascading = cascadingManager.cascade(idResource.getAlias(), obj, CascadeMapping.Cascade.DELETE);
        } else {
            performedCascading = cascadingManager.cascade(obj, CascadeMapping.Cascade.DELETE);
        }
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Object [" + obj.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
    }

    public void delete(Resource resource) throws CompassException {
        checkClosed();
        firstLevelCache.evict(((InternalResource) resource).resourceKey());
        searchEngine.delete(resource);
    }

    public void delete(CompassQuery query) throws CompassException {
        checkClosed();
        // TODO since we don't marshall to objects, we won't get cascading
        CompassHits hits = query.hits();
        for (int i = 0; i < hits.length(); i++) {
            delete(hits.resource(i));
        }
    }

    public void evict(Object obj) {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(obj.getClass(), obj);
        ResourceKey key = ((InternalResource) idResource).resourceKey();
        firstLevelCache.evict(key);
    }

    public void evict(String alias, Object id) {
        checkClosed();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        ResourceKey key = ((InternalResource) idResource).resourceKey();
        firstLevelCache.evict(key);
    }

    public void evict(Resource resource) {
        checkClosed();
        ResourceKey key = ((InternalResource) resource).resourceKey();
        firstLevelCache.evict(key);
    }

    public void evictAll() {
        checkClosed();
        firstLevelCache.evictAll();
    }

    public void close() throws CompassException {
        if (closed) {
            return;
        }
        CompassSession transactionBoundSession = transactionFactory.getTransactionBoundSession();
        if (transactionBoundSession == null || transactionBoundSession != this) {
            closed = true;
            firstLevelCache.evictAll();
            searchEngine.close();
        }
    }

    public boolean isClosed() {
        return this.closed;
    }

    public InternalCompass getCompass() {
        return compass;
    }

    public SearchEngine getSearchEngine() {
        return searchEngine;
    }

    public MarshallingStrategy getMarshallingStrategy() {
        return marshallingStrategy;
    }

    public FirstLevelCache getFirstLevelCache() {
        return firstLevelCache;
    }

    public CompassMapping getMapping() {
        return mapping;
    }

    public CompassMetaData getMetaData() {
        return compassMetaData;
    }

    private void checkClosed() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("CompsesSession already closed");
        }
    }
}
