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
import java.io.PrintStream;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSettings;
import org.compass.core.spi.InternalResource;
import org.compass.core.spi.ResourceKey;

/**
 * Provides a transactional index based on Lucene IndexWriter. <p/> Maintains a
 * transactional segment infos (RAM), and provides full searcher which searches
 * on both the base index and the transactional one, as well as reader and
 * searcher for the original index.
 * </p>
 * <p/> Supports two phase commit transaction. First phase writes a new segment
 * which has all the new documents. The seconds one writes the updated segment
 * infos.
 * </p>
 * <p/> The trans index obtains a lock when it is created.
 * </p>
 *
 * @author kimchy
 */
public class TransIndex {

    // Attributes from IndexWriter

    private Directory directory; // where this index resides

    private Similarity similarity = Similarity.getDefault(); // how to

    private SegmentInfos segmentInfos = new SegmentInfos(); // the segments

    private Lock writeLock;

    private boolean closeDir;

    private PrintStream infoStream = null;

    public void setInfoStream(PrintStream infoStream) {
        this.infoStream = infoStream;
    }

    public PrintStream getInfoStream() {
        return infoStream;
    }

    private final static int MERGE_READ_BUFFER_SIZE = 4096;

    private IndexFileDeleter deleter;

    private SegmentInfos rollbackSegmentInfos;      // segmentInfos we will fallback to if the commit fails

    // New Attributes

    private static final Log log = LogFactory.getLog(TransIndex.class);

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private TransLog transLog;

    private Directory transDir;

    private SegmentInfos transSegmentInfos;

    private ArrayList transCreates;

    private ArrayList transReaders;

    private SegmentInfo newSegment;

    private LuceneSettings luceneSettings;

    /**
     * Opens a new transaction index. Try to obtain a write lock. Opens an index
     * reader and an index searcher on the basic index. <p/> Initializes the
     * transactional support, with a transactional RAM directory. Also
     * initializes a transactional segment infos.
     */
    public TransIndex(String subIndex, Directory dir, LuceneSearchEngine searchEngine) throws IOException {
        this(dir, false, false, searchEngine);
        this.transLog = luceneSettings.createTransLog(searchEngine.getSettings());
        transDir = transLog.getDirectory();
        transCreates = new ArrayList();
        transReaders = new ArrayList();
        transSegmentInfos = new SegmentInfos();
        transSegmentInfos.write(transDir);

        // initialize the index reader
        if (segmentInfos.size() == 1) { // index is optimized
            indexReader = SegmentReader.get(segmentInfos, segmentInfos.info(0), false);
            ((CompassSegmentReader) indexReader).setSubIndex(subIndex);
        } else {
            IndexReader[] readers = new IndexReader[segmentInfos.size()];
            for (int i = segmentInfos.size() - 1; i >= 0; i--) {
                try {
                    readers[i] = SegmentReader.get(segmentInfos.info(i));
                } catch (IOException e) {
                    // Close all readers we had opened:
                    for (i++; i < segmentInfos.size(); i++) {
                        readers[i].close();
                    }
                    throw e;
                }
            }
            indexReader = new CompassMultiReader(subIndex, directory, segmentInfos, false, readers);
        }
        indexSearcher = new IndexSearcher(indexReader);
    }

    // Taken from IndexWriter private constructor
    // Need to monitor against lucene changes
    private TransIndex(Directory d, final boolean create, boolean closeDir, LuceneSearchEngine searchEngine)
            throws IOException {
        this.luceneSettings = searchEngine.getSearchEngineFactory().getLuceneSettings();
        this.closeDir = closeDir;
        directory = d;

        if (create) {
            // Clear the write lock in case it's leftover:
            directory.clearLock(IndexWriter.WRITE_LOCK_NAME);
        }

        Lock writeLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        if (!writeLock.obtain(luceneSettings.getTransactionLockTimout())) // obtain write lock
            throw new IOException("Index locked for write: " + writeLock);
        this.writeLock = writeLock;                   // save it

        try {
            if (create) {
                // Try to read first.  This is to allow create
                // against an index that's currently open for
                // searching.  In this case we write the next
                // segments_N file with no segments:
                try {
                    segmentInfos.read(directory);
                    segmentInfos.clear();
                } catch (IOException e) {
                    // Likely this means it's a fresh directory
                }
                segmentInfos.write(directory);
            } else {
                segmentInfos.read(directory);
            }

            rollbackSegmentInfos = (SegmentInfos) segmentInfos.clone();

            deleter = new IndexFileDeleter(directory,
                    searchEngine.getSearchEngineFactory().getIndexDeletionPolicyManager().createIndexDeletionPolicy(directory),
                    segmentInfos, infoStream);

        } catch (IOException e) {
            this.writeLock.release();
            this.writeLock = null;
            throw e;
        }
    }

