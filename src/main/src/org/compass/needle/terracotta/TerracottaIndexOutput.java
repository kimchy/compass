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

import org.apache.lucene.store.IndexOutput;

/**
 * @author kimchy
 */
public class TerracottaIndexOutput extends IndexOutput {

    private final int bufferSize;

    private final int flushRate;

    private final String name;

    private TerracottaFile file;

    private byte[] firstBuffer;

    private byte[] buffer;

    private int bufferPosition;

    private int currentBucketIndex;

    private int flushCounter;

    private long length;

    private long position;

    private boolean open;

    // seek occured, we only allow to work on the first bucket
    private boolean seekOccured;

    TerracottaIndexOutput(TerracottaDirectory dir, String name) throws IOException {
        this.name = name;
        this.bufferSize = dir.getBufferSize();
        this.flushRate = dir.getFlushRate();
        file = new TerracottaFile();
        dir.addFile(name, file);
        file.lock();
        // add a dummy buffer for the first one
        file.addBuffer(0);

        open = true;
        buffer = new byte[bufferSize];
    }

    public void writeByte(byte b) throws IOException {
        if (bufferPosition == bufferSize) {
            if (seekOccured) {
                throw new IOException("Seek occured and overflowed first buffer for file [" + name + "]");
            }
            flushBuffer();
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
                flushBuffer();
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
        // flush any buffer we might have
        flushBuffer();
        file.setFirstBuffer(firstBuffer);
        file.setLength(length);
        file.setLastModified(System.currentTimeMillis());
        file.unlock();
        buffer = null;
        firstBuffer = null;
    }

    public long getFilePointer() {
        return this.position;
    }

    public void seek(long pos) throws IOException {
        if (pos >= bufferSize) {
            throw new IOException("seek called outside of first buffer boundries for file [" + name + "]");
        }
        // create the first bucket if still not created
        if (firstBuffer == null) {
            firstBuffer = new byte[bufferPosition];
            System.arraycopy(buffer, 0, firstBuffer, 0, bufferPosition);
        } else {
            // flush the current buffer. We only seek into the first bucket
            // so no need to keep it around
            if (!seekOccured) {
                flushBuffer(currentBucketIndex, buffer, bufferPosition);
            }
        }
        position = pos;
        currentBucketIndex = 0;
        bufferPosition = (int) pos;
        buffer = firstBuffer;
        seekOccured = true;
    }

    public long length() throws IOException {
        return length;
    }

    private void flushBuffer() throws IOException {
        if (currentBucketIndex == 0) {
            if (firstBuffer == null) {
                firstBuffer = new byte[bufferPosition];
                System.arraycopy(buffer, 0, firstBuffer, 0, bufferPosition);
            } else {
                // do nothing, we are writing directly into the first buffer
            }
        } else {
            if (bufferPosition > 0) {
                flushBuffer(currentBucketIndex, buffer, bufferPosition);
            }
        }
        currentBucketIndex++;
        bufferPosition = 0;
    }

    private void flushBuffer(long bucketIndex, byte[] buffer, int length) throws IOException {
        byte[] data = file.addBuffer(length);
        System.arraycopy(buffer, 0, data, 0, length);
        if (++flushCounter == flushRate) {
            flushCounter = 0;
            file.unlock();
            file.lock();
        }
    }
}