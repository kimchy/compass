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

package org.apache.lucene.index;

import java.io.IOException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

/**
 * @author kimchy
 */
public abstract class LuceneUtils {

    /**
     * Copies one directory contents to the other. Will automatically compound or uncompound the contents of the
     * src directory into the dest directory.
     *
     * @param src    The src directory to copy from
     * @param dest   The dest directory to copy to
     * @param buffer The buffer to use when copying over
     * @throws IOException
     */
    public static void copy(final Directory src, final Directory dest, final byte[] buffer) throws IOException {
        String[] files = src.list();
        if (files != null) {
            for (String name : files) {
                copy(src, dest, name, buffer);
            }
        }
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
     * Clears all the locks within the array, ignoring any exceptions.
     */
    public static void clearLocks(Lock[] locks) {
        if (locks == null) {
            return;
        }
        for (Lock lock : locks) {
            if (lock != null) {
                try {
                    lock.release();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
