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

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Searcher;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneResource;

/**
 * @author kimchy
 */

public class LuceneSearchEngineHits extends AbstractLuceneSearchEngineHits {

    private Hits hits;

    private Searcher searcher;

    public LuceneSearchEngineHits(Hits hits, List indexHolders, LuceneSearchEngine searchEngine,
                                  LuceneSearchEngineQuery query, Searcher searcher) {
        this.hits = hits;
        this.indexHolders = indexHolders;
        this.searchEngine = searchEngine;
        this.query = query;
        this.searcher = searcher;
    }

    public Resource getResource(int i) throws SearchEngineException {
        try {
            Document doc = hits.doc(i);
            return new LuceneResource(doc, hits.id(i), searchEngine);
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to find hit [" + i + "]", ioe);
        }
    }

    public int getLength() {
        return hits.length();
    }

    public float score(int i) throws SearchEngineException {
        try {
            return hits.score(i);
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to fetch score for hit [" + i + "]", ioe);
        }
    }

    public Hits getHits() {
        return this.hits;
    }

    public Explanation explain(int i) throws SearchEngineException {
        try {
            return searcher.explain(query.getQuery(), hits.id(i));
        } catch (IOException e) {
            throw new SearchEngineException("Failed to explain hit [" + i + "]", e);
        }
    }

}
