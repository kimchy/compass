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
import org.apache.lucene.search.Hits;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneResource;

/**
 * @author kimchy
 */

public class LuceneSearchEngineHits extends AbstractLuceneSearchEngineHits {

    private Hits hits;

    public LuceneSearchEngineHits(Hits hits, List indexHolders, LuceneSearchEngine searchEngine,
                                  LuceneSearchEngineQuery query) {
        this.hits = hits;
        this.indexHolders = indexHolders;
        this.searchEngine = searchEngine;
        this.query = query;
    }

    public Resource getResource(int i) throws SearchEngineException {
        try {
            Document doc = hits.doc(i);
            return new LuceneResource(doc, hits.id(i), searchEngine);
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to find resource[" + i + "].", ioe);
        }
    }

    public int getLength() {
        return hits.length();
    }

    public float score(int i) throws SearchEngineException {
        try {
            return hits.score(i);
        } catch (IOException ioe) {
            throw new SearchEngineException("Failed to fetch score for resource[" + i + "].", ioe);
        }
    }

}
