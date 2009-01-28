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

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Hits;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.lucene.LuceneResource;

/**
 * @author kimchy
 */

public class DefaultLuceneSearchEngineHits implements LuceneSearchEngineHits {

    private final LuceneSearchEngine searchEngine;

    private final LuceneSearchEngineQuery query;

    private LuceneSearchEngineInternalSearch internalSearch;

    private SearchEngineHighlighter highlighter;

    private final Hits hits;

    public DefaultLuceneSearchEngineHits(Hits hits, LuceneSearchEngine searchEngine,
                                         LuceneSearchEngineQuery query, LuceneSearchEngineInternalSearch internalSearch) {
        this.hits = hits;
        this.searchEngine = searchEngine;
        this.query = query;
        this.internalSearch = internalSearch;
    }

    public Resource getResource(int i) throws SearchEngineException {
        verifyWithinTransaction();
        try {
            Document doc = hits.doc(i);
            return new LuceneResource(doc, hits.id(i), searchEngine.getSearchEngineFactory());
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to find hit [" + i + "]", ioe);
        }
    }

    public int getLength() {
        return hits.length();
    }

    public float score(int i) throws SearchEngineException {
        verifyWithinTransaction();
        try {
            return hits.score(i);
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to fetch score for hit [" + i + "]", ioe);
        }
    }

    public Hits getHits() {
        return this.hits;
    }

    public SearchEngineHighlighter getHighlighter() throws SearchEngineException {
        verifyWithinTransaction();
        if (highlighter == null) {
            highlighter = new LuceneSearchEngineHighlighter(query.getOriginalQuery(), internalSearch.getReader(), searchEngine);
        }
        return highlighter.clear();
    }

    public Explanation explain(int i) throws SearchEngineException {
        verifyWithinTransaction();
        try {
            return internalSearch.getSearcher().explain(query.getQuery(), hits.id(i));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to explain hit [" + i + "]", e);
        }
    }

    public void closeDelegate() throws SearchEngineException {
        close(false);
    }

    public void close() throws SearchEngineException {
        close(true);
    }

    private void close(boolean removeDelegate) throws SearchEngineException {
        if (internalSearch != null) {
            try {
                internalSearch.close();
            } finally {
                internalSearch = null;
                if (removeDelegate) {
                    searchEngine.removeDelegatedClose(this);
                }
            }
        }
    }

    private void verifyWithinTransaction() throws SearchEngineException {
        if (!searchEngine.isWithinTransaction()) {
            throw new SearchEngineException("Accessing hits outside of a running transaction, either expand the " +
                    "transaction scope or detach the hits");
        }
    }
}
