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

package org.compass.core.lucene.engine.manager;

import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.compass.core.config.CompassSettings;
import org.compass.core.lucene.LuceneEnvironment;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.lucene.engine.merge.policy.MergePolicyFactory;
import org.compass.core.lucene.engine.merge.scheduler.MergeSchedulerFactory;

/**
 * @author kimchy
 */
public class IndexWritersManager {

    private final LuceneSearchEngineIndexManager indexManager;

    private final LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSettings luceneSettings;

    public IndexWritersManager(LuceneSearchEngineIndexManager indexManager) {
        this.indexManager = indexManager;
        this.searchEngineFactory = indexManager.getSearchEngineFactory();
        this.luceneSettings = indexManager.getSettings();
    }

    public void close() {
        
    }

    public IndexWriter openIndexWriter(CompassSettings settings, String subIndex) throws IOException {
        return openIndexWriter(settings, indexManager.getDirectory(subIndex), false);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, String subIndex, boolean autoCommit) throws IOException {
        return openIndexWriter(settings, indexManager.getDirectory(subIndex), autoCommit, false);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean create) throws IOException {
        return openIndexWriter(settings, dir, true, create);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, true, false, deletionPolicy);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create) throws IOException {
        return openIndexWriter(settings, dir, autoCommit, create, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, autoCommit, create, deletionPolicy, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean autoCommit, boolean create, IndexDeletionPolicy deletionPolicy, Analyzer analyzer) throws IOException {
        if (deletionPolicy == null) {
            deletionPolicy = searchEngineFactory.getIndexDeletionPolicyManager().createIndexDeletionPolicy(dir);
        }
        if (analyzer == null) {
            analyzer = searchEngineFactory.getAnalyzerManager().getDefaultAnalyzer();
        }
        IndexWriter indexWriter = new IndexWriter(dir, analyzer, create, deletionPolicy, new IndexWriter.MaxFieldLength(luceneSettings.getMaxFieldLength()));

        indexWriter.setMergePolicy(MergePolicyFactory.createMergePolicy(settings));
        indexWriter.setMergeScheduler(MergeSchedulerFactory.create(indexManager, settings));

        indexWriter.setMaxMergeDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_MERGE_DOCS, luceneSettings.getMaxMergeDocs()));
        indexWriter.setMergeFactor(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MERGE_FACTOR, luceneSettings.getMergeFactor()));
        indexWriter.setRAMBufferSizeMB(settings.getSettingAsDouble(LuceneEnvironment.SearchEngineIndex.RAM_BUFFER_SIZE, luceneSettings.getRamBufferSize()));
        indexWriter.setMaxBufferedDocs(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DOCS, luceneSettings.getMaxBufferedDocs()));
        indexWriter.setMaxBufferedDeleteTerms(settings.getSettingAsInt(LuceneEnvironment.SearchEngineIndex.MAX_BUFFERED_DELETED_TERMS, luceneSettings.getMaxBufferedDeletedTerms()));
        indexWriter.setUseCompoundFile(indexManager.getStore().isUseCompoundFile());
        indexWriter.setMaxFieldLength(luceneSettings.getMaxFieldLength());
        indexWriter.setTermIndexInterval(luceneSettings.getTermIndexInterval());

        indexWriter.setSimilarity(searchEngineFactory.getSimilarityManager().getIndexSimilarity());

        return indexWriter;
    }
}
