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
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.CompassException;
import org.compass.core.config.CompassConfigurable;
import org.compass.core.config.CompassSettings;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * @author kimchy
 */
public class TransIndex implements CompassConfigurable {

    private static Random transId = new Random();

    private static final String DEFAULT_LOCATION = System.getProperty("java.io.tmpdir") + "/compass/translog";

    private LuceneSearchEngineFactory searchEngineFactory;

    private String subIndex;

    private Directory directory;

    private IndexWriter indexWriter;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private boolean flushRequired = false;

    private boolean optimize;

    public TransIndex(LuceneSearchEngineFactory searchEngineFactory, String subIndex) {
        this.searchEngineFactory = searchEngineFactory;
        this.subIndex = subIndex;
    }

    public void configure(CompassSettings settings) throws CompassException {
        try {
            String transLogConnection = settings.getSetting(LuceneEnvironment.Transaction.ReadCommittedTransLog.CONNECTION, "ram://");
            if ("ram://".equals(transLogConnection)) {
                directory = new RAMDirectory();
            } else {
                if (transLogConnection.equals("file://")) {
                    transLogConnection = DEFAULT_LOCATION;
                } else if (transLogConnection.startsWith("file://")) {
                    transLogConnection = transLogConnection.substring("file://".length());
                }
                transLogConnection += "/" + transId.nextLong();
                directory = FSDirectory.getDirectory(transLogConnection);
                // TODO we can improve the file system one by starting with a ram one and then switching
            }
            // since this is single threaded access, there is no need to have locks
            directory.setLockFactory(new NoLockFactory());
            // create an index writer with autoCommit=true since we want it to be visible to readers (still need to flush)
            indexWriter = searchEngineFactory.getLuceneIndexManager().openIndexWriter(settings, directory, true, true,
                    new KeepOnlyLastCommitDeletionPolicy());
            // TODO lucene23 we probably want to set a merge policy that will perform no merges
            optimize = settings.getSettingAsBoolean(LuceneEnvironment.Transaction.ReadCommittedTransLog.OPTIMIZE_TRANS_LOG, true);
        } catch (IOException e) {
            throw new SearchEngineException("Failed to open transactional index for sub index [" + subIndex + "]", e);
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
        if (optimize) {
            indexWriter.optimize();
        }
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
