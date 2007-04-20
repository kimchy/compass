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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

/**
 * @author kimchy
 */
public abstract class LuceneUtils {


    public static void deleteUnusedFiles(Directory directory) throws IOException {
        SegmentInfos segmentInfos = new SegmentInfos();
        segmentInfos.read(directory);
        IndexFileDeleter deleter = new IndexFileDeleter(segmentInfos, directory);
        deleter.findDeletableFiles();
        deleter.deleteFiles();
    }

    /**
     * Copies one directory contents to the other. Will automatically compound or uncompound the contents of the
     * src directory into the dest directory.
     *
     * @param src            The src directory to copy from
     * @param srcIsCompound  If the src is in compound format or not
     * @param dest           The dest directory to copy to
     * @param destIsCompound If the dest will be in compound format or not
     * @param buffer         The buffer to use when copying over
     * @throws IOException
     */
    public static void copy(final Directory src, boolean srcIsCompound,
                            final Directory dest, boolean destIsCompound,
                            final byte[] buffer) throws IOException {
        if (!IndexReader.indexExists(src)) {
            return;
        }

        // if both of dirs have the structure, just list and copy
        if ((srcIsCompound && destIsCompound) ||
                (!srcIsCompound && !destIsCompound)) {
            String[] names = src.list();
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                copy(src, dest, name, buffer);
            }
            return;
        }

        final SegmentInfos segmentInfos = new SegmentInfos();
        segmentInfos.read(src);

        copy(src, dest, IndexFileNames.SEGMENTS, buffer);

        if (segmentInfos.isEmpty()) {
            return;
        }

        // set up file extensions
        ArrayList nonCompoundRelated = new ArrayList();
        nonCompoundRelated.addAll(Arrays.asList(IndexFileNames.INDEX_EXTENSIONS));
        nonCompoundRelated.addAll(Arrays.asList(IndexFileNames.VECTOR_EXTENSIONS));
        nonCompoundRelated.removeAll(Arrays.asList(IndexFileNames.COMPOUND_EXTENSIONS));

