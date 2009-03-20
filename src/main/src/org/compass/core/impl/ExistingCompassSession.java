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
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.marshall.MarshallingContext;
import org.compass.core.marshall.MarshallingStrategy;
import org.compass.core.metadata.CompassMetaData;
import org.compass.core.spi.DirtyOperationContext;
import org.compass.core.spi.InternalCompass;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.spi.InternalSessionDelegateClose;

/**
 * @author kimchy
 */
public class ExistingCompassSession implements InternalCompassSession {

    private final InternalCompassSession session;

    public ExistingCompassSession(InternalCompassSession session) {
        this.session = session;
    }

    public InternalCompassSession getActualSession() {
        return this.session;
    }

    public void rollback() throws CompassException {
        // do propagate rollback, so we rollback as soon as possible
        session.rollback();
    }

    public void commit() throws CompassException {
        // do nothing, works with existsing one
    }

    public void close() throws CompassException {
        // do nothing, works with existing one
    }

    // simple delegates

    public InternalCompass getCompass() {
        return session.getCompass();
    }

    public SearchEngine getSearchEngine() {
        return session.getSearchEngine();
    }

    public MarshallingStrategy getMarshallingStrategy() {
        return session.getMarshallingStrategy();
    }

    public FirstLevelCache getFirstLevelCache() {
        return session.getFirstLevelCache();
    }

    public Object get(String alias, Object id, MarshallingContext context) throws CompassException {
        return session.get(alias, id, context);
    }

    public Object getByResource(Resource resource) throws CompassException {
        return session.getByResource(resource);
    }

    public Resource getResourceByIdResource(Resource idResource) throws CompassException {
        return session.getResourceByIdResource(idResource);
    }

    public Resource getResourceByIdResourceNoCache(Resource idResource) throws CompassException {
        return session.getResourceByIdResourceNoCache(idResource);
    }

    public CompassMapping getMapping() {
        return session.getMapping();
    }

    public CompassMetaData getMetaData() {
        return session.getMetaData();
    }

    public void addDelegateClose(InternalSessionDelegateClose delegateClose) {
        session.addDelegateClose(delegateClose);
    }

    public void startTransactionIfNeeded() {
        session.startTransactionIfNeeded();
    }

    public void unbindTransaction() {
        session.unbindTransaction();
    }

    public void create(String alias, Object object, DirtyOperationContext context) throws CompassException {
        session.create(alias, object, context);
    }

    public void create(Object object, DirtyOperationContext context) throws CompassException {
        session.create(object, context);
    }

    public void save(String alias, Object object, DirtyOperationContext context) throws CompassException {
        session.save(alias, object, context);
    }

    public void save(Object object, DirtyOperationContext context) throws CompassException {
        session.save(object, context);
    }

    public void delete(String alias, Object obj, DirtyOperationContext context) throws CompassException {
        session.delete(alias, obj, context);
    }

    public void delete(Class clazz, Object obj, DirtyOperationContext context) throws CompassException {
        session.delete(clazz, obj, context);
    }

    public void delete(Object obj, DirtyOperationContext context) throws CompassException {
        session.delete(obj, context);
    }

    public void setReadOnly() {
        session.setReadOnly();
    }

    public boolean isReadOnly() {
        return session.isReadOnly();
    }

    public CompassSession useLocalTransaction() {
        return session.useLocalTransaction();
    }

    public ResourceFactory resourceFactory() {
        return session.resourceFactory();
    }

    public CompassSettings getSettings() {
        return session.getSettings();
    }

    public void flush() throws CompassException {
        session.flush();
    }

    public void flushCommit(String... aliases) throws CompassException {
        session.flushCommit(aliases);
    }

    public CompassTransaction beginTransaction() throws CompassException {
        return session.beginTransaction();
    }

    public CompassTransaction beginLocalTransaction() throws CompassException {
        return session.beginLocalTransaction();
    }

    public CompassQueryBuilder queryBuilder() throws CompassException {
        return session.queryBuilder();
    }

    public CompassQueryFilterBuilder queryFilterBuilder() throws CompassException {
        return session.queryFilterBuilder();
    }

    public CompassTermFreqsBuilder termFreqsBuilder(String... names) throws CompassException {
        return session.termFreqsBuilder(names);
    }

    public CompassAnalyzerHelper analyzerHelper() throws CompassException {
        return session.analyzerHelper();
    }

    public boolean isClosed() {
        return session.isClosed();
    }

    public void delete(Resource resource) throws CompassException {
        session.delete(resource);
    }

    public Resource getResource(Class clazz, Object id) throws CompassException {
        return session.getResource(clazz, id);
    }

    public Resource getResource(Class clazz, Object... ids) throws CompassException {
        return session.getResource(clazz, ids);
    }

    public Resource getResource(String alias, Object id) throws CompassException {
        return session.getResource(alias, id);
    }

    public Resource getResource(String alias, Object... ids) throws CompassException {
        return session.getResource(alias, ids);
    }

    public Resource loadResource(Class clazz, Object id) throws CompassException {
        return session.loadResource(clazz, id);
    }

    public Resource loadResource(Class clazz, Object... ids) throws CompassException {
        return session.loadResource(clazz, ids);
    }

    public Resource loadResource(String alias, Object id) throws CompassException {
        return session.loadResource(alias, id);
    }

    public Resource loadResource(String alias, Object... ids) throws CompassException {
        return session.loadResource(alias, ids);
    }

    public void delete(Object obj) throws CompassException {
        session.delete(obj);
    }

    public void delete(String alias, Object obj) throws CompassException {
        session.delete(alias, obj);
    }

    public void delete(String alias, Object... ids) throws CompassException {
        session.delete(alias, ids);
    }

    public void delete(Class clazz, Object obj) throws CompassException {
        session.delete(clazz, obj);
    }

    public void delete(Class clazz, Object... ids) throws CompassException {
        session.delete(clazz, ids);
    }

    public <T> T get(Class<T> clazz, Object id) throws CompassException {
        return session.get(clazz, id);
    }

    public <T> T get(Class<T> clazz, Object... ids) throws CompassException {
        return session.get(clazz, ids);
    }

    public Object get(String alias, Object id) throws CompassException {
        return session.get(alias, id);
    }

    public Object get(String alias, Object... ids) throws CompassException {
        return session.get(alias, ids);
    }

    public <T> T load(Class<T> clazz, Object id) throws CompassException {
        return session.load(clazz, id);
    }

    public <T> T load(Class<T> clazz, Object... ids) throws CompassException {
        return session.load(clazz, ids);
    }

    public Object load(String alias, Object id) throws CompassException {
        return session.load(alias, id);
    }

    public Object load(String alias, Object... ids) throws CompassException {
        return session.load(alias, ids);
    }

    public void delete(CompassQuery query) throws CompassException {
        session.delete(query);
    }

    public CompassHits find(String query) throws CompassException {
        return session.find(query);
    }

    public void create(Object obj) throws CompassException {
        session.create(obj);
    }

    public void create(String alias, Object obj) throws CompassException {
        session.create(alias, obj);
    }

    public void save(Object obj) throws CompassException {
        session.save(obj);
    }

    public void save(String alias, Object obj) throws CompassException {
        session.save(alias, obj);
    }

    public void evict(Object obj) {
        session.evict(obj);
    }

    public void evict(String alias, Object id) {
        session.evict(alias, id);
    }

    public void evict(Resource resource) {
        session.evict(resource);
    }

    public void evictAll() {
        session.evictAll();
    }
}
