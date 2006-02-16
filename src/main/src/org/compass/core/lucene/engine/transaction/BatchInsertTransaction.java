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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.engine.SearchEngineHits;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.LuceneTermInfoVector;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;
import org.compass.core.lucene.util.LuceneUtils;

/**
 * A batch update transaction management. Only support save (add) operations.
 * Find and delete operations throws an exception. The transaction is very fast
 * when adding new resources, note that if a resource with the save id already
 * exists, we will have two resources with the same id.
 * <p>
 * The Batch Transaction support uses Lucene directly, and the Lucene settings
 * apply and affect greatly on the performance of the batch indexing. Among them
 * are the {@link org.compass.core.lucene.LuceneEnvironment.SearchEngineIndex#MAX_BUFFERED_DOCS},
 * {@link org.compass.core.lucene.LuceneEnvironment.SearchEngineIndex#MAX_MERGE_DOCS},
 * {@link org.compass.core.lucene.LuceneEnvironment.SearchEngineIndex#MERGE_FACTOR}.
 * 
 * @author kimchy
 */
public class BatchInsertTransaction extends AbstractTransaction {

    public static class WriterManager {

        public static class IndexWriterWrapper {
            public String subIndex;

            public IndexWriter indexWriter = null;

            public Directory dir;
        }

        private HashMap writersMap = new HashMap();

        private ArrayList writers = new ArrayList();

        private LuceneSearchEngineIndexManager indexManager;

        public WriterManager(LuceneSearchEngineIndexManager indexManager) {
            this.indexManager = indexManager;
        }

        public boolean hasWriterForSubIndex(String subIndex) {
            return writersMap.get(subIndex) != null;
        }

        public IndexWriterWrapper getWriterForSubIndex(String subIndex) {
            return (IndexWriterWrapper) writersMap.get(subIndex);
        }

        public int size() {
            return writers.size();
        }

        public void clear() {
            writers.clear();
            writersMap.clear();
        }

        public IndexWriterWrapper openWriterByAlias(String alias) throws SearchEngineException {
            String subIndex = indexManager.getStore().getSubIndexForAlias(alias);
            IndexWriterWrapper wrapper = (IndexWriterWrapper) writersMap.get(subIndex);
            if (wrapper == null) {
                wrapper = new IndexWriterWrapper();
                wrapper.subIndex = subIndex;
                wrapper.dir = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
                writers.add(wrapper);
                writersMap.put(subIndex, wrapper);
                try {
                    wrapper.indexWriter = indexManager.openIndexWriter(wrapper.dir, false);
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to open index writer for alias [" + alias
                            + "] and sub-index [" + subIndex + "]", e);
                }
            }
            return wrapper;
        }

        private void closeIndexWriter(IndexWriterWrapper writer) throws SearchEngineException {
            if (writer != null && writer.indexWriter != null) {
                try {
                    writer.indexWriter.close();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to close index writer for sub-index [" + writer.subIndex
                            + "]");
                } finally {
                    writer.indexWriter = null;
                }
            }
        }

        private void optimizeIndexWriter(IndexWriterWrapper writer) throws SearchEngineException {
            if (writer != null && writer.indexWriter != null) {
                try {
                    writer.indexWriter.optimize();
                } catch (IOException e) {
                    throw new SearchEngineException("Failed to optimize index writer for sub-index [" + writer.subIndex
                            + "]");
                }
            }
        }

        public void closeIndexWriters(boolean withDirs, boolean withOptimize) throws SearchEngineException {
            Exception e = null;
            for (Iterator it = writers.iterator(); it.hasNext();) {
                IndexWriterWrapper writer = (IndexWriterWrapper) it.next();
                try {
                    if (withOptimize) {
                        optimizeIndexWriter(writer);
                    }
                } catch (Exception ex) {
                    e = ex;
                }
                try {
                    closeIndexWriter(writer);
                } catch (Exception ex) {
                    e = ex;
                }
                if (withDirs) {
                    try {
                        indexManager.getStore().closeDirectory(writer.dir);
                    } catch (Exception ex) {
                        e = ex;
                    }
                }
            }
            if (e != null) {
                if (e instanceof SearchEngineException) {
                    throw (SearchEngineException) e;
                }
                throw new SearchEngineException("Failed to close index writers", e);
            }
        }
    }

    private WriterManager writerManager;

    protected void doBegin() throws SearchEngineException {
        writerManager = new WriterManager(getIndexManager());
    }

    protected void doRollback() throws SearchEngineException {
        try {
            writerManager.closeIndexWriters(true, true);
        } finally {
            writerManager.clear();
        }
        throw new SearchEngineException("Rollback operation not supported for batch insert transaction");
    }

    protected void doPrepare() throws SearchEngineException {
        // do nothing here
    }

    protected void doCommit(boolean onePhase) throws SearchEngineException {
        try {
            writerManager.closeIndexWriters(true, true);
            getIndexManager().clearCache();
        } finally {
            writerManager.clear();
        }
        if (!onePhase) {
            // do nothing about it
        }
    }

    public void flush() throws SearchEngineException {
        try {
            writerManager.closeIndexWriters(true, true);
        } finally {
            writerManager.clear();
        }
    }

    protected void doCreate(final Resource resource) throws SearchEngineException {
        // open the original index writer, so we lock it for changes
        WriterManager.IndexWriterWrapper wrapper = writerManager.openWriterByAlias(resource.getAlias());
        Analyzer analyzer = getAnalyzerManager().getAnalyzerByResource(resource);
        LuceneUtils.createResource(wrapper.indexWriter, resource, analyzer);
    }

    protected void doDelete(Property[] ids, String alias) throws SearchEngineException {
        throw new SearchEngineException("Delete operation not supported for batch insert transaction");
    }

    protected SearchEngineHits doFind(LuceneSearchEngineQuery query) throws SearchEngineException {
        throw new SearchEngineException("Find operation not supported for batch insert transaction");
    }

    public Resource[] find(Property[] ids, String alias) throws SearchEngineException {
        throw new SearchEngineException("Find operation not supported for batch insert transaction");
    }

    protected SearchEngineHighlighter doHighlighter(LuceneSearchEngineQuery query) throws SearchEngineException {
        throw new SearchEngineException("Highlighter not supported for batch insert transaction");
    }

    public LuceneTermInfoVector getTermInfo(LuceneResource resource, String propertyName) {
        throw new SearchEngineException("Term info not supported for batch insert transaction");
    }

    public LuceneTermInfoVector[] getTermInfos(LuceneResource resource) {
        throw new SearchEngineException("Term info not supported for batch insert transaction");
    }
}
