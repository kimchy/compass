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

package org.apache.lucene.index;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.CompassSession;
import org.compass.core.CompassTransaction;
import org.compass.core.spi.InternalCompassSession;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.manager.LuceneSearchEngineIndexManager;

/**
 * Provides information about the segments within a Lucene index and about the
 * lucene index itself.
 * <p/>
 * Can not be instantiated directly, but has a factory method (<code>getSegmentsInfos</code>)
 * which returns the index info for the given index path.
 * </p>
 *
 * @author kimchy
 */
public class LuceneSubIndexInfo {

    public static class LuceneSegmentInfo {
        private String name;

        private int docCount;

        public LuceneSegmentInfo(String name, int docCount) {
            this.name = name;
            this.docCount = docCount;
        }

        public String name() {
            return this.name;
        }

        public int docCount() {
            return docCount;
        }
    }

    private ArrayList segmentInfos;

    private long version;

    private String subIndex;

    protected LuceneSubIndexInfo(String subIndex, long version, ArrayList segmentInfos) {
        this.subIndex = subIndex;
        this.version = version;
        this.segmentInfos = segmentInfos;
    }

    /**
     * Returns the version of the index.
     *
     * @return The version of the index
     */
    public long version() {
        return this.version;
    }

    /**
     * Retruns the number of segments.
     *
     * @return The number of segments
     */
    public int size() {
        return segmentInfos.size();
    }

    /**
     * The segment info that maps to the given index.
     *
     * @param segmentIndex The segment index
     * @return The segment info structure
     */
    public LuceneSegmentInfo info(int segmentIndex) {
        return (LuceneSegmentInfo) segmentInfos.get(segmentIndex);
    }

    /**
     * The index parh of the given index.
     *
     * @return The index path
     */
    public String getSubIndex() {
        return this.subIndex;
    }

    public static LuceneSubIndexInfo getIndexInfoByAlias(final String alias, final CompassSession session) throws IOException {
        LuceneSearchEngine searchEngine = (LuceneSearchEngine) ((InternalCompassSession) session).getSearchEngine();
        final LuceneSearchEngineIndexManager indexManager = (LuceneSearchEngineIndexManager) searchEngine.getSearchEngineFactory().getIndexManager();
        CompassTransaction tx = null;
        try {
            tx = session.beginTransaction();
            LuceneSubIndexInfo info = getIndexInfo(indexManager.getStore().getSubIndexForAlias(alias), indexManager);
            tx.commit();
            return info;
        } catch (RuntimeException e) {
            if (tx != null) {
                try {
                    tx.rollback();
                } catch (Exception e1) {
                    // do nothing
                }
            }
            throw e;
        }
    }

    /**
     * Contructs the <code>LuceneIndexInfo</code> for the given index path. If the segments file does not exists,
     * return <code>null</code>.
     */
    public static LuceneSubIndexInfo getIndexInfo(String subIndex, LuceneSearchEngineIndexManager indexManager)
            throws IOException {
        final Directory directory = indexManager.getStore().getDirectoryBySubIndex(subIndex, false);
        try {
            final SegmentInfos segmentInfos = new SegmentInfos();
            synchronized (directory) { // in- & inter-process sync
                new Lock.With(directory.makeLock(IndexWriter.COMMIT_LOCK_NAME), IndexWriter.COMMIT_LOCK_TIMEOUT) {
                    public Object doBody() throws IOException {
                        segmentInfos.read(directory);
                        return null;
                    }
                }.run();
            }
            ArrayList segmentInfosList = new ArrayList();
            for (int i = 0; i < segmentInfos.size(); i++) {
                SegmentInfo segmentInfo = segmentInfos.info(i);
                LuceneSegmentInfo luceneSegmentInfo = new LuceneSegmentInfo(segmentInfo.name, segmentInfo.docCount);
                segmentInfosList.add(luceneSegmentInfo);
            }
            LuceneSubIndexInfo luceneSegmentInfos = new LuceneSubIndexInfo(subIndex, segmentInfos.getVersion(),
                    segmentInfosList);
            return luceneSegmentInfos;
        } catch (FileNotFoundException e) {
            // the segments file was not found, return null.
            return null;
        } finally {
            try {
                directory.close();
            } catch (IOException e) {
                // ignore this one
            }
        }
    }
}
