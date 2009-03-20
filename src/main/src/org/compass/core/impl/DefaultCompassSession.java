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

package org.compass.core.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.CompassAnalyzerHelper;
import org.compass.core.CompassException;
import org.compass.core.CompassHits;
import org.compass.core.CompassQuery;
import org.compass.core.CompassQueryBuilder;
import org.compass.core.CompassQueryFilterBuilder;
import org.compass.core.CompassSession;
import org.compass.core.CompassTermFreqsBuilder;
import org.compass.core.CompassTransaction;
import org.compass.core.Resource;
import org.compass.core.ResourceFactory;
import org.compass.core.cache.first.FirstLevelCache;
import org.compass.core.cache.first.NullFirstLevelCache;
import org.compass.core.cascade.CascadingManager;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.events.FilterOperation;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.mapping.Cascade;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.marshall.DefaultMarshallingStrategy;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingException;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.DirtyOperationContext;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.InternalSessionDelegateClose;
import org.compass.core.spi.ResourceKey;
import org.compass.core.transaction.LocalTransaction;
import org.compass.core.transaction.LocalTransactionFactory;
import org.compass.core.transaction.TransactionFactory;

/**
 * @author kimchy
 */
// TODO we need to support multiple resource with ResourceKey and first cache
public class DefaultCompassSession implements InternalCompassSession {

    private static final Log logger = LogFactory.getLog(DefaultCompassSession.class);

    private final InternalCompass compass;

    private final CompassMapping mapping;

    private final CompassMetaData compassMetaData;

    private final SearchEngine searchEngine;

    private final TransactionFactory transactionFactory;

    private final LocalTransactionFactory localTransactionFactory;

    private final MarshallingStrategy marshallingStrategy;

    private FirstLevelCache firstLevelCache;

    private volatile boolean closed = false;

    private final RuntimeCompassSettings runtimeSettings;

    private final CascadingManager cascadingManager;

    private boolean localTransaction;

    private volatile CompassTransaction transaction;

    private final List<InternalSessionDelegateClose> delegateClose = new ArrayList<InternalSessionDelegateClose>();

    public DefaultCompassSession(RuntimeCompassSettings runtimeSettings, InternalCompass compass, SearchEngine searchEngine,
                                 FirstLevelCache firstLevelCache) {
        this.compass = compass;
        this.mapping = compass.getMapping();
        this.compassMetaData = compass.getMetaData();
        this.transactionFactory = compass.getTransactionFactory();
        this.localTransactionFactory = compass.getLocalTransactionFactory();
        this.runtimeSettings = runtimeSettings;
        this.searchEngine = searchEngine;
        this.firstLevelCache = firstLevelCache;
        this.marshallingStrategy = new DefaultMarshallingStrategy(mapping, searchEngine, compass.getConverterLookup(), this);
        this.cascadingManager = new CascadingManager(this);

        transaction = transactionFactory.tryJoinExistingTransaction(this);
    }

    public ResourceFactory resourceFactory() {
        return compass.getResourceFactory();
    }

    public CompassSettings getSettings() {
        return runtimeSettings;
    }

    public void setReadOnly() {
        searchEngine.setReadOnly();
    }

    public boolean isReadOnly() {
        return searchEngine.isReadOnly();
    }

    public CompassQueryBuilder queryBuilder() throws CompassException {
        checkClosed();
        SearchEngineQueryBuilder searchEngineQueryBuilder = searchEngine.queryBuilder();
        return new DefaultCompassQueryBuilder(searchEngineQueryBuilder, compass, this);
    }

    public CompassQueryFilterBuilder queryFilterBuilder() throws CompassException {
        checkClosed();
        return compass.queryFilterBuilder();
    }

    public CompassTermFreqsBuilder termFreqsBuilder(String... names) throws CompassException {
        checkClosed();
        return new DefaultCompassTermFreqsBuilder(this, names);
    }

    public CompassAnalyzerHelper analyzerHelper() throws CompassException {
        checkClosed();
        SearchEngineAnalyzerHelper analyzerHelper = searchEngine.analyzerHelper();
        return new DefaultCompassAnalyzerHelper(analyzerHelper, this);
    }

    public CompassSession useLocalTransaction() {
        if (transaction != null && !(transaction instanceof LocalTransaction)) {
            throw new IllegalStateException("There is already a transaction bounded to this session, and it is not a local one");
        }
        this.localTransaction = true;
        return this;
    }