        for (int segmentIndex = 0; segmentIndex < segmentInfos.size(); segmentIndex++) {
            SegmentInfo segmentInfo = segmentInfos.info(segmentIndex);
            // copy all files that are not part of the compound stuff
            String segment = segmentInfo.name;

            for (Iterator it = nonCompoundRelated.iterator(); it.hasNext();) {
                String name = (String) it.next();
                copy(src, dest, segment + "." + name, buffer);
            }

            String cfsName = segment + ".cfs";

            if (srcIsCompound && !destIsCompound) {
                segmentInfo.setUseCompoundFile(false);
                CompoundFileReader cfsReader = new CompoundFileReader(src, cfsName);
                try {
                    String[] cfsEntriesNames = cfsReader.list();
                    for (int i = 0; i < cfsEntriesNames.length; i++) {
                        String entryName = cfsEntriesNames[i];
                        copy(cfsReader, dest, entryName, buffer);
                    }
                } finally {
                    cfsReader.close();
                }
            } else { //(!srcIsCompound && destIsCompound)
                segmentInfo.setUseCompoundFile(true);
                DualCompoundFileWriter cfsWriter = new DualCompoundFileWriter(src, dest, cfsName, buffer);

                FieldInfos fieldInfos = new FieldInfos(src, segment + ".fnm");

                Vector files = new Vector(IndexFileNames.COMPOUND_EXTENSIONS.length + fieldInfos.size());

                // Basic files
                for (int i = 0; i < IndexFileNames.COMPOUND_EXTENSIONS.length; i++) {
                    files.add(segment + "." + IndexFileNames.COMPOUND_EXTENSIONS[i]);
                }

                // Field norm files
                // Fieldable norm files
                for (int i = 0; i < fieldInfos.size(); i++) {
                    FieldInfo fi = fieldInfos.fieldInfo(i);
                    if (fi.isIndexed && !fi.omitNorms) {
                        files.add(segment + "." + IndexFileNames.NORMS_EXTENSION);
                        break;
                    }
                }

                // Vector files
                if (fieldInfos.hasVectors()) {
                    for (int i = 0; i < IndexFileNames.VECTOR_EXTENSIONS.length; i++) {
                        files.add(segment + "." + IndexFileNames.VECTOR_EXTENSIONS[i]);
                    }
                }

                // Now merge all added files
                Iterator it = files.iterator();
                while (it.hasNext()) {
                    cfsWriter.addFile((String) it.next());
                }

                // Perform the merge
                cfsWriter.close();
            }
        }
        // write back the segments info so the new compound file will take affect
        segmentInfos.write(dest);
    }

    /**
     * Copies over the contents of the name entry from the src directory into the dest directory.
     *
     * @param src    The src directory to copy from
     * @param dest   The dest directory to copy to
     * @param name   the name of the entry
     * @param buffer The buffer to use
     * @throws IOException
     */
    public static void copy(final Directory src, final Directory dest, final String name, byte[] buffer) throws IOException {
        if (!src.fileExists(name)) {
            return;
        }
        IndexInput indexInput = null;
        IndexOutput indexOutput = null;
        try {
            indexInput = src.openInput(name);
            indexOutput = dest.createOutput(name);

            copy(indexInput, indexOutput, name, buffer);

        } finally {
            if (indexInput != null) {
                indexInput.close();
            }
            if (indexOutput != null) {
                indexOutput.close();
            }
        }
    }

    /**
     * Copies the contents of the <code>IndexInput</code> into the <code>IndexOutput</code>.
     *
     * @param indexInput  The content to copy from
     * @param indexOutput The output to write to
     * @param name        The name of the file
     * @param buffer      The buffer to use
     * @throws IOException
     */
    public static void copy(final IndexInput indexInput, final IndexOutput indexOutput, String name, byte[] buffer) throws IOException {
        long length = indexInput.length();
        long remainder = length;
        int chunk = buffer.length;

        while (remainder > 0) {
            int len = (int) Math.min(chunk, remainder);
            indexInput.readBytes(buffer, 0, len);
            indexOutput.writeBytes(buffer, len);
            remainder -= len;
        }

        // Verify that remainder is 0
        if (remainder != 0)
            throw new IOException(
                    "Non-zero remainder length after copying [" + remainder
                            + "] (id [" + name + "] length [" + length
                            + "] buffer size [" + chunk + "])");
    }

    /**
     * Returns <code>true</code> if all the segments of the directory are in compound format.
     * Will return <code>true</code> if the index does not exists or there are no segments.
     */
    public static boolean isCompound(final Directory directory) throws IOException {
        if (!IndexReader.indexExists(directory)) {
            return true;
        }
        final SegmentInfos segmentInfos = new SegmentInfos();
        segmentInfos.read(directory);
        if (segmentInfos.isEmpty()) {
            return true;
        }
        for (int i = 0; i < segmentInfos.size(); i++) {
            SegmentInfo segmentInfo = segmentInfos.info(i);
            if (!segmentInfo.getUseCompoundFile()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> if all the segments of the directory are in un-compound format.
     * Will return <code>true</code> if the index does not exists or there are no segments.
     */
    public static boolean isUnCompound(final Directory directory) throws IOException {
        if (!IndexReader.indexExists(directory)) {
            return true;
        }
        final SegmentInfos segmentInfos = new SegmentInfos();
        segmentInfos.read(directory);
        if (segmentInfos.isEmpty()) {
            return true;
        }
        for (int i = 0; i < segmentInfos.size(); i++) {
            SegmentInfo segmentInfo = segmentInfos.info(i);
            if (segmentInfo.getUseCompoundFile()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compunds the directory. Only works on segments that have no ".cfs" file that already exists.
     */
    public static void compoundDirectory(final Directory directory, long writeLockTimeout) throws IOException {
        Lock writeLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        if (!writeLock.obtain(writeLockTimeout)) {
            throw new IOException("Index locked for write: " + writeLock);
        }
        try {
            final SegmentInfos segmentInfos = new SegmentInfos();
            segmentInfos.read(directory);
            for (int infoIndex = 0; infoIndex < segmentInfos.size(); infoIndex++) {
                SegmentInfo segmentInfo = segmentInfos.info(infoIndex);
                segmentInfo.setUseCompoundFile(true);
                String fileName = segmentInfo.name + ".cfs";
                String segment = segmentInfo.name;
                if (directory.fileExists(fileName)) {
                    // already compound, do nothing
                    continue;
                }
                CompoundFileWriter cfsWriter = new CompoundFileWriter(directory, fileName);

                FieldInfos fieldInfos = new FieldInfos(directory, segment + ".fnm");

                Vector files = new Vector(IndexFileNames.COMPOUND_EXTENSIONS.length + fieldInfos.size());

                // Basic files
                for (int i = 0; i < IndexFileNames.COMPOUND_EXTENSIONS.length; i++) {
                    files.add(segment + "." + IndexFileNames.COMPOUND_EXTENSIONS[i]);
                }

                // Fieldable norm files
                for (int i = 0; i < fieldInfos.size(); i++) {
                    FieldInfo fi = fieldInfos.fieldInfo(i);
                    if (fi.isIndexed && !fi.omitNorms) {
                        files.add(segment + "." + IndexFileNames.NORMS_EXTENSION);
                        break;
                    }
                }

                // Vector files
                if (fieldInfos.hasVectors()) {
                    for (int i = 0; i < IndexFileNames.VECTOR_EXTENSIONS.length; i++) {
                        files.add(segment + "." + IndexFileNames.VECTOR_EXTENSIONS[i]);
                    }
                }

                // Now merge all added files
                Iterator it = files.iterator();
                while (it.hasNext()) {
                    cfsWriter.addFile((String) it.next());
                }

                // Perform the merge
                cfsWriter.close();

                // delete the files
                IndexFileDeleter deleter = new IndexFileDeleter(segmentInfos, directory);
                deleter.deleteFiles(files);
            }
            segmentInfos.write(directory);
        } finally {
            writeLock.release();
        }
    }

    /**
     * Compunds the directory. Only works on segments that have ".cfs" file that already exists.
     */
    public static void unCompoundDirectory(final Directory directory, long writeLockTimeout) throws IOException {
        Lock writeLock = directory.makeLock(IndexWriter.WRITE_LOCK_NAME);
        if (!writeLock.obtain(writeLockTimeout)) {
            throw new IOException("Index locked for write: " + writeLock);
        }
        try {
            final SegmentInfos segmentInfos = new SegmentInfos();
            segmentInfos.read(directory);
            for (int infoIndex = 0; infoIndex < segmentInfos.size(); infoIndex++) {
                SegmentInfo segmentInfo = segmentInfos.info(infoIndex);
                segmentInfo.setUseCompoundFile(false);
                String fileName = segmentInfo.name + ".cfs";
                if (!directory.fileExists(fileName)) {
                    // already un compound, do nothing
                    continue;
                }

                CompoundFileReader cfsReader = new CompoundFileReader(directory, fileName);
                String[] cfsEntriesNames = cfsReader.list();
                for (int i = 0; i < cfsEntriesNames.length; i++) {
                    String entryName = cfsEntriesNames[i];
                    IndexOutput indexOutput = directory.createOutput(entryName);
                    IndexInput indexInput = cfsReader.openInput(entryName);

                    try {
                        byte[] buffer = new byte[1024];
                        long length = indexInput.length();
                        long remainder = length;
                        int chunk = buffer.length;

                        while (remainder > 0) {
                            int len = (int) Math.min(chunk, remainder);
                            indexInput.readBytes(buffer, 0, len);
                            indexOutput.writeBytes(buffer, len);
                            remainder -= len;
                        }

                        // Verify that remainder is 0
                        if (remainder != 0)
                            throw new IOException(
                                    "Non-zero remainder length after copying: " + remainder
                                            + " (id: " + entryName + ", length: " + length
                                            + ", buffer size: " + chunk + ")");
                    } finally {
                        indexInput.close();
                        indexOutput.close();
                    }
                }

                Vector deleteFiles = new Vector();
                deleteFiles.add(fileName);
                IndexFileDeleter deleter = new IndexFileDeleter(segmentInfos, directory);
                deleter.deleteFiles(deleteFiles);
            }
            segmentInfos.write(directory);
        } finally {
            writeLock.release();
        }
    }
}
