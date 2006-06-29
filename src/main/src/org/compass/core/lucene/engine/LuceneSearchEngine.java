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

package org.compass.core.lucene.engine;

import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Field;
import org.compass.core.CompassTermInfoVector;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.spi.MultiResource;
import org.compass.core.CompassTransaction.TransactionIsolation;
import org.compass.core.engine.RepeatableReader;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.LuceneProperty;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.LuceneMultiResource;
import org.compass.core.lucene.engine.transaction.BatchInsertTransaction;
import org.compass.core.lucene.engine.transaction.LuceneSearchEngineTransaction;
import org.compass.core.lucene.engine.transaction.ReadCommittedTransaction;
import org.compass.core.lucene.engine.transaction.SerialableTransaction;
import org.compass.core.lucene.engine.query.LuceneSearchEngineQueryBuilder;
import org.compass.core.lucene.engine.query.LuceneSearchEngineQueryFilterBuilder;
import org.compass.core.lucene.util.LuceneUtils;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.util.ResourceHelper;
import org.compass.core.util.reader.ReverseStringReader;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneSearchEngine implements SearchEngine {

    protected final static Log log = LogFactory.getLog(LuceneSearchEngine.class);

    private static final int UNKNOWN = -1;

    private static final int STARTED = 0;

    private static final int COMMIT = 1;

    private static final int ROLLBACK = 2;

    private int transactionState;

    private LuceneSearchEngineTransaction transaction;

    private LuceneSearchEngineFactory searchEngineFactory;

    private SearchEngineEventManager eventManager = new SearchEngineEventManager();

    public LuceneSearchEngine(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
        this.transactionState = UNKNOWN;
        eventManager.registerLifecycleListener(searchEngineFactory.getEventManager());
        searchEngineFactory.getLuceneIndexManager().getStore().registerEventListeners(this, eventManager);
    }

    public String getNullValue() {
        return "";
    }

    public boolean isNullValue(String value) {
        if (value == null) {
            return true;
        }
        return value.length() == 0;
    }

    public Resource createResource(String alias) throws SearchEngineException {
        return new LuceneMultiResource(alias, this);
    }

    public Property createProperty(String value, ResourcePropertyMapping mapping) throws SearchEngineException {
        return createProperty(mapping.getPath(), value, mapping);
    }

    public Property createProperty(String name, String value, ResourcePropertyMapping mapping) throws SearchEngineException {
        Property property = null;
        if (mapping.getReverse() == ResourcePropertyMapping.ReverseType.NO) {
            property = createProperty(name, value, mapping.getStore(), mapping.getIndex(), mapping.getTermVector());
        } else if (mapping.getReverse() == ResourcePropertyMapping.ReverseType.READER) {
            property = createProperty(name, new ReverseStringReader(value), mapping.getTermVector());
        } else if (mapping.getReverse() == ResourcePropertyMapping.ReverseType.STRING) {
            property = createProperty(name, StringUtils.reverse(value), mapping.getStore(), mapping.getIndex(), mapping.getTermVector());
        }
        property.setBoost(mapping.getBoost());
        return property;
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index)
            throws SearchEngineException {
        return createProperty(name, value, store, index, Property.TermVector.NO);
    }

    public Property createProperty(String name, String value, Property.Store store, Property.Index index,
                                   Property.TermVector termVector) throws SearchEngineException {
        Field.Store fieldStore = LuceneUtils.getFieldStore(store);
        Field.Index fieldIndex = LuceneUtils.getFieldIndex(index);
        Field.TermVector fieldTermVector = LuceneUtils.getFieldTermVector(termVector);
        Field field = new Field(name, value, fieldStore, fieldIndex, fieldTermVector);
        return new LuceneProperty(field);
    }

    public Property createProperty(String name, Reader value) {
        return createProperty(name, value, Property.TermVector.NO);
    }

    public Property createProperty(String name, byte[] value, Property.Store store) throws SearchEngineException {
        Field.Store fieldStore = LuceneUtils.getFieldStore(store);
        Field field = new Field(name, value, fieldStore);
        return new LuceneProperty(field);
    }

    public Property createProperty(String name, Reader value, Property.TermVector termVector) {
        Field.TermVector fieldTermVector = LuceneUtils.getFieldTermVector(termVector);
        Field field = new Field(name, value, fieldTermVector);
        if (value instanceof RepeatableReader) {
            return new LuceneProperty(field, (RepeatableReader) value);
        }
        return new LuceneProperty(field);
    }

    public SearchEngineQueryBuilder queryBuilder() throws SearchEngineException {
        return new LuceneSearchEngineQueryBuilder(this);
    }

    public SearchEngineQueryFilterBuilder queryFilterBuilder() throws SearchEngineException {
        return new LuceneSearchEngineQueryFilterBuilder();
    }

    public SearchEngineAnalyzerHelper analyzerHelper() {
        return new LuceneSearchEngineAnalyzerHelper(this);
    }

    public void begin() throws SearchEngineException {
        if (transactionState == STARTED) {
            throw new SearchEngineException("Transaction already started, why start it again?");
        }

        Class transactionIsolationClass = searchEngineFactory.getLuceneSettings().getTransactionIsolationClass();
        if (transactionIsolationClass != null) {
            try {
                transaction = (LuceneSearchEngineTransaction) transactionIsolationClass.newInstance();
            } catch (Exception e) {
                throw new SearchEngineException("Failed to create an instance for transaction ["
                        + transactionIsolationClass.getName() + "]", e);
            }
            transaction.configure(this);
            eventManager.beforeBeginTransaction();
            transaction.begin();
            eventManager.afterBeginTransaction();
            transactionState = STARTED;
            return;
        }
        TransactionIsolation transactionIsolation = searchEngineFactory.getLuceneSettings().getTransactionIsolation();
        begin(transactionIsolation);
    }

    public void begin(TransactionIsolation transactionIsolation) throws SearchEngineException {
        if (transactionIsolation == null) {
            transactionIsolation = searchEngineFactory.getLuceneSettings().getTransactionIsolation();
        }
        if (transactionIsolation == TransactionIsolation.READ_COMMITTED) {
            transaction = new ReadCommittedTransaction();
        } else if (transactionIsolation == TransactionIsolation.READ_ONLY_READ_COMMITTED) {
            transaction = new ReadCommittedTransaction();
        } else if (transactionIsolation == TransactionIsolation.BATCH_INSERT) {
            transaction = new BatchInsertTransaction();
        } else if (transactionIsolation == TransactionIsolation.SERIALIZABLE) {
            transaction = new SerialableTransaction();
        }
        transaction.configure(this);
        eventManager.beforeBeginTransaction();
        transaction.begin();
        eventManager.afterBeginTransaction();
        transactionState = STARTED;
    }

    private void checkTransactionStarted() throws SearchEngineException {
        if (transactionState == UNKNOWN)
            throw new SearchEngineException(
                    "Search engine transaction not successfully started");
    }

    public void prepare() throws SearchEngineException {
        checkTransactionStarted();
        if (transaction != null) {
            transaction.prepare();
        }
        eventManager.afterPrepare();
    }

    public void commit(boolean onePhase) throws SearchEngineException {
        checkTransactionStarted();
        try {
            if (transaction != null) {
                transaction.commit(onePhase);
                eventManager.afterCommit(onePhase);
            }
        } finally {
            transaction = null;
            transactionState = COMMIT;
        }
    }

    public void rollback() throws SearchEngineException {
        checkTransactionStarted();
        try {
            if (transaction != null) {
                transaction.rollback();
                eventManager.afterRollback();
            }
        } finally {
            transaction = null;
            transactionState = ROLLBACK;
        }
    }

    public void flush() throws SearchEngineException {
        checkTransactionStarted();
        if (transaction != null) {
            transaction.flush();
        }
    }

    public boolean wasRolledBack() throws SearchEngineException {
        return transactionState == ROLLBACK;
    }

    public boolean wasCommitted() throws SearchEngineException {
        return transactionState == COMMIT;
    }

    public void close() throws SearchEngineException {
        eventManager.close();
        if (transactionState == STARTED) {
            log.warn("Transaction not committed/rolled backed, rolling back");
            try {
                rollback();
            } catch (Exception e) {
                log.warn("Failed to rollback transcation, ignoring", e);
            }
        }
        eventManager = null;
    }

    public void delete(String[] ids, String alias) throws SearchEngineException {
        ResourceMapping resourceMapping = searchEngineFactory.getMapping().getRootMappingByAlias(alias);
        delete(ResourceHelper.toIds(this, ids, resourceMapping), alias);
    }

    public void delete(Resource resource) throws SearchEngineException {
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                delete(ResourceHelper.toIds(resource.getAlias(), multiResource.resource(i), searchEngineFactory.getMapping()),
                        resource.getAlias());
            }
        } else {
            delete(ResourceHelper.toIds(resource.getAlias(), resource, searchEngineFactory.getMapping()),
                    resource.getAlias());
        }
    }

    public void delete(final Property[] ids, String alias) throws SearchEngineException {
        checkTransactionStarted();
        if (ids.length == 0) {
            throw new SearchEngineException("Cannot delete a resource with no ids");
        }
        transaction.delete(ids, alias);
        if (log.isDebugEnabled()) {
            log.debug("RESOURCE DELETE {" + alias + "} " + StringUtils.arrayToCommaDelimitedString(ids));
        }
    }

    public void create(final Resource resource) throws SearchEngineException {
        checkTransactionStarted();
        String alias = resource.getAlias();
        ResourceMapping resourceMapping = searchEngineFactory.getMapping().getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            throw new SearchEngineException("Failed to find mapping for alias [" + alias + "]");
        }
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                Resource resource1 = multiResource.resource(i);
                LuceneUtils.addAllPropertyIfNeeded(resource1, resourceMapping, this);
                transaction.create(resource1);
                if (log.isDebugEnabled()) {
                    log.debug("RESOURCE CREATE " + resource1);
                }
            }
        } else {
            LuceneUtils.addAllPropertyIfNeeded(resource, resourceMapping, this);
            transaction.create(resource);
            if (log.isDebugEnabled()) {
                log.debug("RESOURCE CREATE " + resource);
            }
        }
    }

    public void save(Resource resource) throws SearchEngineException {
        delete(resource);
        create(resource);
    }

    public Resource get(Resource idResource) throws SearchEngineException {
        String alias = idResource.getAlias();
        return get(ResourceHelper.toIds(alias, idResource, searchEngineFactory.getMapping()), alias);
    }

    public Resource get(String[] ids, String alias) throws SearchEngineException {
        ResourceMapping resourceMapping = searchEngineFactory.getMapping().getRootMappingByAlias(alias);
        return get(ResourceHelper.toIds(this, ids, resourceMapping), alias);
    }

    public Resource get(Property[] ids, String alias) throws SearchEngineException {
        checkTransactionStarted();
        if (ids.length == 0) {
            throw new SearchEngineException("Cannot load a resource with no ids");
        }
        Resource[] result = transaction.find(ids, alias);
        if (result.length == 0) {
            return null;
        } else if (result.length > 1) {
            log.warn("Found several matches in get/load operation for resource alias [" + alias + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(ids) + "]");
            return result[result.length - 1];
        }
        return result[0];
    }

    public Resource load(Resource idResource) throws SearchEngineException {
        String alias = idResource.getAlias();
        Resource resource = get(idResource);
        if (resource == null) {
            throw new SearchEngineException("Failed to find resource with alias [" + alias + "] and ids " + idResource);
        }
        return resource;
    }

    public Resource load(String[] ids, String alias) throws SearchEngineException {
        Resource resource = get(ids, alias);
        if (resource == null) {
            throw new SearchEngineException("Failed to find resource with alias [" + alias + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(ids) + "]");
        }
        return resource;
    }

    public Resource load(Property[] ids, String alias) throws SearchEngineException {
        Resource resource = get(ids, alias);
        if (resource == null) {
            throw new SearchEngineException("Failed to find resource with alias [" + alias + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(ids) + "]");
        }
        return resource;
    }

    public SearchEngineHits find(SearchEngineQuery query) throws SearchEngineException {
        checkTransactionStarted();
        SearchEngineHits hits = transaction.find(query);
        if (log.isDebugEnabled()) {
            log.debug("RESOURCE QUERY [" + query + "] HITS [" + hits.getLength() + "]");
        }
        return hits;
    }

    public SearchEngineHighlighter highlighter(SearchEngineQuery query) throws SearchEngineException {
        checkTransactionStarted();
        return transaction.highlighter(query);
    }

    public CompassTermInfoVector[] getTermInfos(Resource resource) throws SearchEngineException {
        checkTransactionStarted();
        return transaction.getTermInfos((LuceneResource) resource);
    }

    public CompassTermInfoVector getTermInfo(Resource resource, String propertyName) throws SearchEngineException {
        checkTransactionStarted();
        return transaction.getTermInfo((LuceneResource) resource, propertyName);
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }
}
