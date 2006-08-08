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

import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.engine.SearchEngineQuery;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.LuceneTermInfoVector;
import org.compass.core.lucene.engine.LuceneSearchEngine;

/**
 * A Lucene transaction interface. All Lucene based transactions must implement
 * the interface for the LuceneSearchEngine to work with them.
 * 
 * @author kimchy
 */
public interface LuceneSearchEngineTransaction {

    void configure(LuceneSearchEngine searchEngine);

    boolean isDirty();

    void begin() throws SearchEngineException;

    void prepare() throws SearchEngineException;

    void commit(boolean onePhase) throws SearchEngineException;

    void rollback() throws SearchEngineException;

    /**
     * Supported only in batch insert transacitons.
     */
    void flush() throws SearchEngineException;

    void create(final Resource resource) throws SearchEngineException;

    void delete(final Property[] ids, String alias) throws SearchEngineException;

    SearchEngineHits find(SearchEngineQuery query) throws SearchEngineException;

    Resource[] find(Property[] ids, String alias) throws SearchEngineException;
    
    LuceneTermInfoVector[] getTermInfos(LuceneResource resource) throws SearchEngineException;

    LuceneTermInfoVector getTermInfo(LuceneResource resource, String propertyName) throws SearchEngineException;
}
