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

package org.compass.needle.coherence;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.lucene.store.BufferedIndexOutput;

/**
 * @author kimchy
 */
class CoherenceFileIndexOutput extends BufferedIndexOutput {

    private CoherenceDirectory dir;

    private File tempFile;

    private RandomAccessFile file = null;

    private String fileName;

    // remember if the file is open, so that we don't try to close it
    // more than once
    private boolean isOpen;

    public CoherenceFileIndexOutput(CoherenceDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        tempFile = File.createTempFile(dir.getIndexName() + "_" + fileName + "_" + System.currentTimeMillis(), ".lucene-coherencedir");
        this.file = new RandomAccessFile(tempFile, "rw");
        isOpen = true;
    }

    /**
     * output methods:
     */
    public void flushBuffer(byte[] b, int offset, int size) throws IOException {
        file.write(b, offset, size);
    }

    public void close() throws IOException {
        // only close the file if it has not been closed yet
        if (isOpen) {
            super.close();

            // this file is overridden by Lucene, so delete it first
            if (fileName.equals("segments.gen")) {
                dir.deleteFile(fileName);
            }

            // go over the file and write it to the space
            file.seek(0);
            byte[] buffer = new byte[dir.getBucketSize()];

            // TODO consider using transactions to wrap all the writes
            int offset = 0;
            int length = dir.getBucketSize();
            long bucketIndex = 0;
            while (true) {
                int sizeRead = file.read(buffer, offset, length);
                if (sizeRead == -1) {
                    flushBucket(bucketIndex, buffer, offset);
                    break;
                }
                offset += sizeRead;
                length -= sizeRead;
                if (length <= 0) {
                    flushBucket(bucketIndex++, buffer, dir.getBucketSize());
                    offset = 0;
                    length = dir.getBucketSize();
                }
            }
            FileHeaderKey fileHeaderKey = new FileHeaderKey(dir.getIndexName(), fileName);
            FileHeaderValue fileHeaderValue = new FileHeaderValue(System.currentTimeMillis(), file.length());
            dir.getCache().put(fileHeaderKey, fileHeaderValue);
            file.close();
            tempFile.delete();
            isOpen = false;
        }
    }

    /**
     * Random-access methods
     */
    public void seek(long pos) throws IOException {
        super.seek(pos);
        file.seek(pos);
    }

    public long length() throws IOException {
        return file.length();
    }

    private void flushBucket(long bucketIndex, byte[] buffer, int length) throws IOException {
        FileBucketKey fileBucketKey = new FileBucketKey(dir.getIndexName(), fileName, bucketIndex);
        byte[] data = new byte[length];
        System.arraycopy(buffer, 0, data, 0, length);
        FileBucketValue fileBucketValue = new FileBucketValue(data);
        dir.getCache().put(fileBucketKey, fileBucketValue);
    }
}
