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
import java.util.Vector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.RAMDirectory;
import org.compass.core.Property;
import org.compass.core.Resource;
import org.compass.core.impl.ResourceIdKey;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSettings;

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

    // New Attributes

    private static final Log log = LogFactory.getLog(TransIndex.class);

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    private Directory transDir;

    private SegmentInfos transSegmentInfos;

    private ArrayList transCreates;

    private ArrayList transReaders;

    private String newSegmentName;

    private Vector filesToDeleteCS;

    private LuceneSettings luceneSettings;

    /**
     * Opens a new transaction index. Try to obtain a write lock. Opens an index
     * reader and an index searcher on the basic index. <p/> Initializes the
     * transactional support, with a transactional RAM directory. Also
     * initializes a transactional segment infos.
     */
    public TransIndex(String subIndex, Directory dir, LuceneSettings luceneSettings) throws IOException {
        this(dir, false, false, luceneSettings);
        transDir = new RAMDirectory();
        transSegmentInfos = new SegmentInfos();
        transSegmentInfos.write(transDir);
        transCreates = new ArrayList();
        // initialize the index reader
        if (segmentInfos.size() == 1) { // index is optimized
            indexReader = SegmentReader.get(segmentInfos, segmentInfos.info(0), false);
            ((CompassSegmentReader) indexReader).setSubIndex(subIndex);
        } else {
            IndexReader[] readers = new IndexReader[segmentInfos.size()];
            for (int i = 0; i < segmentInfos.size(); i++) {
                readers[i] = SegmentReader.get(segmentInfos.info(i));
            }
            indexReader = new CompassMultiReader(subIndex, directory, segmentInfos, false, readers);
        }
        indexSearcher = new IndexSearcher(indexReader);
        transReaders = new ArrayList();
    }

    // Taken from IndexWriter private constructor
    // Need to monitor against lucene changes
    private TransIndex(Directory d, final boolean create, boolean closeDir, LuceneSettings luceneSettings)
            throws IOException {
        this.luceneSettings = luceneSettings;
        this.closeDir = closeDir;
        directory = d;

        Lock writeLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        if (!writeLock.obtain(luceneSettings.getTransactionLockTimout())) { // obtain
            // write
            // lock
            throw new IOException("Lock obtain failed: " + writeLock);
        }
        this.writeLock = writeLock; // save it

        synchronized (directory) { // in- & inter-process sync
            new Lock.With(directory.makeLock(IndexWriter.COMMIT_LOCK_NAME), luceneSettings
                    .getTransactionCommitTimeout()) {
                public Object doBody() throws IOException {
                    if (create)
                        segmentInfos.write(directory);
                    else
                        segmentInfos.read(directory);
                    return null;
                }
            }.run();
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
    public void addResource(Resource resource, Property[] ids, Analyzer analyzer) throws IOException {
        DocumentWriter dw = new DocumentWriter(transDir, analyzer, similarity, luceneSettings.getMaxFieldLength());
        dw.setInfoStream(infoStream);
        String segmentName = newTransSegmentName();
        dw.addDocument(segmentName, ((LuceneResource) resource).getDocument());
        transCreates.add(new ResourceIdKey(resource.getAlias(), ids));
        SegmentInfo segmentInfo = new SegmentInfo(segmentName, 1, transDir);
        transSegmentInfos.addElement(segmentInfo);
        ((LuceneResource) resource).setDocNum(indexReader.maxDoc() + transCreates.size() - 1);
    }

    // logic taken from lucene, need to monitor
    private final synchronized String newTransSegmentName() {
        return "_" + Integer.toString(transSegmentInfos.counter++, Character.MAX_RADIX);
    }

    private final synchronized String newSegmentName() {
        return "_" + Integer.toString(segmentInfos.counter++, Character.MAX_RADIX);
    }

    /**
     * Deletets the transactional resource. Removes it from the resource <->
     * transactional index as well as the transactional reader (if created).
     *
     * @param ids
     * @param alias
     * @throws IOException
     */
    public void deleteTransResource(Property[] ids, String alias) throws IOException {
        ResourceIdKey idKey = new ResourceIdKey(alias, ids);
        // TODO: Maybe can be implemented to have better performance
        int i = transCreates.indexOf(idKey);
        if (i != -1) {
            transCreates.remove(i);
            transSegmentInfos.removeElementAt(i);
            // if we already created trans readers, we need to remove it as well
            if (i < transReaders.size()) {
                transReaders.remove(i);
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
        if (transSegmentInfos.size() != transReaders.size()) {
            for (int i = transReaders.size(); i < transSegmentInfos.size(); i++) {
                transReaders.add(SegmentReader.get(transSegmentInfos.info(i)));
            }
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
        if (transSegmentInfos.size() != transReaders.size()) {
            for (int i = transReaders.size(); i < transSegmentInfos.size(); i++) {
                transReaders.add(SegmentReader.get(transSegmentInfos.info(i)));
            }
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
        newSegmentName = newSegmentName();
        SegmentMerger merger = new SegmentMerger(directory, newSegmentName);

        // TODO do we need to merge if we only have one segment?
        // TODO maybe we need an option to somehow flush the trans directory content into the direcotry without performing merge (it might be faster)
        for (int i = 0; i < transSegmentInfos.size(); i++) {
            SegmentInfo si = transSegmentInfos.info(i);
            if (infoStream != null)
                infoStream.print(" " + si.name + " (" + si.docCount + " docs)");
            IndexReader reader = SegmentReader.get(si);
            merger.add(reader);
        }
        int mergedDocCount = merger.merge();
        if (infoStream != null) {
            infoStream.println(" into " + newSegmentName + " (" + mergedDocCount + " docs)");
        }
        merger.closeReaders();
        segmentInfos.addElement(new SegmentInfo(newSegmentName, mergedDocCount, directory));
        if (luceneSettings.isUseCompoundFile()) {
            filesToDeleteCS = merger.createCompoundFile(newSegmentName + ".tmp");
        }
    }

    /**
     * Writes the segment infos to the disk. If using compound file, making it
     * non-temporary, as well as deleting the non compound files.
     *
     * @throws IOException
     */
    public void secondPhase() throws IOException {
        if (newSegmentName == null) {
            throw new IOException("Transaction not called first phase");
        }
        synchronized (directory) { // in- & inter-process sync
            new Lock.With(directory.makeLock(IndexWriter.COMMIT_LOCK_NAME), luceneSettings
                    .getTransactionCommitTimeout()) {
                public Object doBody() throws IOException {
                    if (luceneSettings.isUseCompoundFile()) {
                        directory.renameFile(newSegmentName + ".tmp", newSegmentName + ".cfs");
                        // delete now unused files of segment
                        LuceneUtils.deleteFiles(filesToDeleteCS, directory);
                        filesToDeleteCS = null;
                    }
                    segmentInfos.write(directory); // commit before deleting
                    return null;
                }
            }.run();
        }
    }

    /**
     * Roll back the transaction. If first phase we not called, does nothing. If
     * it was called, cleans it up.
     *
     * @throws IOException
     */
    public void rollback() throws IOException {
        // check if firstPhase was called at all
        if (newSegmentName == null) {
            return;
        }
        String[] files = directory.list();
        String segmentPrefix = newSegmentName + ".";
        Vector vFilesToDelete = new Vector();
        for (int i = 0; i < files.length; i++) {
            String fileName = files[i];
            if (fileName.startsWith(segmentPrefix)) {
                vFilesToDelete.add(fileName);
            }
        }
        LuceneUtils.deleteFiles(vFilesToDelete, directory);
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
            indexSearcher.close();
        } catch (IOException ex) {
            // swallow this one
            log.warn("Failed to close index searcher, ignoring", ex);
        }
        indexSearcher = null;
        try {
            indexReader.close();
        } catch (IOException ex) {
            // swallow this one
            log.warn("Failed to close index reader, ignoring", ex);
        }
        indexReader = null;
        try {
            transDir.close();
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

    protected void finalize() throws IOException {
        if (writeLock != null) {
            writeLock.release(); // release write lock
            writeLock = null;
        }
    }

}
