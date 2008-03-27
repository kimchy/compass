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

package org.compass.needle.terracotta;

import java.io.IOException;

import org.apache.lucene.store.IndexOutput;

/**
 * @author kimchy
 */
public class TerracottaIndexOutput extends IndexOutput {

    final int bufferSize;

    private TerracottaFile file;

    // we don't have a case where more than one thread uses it
    // so, no need for volatile
    private byte[] currentBuffer;
    private int currentBufferIndex;

    private int bufferPosition;
    private long bufferStart;
    private int bufferLength;

    TerracottaIndexOutput(TerracottaFile f, int bufferSize) {
        this.bufferSize = bufferSize;
        file = f;

        // make sure that we switch to the
        // first needed buffer lazily
        currentBufferIndex = -1;
        currentBuffer = null;

        f.lock();
    }

    /**
     * Copy the current contents of this buffer to the named output.
     */
    public void writeTo(IndexOutput out) throws IOException {
        flush();
        final long end = file.length;
        long pos = 0;
        int buffer = 0;
        while (pos < end) {
            int length = bufferSize;
            long nextPos = pos + length;
            if (nextPos > end) {                        // at the last buffer
                length = (int) (end - pos);
            }
            out.writeBytes(file.getBuffer(buffer++), length);
            pos = nextPos;
        }
    }

    /**
     * Resets this to an empty buffer.
     */
    public void reset() {
        try {
            seek(0);
        } catch (IOException e) {                     // should never happen
            throw new RuntimeException(e.toString());
        }

        file.setLength(0);
    }

    public void close() throws IOException {
        flush();
        file.unlock();
    }

    public void seek(long pos) throws IOException {
        // set the file length in case we seek back
        // and flush() has not been called yet
        setFileLength();
        if (pos < bufferStart || pos >= bufferStart + bufferLength) {
            currentBufferIndex = (int) (pos / bufferSize);
            switchCurrentBuffer();
        }

        bufferPosition = (int) (pos % bufferSize);
    }

    public long length() {
        return file.length;
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

    private void switchCurrentBuffer() throws IOException {
        if (currentBufferIndex == file.getNumBuffers()) {
            currentBuffer = file.addBuffer(bufferSize);
        } else {
            currentBuffer = file.getBuffer(currentBufferIndex);
        }
        bufferPosition = 0;
        bufferStart = (long) bufferSize * (long) currentBufferIndex;
        bufferLength = currentBuffer.length;
    }

    private void setFileLength() {
        long pointer = bufferStart + bufferPosition;
        if (pointer > file.length) {
            file.setLength(pointer);
        }
    }

    public void flush() throws IOException {
        file.setLastModified(System.currentTimeMillis());
        setFileLength();
    }

    public long getFilePointer() {
        return currentBufferIndex < 0 ? 0 : bufferStart + bufferPosition;
    }
}