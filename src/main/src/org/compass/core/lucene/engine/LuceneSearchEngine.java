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

package org.compass.core.lucene.engine;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.compass.core.Resource;
import org.compass.core.config.CompassSettings;
import org.compass.core.config.RuntimeCompassSettings;
import org.compass.core.engine.SearchEngine;
import org.compass.core.engine.SearchEngineAnalyzerHelper;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineInternalSearch;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.engine.SearchEngineQueryBuilder;
import org.compass.core.engine.SearchEngineQueryFilterBuilder;
import org.compass.core.engine.SearchEngineTermFrequencies;
import org.compass.core.engine.event.SearchEngineEventManager;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.transaction.TransactionProcessor;
import org.compass.core.lucene.engine.transaction.TransactionProcessorFactory;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.MultiResource;
import org.compass.core.spi.ResourceKey;
import org.compass.core.util.StringUtils;

/**
 * @author kimchy
 */
public class LuceneSearchEngine implements SearchEngine {

    protected final static Log log = LogFactory.getLog(LuceneSearchEngine.class);

    private static final int NOT_STARTED = -1;

    private static final int STARTED = 0;

    private static final int COMMIT = 1;

    private static final int ROLLBACK = 2;

    private volatile int transactionState;

    private final LuceneSearchEngineFactory searchEngineFactory;

    private final RuntimeCompassSettings runtimeSettings;
    
    private final SearchEngineEventManager eventManager = new SearchEngineEventManager();


    private TransactionProcessor transactionProcessor;

    private boolean onlyReadOnlyOperations;

    private boolean readOnly;

    private final ArrayList<LuceneDelegatedClose> delegateClose = new ArrayList<LuceneDelegatedClose>();


    public LuceneSearchEngine(RuntimeCompassSettings runtimeSettings, LuceneSearchEngineFactory searchEngineFactory) {
        this.runtimeSettings = runtimeSettings;
        this.onlyReadOnlyOperations = true;
        this.searchEngineFactory = searchEngineFactory;
        this.transactionState = NOT_STARTED;
        eventManager.registerLifecycleListener(searchEngineFactory.getEventManager());
        searchEngineFactory.getLuceneIndexManager().getStore().registerEventListeners(this, eventManager);
    }

    public SearchEngineQueryBuilder queryBuilder() throws SearchEngineException {
        return searchEngineFactory.queryBuilder();
    }

    public SearchEngineQueryFilterBuilder queryFilterBuilder() throws SearchEngineException {
        return searchEngineFactory.queryFilterBuilder();
    }

    public SearchEngineAnalyzerHelper analyzerHelper() {
        return new LuceneSearchEngineAnalyzerHelper(this);
    }

