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
import net.jini.core.transaction.TransactionException;
import org.apache.lucene.store.IndexOutput;

/**
 * A specialized index output that only flush on close to the space, allowing for seeking anywhere and anytime.
 *
 * @author kimchy
 */
public class FlushOnCloseGigaSpaceIndexOutput extends IndexOutput {

    private GigaSpaceDirectory dir;

    private final ArrayList<byte[]> buckets = new ArrayList<byte[]>();

    private final int bucketSize;

    private final String fileName;

    private byte[] currentBuffer;

    private int currentBufferIndex;

    private int bufferPosition;

    private long bufferStart;

    private int bufferLength;

    private boolean open;

    private long length;

    public FlushOnCloseGigaSpaceIndexOutput(GigaSpaceDirectory dir, String fileName) throws IOException {
        this.dir = dir;
        this.fileName = fileName;
        this.bucketSize = dir.getBucketSize();

        open = true;
        // make sure that we switch to the
        // first needed buffer lazily
        currentBufferIndex = -1;
        currentBuffer = null;
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == bufferLength) {
            currentBufferIndex++;
            switchCurrentBuffer();
        }
        currentBuffer[bufferPosition++] = b;
    }

    public void writeBytes(byte[] b, int offset, int len) throws IOException {
        while (len > 0) {
            if (bufferPosition == bufferLength) {
                currentBufferIndex++;
                switchCurrentBuffer();
            }

            int remainInBuffer = currentBuffer.length - bufferPosition;
            int bytesToCopy = len < remainInBuffer ? len : remainInBuffer;
            System.arraycopy(b, offset, currentBuffer, bufferPosition, bytesToCopy);
            offset += bytesToCopy;
            len -= bytesToCopy;
            bufferPosition += bytesToCopy;
        }
    }

    public long getFilePointer() {
        return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
    }

    public void flush() throws IOException {
        setFileLength();
    }

    public void close() throws IOException {
        if (!open) {
            return;
        }
        open = false;
        // flush any buffer we might have
        flush();

        Object[] entries = new Object[buckets.size() + 1];
        for (int i = 0; i < (buckets.size()); i++) {
            if (i == (buckets.size() - 1)) {
                // the last buffer is (probably) smaller than the bucket size, make sure we use only the part that has data
                byte[] buff = new byte[(int) (length - (bucketSize * i))];
                System.arraycopy(buckets.get(i), 0, buff, 0, buff.length);
                entries[i] = new FileBucketEntry(dir.getIndexName(), fileName, i, buff);
            } else {
                entries[i] = new FileBucketEntry(dir.getIndexName(), fileName, i, buckets.get(i));
            }
        }
        entries[buckets.size()] = new FileEntry(dir.getIndexName(), fileName, length);

        try {
            dir.getSpace().writeMultiple(entries, null, Lease.FOREVER);
        } catch (TransactionException e) {
            throw new GigaSpaceDirectoryException(dir.getIndexName(), fileName, "Failed to write to space", e);
        }

        currentBuffer = null;
    }

    public long length() {
        return length;
    }

    public void seek(long pos) throws IOException {
        // set the file length in case we seek back
        // and flush() has not been called yet
        setFileLength();
        if (pos < bufferStart || pos >= bufferStart + bufferLength) {
            currentBufferIndex = (int) (pos / bucketSize);
            switchCurrentBuffer();
        }

        bufferPosition = (int) (pos % bucketSize);
    }

    private void switchCurrentBuffer() throws IOException {
        if (currentBufferIndex == buckets.size()) {
            currentBuffer = new byte[bucketSize];
            buckets.add(currentBuffer);
        } else {
            currentBuffer = buckets.get(currentBufferIndex);
        }
        bufferPosition = 0;
        bufferStart = (long) bucketSize * (long) currentBufferIndex;
        bufferLength = currentBuffer.length;
    }

    private void setFileLength() {
        long pointer = bufferStart + bufferPosition;
        if (pointer > length) {
            length = pointer;
        }
    }
}
