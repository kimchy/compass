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

package org.compass.needle.terracotta;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.lucene.store.IndexOutput;

/**
 * An index output that obtains a lock only when adding data to the file (to terracotta).
 *
 * @author kimchy
 */
public class TerracottaIndexOutput2 extends IndexOutput {

    private final int bufferSize;

    private final int flushRate;

    private final String name;

    private TerracottaFile file;

    private byte[] firstBucketEntry;

    private byte[] buffer;

    private int bufferPosition;

    private int currentBucketIndex;

    private long length;

    private long position;

    private boolean open;

    // seek occured, we only allow to work on the first bucket
    private boolean seekOccured;

    private ArrayList<byte[]> flushBuckets;

    TerracottaIndexOutput2(TerracottaDirectory dir, String name) throws IOException {
        this.name = name;
        this.bufferSize = dir.getBufferSize();
        this.flushRate = dir.getFlushRate();
        file = new TerracottaFile();
        dir.addFile(name, file);
        file.lock();
        // add a dummy buffer for the first one
        file.addBuffer(0);
        file.unlock();

        open = true;
        buffer = new byte[bufferSize];

        flushBuckets = new ArrayList<byte[]>(flushRate);
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == bufferSize) {
            if (seekOccured) {
                throw new IOException("Seek occured and overflowed first buffer for file [" + name + "]");
            }
            flushBucket(true);
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
            if (bufferPosition == bufferSize) {
                if (seekOccured) {
                    throw new IOException("Seek occured and overflowed first bucket for file [" + name + "]");
                }
                flushBucket(true);
            }

            int remainInBuffer = bufferSize - bufferPosition;
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
        // flush any bucket we might have
        flushBucket(false);
        forceFlushBuckets(firstBucketEntry);
        buffer = null;
        firstBucketEntry = null;
    }

    public long getFilePointer() {
        return this.position;
    }

    public void seek(long pos) throws IOException {
        if (pos >= bufferSize) {
            throw new IOException("seek called outside of first buffer boundries for file [" + name + "]");
        }
        // create the first bucket if still not created
        if (firstBucketEntry == null) {
            firstBucketEntry = new byte[bufferPosition];
            System.arraycopy(buffer, 0, firstBucketEntry, 0, bufferPosition);
        } else {
            // flush the current buffer. We only seek into the first bucket
            // so no need to keep it around
            if (!seekOccured) {
                flushBucket(buffer, bufferPosition, true);
            }
        }
        position = pos;
        currentBucketIndex = 0;
        bufferPosition = (int) pos;
        buffer = firstBucketEntry;
        seekOccured = true;
    }

    public long length() throws IOException {
        return length;
    }

    private void flushBucket(boolean forceFlush) throws IOException {
        if (currentBucketIndex == 0) {
            if (firstBucketEntry == null) {
                firstBucketEntry = new byte[bufferPosition];
                System.arraycopy(buffer, 0, firstBucketEntry, 0, bufferPosition);
            } else {
                // do nothing, we are writing directly into the first buffer
            }
        } else {
            if (bufferPosition > 0) {
                flushBucket(buffer, bufferPosition, forceFlush);
            }
        }
        currentBucketIndex++;
        bufferPosition = 0;
    }

    private void flushBucket(byte[] buffer, int length, boolean forceFlush) throws IOException {
        byte[] data = new byte[length];
        System.arraycopy(buffer, 0, data, 0, length);
        flushBuckets.add(data);
        if (flushBuckets.size() >= flushRate && forceFlush) {
            forceFlushBuckets(null);
        }
    }

    private void forceFlushBuckets(byte[] firstBuffer) throws IOException {
        if (flushBuckets.size() == 0 && firstBuffer == null) {
            return;
        }
        file.lock();
        try {
            if (!flushBuckets.isEmpty()) {
                byte[][] newBuffers = flushBuckets.toArray(new byte[flushBuckets.size()][]);
                file.addBuffers(newBuffers);
            }
            if (firstBuffer != null) {
                // we only flush on close
                file.setLength(length);
                file.setLastModified(System.currentTimeMillis());
                file.setFirstBuffer(firstBuffer);
            }
        } finally {
            file.unlock();
            flushBuckets.clear();
        }
    }
}