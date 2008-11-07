/*
 * Copyright 2004-2008 the original author or authors.
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

import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.lucene.engine.LuceneSearchEngineHits;
import org.compass.core.lucene.engine.LuceneSearchEngineInternalSearch;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * A Lucene transaction interface. All Lucene based transactions must implement
 * the interface for the LuceneSearchEngine to work with them.
 *
 * @author kimchy
 */
public interface TransactionProcessor {

    boolean isDirty();

    void begin() throws SearchEngineException;

    void prepare() throws SearchEngineException;

    void commit(boolean onePhase) throws SearchEngineException;

    void rollback() throws SearchEngineException;

    void flush() throws SearchEngineException;

    void create(final InternalResource resource) throws SearchEngineException;

    void update(final InternalResource resource) throws SearchEngineException;

    void delete(final ResourceKey resourceKey) throws SearchEngineException;

    LuceneSearchEngineHits find(SearchEngineQuery query) throws SearchEngineException;

    Resource[] get(ResourceKey resourceKey) throws SearchEngineException;

    LuceneSearchEngineInternalSearch internalSearch(String[] subIndexes, String[] aliases) throws SearchEngineException;
}
