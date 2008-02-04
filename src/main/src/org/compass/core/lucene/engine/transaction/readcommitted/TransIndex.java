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

package org.compass.core.lucene.engine.transaction.readcommitted;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public class TransIndex implements CompassConfigurable {

    private LuceneSearchEngineFactory searchEngineFactory;

    private String subIndex;

    private Directory directory;

    private IndexWriter indexWriter;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private boolean flushRequired = false;

    public TransIndex(LuceneSearchEngineFactory searchEngineFactory, String subIndex) {
        this.searchEngineFactory = searchEngineFactory;
        this.subIndex = subIndex;
    }

    public void configure(CompassSettings settings) throws CompassException {
        try {
            this.directory = new RAMDirectory();
            // create an index writer with autoCommit=true since we want it to be visible to readers (still need to flush)
            indexWriter = new IndexWriter(directory, searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer(), true);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open transactional index for sub index [" + subIndex + "]");
        }
    }

    public void create(InternalResource resource, Analyzer analyzer) throws IOException {
        indexWriter.addDocument(((LuceneResource) resource).getDocument(), analyzer);
        flushRequired = true;
    }

    public void update(InternalResource resource, Analyzer analyzer) throws IOException {
        indexWriter.updateDocument(new Term(resource.resourceKey().getUIDPath(), resource.resourceKey().buildUID()),
                ((LuceneResource) resource).getDocument(), analyzer);
        flushRequired = true;
    }

    public void delete(ResourceKey resourceKey) throws IOException {
        indexWriter.deleteDocuments(new Term(resourceKey.getUIDPath(), resourceKey.buildUID()));
        flushRequired = true;
    }

    public IndexReader getReader() throws IOException {
        refreshIfNeeded();
        return this.indexReader;
    }

    public IndexSearcher getSearcher() throws IOException {
        refreshIfNeeded();
        return this.indexSearcher;
    }

    public Directory getDirectory() {
        return this.directory;
    }

    public void commit() throws IOException {
        if (indexSearcher != null) {
            indexSearcher.close();
        }
        if (indexReader != null) {
            indexReader.close();
        }
        // TODO: lucene23 optimizing here, it should be optional
        indexWriter.optimize();
        indexWriter.close();
        indexWriter = null;
    }

    public void close() throws IOException {
        directory.close();
    }

    private void refreshIfNeeded() throws IOException {
        if (flushRequired) {
            if (indexWriter != null) {
                indexWriter.flush();
            }
            if (indexReader == null) {
                indexReader = IndexReader.open(directory);
                indexSearcher = new IndexSearcher(indexReader);
            } else {
                IndexReader tmpReader = indexReader.reopen();
                if (tmpReader != indexReader) {
                    indexReader.close();
                    indexSearcher.close();
                    indexSearcher = new IndexSearcher(tmpReader);
                }
                indexReader = tmpReader;
            }
            flushRequired = false;
        }
    }
}
