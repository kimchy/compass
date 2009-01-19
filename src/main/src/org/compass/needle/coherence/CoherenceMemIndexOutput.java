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
import java.util.HashMap;

import org.apache.lucene.index.LuceneFileNames;
import org.apache.lucene.store.IndexOutput;

/**
 * Only use buckets to write the file to the index. Writes a header file and one
 * or more buckets that hold the file content.
 *
 * <p>Allows only seeking to the first bucket, which is what Lucene does with typical
 * bucket size.
 *
 * @author kimchy
 */
class CoherenceMemIndexOutput extends IndexOutput {

    private CoherenceDirectory dir;

    private String fileName;

    private FileBucketKey firstBucketKey;

    private FileBucketValue firstBucketValue;

    private HashMap<Object, Object> flushBuckets;

    private byte[] buffer;

    private int bufferPosition;

    private int currentBucketIndex;

    private long length;

    private long position;

    private boolean open;

    // seek occured, we only allow to work on the first bucket
    private boolean seekOccured;

    public CoherenceMemIndexOutput(CoherenceDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        open = true;
        buffer = new byte[dir.getBucketSize()];
        flushBuckets = new HashMap<Object, Object>();
        // this file is overridden by Lucene, so delete it first
        if (LuceneFileNames.isStaticFile(fileName)) {
            dir.deleteFile(fileName);
        }
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == dir.getBucketSize()) {
            if (seekOccured) {
                throw new CoherenceDirectoryException(dir.getIndexName(), fileName, "Seek occured and overflowed first bucket");
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
                    throw new CoherenceDirectoryException(dir.getIndexName(), fileName, "Seek occured and overflowed first bucket");
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
            flushBuckets.put(firstBucketKey, firstBucketValue);
            flushBuckets.put(new FileHeaderKey(dir.getIndexName(), fileName), new FileHeaderValue(System.currentTimeMillis(), length));
            forceFlushBuckets();
            buffer = null;
            firstBucketKey = null;
            firstBucketValue = null;
        } finally {
            dir.getOnGoingIndexOutputs().remove(fileName);
        }
    }

    public long getFilePointer() {
        return this.position;
    }

    public void seek(long pos) throws IOException {
        if (pos >= dir.getBucketSize()) {
            throw new CoherenceDirectoryException(dir.getIndexName(), fileName, "seek called outside of first bucket boundries");
        }
        // create the first bucket if still not created
        if (firstBucketKey == null) {
            firstBucketKey = new FileBucketKey(dir.getIndexName(), fileName, 0);
            firstBucketValue = new FileBucketValue(new byte[bufferPosition]);
            System.arraycopy(buffer, 0, firstBucketValue.getData(), 0, bufferPosition);
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
        buffer = firstBucketValue.getData();
        seekOccured = true;
    }

    public long length() throws IOException {
        return length;
    }

    private void flushBucket() throws IOException {
        if (currentBucketIndex == 0) {
            if (firstBucketKey == null) {
                firstBucketKey = new FileBucketKey(dir.getIndexName(), fileName, 0);
                firstBucketValue = new FileBucketValue(new byte[bufferPosition]);
                System.arraycopy(buffer, 0, firstBucketValue.getData(), 0, bufferPosition);
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
        FileBucketKey fileBucketKey = new FileBucketKey(dir.getIndexName(), fileName, bucketIndex);
        FileBucketValue fileBucketValue = new FileBucketValue(new byte[length]);
        System.arraycopy(buffer, 0, fileBucketValue.getData(), 0, length);
        flushBuckets.put(fileBucketKey, fileBucketValue);
        if (flushBuckets.size() >= dir.getFlushRate()) {
            forceFlushBuckets();
        }
    }

    private void forceFlushBuckets() throws IOException {
        if (flushBuckets.size() == 0) {
            return;
        }
        try {
            dir.getCache().putAll(flushBuckets);
        } catch (Exception e) {
            throw new CoherenceDirectoryException(dir.getIndexName(), fileName, "Failed to flush buckets", e);
        } finally {
            flushBuckets.clear();
        }
    }
}