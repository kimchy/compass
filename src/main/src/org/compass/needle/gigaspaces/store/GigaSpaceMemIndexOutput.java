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
import java.util.ArrayList;

import net.jini.core.lease.Lease;
import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.IndexOutput;

/**
 * Only use buckets to write the file to the index. Writes a header file and one
 * or more buckets that hold the file content.
 *
 * <p>Allows only seeking to the first bucket, which is what Lucene does.
 *
 * @author kimchy
 */
class GigaSpaceMemIndexOutput extends IndexOutput {

    private GigaSpaceDirectory dir;

    private String fileName;

    private FileBucketEntry firstBucketEntry;

    private byte[] buffer;

    private int bufferPosition;

    private int currentBucketIndex;

    private long length;

    private long position;

    private boolean open;

    // seek occured, we only allow to work on the first bucket
    private boolean seekOccured;

    private final ArrayList<FileBucketEntry> flushBuckets;

    public GigaSpaceMemIndexOutput(GigaSpaceDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        open = true;
        buffer = new byte[dir.getBucketSize()];
        // this file is overridden by Lucene, so delete it first
        if (LuceneFileNames.isStaticFile(fileName)) {
            dir.deleteFile(fileName);
        }
        flushBuckets = new ArrayList<FileBucketEntry>(dir.getFlushRate());
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == dir.getBucketSize()) {
            if (seekOccured) {
                throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Seek occured and overflowed first bucket");
            }
            flushBucket();
        }
        buffer[bufferPosition++] = b;
        if (!seekOccured) {
            length++;
            position++;
        }
    }

    public void writeBytes(byte[] b, int offset, int len) throws IOException {
        if (!seekOccured) {
            position += len;
            length += len;
        }
        while (len > 0) {
            if (bufferPosition == dir.getBucketSize()) {
                if (seekOccured) {
                    throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Seek occured and overflowed first bucket");
                }
                flushBucket();
            }

            int remainInBuffer = dir.getBucketSize() - bufferPosition;
            int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
            System.arraycopy(b, offset, buffer, bufferPosition, bytesToCopy);
            offset += bytesToCopy;
            len -= bytesToCopy;
            bufferPosition += bytesToCopy;
        }
    }

    public void flush() throws IOException {
        // do nothing here
    }

    public void close() throws IOException {
        if (!open) {
            return;
        }
        open = false;
        try {
            // flush any bucket we might have
            flushBucket();
            forceFlushBuckets(firstBucketEntry, new FileEntry(dir.getIndexName(), fileName, length));
            buffer = null;
            firstBucketEntry = null;
        } finally {
            dir.getOnGoingIndexOutputs().remove(fileName);
        }
    }

    public long getFilePointer() {
        return this.position;
    }

    public void seek(long pos) throws IOException {
        if (pos >= dir.getBucketSize()) {
            throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "seek called outside of first bucket boundries");
        }
        // create the first bucket if still not created
        if (firstBucketEntry == null) {
            firstBucketEntry = new FileBucketEntry(dir.getIndexName(), fileName, 0, new byte[bufferPosition]);
            System.arraycopy(buffer, 0, firstBucketEntry.data, 0, bufferPosition);
        } else {
            // flush the current buffer. We only seek into the first bucket
            // so no need to keep it around
            if (!seekOccured) {
                flushBucket(currentBucketIndex, buffer, bufferPosition);
            }
        }
        position = pos;
        currentBucketIndex = 0;
        bufferPosition = (int) pos;
        buffer = firstBucketEntry.data;
        seekOccured = true;
    }

    public long length() throws IOException {
        return length;
    }

    private void flushBucket() throws IOException {
        if (currentBucketIndex == 0) {
            if (firstBucketEntry == null) {
                firstBucketEntry = new FileBucketEntry(dir.getIndexName(), fileName, 0, new byte[bufferPosition]);
                System.arraycopy(buffer, 0, firstBucketEntry.data, 0, bufferPosition);
            } else {
                // do nothing, we are writing directly into the first buffer
            }
        } else {
            if (bufferPosition > 0) {
                flushBucket(currentBucketIndex, buffer, bufferPosition);
            }
        }
        currentBucketIndex++;
        bufferPosition = 0;
    }

    private void flushBucket(long bucketIndex, byte[] buffer, int length) throws IOException {
        FileBucketEntry fileBucketEntry = new FileBucketEntry(dir.getIndexName(), fileName, bucketIndex, null);
        fileBucketEntry.data = new byte[length];
        System.arraycopy(buffer, 0, fileBucketEntry.data, 0, length);
        flushBuckets.add(fileBucketEntry);
        if (flushBuckets.size() >= dir.getFlushRate()) {
            forceFlushBuckets();
        }
    }

    private void forceFlushBuckets(Object ... additionalEntries) throws IOException {
        if (flushBuckets.size() == 0 && additionalEntries == null) {
            return;
        }
        Object[] entries = new Object[flushBuckets.size() + additionalEntries.length];
        flushBuckets.toArray(entries);
        for (int i = 0; i < additionalEntries.length; i++) {
            entries[flushBuckets.size() + i] = additionalEntries[i];
        }
        try {
            dir.getSpace().writeMultiple(entries, null, Lease.FOREVER);
        } catch (Exception e) {
            throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Failed to write buckets", e);
        } finally {
            flushBuckets.clear();
        }
    }
}