    /**
     * Adds a document to this index, using the provided analyzer instead of the
     * value of the analyzer. If the document contains more than maxFieldLength
     * terms for a given field, the remainder are discarded. <p/> Adds the
     * resource to the transacitonal segment infos, as well to the resource <->
     * transaction index map.
     */
    // logic taken from lucene, need to monitor
    public void addResource(InternalResource resource, Analyzer analyzer) throws IOException {
        DocumentWriter dw = new DocumentWriter(transDir, analyzer, similarity, luceneSettings.getMaxFieldLength());
        dw.setInfoStream(infoStream);
        String segmentName = newTransSegmentName();
        dw.addDocument(segmentName, ((LuceneResource) resource).getDocument());

        SegmentInfo segmentInfo = new SegmentInfo(segmentName, 1, transDir, false, false);
        transSegmentInfos.addElement(segmentInfo);

        if (transLog.shouldUpdateTransSegments()) {
            transSegmentInfos.write(transDir);
        }

        transLog.onDocumentAdded();

        // TODO: expose this as a configurable parmeter
        transReaders.add(SegmentReader.get(segmentInfo, MERGE_READ_BUFFER_SIZE));
        transCreates.add(resource.resourceKey());

        ((LuceneResource) resource).setDocNum(indexReader.maxDoc() + transCreates.size() - 1);
    }

    // logic taken from lucene, need to monitor
    private final String newTransSegmentName() {
        return "_" + Integer.toString(transSegmentInfos.counter++, Character.MAX_RADIX);
    }

    private final String newSegmentName() {
        return "_" + Integer.toString(segmentInfos.counter++, Character.MAX_RADIX);
    }

    /**
     * Deletets the transactional resource. Removes it from the resource <->
     * transactional index as well as the transactional reader (if created).
     */
    public void deleteTransResource(ResourceKey resourceKey) throws IOException {
        // TODO need to improve performance here
        int i = transCreates.indexOf(resourceKey);
        if (i != -1) {
            transSegmentInfos.removeElementAt(i);
            transCreates.remove(i);
            IndexReader indexReader = (IndexReader) transReaders.remove(i);
            try {
                indexReader.close();
            } catch (IOException e) {
                // ignore this error
            }
            if (transLog.shouldUpdateTransSegments()) {
                transSegmentInfos.write(transDir);
            }
        }
    }

    /**
     * Returns the basix index reader (without the transactional data).
     */
    public IndexReader getIndexReader() {
        return indexReader;
    }

    /**
     * Returns the basic index searcher (without the transactional data)
     */
    public Searcher getIndexSearcher() {
        return indexSearcher;
    }

    /**
     * Returns the full index reader (with the transactional data). No need to
     * close it since the trans index will close it when the
     * <code>close()</code> method is called.
     *
     * @return Full index reader with transactional data
     * @throws IOException
     */
    public IndexReader[] getFullIndexReaderAsArray() throws IOException {
        if (transSegmentInfos.size() == 0) {
            return new IndexReader[]{indexReader};
        }
        IndexReader[] readers = new IndexReader[1 + transReaders.size()];
        readers[0] = indexReader;
        for (int i = 1; i < readers.length; i++) {
            readers[i] = (IndexReader) transReaders.get(i - 1);
        }
        return readers;
    }

    /**
     * Returns the full index reader (with the transactional data). No need to
     * close it since the trans index will close it when the
     * <code>close()</code> method is called.
     *
     * @return Full index reader with transactional data.
     * @throws IOException
     */
    public IndexReader getFullIndexReader() throws IOException {
        if (transSegmentInfos.size() == 0) {
            return indexReader;
        }
        IndexReader[] readers = getFullIndexReaderAsArray();
        return new MultiReader(readers);
    }

    /**
     * Returns the full index searcher (with the transactional data). No need to
     * close it since the trans index will close it when the
     * <code>close()</code> method is called.
     *
     * @return Full index searcher with transactional data
     * @throws IOException
     */
    public Searcher getFullIndexSearcher() throws IOException {
        return new MultiSearcher(getFullIndexSearcherAsArray());
    }

