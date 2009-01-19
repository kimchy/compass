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

package org.compass.needle.gigaspaces.store;

import java.io.IOException;

import org.apache.lucene.store.IndexInput;

/**
 * An index input using the {@link org.compass.needle.gigaspaces.store.FileEntry} as the
 * "header" information and load buckets as needed.
 *
 * @author kimchy
 */
class GigaSpaceIndexInput extends IndexInput {

    private GigaSpaceDirectory dir;

    private FileEntry fileEntry;

    private long position;

    private FileBucketEntry bucketEntry;

    private int currentBucketPosition;

    public GigaSpaceIndexInput(GigaSpaceDirectory dir, FileEntry fileEntry) {
        this.dir = dir;
        this.fileEntry = fileEntry;
        this.bucketEntry = new FileBucketEntry(fileEntry.indexName, fileEntry.fileName, -1, null);
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
        return fileEntry.getSize();
    }

    /**
     * Reads and returns a single byte.
     *
     * @see org.apache.lucene.store.IndexOutput#writeByte(byte)
     */
    public byte readByte() throws IOException {
        loadBucketIfNeeded();
        position++;
        return bucketEntry.data[currentBucketPosition++];
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
                System.arraycopy(bucketEntry.data, currentBucketPosition, b, offset, len);
            }
            currentBucketPosition += len;
            position += len;
            return;
        }

        // cycle through the reads
        while (true) {
            int available = dir.getBucketSize() - currentBucketPosition;
            int sizeToRead = (len <= available) ? len : available;
            System.arraycopy(bucketEntry.data, currentBucketPosition, b, offset, sizeToRead);
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

    private void loadBucketIfNeeded() throws GigaSpaceDirectoryException {
        currentBucketPosition = (int) position % dir.getBucketSize();
        long bucketIndex = position / dir.getBucketSize();
        // check if we need to load the bucket
        if (bucketIndex == bucketEntry.bucketIndex) {
            return;
        }
        // reuse the current bucket entry as the template
        bucketEntry.data = null;
        bucketEntry.bucketIndex = bucketIndex;
        try {
            bucketEntry = (FileBucketEntry) dir.getSpace().read(bucketEntry, null, 0);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(fileEntry.indexName, fileEntry.fileName,
                    "Failed to read bucket [" + bucketIndex + "]", e);
        }
        if (bucketEntry == null) {
            throw new GigaSpaceDirectoryException(fileEntry.indexName, fileEntry.fileName, "Bucket [" + bucketIndex
                    + "] not found");
        }
        if (bucketEntry.data == null) {
            throw new GigaSpaceDirectoryException(fileEntry.indexName, fileEntry.fileName, "Bucket [" + bucketIndex
                    + "] has no data");
        }
    }

    public Object clone() {
        GigaSpaceIndexInput indexInput = (GigaSpaceIndexInput) super.clone();
        indexInput.bucketEntry = new FileBucketEntry(fileEntry.indexName, fileEntry.fileName, -1, null);
        return indexInput;
    }
}
