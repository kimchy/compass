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

package org.compass.core.lucene.engine.transaction.readcommitted;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public class TransIndexManager implements CompassConfigurable {

    private LuceneSearchEngineFactory searchEngineFactory;

    private CompassSettings settings;

    private Map<String, TransIndex> transIndexMap = new HashMap<String, TransIndex>();

    public TransIndexManager(LuceneSearchEngineFactory searchEngineFactory) {
        this.searchEngineFactory = searchEngineFactory;
    }

    public void configure(CompassSettings settings) throws CompassException {
        this.settings = settings;
    }

    public void create(InternalResource resource, Analyzer analyzer) throws IOException {
        getTransIndex(resource.getSubIndex()).create(resource, analyzer);
    }

    public void update(InternalResource resource, Analyzer analyzer) throws IOException {
        getTransIndex(resource.getSubIndex()).update(resource, analyzer);
    }

    public void delete(ResourceKey resourceKey) throws IOException {
        // no need to delete anything if we don't have a transactional index
        if (!transIndexMap.containsKey(resourceKey.getSubIndex())) {
            return;
        }
        getTransIndex(resourceKey.getSubIndex()).delete(resourceKey);
    }

    public IndexReader getReader(String subIndex) throws IOException {
        return transIndexMap.get(subIndex).getReader(); 
    }

    public IndexSearcher getSearcher(String subIndex) throws IOException {
        return transIndexMap.get(subIndex).getSearcher();
    }

    public Directory getDirectory(String subIndex) {
        return transIndexMap.get(subIndex).getDirectory();
    }

    public boolean hasTransactions() {
        return !transIndexMap.isEmpty();
    }

    public boolean hasTransIndex(String subIndex) {
        return transIndexMap.containsKey(subIndex);
    }

    public void commit() throws IOException {
        for (TransIndex transIndex : transIndexMap.values()) {
            transIndex.commit();
        }
    }

    public void commit(String subIndex) throws IOException {
        transIndexMap.get(subIndex).commit();
    }

    public void close(String subIndex) throws IOException {
        TransIndex transIndex = transIndexMap.remove(subIndex);
        if (transIndex != null) {
            transIndex.close();
        }
    }

    private TransIndex getTransIndex(String subIndex) {
        TransIndex transIndex = transIndexMap.get(subIndex);
        if (transIndex == null) {
            transIndex = new TransIndex(searchEngineFactory, subIndex);
            transIndex.configure(settings);
            transIndexMap.put(subIndex, transIndex);
        }
        return transIndex;
    }
}
