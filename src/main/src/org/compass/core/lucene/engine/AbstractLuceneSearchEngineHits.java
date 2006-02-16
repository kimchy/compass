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

import java.util.Iterator;
import java.util.List;

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

    public AbstractLuceneSearchEngineHits() {
        closed = false;
    }

    public SearchEngineHighlighter getHighlighter() throws SearchEngineException {
        if (highlighter == null) {
            highlighter = searchEngine.highlighter(query);
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
            for (Iterator it = indexHolders.iterator(); it.hasNext();) {
                LuceneSearchEngineIndexManager.LuceneIndexHolder indexHolder =
                        (LuceneSearchEngineIndexManager.LuceneIndexHolder) it.next();
                indexHolder.release();
            }
        }
    }
}