    public void setReadOnly() {
        this.readOnly = true;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void begin() throws SearchEngineException {
        this.onlyReadOnlyOperations = true;
        if (transactionState == STARTED) {
            throw new SearchEngineException("Transaction already started, why start it again?");
        }

        closeDelegateClosed();
        String defaultProcessorFactory = LuceneEnvironment.Transaction.Processor.ReadCommitted.NAME;
        if (readOnly) {
            defaultProcessorFactory = LuceneEnvironment.Transaction.Processor.Search.NAME;
        }

        TransactionProcessorFactory transactionProcessorFactory = searchEngineFactory.getTransactionProcessorManager()
                .getProcessorFactory(runtimeSettings.getSetting(LuceneEnvironment.Transaction.Processor.TYPE, defaultProcessorFactory));
        transactionProcessor = transactionProcessorFactory.create(this);
        eventManager.beforeBeginTransaction();
        transactionProcessor.begin();
        eventManager.afterBeginTransaction();
        transactionState = STARTED;
    }

    public void verifyNotReadOnly() throws SearchEngineException {
        if (readOnly) {
            throw new SearchEngineException("Transaction is set as read only");
        }
    }

    public void verifyWithinTransaction() throws SearchEngineException {
        if (transactionState != STARTED) {
            if (transactionState == COMMIT) {
                throw new SearchEngineException("Search engine transactionProcessor already committed while trying to perform an operation");
            } else if (transactionState == ROLLBACK) {
                throw new SearchEngineException("Search engine transactionProcessor already rolled back while trying to perform an operation");
            } else if (transactionState == NOT_STARTED) {
                throw new SearchEngineException("Search engine transactionProcessor not stated, please call begin transactionProcessor in order to perform operations");
            }
        }
    }

    public boolean isWithinTransaction() throws SearchEngineException {
        return transactionState == STARTED;
    }

    public void prepare() throws SearchEngineException {
        verifyWithinTransaction();
        closeDelegateClosed();
        if (transactionProcessor != null) {
            transactionProcessor.prepare();
        }
        eventManager.afterPrepare();
    }

    public void commit(boolean onePhase) throws SearchEngineException {
        verifyWithinTransaction();
        closeDelegateClosed();
        if (transactionProcessor != null) {
            transactionProcessor.commit(onePhase);
            eventManager.afterCommit(onePhase);
        }
        transactionProcessor = null;
        transactionState = COMMIT;
    }

    public void rollback() throws SearchEngineException {
        verifyWithinTransaction();
        closeDelegateClosed();
        try {
            if (transactionProcessor != null) {
                try {
                    transactionProcessor.rollback();
                } finally {
                    eventManager.afterRollback();
                }
            }
        } finally {
            transactionProcessor = null;
            transactionState = ROLLBACK;
        }
    }

    public void flush() throws SearchEngineException {
        verifyWithinTransaction();
        if (transactionProcessor != null) {
            transactionProcessor.flush();
        }
    }

    public void flushCommit(String ... aliases) throws SearchEngineException {
        verifyWithinTransaction();
        if (transactionProcessor != null) {
            transactionProcessor.flushCommit(aliases);
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
            log.warn("Transaction not committed/rolledback, rolling back");
            try {
                rollback();
            } catch (Exception e) {
                log.warn("Failed to rollback transcation, ignoring", e);
            }
        }
    }

    public void delete(Resource resource) throws SearchEngineException {
        verifyWithinTransaction();
        verifyNotReadOnly();
        onlyReadOnlyOperations = false;
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                delete(((InternalResource) multiResource.resource(i)).getResourceKey());
            }
        } else {
            delete(((InternalResource) resource).getResourceKey());
        }
    }

    private void delete(ResourceKey resourceKey) throws SearchEngineException {
        if (resourceKey.getIds().length == 0) {
            throw new SearchEngineException("Cannot delete a resource with no ids and alias [" + resourceKey.getAlias() + "]");
        }
        transactionProcessor.delete(resourceKey);
        String[] extendingAliases = resourceKey.getResourceMapping().getExtendingAliases();
        for (String extendingAlias : extendingAliases) {
            ResourceMapping extendingMapping = getSearchEngineFactory().getMapping().getMappingByAlias(extendingAlias);
            ResourceKey key = new ResourceKey(extendingMapping, resourceKey.getIds());
            transactionProcessor.delete(key);
        }
        if (log.isTraceEnabled()) {
            log.trace("RESOURCE DELETE {" + resourceKey.getAlias() + "} " + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()));
        }
    }

    public void delete(SearchEngineQuery query) throws SearchEngineException {
        verifyWithinTransaction();
        verifyNotReadOnly();
        onlyReadOnlyOperations = false;
        transactionProcessor.delete((LuceneSearchEngineQuery) query);
        if (log.isTraceEnabled()) {
            log.trace("QUERY DELETE [" + query + "]");
        }
    }

    public void save(Resource resource) throws SearchEngineException {
        onlyReadOnlyOperations = false;
        createOrUpdate(resource, true);
    }

    public void create(Resource resource) throws SearchEngineException {
        onlyReadOnlyOperations = false;
        createOrUpdate(resource, false);
    }

    private void createOrUpdate(final Resource resource, boolean update) throws SearchEngineException {
        verifyWithinTransaction();
        verifyNotReadOnly();
        onlyReadOnlyOperations = false;
        String alias = resource.getAlias();
        ResourceMapping resourceMapping = searchEngineFactory.getMapping().getRootMappingByAlias(alias);
        if (resourceMapping == null) {
            throw new SearchEngineException("Failed to find mapping for alias [" + alias + "]");
        }
        if (resource instanceof MultiResource) {
            MultiResource multiResource = (MultiResource) resource;
            for (int i = 0; i < multiResource.size(); i++) {
                InternalResource resource1 = (InternalResource) multiResource.resource(i);
                if (update) {
                    transactionProcessor.update(resource1);
                    if (log.isTraceEnabled()) {
                        log.trace("RESOURCE SAVE " + resource1);
                    }
                } else {
                    transactionProcessor.create(resource1);
                    if (log.isTraceEnabled()) {
                        log.trace("RESOURCE CREATE " + resource1);
                    }
                }
            }
        } else {
            InternalResource resource1 = (InternalResource) resource;
            if (update) {
                transactionProcessor.update(resource1);
                if (log.isTraceEnabled()) {
                    log.trace("RESOURCE SAVE " + resource1);
                }
            } else {
                transactionProcessor.create(resource1);
                if (log.isTraceEnabled()) {
                    log.trace("RESOURCE CREATE " + resource1);
                }
            }
        }
    }

    public Resource get(Resource idResource) throws SearchEngineException {
        verifyWithinTransaction();
        ResourceKey resourceKey = ((InternalResource) idResource).getResourceKey();
        if (resourceKey.getIds().length == 0) {
            throw new SearchEngineException("Cannot load a resource with no ids and alias [" + resourceKey.getAlias() + "]");
        }
        Resource[] result = transactionProcessor.get(resourceKey);
        if (result.length == 0) {
            // none directly, try and load polymorphic ones
            String[] extendingAliases = resourceKey.getResourceMapping().getExtendingAliases();
            for (String extendingAlias : extendingAliases) {
                ResourceMapping extendingMapping = getSearchEngineFactory().getMapping().getMappingByAlias(extendingAlias);
                ResourceKey key = new ResourceKey(extendingMapping, resourceKey.getIds());
                result = transactionProcessor.get(key);
                if (result.length > 0) {
                    return result[result.length - 1];
                }
            }
            // did not find in the extending aliases as well
            return null;
        } else if (result.length > 1) {
            log.warn("Found several matches in get/load operation for resource alias [" + resourceKey.getAlias() + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(resourceKey.getIds()) + "]");
            return result[result.length - 1];
        }
        return result[0];
    }

    public Resource load(Resource idResource) throws SearchEngineException {
        String alias = idResource.getAlias();
        Resource resource = get(idResource);
        if (resource == null) {
            throw new SearchEngineException("Failed to find resource with alias [" + alias + "] and ids ["
                    + StringUtils.arrayToCommaDelimitedString(idResource.getIds()) + "]");
        }
        return resource;
    }

    public SearchEngineHits find(SearchEngineQuery query) throws SearchEngineException {
        verifyWithinTransaction();
        LuceneSearchEngineHits hits = transactionProcessor.find((LuceneSearchEngineQuery) query);
        if (log.isTraceEnabled()) {
            log.trace("RESOURCE QUERY [" + query + "] HITS [" + hits.getLength() + "]");
        }
        delegateClose.add(hits);
        return hits;
    }

    public SearchEngineTermFrequencies termFreq(String[] propertyNames, int size, SearchEngineInternalSearch internalSearch) {
        return new LuceneSearchEngineTermFrequencies(propertyNames, size, (LuceneSearchEngineInternalSearch) internalSearch);
    }

    public SearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException {
        verifyWithinTransaction();
        LuceneSearchEngineInternalSearch internalSearch = transactionProcessor.internalSearch(subIndexes, aliases);
        delegateClose.add(internalSearch);
        return internalSearch;
    }

    public void removeDelegatedClose(LuceneDelegatedClose closable) {
        delegateClose.remove(closable);
    }

    protected void closeDelegateClosed() throws SearchEngineException {
        LuceneDelegatedClose[] closeables = delegateClose.toArray(new LuceneDelegatedClose[delegateClose.size()]);
        delegateClose.clear();
        for (LuceneDelegatedClose delegatedClose : closeables) {
            try {
                delegatedClose.closeDelegate();
            } catch (Exception e) {
                // swallow the exception
            }
        }
    }

    public LuceneSearchEngineFactory getSearchEngineFactory() {
        return searchEngineFactory;
    }

    public TransactionProcessor getTransactionProcessor() {
        return transactionProcessor;
    }

    /**
     * Returns the runtime settings of the session / search engine.
     */
    public CompassSettings getSettings() {
        return runtimeSettings;
    }

    public boolean onlyReadOperations() {
        return this.onlyReadOnlyOperations;
    }
}