    public CompassTransaction beginTransaction() throws CompassException {
        checkClosed();
        if (LuceneEnvironment.Transaction.Processor.Lucene.NAME.equalsIgnoreCase(getSettings().getSetting(LuceneEnvironment.Transaction.Processor.TYPE))) {
            firstLevelCache = NullFirstLevelCache.INSTANCE;
        }
        transaction = transactionFactory.beginTransaction(this);
        return transaction;
    }

    public CompassTransaction beginLocalTransaction() throws CompassException {
        checkClosed();
        if (LuceneEnvironment.Transaction.Processor.Lucene.NAME.equalsIgnoreCase(getSettings().getSetting(LuceneEnvironment.Transaction.Processor.TYPE))) {
            firstLevelCache = new NullFirstLevelCache();
        }
        transaction = localTransactionFactory.beginTransaction(this);
        return transaction;
    }

    public void flush() throws CompassException {
        checkClosed();
        searchEngine.flush();
    }

    public void flushCommit(String... aliases) throws CompassException {
        checkClosed();
        searchEngine.flushCommit(aliases);
    }

    public Resource getResource(Class clazz, Object... ids) throws CompassException {
        return getResource(clazz, (Object) ids);
    }

    public Resource getResource(Class clazz, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(clazz, id);
        if (idResource == null) {
            return null;
        }
        return getResourceByIdResource(idResource);
    }

    public Resource getResource(String alias, Object... ids) throws CompassException {
        return getResource(alias, (Object) ids);
    }

    public Resource getResource(String alias, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        if (idResource == null) {
            return null;
        }
        return getResourceByIdResource(idResource);
    }

    public Resource getResourceByIdResource(Resource idResource) {
        checkClosed();
        startTransactionIfNeeded();
        ResourceKey key = ((InternalResource) idResource).getResourceKey();
        Resource cachedValue = firstLevelCache.getResource(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        Resource value = searchEngine.get(idResource);
        if (value != null) {
            firstLevelCache.setResource(key, value);
        }
        return value;
    }

    public Resource getResourceByIdResourceNoCache(Resource idResource) {
        checkClosed();
        startTransactionIfNeeded();
        return searchEngine.get(idResource);
    }

    public <T> T get(Class<T> clazz, Object... ids) throws CompassException {
        return get(clazz, (Object) ids);
    }

    public <T> T get(Class<T> clazz, Object id) throws CompassException {
        Resource resource = getResource(clazz, id);
        if (resource == null) {
            return null;
        }
        //noinspection unchecked
        return (T) getByResource(resource);
    }

    public Object get(String alias, Object... ids) throws CompassException {
        return get(alias, (Object) ids);
    }

    public Object get(String alias, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
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
        startTransactionIfNeeded();
        ResourceKey key = ((InternalResource) resource).getResourceKey();
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

    public Resource loadResource(Class clazz, Object... ids) throws CompassException {
        return loadResource(clazz, (Object) ids);
    }

    public Resource loadResource(Class clazz, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(clazz, id);
        return loadResourceByIdResource(idResource);
    }

    public Resource loadResource(String alias, Object... ids) throws CompassException {
        return loadResource(alias, (Object) ids);
    }

    public Resource loadResource(String alias, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        return loadResourceByIdResource(idResource);
    }

    public Resource loadResourceByIdResource(Resource idResource) {
        checkClosed();
        startTransactionIfNeeded();
        ResourceKey key = ((InternalResource) idResource).getResourceKey();
        Resource cachedValue = firstLevelCache.getResource(key);
        if (cachedValue != null) {
            return cachedValue;
        }
        Resource value = searchEngine.load(idResource);
        firstLevelCache.setResource(key, value);
        return value;
    }

    public <T> T load(Class<T> clazz, Object... ids) throws CompassException {
        return load(clazz, (Object) ids);
    }

    public <T> T load(Class<T> clazz, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource resource = loadResource(clazz, id);
        //noinspection unchecked
        return (T) getByResource(resource);
    }

    public Object load(String alias, Object... ids) throws CompassException {
        return load(alias, (Object) ids);
    }

    public Object load(String alias, Object id) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        Resource resource = loadResource(alias, id);
        return getByResource(resource);
    }

    public CompassHits find(String query) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        return queryBuilder().queryString(query).toQuery().hits();
    }

