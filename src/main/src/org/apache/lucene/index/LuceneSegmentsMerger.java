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
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.lucene.engine.LuceneSettings;

/**
 * A helper class that can merge segments from a certain segment number till the
 * last.
 *
 * @author kimchy
 */
public class LuceneSegmentsMerger {

    private static final Log log = LogFactory.getLog(LuceneSegmentsMerger.class);

    private Directory directory; // where this index resides

    //private Analyzer analyzer; // how to analyze text

    //private Similarity similarity = Similarity.getDefault(); // how to

    private SegmentInfos segmentInfos = new SegmentInfos(); // the segments

    private Lock writeLock;

    private String newSegmentName;

    private Vector segmentsToDelete = new Vector();

    private boolean closeDir;

    private LuceneSettings luceneSettings;

    private SegmentInfos rollbackSegmentInfos;      // segmentInfos we will fallback to if the commit fails

    private IndexFileDeleter deleter;

    public LuceneSegmentsMerger(Directory dir, boolean closeDir, LuceneSearchEngineFactory searchEngineFactory) throws IOException {
        this.closeDir = closeDir;
        this.directory = dir;
        this.luceneSettings = searchEngineFactory.getLuceneSettings();
        //this.analyzer = a;
        Lock writeLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        if (!writeLock.obtain(luceneSettings.getTransactionLockTimout())) { // obtain write lock
            throw new IOException("Lock obtain failed: " + writeLock);
        }
        this.writeLock = writeLock; // save it
        segmentInfos.read(directory);
        rollbackSegmentInfos = (SegmentInfos) segmentInfos.clone();
        deleter = new IndexFileDeleter(directory,
                searchEngineFactory.getIndexDeletionPolicyManager().createIndexDeletionPolicy(dir),
                segmentInfos, null);
    }

    public void mergeFromSegment(int fromSegment) throws IOException {
        newSegmentName = newSegmentName();
        SegmentMerger merger = new SegmentMerger(directory, newSegmentName);

        for (int i = fromSegment; i < segmentInfos.size(); i++) {
            SegmentInfo si = segmentInfos.info(i);
            // TODO: expose this as a configurable parmeter (same as in TransReader)
            IndexReader reader = SegmentReader.get(si, 4096);
            merger.add(reader);
            segmentsToDelete.addElement(reader); // queue segment for deletion
        }
        int mergedDocCount = merger.merge();

        merger.closeReaders();
        segmentInfos.setSize(fromSegment); // pop old infos & add new
        SegmentInfo newSegmentInfo = new SegmentInfo(newSegmentName, mergedDocCount, directory, false, true);
        segmentInfos.addElement(newSegmentInfo);
        deleter.checkpoint(segmentInfos, false);
        if (luceneSettings.isUseCompoundFile()) {
            newSegmentInfo.setUseCompoundFile(true);
            merger.createCompoundFile(newSegmentName + ".cfs");
            deleter.checkpoint(segmentInfos, false);
        }
    }

    public void commit() throws IOException {
        segmentInfos.write(directory); // commit before deleting
        deleter.checkpoint(segmentInfos, true);
    }

    public void rollback() throws IOException {
        segmentInfos.clear();
        segmentInfos.addAll(rollbackSegmentInfos);
        rollbackSegmentInfos = null;

        // Ask deleter to locate unreferenced files we had
        // created & remove them:
        deleter.checkpoint(segmentInfos, false);

        deleter.refresh();

    }

    public void close() throws IOException {
        if (closeDir) {
            try {
                directory.close();
            } catch (IOException ex) {
                // swallow this one
                log.warn("Failed to close directory, ignoring", ex);
            }
            directory = null;
        }
        if (writeLock != null) {
            writeLock.release();
            writeLock = null;
        }
    }

    private final synchronized String newSegmentName() {
        return "_" + Integer.toString(segmentInfos.counter++, Character.MAX_RADIX);
    }

    protected void finalize() throws IOException {
        if (writeLock != null) {
            writeLock.release(); // release write lock
            writeLock = null;
        }
    }

}
