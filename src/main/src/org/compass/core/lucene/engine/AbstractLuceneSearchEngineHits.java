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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searchable;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * @author kimchy
 */
public abstract class AbstractLuceneSearchEngineHits implements SearchEngineHits, LuceneDelegatedClose {

    protected LuceneSearchEngine searchEngine;
    protected List indexHolders;
    protected LuceneSearchEngineQuery query;
    private SearchEngineHighlighter highlighter;
    protected boolean closed;
    protected MultiSearcher searcher;
    protected MultiReader reader;

    public AbstractLuceneSearchEngineHits() {
        closed = false;
    }

    public SearchEngineHighlighter getHighlighter() throws SearchEngineException {
        if (highlighter == null) {
            Searchable[] searchables = searcher.getSearchables();
            if (reader == null) {
                IndexReader[] readers = new IndexReader[searchables.length];
                for (int i = 0; i < searchables.length; i++) {
                    readers[i] = ((IndexSearcher)searchables[i]).getIndexReader();
                }
                try {
                    reader = new MultiReader(readers);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to open readers for highlighting", e);
                }
            }
            highlighter = new LuceneSearchEngineHighlighter(query, reader, searchEngine);
        }
        return highlighter.clear();
    }

    public void close() throws SearchEngineException {
        if (closed) {
            return;
        }
        closed = true;

        if (highlighter != null) {
            highlighter.close();
        }

        if (indexHolders != null) {
            for (int i = 0; i < indexHolders.size(); i++) {
                LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder =
                        (LuceneSearchEngineIndexManager.LuceneIndexHolder) indexHolders.get(i);
                indexHolder.release();
            }
        }
    }
}
