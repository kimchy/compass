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

package org.compass.needle.coherence;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

/**
 * @author kimchy
 */
class CoherenceIndexInput extends IndexInput {

    private CoherenceDirectory dir;

    private FileHeaderKey fileHeaderKey;

    private FileHeaderValue fileHeaderValue;

    private long position;

    private FileBucketKey fileBucketKey;

    private FileBucketValue fileBucketValue;

    private int currentBucketPosition;

    public CoherenceIndexInput(CoherenceDirectory dir, FileHeaderKey fileHeaderKey, FileHeaderValue fileHeaderValue) {
        this.dir = dir;
        this.fileHeaderKey = fileHeaderKey;
        this.fileHeaderValue = fileHeaderValue;
        this.fileBucketKey = new FileBucketKey(fileHeaderKey.getIndexName(), fileHeaderKey.getFileName(), -1);
    }

    public void close() throws IOException {
    }

    /**
     * Returns the current position in this file, where the next read will
     * occur.
     *
     * @see #seek(long)
     */
    public long getFilePointer() {
        return this.position;
    }

    /**
     * The number of bytes in the file.
     */
    public long length() {
        return fileHeaderValue.getSize();
    }

    /**
     * Reads and returns a single byte.
     *
     * @see org.apache.lucene.store.IndexOutput#writeByte(byte)
     */
    public byte readByte() throws IOException {
        loadBucketIfNeeded();
        position++;
        return fileBucketValue.getData()[currentBucketPosition++];
    }

    /**
     * Reads a specified number of bytes into an array at the specified
     * offset.
     *
     * @param b      the array to read bytes into
     * @param offset the offset in the array to start storing bytes
     * @param len    the number of bytes to read
     * @see org.apache.lucene.store.IndexOutput#writeBytes(byte[],int)
     */
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        loadBucketIfNeeded();
        // if there is enough place to load at once
        if (len <= (dir.getBucketSize() - currentBucketPosition)) {
            if (len > 0) {
                System.arraycopy(fileBucketValue.getData(), currentBucketPosition, b, offset, len);
            }
            currentBucketPosition += len;
            position += len;
            return;
        }

        // cycle through the reads
        while (true) {
            int available = dir.getBucketSize() - currentBucketPosition;
            int sizeToRead = (len <= available) ? len : available;
            System.arraycopy(fileBucketValue.getData(), currentBucketPosition, b, offset, sizeToRead);
            len -= sizeToRead;
            offset += sizeToRead;
            position += sizeToRead;
            currentBucketPosition += sizeToRead;
            // check if we read enough, if we did, bail
            if (len <= 0) {
                break;
            }
            loadBucketIfNeeded();
        }
    }

    /**
     * Sets current position in this file, where the next read will occur.
     *
     * @see #getFilePointer()
     */
    public void seek(long pos) throws IOException {
        position = pos;
    }

    private void loadBucketIfNeeded() throws IOException {
        currentBucketPosition = (int) position % dir.getBucketSize();
        long bucketIndex = position / dir.getBucketSize();
        // check if we need to load the bucket
        if (bucketIndex == fileBucketKey.getBucketIndex()) {
            return;
        }
        fileBucketKey = new FileBucketKey(fileHeaderKey.getIndexName(), fileHeaderKey.getFileName(), bucketIndex);
        try {
            fileBucketValue = (FileBucketValue) dir.getCache().get(fileBucketKey);
        } catch (Exception e) {
            throw new CoherenceDirectoryException(fileBucketKey.getIndexName(), fileBucketKey.getFileName(),
                    "Failed to read bucket [" + bucketIndex + "]", e);
        }
        if (fileBucketValue == null) {
            throw new CoherenceDirectoryException(fileBucketKey.getIndexName(), fileBucketKey.getFileName(), "Bucket [" + bucketIndex
                    + "] not found");
        }
    }

    public Object clone() {
        CoherenceIndexInput indexInput = (CoherenceIndexInput) super.clone();
        indexInput.fileBucketKey = new FileBucketKey(fileHeaderKey.getIndexName(), fileHeaderKey.getFileName(), -1);
        return indexInput;
    }
}