    /**
     * Returns the full index searcher (with the transactional data). No need to
     * close it since the trans index will close it when the
     * <code>close()</code> method is called.
     *
     * @return Full index searchers with transactional data
     * @throws IOException
     */
    public Searcher[] getFullIndexSearcherAsArray() throws IOException {
        if (transSegmentInfos.size() == 0) {
            return new Searcher[]{indexSearcher};
        }
        // TODO can cache this stuff if no changes were made from the last call
        IndexReader[] transReadersArr = (IndexReader[]) transReaders.toArray(new IndexReader[transReaders.size()]);
        IndexReader transReader = new MultiReader(transDir, transSegmentInfos, false, transReadersArr);
        IndexSearcher transSearcher = new IndexSearcher(transReader);
        return new Searcher[]{indexSearcher, transSearcher};
    }

    /**
     * First phase of the transactional commit operation. Writes the
     * tranasctional data into a new file based segment. Adds it to the original
     * segment infos (without writing it), and creates the compound file if
     * using the compund file option (temp one).
     *
     * @throws IOException
     */
    public void firstPhase() throws IOException {
        String newSegmentName = newSegmentName();

        SegmentMerger merger = new SegmentMerger(directory, newSegmentName);

        // TODO do we need to merge if we only have one segment?
        // TODO maybe we need an option to somehow flush the trans directory content into the direcotry without performing merge (it might be faster)
        for (int i = 0; i < transReaders.size(); i++) {
            merger.add((IndexReader) transReaders.get(i));
        }
        int mergedDocCount = merger.merge();
        if (infoStream != null) {
            infoStream.println(" into " + newSegmentName + " (" + mergedDocCount + " docs)");
        }
        merger.closeReaders();
        // first create a new segment with no compound format (so deletion policy will delete non compunds if in compound mode)
        newSegment = new SegmentInfo(newSegmentName, mergedDocCount, directory, false, true);
        segmentInfos.addElement(newSegment);
        deleter.checkpoint(segmentInfos, false);
        if (luceneSettings.isUseCompoundFile()) {
            // compound if needed
            newSegment.setUseCompoundFile(true);
            merger.createCompoundFile(newSegmentName + "." + IndexFileNames.COMPOUND_FILE_EXTENSION);
            deleter.checkpoint(segmentInfos, false);
        }
    }

    /**
     * Writes the segment infos to the disk. If using compound file, making it
     * non-temporary, as well as deleting the non compound files.
     *
     * @throws IOException
     */
    public void secondPhase() throws IOException {
        if (newSegment == null) {
            throw new IOException("Transaction not called first phase");
        }

        // COMPASS - Close the index reader so we commit the deleted documents
        indexSearcher.close();
        indexSearcher = null;
        indexReader.doCommit();
        indexReader.doClose();
        indexReader = null;

        // now commit the segments
        segmentInfos.write(directory);
        deleter.checkpoint(segmentInfos, true);

        rollbackSegmentInfos = null;
    }

    /**
     * Roll back the transaction. If first phase we not called, does nothing. If
     * it was called, cleans it up.
     *
     * @throws IOException
     */
    public void rollback() throws IOException {
        // check if firstPhase was called at all
        if (newSegment == null) {
            return;
        }
        segmentInfos.clear();
        segmentInfos.addAll(rollbackSegmentInfos);

        // Ask deleter to locate unreferenced files & remove
        // them:
        deleter.checkpoint(segmentInfos, false);
        deleter.refresh();
    }

    /**
     * Closes the trans index.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        transCreates.clear();

        // close all the transactional index readers that are opened
        for (int i = 0; i < transReaders.size(); i++) {
            try {
                ((IndexReader) transReaders.get(i)).close();
            } catch (IOException ex) {
                // swallow this one
                log.warn("Failed to close transaction index readers, ignoring", ex);
            }
        }
        transReaders.clear();

        try {
            transLog.close();
            transDir = null;
        } catch (IOException ex) {
            // swallow this one
            log.warn("Failed to close transactional directory, ignoring", ex);
        }
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

    protected void finalize() throws Throwable {
        super.finalize();
        if (writeLock != null) {
            writeLock.release(); // release write lock
            writeLock = null;
        }
    }

}
