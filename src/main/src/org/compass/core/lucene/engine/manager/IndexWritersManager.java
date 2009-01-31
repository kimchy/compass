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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * A manager responsible for opening {@link org.apache.lucene.index.IndexWriter}. Also provides tracking
 * of opened {@link org.apache.lucene.index.IndexWriter} if enabled and if used by other components.
 *
 * <p>Other components, in order to use tracking, should call {@link #trackOpenIndexWriter(String, org.apache.lucene.index.IndexWriter)}
 * once an index writer is opened, and {@link #trackCloseIndexWriter(String, org.apache.lucene.index.IndexWriter)} once
 * the index writer is closed or rolled back.
 *
 * @author kimchy
 */
public class IndexWritersManager {

    private static final Log logger = LogFactory.getLog(IndexWritersManager.class);

    private final LuceneSearchEngineIndexManager indexManager;

    private final LuceneSearchEngineFactory searchEngineFactory;

    private LuceneSettings luceneSettings;

    private final ConcurrentMap<String, IndexWriter> trackedOpenIndexWriters;

    private final boolean trackOpenIndexWriters;

    public IndexWritersManager(LuceneSearchEngineIndexManager indexManager) {
        this.indexManager = indexManager;
        this.searchEngineFactory = indexManager.getSearchEngineFactory();
        this.luceneSettings = indexManager.getSettings();
        trackOpenIndexWriters =
                luceneSettings.getSettings().getSettingAsBoolean(LuceneEnvironment.SearchEngineIndex.TRACK_OPENED_INDEX_WRITERS, true) || indexManager.getSearchEngineFactory().isDebug();
        if (trackOpenIndexWriters) {
            if (logger.isTraceEnabled()) {
                logger.trace("Tracking open index writers");
            }
            trackedOpenIndexWriters = new ConcurrentHashMap<String, IndexWriter>();
        } else {
            trackedOpenIndexWriters = null;
        }
    }

    public void close() {
        if (trackOpenIndexWriters) {
            for (Map.Entry<String, IndexWriter> entry : trackedOpenIndexWriters.entrySet()) {
                logger.error("[INDEX WRITER] Sub Index [" + entry.getKey() + "] is still open, rolling back");
                try {
                    entry.getValue().rollback();
                } catch (Exception e) {
                    // do nothing, ignore
                }
            }
        }
    }

    public void trackOpenIndexWriter(String subIndex, IndexWriter indexWriter) {
        if (trackOpenIndexWriters) {
            IndexWriter oldValue = trackedOpenIndexWriters.put(subIndex, indexWriter);
            if (oldValue != null) {
                logger.error("Illegal state, marking an index writer as open, while another is marked as open for sub index [" + subIndex + "]");
            }
        }
    }

    public void trackCloseIndexWriter(String subIndex, IndexWriter indexWriter) {
        if (trackOpenIndexWriters) {
            IndexWriter value = trackedOpenIndexWriters.remove(subIndex);
            if (value == null) {
                logger.error("Illegal state, marking an index writer as closed, but none was opened before (or closed twice) for sub index [" + subIndex + "]");
            } else if (value != indexWriter) {
                logger.error("Illegal state, marking an index writer as closed, but a different was opened before for sub index [" + subIndex + "]");
            }
        }
    }

    public IndexWriter openIndexWriter(CompassSettings settings, String subIndex) throws IOException {
        return openIndexWriter(settings, indexManager.getDirectory(subIndex), false);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean create) throws IOException {
        return openIndexWriter(settings, dir, create, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, false, deletionPolicy);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean create, IndexDeletionPolicy deletionPolicy) throws IOException {
        return openIndexWriter(settings, dir, create, deletionPolicy, null);
    }

    public IndexWriter openIndexWriter(CompassSettings settings, Directory dir, boolean create, IndexDeletionPolicy deletionPolicy, Analyzer analyzer) throws IOException {
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
