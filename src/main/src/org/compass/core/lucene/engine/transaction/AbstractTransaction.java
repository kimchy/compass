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

package org.compass.core.lucene.engine.transaction;

import java.util.ArrayList;

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.lucene.engine.LuceneDelegatedClose;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.analyzer.LuceneAnalyzerManager;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.ResourceMapping;

/**
 * A base class for all Lucene based transactions. Provides helper methods for
 * Lucene index transaction management, and default state management for the
 * transcational operations.
 *
 * @author kimchy
 */
public abstract class AbstractTransaction implements LuceneSearchEngineTransaction {

    private LuceneSearchEngine searchEngine;

    private LuceneSearchEngineIndexManager indexManager;

    private CompassMapping mapping;

    private LuceneAnalyzerManager analyzerManager;

    private ArrayList delegateClose = new ArrayList();

    protected boolean dirty;

    public void configure(LuceneSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
        this.indexManager = searchEngine.getSearchEngineFactory().getLuceneIndexManager();
        this.mapping = searchEngine.getSearchEngineFactory().getMapping();
        this.analyzerManager = searchEngine.getSearchEngineFactory().getAnalyzerManager();
    }

    public void begin() throws SearchEngineException {
        closeHits();
        doBegin();
    }

    protected abstract void doBegin() throws SearchEngineException;

    public void rollback() throws SearchEngineException {
        closeHits();
        doRollback();
    }

    protected abstract void doRollback() throws SearchEngineException;

    public void prepare() throws SearchEngineException {
        doPrepare();
    }

    protected abstract void doPrepare() throws SearchEngineException;

    public void commit(boolean onePhase) throws SearchEngineException {
        closeHits();
        doCommit(onePhase);
    }

    protected abstract void doCommit(boolean onePhase) throws SearchEngineException;

    public SearchEngineHits find(SearchEngineQuery query) throws SearchEngineException {
        SearchEngineHits hits = doFind((LuceneSearchEngineQuery) query);
        delegateClose.add(hits);
        return hits;
    }

    protected abstract SearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException;

    public SearchEngineHighlighter highlighter(SearchEngineQuery query) throws SearchEngineException {
        SearchEngineHighlighter highlighter = doHighlighter((LuceneSearchEngineQuery) query);
        delegateClose.add(highlighter);
        return highlighter;
    }

    protected abstract SearchEngineHighlighter doHighlighter(LuceneSearchEngineQuery query)
            throws SearchEngineException;

    public void create(final Resource resource) throws SearchEngineException {
        dirty = true;
        doCreate(resource);
    }

    protected abstract void doCreate(final Resource resource) throws SearchEngineException;

    public void delete(final Property[] ids, String alias) throws SearchEngineException {
        dirty = true;
        doDelete(ids, alias);
    }

    protected abstract void doDelete(final Property[] ids, String alias) throws SearchEngineException;

    public boolean isDirty() {
        return dirty;
    }

    protected void closeHits() throws SearchEngineException {
        for (int i = 0; i < delegateClose.size(); i++) {
            try {
                ((LuceneDelegatedClose) delegateClose.get(i)).close();
            } catch (Exception e) {
                // swallow the exception
            }
        }
        delegateClose.clear();
    }

    protected ResourceMapping getResourceMapping(String alias) {
        return mapping.getRootMappingByAlias(alias);
    }

    public LuceneSearchEngine getSearchEngine() {
        return searchEngine;
    }

    public LuceneSearchEngineIndexManager getIndexManager() {
        return indexManager;
    }

    public CompassMapping getMapping() {
        return mapping;
    }

    public LuceneAnalyzerManager getAnalyzerManager() {
        return analyzerManager;
    }
}