    public void create(String alias, Object object) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        create(alias, object, new DirtyOperationContext());
    }

    public void create(String alias, Object object, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(object)) {
            return;
        }
        if (compass.getEventManager().onPreCreate(alias, object) == FilterOperation.YES) {
            return;
        }
        Resource resource = marshallingStrategy.marshall(alias, object);
        if (resource != null) {
            if (compass.getEventManager().onPreCreate(resource) == FilterOperation.YES) {
                return;
            }
            searchEngine.create(resource);
            ResourceKey key = ((InternalResource) resource).getResourceKey();
            firstLevelCache.set(key, object);
            firstLevelCache.setResource(key, resource);
        }
        context.addOperatedObjects(object);
        boolean performedCascading = cascadingManager.cascade(alias, object, Cascade.CREATE, context);
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (resource != null) {
            compass.getEventManager().onPostCreate(resource);
        }
        compass.getEventManager().onPostCreate(alias, object);
    }

    public void create(Object object) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        create(object, new DirtyOperationContext());
    }

    public void create(Object object, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(object)) {
            return;
        }
        if (compass.getEventManager().onPreCreate(null, object) == FilterOperation.YES) {
            return;
        }
        boolean performedCascading;
        Resource resource = marshallingStrategy.marshall(object);
        if (resource != null) {
            if (compass.getEventManager().onPreCreate(resource) == FilterOperation.YES) {
                return;
            }
            searchEngine.create(resource);
            ResourceKey key = ((InternalResource) resource).getResourceKey();
            firstLevelCache.set(key, object);
            firstLevelCache.setResource(key, resource);
            context.addOperatedObjects(object);
            // if we found a resource, we perform the cascading based on its alias
            performedCascading = cascadingManager.cascade(key.getAlias(), object, Cascade.CREATE, context);
        } else {
            context.addOperatedObjects(object);
            // actuall, no root mapping to create a resource, try and create one based on the object
            performedCascading = cascadingManager.cascade(object, Cascade.CREATE, context);
        }
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Object [" + object.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (resource != null) {
            compass.getEventManager().onPostCreate(resource);
            compass.getEventManager().onPostCreate(resource.getAlias(), object);
        } else {
            compass.getEventManager().onPostCreate(null, object);
        }
    }

    public void save(String alias, Object object) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        save(alias, object, new DirtyOperationContext());
    }

    public void save(String alias, Object object, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(object)) {
            return;
        }
        if (compass.getEventManager().onPreSave(alias, object) == FilterOperation.YES) {
            return;
        }
        Resource resource = marshallingStrategy.marshall(alias, object);
        if (resource != null) {
            if (compass.getEventManager().onPreSave(resource) == FilterOperation.YES) {
                return;
            }
            searchEngine.save(resource);
            ResourceKey key = ((InternalResource) resource).getResourceKey();
            firstLevelCache.set(key, object);
            firstLevelCache.setResource(key, resource);
        }
        context.addOperatedObjects(object);
        boolean performedCascading = cascadingManager.cascade(alias, object, Cascade.SAVE, context);
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (resource != null) {
            compass.getEventManager().onPostSave(resource);
        }
        compass.getEventManager().onPostSave(alias, object);
    }

    public void save(Object object) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        save(object, new DirtyOperationContext());
    }

    public void save(Object object, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(object)) {
            return;
        }
        if (compass.getEventManager().onPreSave(null, object) == FilterOperation.YES) {
            return;
        }
        boolean performedCascading;
        Resource resource = marshallingStrategy.marshall(object);
        if (resource != null) {
            if (compass.getEventManager().onPreSave(resource) == FilterOperation.YES) {
                return;
            }
            searchEngine.save(resource);
            ResourceKey key = ((InternalResource) resource).getResourceKey();
            firstLevelCache.setResource(key, resource);
            firstLevelCache.set(key, object);
            context.addOperatedObjects(object);
            performedCascading = cascadingManager.cascade(key.getAlias(), object, Cascade.SAVE, context);
        } else {
            context.addOperatedObjects(object);
            performedCascading = cascadingManager.cascade(object, Cascade.SAVE, context);
        }
        if (resource == null && !performedCascading) {
            throw new MarshallingException("Object [" + object.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (resource != null) {
            compass.getEventManager().onPostSave(resource);
            compass.getEventManager().onPostSave(resource.getAlias(), object);
        } else {
            compass.getEventManager().onPostSave(null, object);
        }
    }

    public void delete(String alias, Object... ids) throws CompassException {
        delete(alias, (Object) ids);
    }

    public void delete(String alias, Object obj) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        delete(alias, obj, new DirtyOperationContext());
    }

    public void delete(String alias, Object obj, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(obj)) {
            return;
        }
        if (compass.getEventManager().onPreDelete(alias, obj) == FilterOperation.YES) {
            return;
        }
        boolean performedCascading = false;
        Resource idResource = marshallingStrategy.marshallIds(alias, obj);
        if (idResource != null) {
            if (compass.getEventManager().onPreDelete(idResource) == FilterOperation.YES) {
                return;
            }
            Object cascadeObj = null;
            // in case we need to do cascading, we need to load the object to we can delete its cascaded objects
            if (cascadingManager.shouldCascade(idResource.getAlias(), obj, Cascade.DELETE)) {
                Resource resouce = getResourceByIdResource(idResource);
                if (resouce != null) {
                    cascadeObj = getByResource(resouce);
                }
            }
            delete(idResource);
            if (cascadeObj != null) {
                context.addOperatedObjects(cascadeObj);
                performedCascading = cascadingManager.cascade(idResource.getAlias(), cascadeObj, Cascade.DELETE, context);
            }
        } else {
            context.addOperatedObjects(obj);
            performedCascading = cascadingManager.cascade(alias, obj, Cascade.DELETE, context);
        }
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Alias [" + alias + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (idResource != null) {
            compass.getEventManager().onPostDelete(idResource);
        }
        compass.getEventManager().onPostDelete(alias, obj);
    }

    public void delete(Class clazz, Object... ids) throws CompassException {
        delete(clazz, (Object) ids);
    }

    public void delete(Class clazz, Object obj) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        delete(clazz, obj, new DirtyOperationContext());
    }

    public void delete(Class clazz, Object obj, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(obj)) {
            return;
        }
        if (compass.getEventManager().onPreDelete(clazz, obj) == FilterOperation.YES) {
            return;
        }
        boolean performedCascading = false;
        Resource idResource = marshallingStrategy.marshallIds(clazz, obj);
        if (idResource != null) {
            if (compass.getEventManager().onPreDelete(idResource) == FilterOperation.YES) {
                return;
            }
            Object cascadeObj = null;
            // in case we need to do cascading, we need to load the object to we can delete its cascaded objects
            if (cascadingManager.shouldCascade(idResource.getAlias(), obj, Cascade.DELETE)) {
                Resource resouce = getResourceByIdResource(idResource);
                if (resouce != null) {
                    cascadeObj = getByResource(resouce);
                }
            }
            delete(idResource);
            if (cascadeObj != null) {
                context.addOperatedObjects(cascadeObj);
                performedCascading = cascadingManager.cascade(idResource.getAlias(), cascadeObj, Cascade.DELETE, context);
            }
        } else {
            context.addOperatedObjects(obj);
            performedCascading = cascadingManager.cascade(clazz, obj, Cascade.DELETE, context);
        }
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Object [" + clazz + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (idResource != null) {
            compass.getEventManager().onPostDelete(idResource);
            compass.getEventManager().onPostDelete(idResource.getAlias(), obj);
        } else {
            compass.getEventManager().onPostDelete(clazz, obj);
        }
    }

    public void delete(Object obj) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        delete(obj, new DirtyOperationContext());
    }

    public void delete(Object obj, DirtyOperationContext context) throws CompassException {
        if (context.alreadyPerformedOperation(obj)) {
            return;
        }
        if (compass.getEventManager().onPreDelete((String) null, obj) == FilterOperation.YES) {
            return;
        }
        boolean performedCascading = false;
        Resource idResource = marshallingStrategy.marshallIds(obj);
        if (idResource != null) {
            if (compass.getEventManager().onPreDelete(idResource) == FilterOperation.YES) {
                return;
            }
            Object cascadeObj = null;
            // in case we need to do cascading, we need to load the object to we can delete its cascaded objects
            if (cascadingManager.shouldCascade(idResource.getAlias(), obj, Cascade.DELETE)) {
                Resource resouce = getResourceByIdResource(idResource);
                if (resouce != null) {
                    cascadeObj = getByResource(resouce);
                }
            }
            delete(idResource);
            if (cascadeObj != null) {
                context.addOperatedObjects(cascadeObj);
                performedCascading = cascadingManager.cascade(idResource.getAlias(), cascadeObj, Cascade.DELETE, context);
            }
        } else {
            context.addOperatedObjects(obj);
            performedCascading = cascadingManager.cascade(obj, Cascade.DELETE, context);
        }
        if (idResource == null && !performedCascading) {
            throw new MarshallingException("Object [" + obj.getClass().getName() + "] has no root mappings and no cascading defined, no operation was perfomed");
        }
        if (idResource != null) {
            compass.getEventManager().onPostDelete(idResource);
            compass.getEventManager().onPostDelete(idResource.getAlias(), obj);
        } else {
            compass.getEventManager().onPostDelete((String) null, obj);
        }
    }

    public void delete(Resource resource) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        firstLevelCache.evict(((InternalResource) resource).getResourceKey());
        if (compass.getEventManager().onPreDelete(resource) == FilterOperation.YES) {
            return;
        }
        searchEngine.delete(resource);
        compass.getEventManager().onPostDelete(resource);
    }

    public void delete(CompassQuery query) throws CompassException {
        checkClosed();
        startTransactionIfNeeded();
        query.delete();
    }

    public void evict(Object obj) {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(obj.getClass(), obj);
        ResourceKey key = ((InternalResource) idResource).getResourceKey();
        firstLevelCache.evict(key);
    }

    public void evict(String alias, Object id) {
        checkClosed();
        startTransactionIfNeeded();
        Resource idResource = marshallingStrategy.marshallIds(alias, id);
        ResourceKey key = ((InternalResource) idResource).getResourceKey();
        firstLevelCache.evict(key);
    }

    public void evict(Resource resource) {
        checkClosed();
        ResourceKey key = ((InternalResource) resource).getResourceKey();
        firstLevelCache.evict(key);
    }

    public void evictAll() {
        checkClosed();
        firstLevelCache.evictAll();
    }

    public void addDelegateClose(InternalSessionDelegateClose delegateClose) {
        this.delegateClose.add(delegateClose);
    }

    public void unbindTransaction() {
        transaction = null;
    }

    private boolean rolledback;

    public void rollback() throws CompassException {
        try {
            if (transaction != null) {
                rolledback = true;
                transaction.rollback();
            }
        } finally {
            close();
        }
    }

    public void commit() throws CompassException {
        try {
            if (transaction != null) {
                transaction.commit();
                transaction = null;
            }
        } finally {
            close();
        }
    }

    public void close() throws CompassException {
        if (closed) {
            return;
        }
        for (InternalSessionDelegateClose delegateClose : this.delegateClose) {
            delegateClose.close();
        }
        // if we are closing this session, and we still have a transction open, we can commit it automatically
        // Note: committing a transaction does not always end up with actual commit, for example, if it is an
        // "outer" managed transaction
        CompassException ex = null;
        if (transaction != null) {
            try {
                transaction.commit();
            } catch (CompassException e) {
                ex = e;
                try {
                    transaction.rollback();
                } catch (Exception e1) {
                    logger.warn("Failed to rollback transaction after auto commit on session close, ignoring", e);
                }
            }
        }
        CompassSession transactionBoundSession = transactionFactory.getTransactionBoundSession();
        // if there is no bounded transaction (it was already committed / rolledback, thus unbounded)
        // or if the current bounded session is not the same one, we need to close the session
        if (transactionBoundSession == null || transactionBoundSession != this) {
            closed = true;
            if (transaction != null && !rolledback) {
                logger.warn("Session close without being committed / rolledback, rolling back the transaction");
                try {
                    transaction.rollback();
                } catch (CompassException e) {
                    logger.debug("Failed to rollback un-committed transaction", e);
                }                             
            }

            firstLevelCache.evictAll();
            searchEngine.close();
        }

        if (ex != null) {
            throw ex;
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

    public void startTransactionIfNeeded() {
        if (rolledback) {
            throw new CompassException("Transaction already rolled back");
        }
        if (transaction == null) {
            synchronized (this) {
                if (transaction == null) {
                    if (localTransaction) {
                        transaction = beginLocalTransaction();
                    } else {
                        transaction = beginTransaction();
                    }
                }
            }
        }
    }

    private void checkClosed() throws IllegalStateException {
        if (closed) {
            throw new IllegalStateException("CompassSession already closed");
        }
    }
}
